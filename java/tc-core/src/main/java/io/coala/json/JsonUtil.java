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
package io.coala.json;

import io.coala.error.ExceptionBuilder;
import io.coala.refer.Identifier;
import io.coala.type.DynaBean;
import io.coala.type.TypeUtil;

import java.beans.PropertyEditorSupport;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aeonbits.owner.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	}

	/** */
	public synchronized static ObjectMapper getJOM()
	{
		return JOM;
	}

	/**
	 * @param object
	 * @return
	 */
	public static String toJSON(final Object object)
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
	 * @param profile
	 * @return
	 */
	public static JsonNode toJSONNode(final Object object)
	{
		return getJOM().valueToTree(object);
	}

	/**
	 * @param object
	 * @return
	 */
	public static String toPrettyJSON(final Object object)
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
	 * @param stream
	 * @return
	 */
	public static JsonNode valueOf(final InputStream json)
	{
		try
		{
			return getJOM().readTree(json);
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked("Problem unmarshalling", e)
					.build();
		}
	}

	/**
	 * @param json the {@link InputStream}
	 * @param resultType the type of result {@link Object}
	 * @return the unmarshalled {@link Object}
	 */
	public static <T> T valueOf(final InputStream json,
			final Class<T> resultType, final Map<?, ?>... imports)
	{
		try
		{
			return (T) getJOM().readValue(json,
					checkRegistered(resultType, imports));
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked(
					"Problem unmarshalling " + resultType.getName()
							+ " from JSON stream", e).build();
		}
	}

	/**
	 * @param string
	 * @return
	 */
	public static JsonNode valueOf(final String json)
	{
		try
		{
			return getJOM().readTree(json);
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked(
					"Problem unmarshalling JSON: " + json, e).build();
		}
	}

	/**
	 * @param json
	 * @param resultType the type of result {@link Object}
	 * @return the unmarshalled {@link Object}
	 */
	public static <T> T valueOf(final String json, final Class<T> resultType,
			final Map<?, ?>... imports)
	{
		try
		{
			return (T) getJOM().readValue(json,
					checkRegistered(resultType, imports));
		} catch (final Exception e)
		{
			throw ExceptionBuilder.unchecked(
					"Problem unmarshalling " + resultType.getName()
							+ " from JSON: " + json, e).build();
		}
	}

	/** cache of type arguments for known {@link Identifier} sub-types */
	public static final Set<Class<?>> JSON_REGISTRATION_CACHE = new HashSet<>();

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
	 * @param type
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Class<T> checkRegistered(final Class<T> type,
			final Map<?, ?>... imports)
	{
		if (JSON_REGISTRATION_CACHE.contains(type))
			return type;

		// use Class.forName(String) ?
		// see http://stackoverflow.com/a/9130560

		if (Config.class.isAssignableFrom(type))
		{
			// TODO implement dynamic generic Converter for JSON bean properties
			// final Class<?> editorType = new
			// JsonPropertyEditor<T>().getClass();
			// PropertyEditorManager.registerEditor(type, editorType);
			// LOG.trace("Registered " + editorType + " - "
			// + PropertyEditorManager.findEditor(type));
		}

		if (Modifier.isAbstract(type.getModifiers()))
		{
			// LOG.trace("Dynabean JSON de/serializer for type: "+type);
			DynaBean.registerType(type, imports);
		} else if (JsonWrapper.class.isAssignableFrom(type))
		{
			// LOG.trace("Wrapper JSON de/serializer for type: "+type);
			JsonWrapper.Util.registerType((Class<? extends JsonWrapper>) type);
		}
		// else
		// LOG.trace("Normal JSON de/serializer for type: "+type);

		JSON_REGISTRATION_CACHE.add(type);
		return type;
	}
}
