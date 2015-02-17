/* $Id$
 * $URL$
 * 
 * Part of the EU project Inertia, see http://www.inertia-project.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2014 Almende B.V. 
 */
package io.coala.type;

import io.coala.error.ExceptionBuilder;
import io.coala.json.JsonUtil;
import io.coala.refer.Identifier;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import javax.inject.Provider;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * {@link DynaBean} implements a dynamic bean, ready for JSON de/serialization
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@JsonInclude(Include.NON_NULL)
public class DynaBean
{

	/** */
	private static final Logger LOG = LogManager.getLogger(DynaBean.class);

	/** leave null as long as possible */
	@JsonIgnore
	private Map<String, Object> dynamicProperties = null;

	/**
	 * {@link DynaBean} zero-arg bean constructor for (de)serialization
	 */
	@JsonCreator
	protected DynaBean()
	{
		// empty
	}

	protected void lock()
	{
		// TODO prevent infinite locking?
		if (this.dynamicProperties != null)
			this.dynamicProperties = Collections
					.unmodifiableMap(this.dynamicProperties);
	}

	/**
	 * helper-method
	 * 
	 * @param key
	 * @param defaultValue
	 * @return the dynamically set value, or {@code defaultValue} if not set
	 */
	@SuppressWarnings("unchecked")
	protected <T> T any(final String key, final T defaultValue)
	{
		final Object result = any().get(key);
		return result == null ? defaultValue : (T) result;
	}

	/**
	 * helper-method
	 * 
	 * @param key
	 * @param returnType
	 * @return the currently set value, or {@code null} if not set
	 */
	@SuppressWarnings("unchecked")
	protected <T> T any(final String key, final Class<T> returnType)
	{
		return (T) any().get(key);
	}

	@JsonAnyGetter
	protected Map<String, Object> any()
	{
		if (this.dynamicProperties == null)
			return Collections.emptyMap();

		return this.dynamicProperties;
	}

	@JsonAnySetter
	protected Object set(final String name, final Object value)
	{
		// LOG.trace("Setting " + name + " = " + value);
		if (this.dynamicProperties == null)
			this.dynamicProperties = new TreeMap<String, Object>();

		return this.dynamicProperties.put(name, value);
	}

	@Override
	public int hashCode()
	{
		return any().hashCode();
	}

	@Override
	public boolean equals(final Object other)
	{
		return any().equals(other);
	}

	@Override
	public String toString()
	{
		try
		{
			return JsonUtil.getJOM().writeValueAsString(any());
		} catch (final IOException e)
		{
			LOG.warn("Problem serializing " + getClass().getName(), e);
			return super.toString();
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public static <T> T proxyOf(final Class<T> type, final Map<?, ?>... imports)
	{
		return proxyOf(type, new DynaBean());
	}

	/**
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T proxyOf(final Class<T> type, final DynaBean bean,
			final Map<?, ?>... imports)
	{
		if (!type.isInterface())
			throw ExceptionBuilder.unchecked(
					"Type is not an interface: " + type.getName()).build();

		return (T) Proxy.newProxyInstance(type.getClassLoader(),
				new Class[] { type }, new DefaultBeanInvocationHandler(type,
						bean, imports));
	}

	/**
	 * @param wrapperType
	 * @param <S>
	 * @param <T>
	 * @return
	 */
	public static final <S, T> JsonSerializer<T> createJsonSerializer(
			final Class<T> type)
	{
		return new JsonSerializer<T>()
		{
			@Override
			public void serialize(final T value, final JsonGenerator jgen,
					final SerializerProvider serializers) throws IOException,
					JsonProcessingException
			{
				final boolean isProxy = Proxy.isProxyClass(value.getClass());
				final Object vvalue = isProxy ? ((DefaultBeanInvocationHandler) Proxy
						.getInvocationHandler(value)).bean : value;
				// LOG.trace("Finding de/serializer for " + vvalue.getClass());
				serializers.findValueSerializer(vvalue.getClass()).serialize(
						vvalue, jgen, serializers);
			}
		};
	}

	/** */
	public static <T> void registerType(final Class<T> type,
			final Map<?, ?>... imports)
	{
		JsonUtil.getJOM().registerModule(
				new SimpleModule().addSerializer(type,
						createJsonSerializer(type)).addDeserializer(type,
						createJsonDeserializer(type, imports)));
	}

	/** cache of type arguments for known {@link Identifier} sub-types */
	public static final Map<Class<?>, Provider<?>> DYNABEAN_PROVIDER_CACHE = new WeakHashMap<>();

	/**
	 * @param referenceType
	 * @param <S>
	 * @param <T>
	 * @return
	 */
	public static final <S, T> JsonDeserializer<T> createJsonDeserializer(
			final Class<T> resultType, final Map<?, ?>... imports)
	{
		return new JsonDeserializer<T>()
		{
			@Override
			public T deserialize(final JsonParser jp,
					final DeserializationContext ctxt) throws IOException,
					JsonProcessingException
			{
				if (jp.getText() == null || jp.getText().length() == 0
						|| jp.getText().equals("null"))
					return null;

				// can't parse directly to interface type
				final DynaBean bean = new DynaBean();
				final TreeNode tree = jp.readValueAsTree();

				// override attributes as defined in interface getters
				final Set<String> attributes = new HashSet<>();
				for (Method method : resultType.getMethods())
				{
					if (!method.getReturnType().equals(Void.TYPE)
							&& method.getParameterTypes().length == 0)
					{
						final String attribute = method.getName();
						if (attribute.equals("toString")
								|| attribute.equals("hashCode"))
							continue;

						attributes.add(attribute);
						final TreeNode value = tree.get(attribute);// bean.any().get(attributeName);
						if (value == null)
							continue;

						bean.set(
								method.getName(),
								JsonUtil.getJOM()
										.treeToValue(
												value,
												JsonUtil.checkRegistered(
														method.getReturnType(),
														imports)));
					}
				}
				if (tree.isObject())
				{
					// keep superfluous properties as TreeNodes, just in case
					final Iterator<String> fieldNames = tree.fieldNames();
					while (fieldNames.hasNext())
					{
						final String fieldName = fieldNames.next();
						if (!attributes.contains(fieldName))
							bean.set(fieldName, tree.get(fieldName));
					}
				} else
					throw ExceptionBuilder.unchecked(
							"Expected " + resultType.getName()
									+ " but parsed: "
									+ tree.getClass().getSimpleName()).build();

				return DynaBean.proxyOf(resultType, bean, imports);
			}
		};
	}

	/**
	 * {@link DefaultBeanInvocationHandler}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	protected static class DefaultBeanInvocationHandler implements
			InvocationHandler
	{
		/** */
		private final Class<?> type;

		/** */
		private final DynaBean bean;

		/** */
		private final Config config;

		/**
		 * {@link DefaultBeanInvocationHandler} constructor
		 */
		public DefaultBeanInvocationHandler(final Class<?> type,
				final DynaBean bean, final Map<?, ?>... imports)
		{
			this.type = type;
			this.bean = bean;
			LOG.trace("Using imports: " + Arrays.asList(imports));
			this.config = Config.class.isAssignableFrom(type) ? ConfigCache
					.getOrCreate(type.asSubclass(Config.class), imports) : null;

			// TODO use event listeners of Mutable interface to dynamically add
			// Converters at runtime
		}

		@Override
		public Object invoke(final Object proxy, final Method method,
				final Object[] args) throws Throwable
		{
			// LOG.trace("Calling " + this.type.getSimpleName() + "#"
			// + method.getName() + "()");

			final String attribute = method.getName();
			switch (args == null ? 0 : args.length)
			{
			case 0:
				if (attribute.equals("toString"))
					return this.bean.toString();

				if (attribute.equals("hashCode"))
					return this.bean.hashCode();

				// ! can't intercept call to native method
				// if (method.getName().equals("getClass"))
				// return this.type;

				if (!method.getReturnType().equals(Void.TYPE))
				{
					final Object result = this.bean.any().get(attribute);
					if (result != null)
						return result;
				}
				break;

			case 1:
				if (attribute.equals("equals"))
					return this.bean.equals(args[0]);

				if (method.getReturnType().equals(Void.TYPE))
				{
					this.bean.set(attribute, args[0]);
					return null; // setters return void
				}
				break;
			}

			if (method.getReturnType().equals(Void.TYPE))
				return null;

			if (this.config != null)
			{
				LOG.trace("Checking config for " + attribute);
				return method.invoke(this.config, args);
			}

			if (method.getReturnType().isPrimitive())
				throw ExceptionBuilder.unchecked(
						"(primitive) value not set: "
								+ this.type.getSimpleName() + "#" + attribute
								+ "()").build();

			throw ExceptionBuilder.unchecked(
					"Unhandleable bean method: " + method
							+ " for run-time type: " + this.type).build();
		}
	}

	/**
	 * @param beanType should be a non-abstract concrete {@link Class} that has
	 *            a public zero-arg constructor
	 * @return the new {@link Provider} instance
	 */
	public static <T> Provider<T> createProvider(final Class<T> beanType,
			final Map<Class<?>, Provider<?>> cache, final Map<?, ?>... imports)
	{
		synchronized (cache)
		{
			@SuppressWarnings("unchecked")
			Provider<T> result = (Provider<T>) cache.get(beanType);
			if (result == null)
			{
				result = new DefaultProvider<T>(beanType, imports);
				cache.put(beanType, result);
			}
			return result;
		}
	}

	/**
	 * {@link DefaultProvider}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 *
	 * @param <T>
	 */
	protected static class DefaultProvider<T> implements Provider<T>
	{
		/** */
		private final Class<T> type;

		/** */
		private final Map<?, ?>[] imports;

		/**
		 * {@link DefaultProvider} constructor
		 * 
		 * @param type
		 */
		public DefaultProvider(final Class<T> type, final Map<?, ?>... imports)
		{
			this.type = type;
			this.imports = imports;
			// test bean property of having an accessible public
			// zero-arg constructor
			/*if (!type.isInterface())
				try
				{
					this.type.getConstructor().setAccessible(true);
				} catch (final Throwable t)
				{
					throw ExceptionBuilder.unchecked(
							"No public zero-arg bean constructor found for type: "
									+ this.type.getName(), t).build();
				}*/
		}

		@Override
		public T get()
		{
			try
			{
				return DynaBean.proxyOf(this.type, this.imports);
			} catch (final Throwable t)
			{
				throw ExceptionBuilder.unchecked(
						"Problem providing " + DynaBean.class.getSimpleName()
								+ " for type: " + this.type.getName(), t)
						.build();
			}
		}
	}

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 * 
	 * @param <T> the result type
	 * @param <THIS> the builder type
	 */
	protected static abstract class Builder<T extends DynaBean, THIS extends Builder<T, THIS>>
			implements Provider<T>
	{

		/** cache of type arguments for known {@link Builder} sub-types */
		private static final Map<Class<?>, List<Class<?>>> BUILDER_TYPE_ARGUMENT_CACHE = new HashMap<>();

		/** */
		private T ref;

		/**
		 * {@link Builder} constructor, to be extended by a public zero-arg
		 * constructor in concrete sub-types
		 */
		protected Builder()
		{
			this.ref = get();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T get()
		{
			final Class<T> type = (Class<T>) TypeUtil.getTypeArguments(
					Builder.class, getClass(), BUILDER_TYPE_ARGUMENT_CACHE)
					.get(0);
			try
			{
				return type.newInstance();
			} catch (final Throwable t)
			{
				throw ExceptionBuilder.unchecked(
						"Problem instantiating " + type, t).build();
			}
		}

		/**
		 * @return
		 */
		@JsonIgnore
		protected T getResult()
		{
			return this.ref;
		}

		/**
		 * @param key
		 * @param value
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public THIS with(final String key, final Object value)
		{
			getResult().set(key, value);
			return (THIS) this;
		}

		/**
		 * @return
		 */
		public T build()
		{
			getResult().lock();
			return getResult();
		}
	}
}