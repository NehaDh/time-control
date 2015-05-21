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
package io.coala.json.dynabean;

import io.coala.error.ExceptionBuilder;
import io.coala.id.Identifier;
import io.coala.util.JsonUtil;
import io.coala.util.LogUtil;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import javax.inject.Provider;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
public class DynaBean implements Cloneable
{

	/** */
	static final Logger LOG = LogUtil.getLogger(DynaBean.class);

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

	@JsonAnyGetter
	protected Map<String, Object> any()
	{
		if (this.dynamicProperties == null)
			return Collections.emptyMap();

		return this.dynamicProperties;
	}

	/**
	 * @param key
	 * @return
	 */
	public Object get(final String key)
	{
		return any().get(key);
	}

	/**
	 * helper-method
	 * 
	 * @param key
	 * @param defaultValue
	 * @return the dynamically set value, or {@code defaultValue} if not set
	 */
	@SuppressWarnings("unchecked")
	protected <T> T get(final String key, final T defaultValue)
	{
		final Object result = get(key);
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
	protected <T> T get(final String key, final Class<T> returnType)
	{
		return (T) get(key);
	}

	private Map<String, Object> getOrCreateMap()
	{
		if (this.dynamicProperties == null)
			this.dynamicProperties = new TreeMap<String, Object>();
		return this.dynamicProperties;
	}

	protected void set(final Map<String, Object> values)
	{
		Map<String, Object> map = getOrCreateMap();
		synchronized (map)
		{
			map.putAll(values);
		}
	}

	@JsonAnySetter
	protected Object set(final String key, final Object value)
	{
		Map<String, Object> map = getOrCreateMap();
		synchronized (map)
		{
			return map.put(key, value);
		}
	}

	protected Object remove(final String key)
	{
		Map<String, Object> map = getOrCreateMap();
		synchronized (map)
		{
			return map.remove(key);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected DynaBean clone()
	{
		final Map<String, Object> values = any();
		final DynaBean result = new DynaBean();
		result.set(JsonUtil.valueOf(JsonUtil.toTree(values), values.getClass()));
		return result;
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
			return JsonUtil.getJOM()
					.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
					.writeValueAsString(any());
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
	public static <T> T valueOf(final Class<T> type,
			final Properties... imports)
	{
		return valueOf(type, new DynaBean());
	}

	/**
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T valueOf(final Class<T> type, final DynaBean bean,
			final Properties... imports)
	{
		if (!type.isAnnotationPresent(BeanWrapper.class))
			throw ExceptionBuilder.unchecked(
					"Type is not a @" + BeanWrapper.class.getSimpleName())
					.build();

		return (T) Proxy.newProxyInstance(type.getClassLoader(),
				new Class[] { type }, new DynaBeanInvocationHandler(type, bean,
						imports));
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
				// non-Proxy objects get default treatment
				if (!Proxy.isProxyClass(value.getClass()))
				{
					serializers.findValueSerializer(value.getClass(), null)
							.serialize(value, jgen, serializers);
					return;
				}

				// BeanWrapper gets special treatment
				if (DynaBeanInvocationHandler.class.isInstance(Proxy
						.getInvocationHandler(value)))
				{
					final DynaBeanInvocationHandler handler = (DynaBeanInvocationHandler) Proxy
							.getInvocationHandler(value);
					// LOG.trace("Finding serializer for " +
					// handler.bean.getClass());
					serializers.findValueSerializer(handler.bean.getClass(),
							null).serialize(handler.bean, jgen, serializers);
					return;
				}

				// Config (Accessible) gets special treatment
				if (Accessible.class.isInstance(value))
				{
					final Accessible config = (Accessible) value;
					final Properties entries = new Properties();
					for (String key : config.propertyNames())
						entries.put(key, config.getProperty(key));
					serializers.findValueSerializer(entries.getClass(), null)
							.serialize(entries, jgen, serializers);
					return;
				}

				if (Config.class.isInstance(value))
					throw new JsonGenerationException("Config should extend "
							+ Accessible.class.getName()
							+ " required for serialization: "
							+ Arrays.asList(value.getClass().getInterfaces()));

				throw new JsonGenerationException(
						"No serializer found for proxy of: "
								+ Arrays.asList(value.getClass()
										.getInterfaces()));
			}
		};
	}

	/** */
	public static <T> void registerType(final ObjectMapper om,
			final Class<T> type, final Properties... imports)
	{
		// TODO implement dynamic generic Converter(s) for JSON bean
		// properties ?

		// if (Config.class.isAssignableFrom(type))
		// {
		// final Class<?> editorType = new
		// JsonPropertyEditor<T>().getClass();
		// PropertyEditorManager.registerEditor(type, editorType);
		// LOG.trace("Registered " + editorType + " - "
		// + PropertyEditorManager.findEditor(type));
		// }

		om.registerModule(new SimpleModule().addSerializer(type,
				createJsonSerializer(type)).addDeserializer(type,
				createJsonDeserializer(om, type, imports)));
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
			final ObjectMapper om, final Class<T> resultType,
			final Properties... imports)
	{
		return new JsonDeserializer<T>()
		{
			@Override
			public T deserialize(final JsonParser jp,
					final DeserializationContext ctxt) throws IOException,
					JsonProcessingException
			{
				if (jp.getCurrentToken() == JsonToken.VALUE_NULL)
					return null;

				if (Config.class.isAssignableFrom(resultType))
				{
					final Map<String, Object> entries = jp
							.readValueAs(new TypeReference<Map<String, Object>>()
							{
							});

					final Iterator<Entry<String, Object>> it = entries
							.entrySet().iterator();
					for (Entry<String, Object> next = null; it.hasNext(); next = it
							.next())
						if (next != null && next.getValue() == null)
						{
							LOG.trace("Ignoring null value: {}", next);
							it.remove();
						}
					return resultType.cast(ConfigFactory.create(
							resultType.asSubclass(Config.class), entries));
				}
				// else if (Config.class.isAssignableFrom(resultType))
				// throw new JsonGenerationException(
				// "Config does not extend "+Mutable.class.getName()+" required for deserialization: "
				// + Arrays.asList(resultType
				// .getInterfaces()));

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

						bean.set(method.getName(), om.treeToValue(
								value,
								JsonUtil.checkRegistered(om,
										method.getReturnType(), imports)));
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

				return DynaBean.valueOf(resultType, bean, imports);
			}
		};
	}

	/**
	 * @param beanType should be a non-abstract concrete {@link Class} that has
	 *            a public zero-arg constructor
	 * @return the new {@link Provider} instance
	 */
	public static <T> Provider<T> createProvider(final Class<T> beanType,
			final Map<Class<?>, Provider<?>> cache, final Properties... imports)
	{
		synchronized (cache)
		{
			@SuppressWarnings("unchecked")
			Provider<T> result = (Provider<T>) cache.get(beanType);
			if (result == null)
			{
				result = new DynaBeanProxyProvider<T>(new DynaBean(), imports);
				cache.put(beanType, result);
			}
			return result;
		}
	}

	/**
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface BeanWrapper
	{
		/**
		 * @return
		 */
		String comparableOn() default "";
	}

	/** */
	private static final Map<BeanWrapper, Comparator<?>> COMPARATOR_CACHE = new TreeMap<>();

	/**
	 * @param annot the {@link BeanWrapper} instance for the type of wrapper of
	 *            {@link DynaBean}s containing the {@link Comparable} value type
	 *            in the annotated property key
	 * @return a (cached) comparator
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <S extends Comparable> Comparator<S> getComparator(
			final BeanWrapper annot)
	{
		if (annot.comparableOn().isEmpty())
			return null;
		synchronized (COMPARATOR_CACHE)
		{
			Comparator<S> result = (Comparator<S>) COMPARATOR_CACHE.get(annot);
			if (result == null)
			{
				result = new Comparator<S>()
				{
					@Override
					public int compare(final S o1, final S o2)
					{
						final S key1 = (S) ((DynaBeanInvocationHandler) Proxy
								.getInvocationHandler(o1)).bean.any().get(
								annot.comparableOn());
						final S key2 = (S) ((DynaBeanInvocationHandler) Proxy
								.getInvocationHandler(o2)).bean.any().get(
								annot.comparableOn());
						return key1.compareTo(key2);
					}
				};
				LOG.trace("Created comparator for " + annot);
				COMPARATOR_CACHE.put(annot, result);
			}
			return result;
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
	public static class Builder<T, THIS extends Builder<T, THIS>> extends
			DynaBeanProxyProvider<T>
	{

		/** */
		private final DynaBean bean;

		/**
		 * {@link Builder} constructor, to be extended by a public zero-arg
		 * constructor in concrete sub-types
		 */
		protected Builder(final Properties... imports)
		{
			this(new DynaBean(), imports);
		}

		/**
		 * {@link Builder} constructor, to be extended by a public zero-arg
		 * constructor in concrete sub-types
		 */
		protected Builder(final DynaBean bean, final Properties... imports)
		{
			super(bean, imports);
			this.bean = bean;
		}

		/**
		 * helper-method
		 * 
		 * @param key
		 * @param returnType
		 * @return the currently set value, or {@code null} if not set
		 */
		@SuppressWarnings("unchecked")
		protected <S> S get(final String key, final Class<S> returnType)
		{
			return (S) this.bean.get(key);
		}

		/**
		 * @param key
		 * @param value
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public THIS with(final String key, final Object value)
		{
			this.bean.set(key, value);
			return (THIS) this;
		}

		public THIS with(final String key, final TreeNode value,
				final Class<?> valueType)
		{
			return (THIS) with(key, JsonUtil.valueOf(value, valueType));
		}

		/**
		 * @return this Builder with the immutable bean
		 */
		@SuppressWarnings("unchecked")
		public THIS lock()
		{
			this.bean.lock();
			return (THIS) this;
		}

		/**
		 * @return the provided instance of <T>
		 */
		public T build()
		{
			return get();
		}
	}
}