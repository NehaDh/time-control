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
package io.coala.json;

import io.coala.error.ExceptionBuilder;
import io.coala.refer.Identifier;
import io.coala.type.TypeUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Provider;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * {@link JsonWrapper} is a tag for types that should be automatically
 * un/wrapped upon JSON de/serialization
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface JsonWrapper<T>
{

	/**
	 * @return the wrapped value
	 */
	T getValue();

	/**
	 * @param value the value to wrap
	 */
	void setValue(T value);

	/**
	 * {@link Util}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class Util
	{

		/** */
		// private static final Logger LOG = LogManager.getLogger(Util.class);

		/** singleton constructor */
		private Util()
		{
			// singleton
		}

		/** cache of type arguments for known {@link Identifier} sub-types */
		public static final Map<Class<?>, List<Class<?>>> WRAPPER_TYPE_ARGUMENT_CACHE = new WeakHashMap<>();

		/**  */
		public static <S, T extends JsonWrapper<S>> void registerType(
				final Class<T> type)
		{
			@SuppressWarnings("unchecked")
			final Class<S> valueType = (Class<S>) TypeUtil.getTypeArguments(
					JsonWrapper.class, type).get(0);
			// LOG.trace("Resolved value type arg: " + valueType);
			registerType(type, valueType);
		}

		/**  */
		public static <S, T extends JsonWrapper<S>> void registerType(
				final Class<T> type, final Class<S> valueType)
		{
			final SimpleModule module = new SimpleModule();
			module.addSerializer(type, createJsonSerializer(type, valueType))
					.addDeserializer(type,
							createJsonDeserializer(type, valueType));
			JsonUtil.getJOM().registerModule(module);
		}

		/**
		 * @param wrapperType
		 * @param <S>
		 * @param <T>
		 * @return
		 */
		public static final <S, T extends JsonWrapper<S>> JsonSerializer<T> createJsonSerializer(
				final Class<T> type, final Class<S> valueType)
		{
			return new JsonSerializer<T>()
			{
				@Override
				public void serialize(final T value, final JsonGenerator jgen,
						final SerializerProvider serializers)
						throws IOException, JsonProcessingException
				{
					serializers.findValueSerializer(valueType).serialize(
							value.getValue(), jgen, serializers);
				}
			};
		}

		/**
		 * @param referenceType
		 * @param <S>
		 * @param <T>
		 * @return
		 */
		public static final <S, T extends JsonWrapper<S>> JsonDeserializer<T> createJsonDeserializer(
				final Class<T> type, final Class<S> valueType)
		{
			return new JsonDeserializer<T>()
			{
				private final Provider<T> provider = TypeUtil
						.createBeanProvider(type);

				@Override
				public T deserialize(final JsonParser jp,
						final DeserializationContext ctxt) throws IOException,
						JsonProcessingException
				{
					if (jp.getText() == null || jp.getText().length() == 0
							|| jp.getText().equals("null"))
						return null;

					final T result = this.provider.get();
					result.setValue((S) jp.readValueAs(valueType));
					return result;
				}
			};
		}

		/**
		 * @param value
		 * @param type
		 * @return
		 */
		public static <S, T extends JsonWrapper<S>> T wrapperOf(
				final String value, final Class<T> type)
		{
			return wrapperOf(value, TypeUtil.createBeanProvider(type));
		}

		/**
		 * @param value
		 * @param provider
		 * @return
		 */
		public static <S, T extends JsonWrapper<S>> T wrapperOf(
				final String value, final Provider<T> provider)
		{
			return wrapperOf(value, provider.get());
		}

		/**
		 * @param value
		 * @param result
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static <S, T extends JsonWrapper<S>> T wrapperOf(
				final String value, final T result)
		{
			try
			{
				final Class<S> valueType = (Class<S>) TypeUtil
						.getTypeArguments(JsonWrapper.class, result.getClass(),
								WRAPPER_TYPE_ARGUMENT_CACHE).get(0);
				result.setValue(JsonUtil.valueOf(value, valueType));
				return result;
			} catch (final Throwable e)
			{
				throw ExceptionBuilder.unchecked(
						"Problem reading value: " + value, e).build();
			}
		}
	}
}
