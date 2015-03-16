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
import io.coala.id.Identifier;
import io.coala.util.JsonUtil;
import io.coala.util.TypeUtil;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeType;

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

	// TODO enforce #valueOf(String), #valueOf(Number) or #valueOf(JsonNode) ?

	/**
	 * {@linkplain JsonPolymorphic} indicates that a certain
	 * {@linkplain JsonWrapper}-subtype can be deserialized (using alternate
	 * subtypes of the default wrapped value type, applying respective
	 * {@linkplain JsonSerializer}s and {@linkplain JsonDeserializer}s) from
	 * various JSON value types (number, string, object, or boolean).
	 * <p>
	 * For instance, a {@code MyJsonWrapper} wraps a {@link Number} for its
	 * values, and is annotated as {@linkplain #objectType()
	 * JsonPolymorphic(objectType=MyNumber.class)} to indicate JSON object type
	 * values must be deserialized as custom defined {@code MyNumber} instances
	 * (which also extend the default {@link Number} value type)
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface JsonPolymorphic
	{
		/**
		 * @return the value subtype to parse in case of a
		 *         {@link JsonNodeType#NUMBER} or
		 *         {@link JsonToken#VALUE_NUMBER_INT} or
		 *         {@link JsonToken#VALUE_NUMBER_FLOAT}
		 */
		Class<?> numberType() default Empty.class;

		/**
		 * @return the value subtype to parse in case of a
		 *         {@link JsonNodeType#STRING} or {@link JsonToken#VALUE_STRING}
		 */
		Class<?> stringType() default Empty.class;

		/**
		 * @return the value subtype to parse in case of a
		 *         {@link JsonNodeType#OBJECT} or {@link JsonToken#START_OBJECT}
		 */
		Class<?> objectType() default Empty.class;

		/**
		 * @return the value subtype to parse in case of a
		 *         {@link JsonNodeType#BOOLEAN} or {@link JsonToken#VALUE_TRUE}
		 *         or {@link JsonToken#VALUE_FALSE}
		 */
		Class<?> booleanType() default Empty.class;

		/**
		 * {@link Empty}
		 * 
		 * @date $Date$
		 * @version $Id$
		 * @author <a href="mailto:rick@almende.org">Rick</a>
		 */
		class Empty
		{

		}
	}

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
		private static final Logger LOG = LogManager.getLogger(Util.class);

		/** singleton constructor */
		private Util()
		{
			// singleton
		}

		/** cache of type arguments for known {@link Identifier} sub-types */
		public static final Map<Class<?>, List<Class<?>>> WRAPPER_TYPE_ARGUMENT_CACHE = new WeakHashMap<>();

		/**  */
		public static <S, T extends JsonWrapper<S>> void registerType(
				final ObjectMapper om, final Class<T> type)
		{
			// LOG.trace("Resolving value type arg for: " + type.getName());
			@SuppressWarnings("unchecked")
			final Class<S> valueType = (Class<S>) TypeUtil.getTypeArguments(
					JsonWrapper.class, type).get(0);
			// LOG.trace("Resolved value type arg: " + valueType);
			registerType(om, type, valueType);
		}

		/**  */
		public static <S, T extends JsonWrapper<S>> void registerType(
				final ObjectMapper om, final Class<T> type,
				final Class<S> valueType)
		{
			om.registerModule(new SimpleModule().addSerializer(type,
					createJsonSerializer(type, valueType)).addDeserializer(
					type, createJsonDeserializer(type, valueType)));
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

					// LOG.trace("parsing " + jp.getText() + " as "
					// + type.getName());
					final JsonPolymorphic annot = type
							.getAnnotation(JsonPolymorphic.class);

					final S value; // = jp.readValueAs(valueType)

					if (annot == null)
						value = jp.readValueAs(valueType);
					else
					{
						final Class<? extends S> valueSubtype = resolveSubtype(
								annot, valueType, jp.getCurrentToken());
						// LOG.trace("parsing " + jp.getCurrentToken() + " as "
						// + valueSubtype.getName());
						value = jp.readValueAs(valueSubtype);

						// final JsonNode tree = jp.readValueAsTree();
						// final Class<? extends S> valueSubtype =
						// resolveSubtype(
						// annot, valueType, tree.getNodeType());
						// LOG.trace("parsing " + tree.getNodeType() + " as "
						// + valueSubtype.getName());
						// value = JsonUtil.getJOM().treeToValue(tree,
						// valueSubtype);
					}

					final T result = this.provider.get();
					result.setValue(value);
					return result;
				}
			};
		}

		/**
		 * @param json
		 * @param type
		 * @return
		 */
		public static <S, T extends JsonWrapper<S>> T valueOf(
				final String json, final Class<T> type)
		{
			return valueOf(json, TypeUtil.createBeanProvider(type));
		}

		/**
		 * @param json
		 * @param provider
		 * @return
		 */
		public static <S, T extends JsonWrapper<S>> T valueOf(
				final String json, final Provider<T> provider)
		{
			return valueOf(json, provider.get());
		}

		/**
		 * @param json
		 * @param result
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static <S, T extends JsonWrapper<S>> T valueOf(
				final String json, final T result)
		{
			try
			{
				final Class<S> valueType = (Class<S>) TypeUtil
						.getTypeArguments(JsonWrapper.class, result.getClass(),
								JsonWrapper.Util.WRAPPER_TYPE_ARGUMENT_CACHE)
						.get(0);

				final JsonPolymorphic annot = result.getClass().getAnnotation(
						JsonPolymorphic.class);

				final S value;

				if (annot == null)
					value = valueType == String.class ? (S) json : JsonUtil
							.valueOf(json, valueType);
				else
				{
					final JsonNode tree = JsonUtil.valueOf(json);
					final Class<? extends S> valueSubtype = resolveSubtype(
							annot, valueType, tree.getNodeType());
					value = JsonUtil.valueOf(json, valueSubtype);
				}
				result.setValue(value);
				return result;
			} catch (final Throwable e)
			{
				throw ExceptionBuilder.unchecked(
						"Problem reading value: " + json, e).build();
			}
		}

		/**
		 * @param annot
		 * @param valueType
		 * @return the correct
		 */
		public static <S, T extends JsonWrapper<S>> Class<? extends S> resolveSubtype(
				final JsonPolymorphic annot, final Class<S> valueType,
				final JsonToken jsonToken)
		{
			final Class<?> result;
			switch (jsonToken)
			{
			case VALUE_TRUE:
			case VALUE_FALSE:
				result = annot.booleanType();
				break;
			case VALUE_NUMBER_INT:
			case VALUE_NUMBER_FLOAT:
				result = annot.numberType();
				break;
			case VALUE_EMBEDDED_OBJECT:
				result = annot.objectType();
				break;
			case VALUE_STRING:
				result = annot.stringType();
				break;
			default:
				return valueType;
			}

			if (result == null || result == JsonPolymorphic.Empty.class)
				return valueType;

			if (!valueType.isAssignableFrom(result))
			{
				LOG.warn(JsonPolymorphic.class.getSimpleName()
						+ " annotation contains illegal value: "
						+ result.getName() + " does not extend/implement "
						+ valueType.getName());
				return valueType;
			}

			return result.asSubclass(valueType);
		}

		/**
		 * @param polymorphicType
		 * @param jsonNodeType
		 * @return the correct
		 */
		public static <S, T extends JsonWrapper<S>> Class<? extends S> resolveSubtype(
				final JsonPolymorphic annot, final Class<S> valueType,
				final JsonNodeType jsonNodeType)
		{
			final Class<?> result;
			switch (jsonNodeType)
			{
			case BOOLEAN:
				result = annot.booleanType();
				break;
			case NUMBER:
				result = annot.numberType();
				break;
			case OBJECT:
				result = annot.objectType();
				break;
			case POJO:
				result = annot.objectType();
				break;
			case STRING:
				result = annot.stringType();
				break;
			default:
				return valueType;
			}

			if (result == null || result == JsonPolymorphic.Empty.class)
				return valueType;

			if (!valueType.isAssignableFrom(result))
			{
				LOG.warn(JsonPolymorphic.class.getSimpleName()
						+ " annotation contains illegal value: "
						+ result.getName() + " does not extend/implement "
						+ valueType.getName());
				return valueType;
			}

			return result.asSubclass(valueType);
		}
	}
}
