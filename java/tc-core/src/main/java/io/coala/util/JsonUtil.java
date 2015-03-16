/* $Id: cccb3d9313ff65e159321e3a8283c101ef6a24b3 $
 * 
 * Part of the EU project Adapt4EE, see http://www.adapt4ee.eu/
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
 * Copyright (c) 2015 Almende B.V. 
 */
package io.coala.util;

import io.coala.error.ExceptionBuilder;
import io.coala.json.JsonWrapper;
import io.coala.json.dynabean.DynaBean;
import io.coala.json.dynabean.DynaBean.BeanWrapper;

import java.beans.PropertyEditorSupport;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * {@link JsonUtil}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class JsonUtil
{

	/** */
	private static final Logger LOG = LogManager.getLogger(JsonUtil.class);

	/** */
	private static final ObjectMapper JOM = new ObjectMapper();

	/** singleton design pattern constructor */
	private JsonUtil()
	{
		// singleton design pattern
		LOG.trace("Using jackson v: " + JOM.version());
		JOM.registerModule(new JodaModule());
	}

	/** */
	public synchronized static ObjectMapper getJOM()
	{
		return JOM;
	}

	/**
	 * @param object the object to serialize/marshal
	 * @return the (minimal) JSON representation
	 */
	public static String stringify(final Object object)
	{
		try
		{
			return getJOM().writer().writeValueAsString(object);
		} catch (final JsonProcessingException e)
		{
			throw ExceptionBuilder.unchecked("Problem JSONifying", e).build();
		}
	}

	/**
	 * @param object the object to serialize/marshal
	 * @return the (pretty) JSON representation
	 */
	public static String toJSON(final Object object)
	{
		try
		{
			return getJOM()
					// .setSerializationInclusion(JsonInclude.Include.NON_NULL)
					.writer().withDefaultPrettyPrinter()
					.writeValueAsString(object);
		} catch (final JsonProcessingException e)
		{
			throw ExceptionBuilder.unchecked("Problem JSONifying", e).build();
		}
	}

	/**
	 * @param profile
	 * @return
	 */
	public static JsonNode toTree(final Object object)
	{
		final ObjectMapper om = getJOM();
		// checkRegistered(om, object.getClass());
		// return om.valueToTree(object);
		try
		{
			return om.readTree(stringify(object));
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked(e,
					"Problem serializing " + object.getClass().getSimpleName())
					.build();
		}
	}

	/**
	 * @param json the {@link InputStream}
	 * @return
	 */
	public static JsonNode valueOf(final InputStream json)
	{
		try
		{
			return json == null ? null : getJOM().readTree(json);
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked("Problem unmarshalling", e)
					.build();
		}
	}

	/**
	 * @param json the {@link InputStream}
	 * @param resultType the type of result {@link Object}
	 * @return the parsed/deserialized/unmarshalled {@link Object}
	 */
	public static <T> T valueOf(final InputStream json,
			final Class<T> resultType, final Properties... imports)
	{
		try
		{
			final ObjectMapper om = getJOM();
			return json == null ? null : (T) om.readValue(json,
					checkRegistered(om, resultType, imports));
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked(
					"Problem unmarshalling " + resultType.getName()
							+ " from JSON stream", e).build();
		}
	}

	/**
	 * @param json the JSON formatted value
	 * @return the parsed/deserialized/unmarshalled {@link JsonNode} tree
	 * @see ObjectMapper#readTree(String)
	 */
	public static JsonNode valueOf(final String json)
	{
		try
		{
			return json == null || json.isEmpty() ? null : getJOM().readTree(
					json);
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked(
					"Problem unmarshalling JSON: " + json, e).build();
		}
	}

	/**
	 * @param json the JSON formatted value
	 * @param resultType the type of result {@link Object}
	 * @return the parsed/deserialized/unmarshalled {@link Object}
	 */
	public static <T> T valueOf(final String json, final Class<T> resultType,
			final Properties... imports)
	{
		try
		{
			final ObjectMapper om = getJOM();
			return json == null || json.isEmpty() ? null : (T) om.readValue(
					json, checkRegistered(om, resultType, imports));
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked(
					"Problem unmarshalling " + resultType.getName()
							+ " from JSON: " + json, e).build();
		}
	}

	/**
	 * @param node the partially parsed JSON {@link TreeNode}
	 * @param resultType the type of result {@link Object}
	 * @return the parsed/deserialized/unmarshalled {@link Object}
	 */
	public static <T> T valueOf(final TreeNode json, Class<T> resultType,
			final Properties... imports)
	{
		try
		{
			final ObjectMapper om = getJOM();
			return json == null ? null : (T) om.treeToValue(json,
					checkRegistered(om, resultType, imports));
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked(
					"Problem unmarshalling " + resultType.getName()
							+ " from JSON: " + json, e).build();
		}
	}

	public static class JsonPropertyEditor<E> extends PropertyEditorSupport
	{
		/** */
		private Class<E> type;

		@SuppressWarnings({ "unchecked" })
		private JsonPropertyEditor()
		{
			this.type = (Class<E>) TypeUtil.getTypeArguments(
					JsonPropertyEditor.class, getClass()).get(0);
		}

		@Override
		public void setAsText(final String json)
				throws IllegalArgumentException
		{
			try
			{
				setValue(JsonUtil.valueOf(json, this.type));
			} catch (final Throwable e)
			{
				throw new IllegalArgumentException(
						"Problem editing property of type: "
								+ this.type.getName() + " from JSON value: "
								+ json, e);
			}
		}
	}

	/**
	 * cache of registered {@link JsonWrapper} or {@link DynaBean} types per
	 * {@link ObjectMapper}'s {@link #hashCode()}
	 */
	public static final Map<ObjectMapper, Set<Class<?>>> JSON_REGISTRATION_CACHE = new WeakHashMap<>();

	/**
	 * @param type
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Class<T> checkRegistered(final ObjectMapper om,
			final Class<T> type, final Properties... imports)
	{
		synchronized (JSON_REGISTRATION_CACHE)
		{
			Set<Class<?>> cache = JSON_REGISTRATION_CACHE.get(om);
			if (cache == null)
			{
				cache = new HashSet<>();
				JSON_REGISTRATION_CACHE.put(om, cache);
			}
			if (cache.contains(type))
				return type;

			// use Class.forName(String) ?
			// see http://stackoverflow.com/a/9130560

			if (Proxy.isProxyClass(type)
					|| type.isAnnotationPresent(BeanWrapper.class))
			{
				DynaBean.registerType(om, type, imports);

				for (Method method : type.getDeclaredMethods())
					if (method.getReturnType() != Void.TYPE
							&& method.getReturnType() != type
							&& !cache.contains(type))
					{
						checkRegistered(om, method.getReturnType(), imports);
						cache.add(method.getReturnType());
					}

				// LOG.trace("Registered Dynabean de/serializer for: " + type);
			} else if (JsonWrapper.class.isAssignableFrom(type))
				// {
				JsonWrapper.Util.registerType(om,
						(Class<? extends JsonWrapper>) type);

			// LOG.trace("Registered Wrapper de/serializer for: " + type);
			// } else
			// LOG.trace("Assume default de/serializer for: " + type);

			cache.add(type);

			return type;
		}
	}
}
