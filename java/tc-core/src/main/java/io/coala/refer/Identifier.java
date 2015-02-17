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
package io.coala.refer;

import io.coala.error.ExceptionBuilder;
import io.coala.json.JsonUtil;
import io.coala.json.JsonWrapper;
import io.coala.type.TypeUtil;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.inject.Provider;
import javax.persistence.Embeddable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * {@link Identifier} wraps some reference value, e.g. of an
 * {@link Identifiable} object. Its un/wrapping should be handled automatically
 * handled at JSON de/serialization. See also this page on using <a
 * href="http://wiki.fasterxml.com/JacksonPolymorphicDeserialization" >Jackson
 * Polymorphic Deserialization</a>
 * 
 * <p>
 * TODO apply javax.naming implementations ({@link javax.naming.Reference
 * Reference}, {@link javax.naming.Referenceable Referenceable})?
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 * 
 * @param <T> the wrapped ({@link Comparable}) type of reference value
 */
@Embeddable
@JsonInclude(Include.NON_NULL)
public abstract class Identifier<T extends Comparable<T>> implements
		Comparable<Identifier<T>>, JsonWrapper<T>
{

	/** */
	private static final Logger LOG = LogManager.getLogger(Identifier.class);

	/** */
	private T value = null;

	/**
	 * @param value the new reference value
	 */
	public void setValue(final T value)
	{
		this.value = value;
	}

	/**
	 * @return the reference value
	 */
	public T getValue()
	{
		return this.value;
	}

	@Override
	public String toString()
	{
		try
		{
			return JsonUtil.getJOM().writeValueAsString(getValue());
		} catch (final IOException e)
		{
			LOG.warn("Problem serializing " + getClass().getName()
					+ " wrapping " + getValue(), e);
			return getValue().toString();
		}
	}

	@Override
	public int compareTo(final Identifier<T> o)
	{
		return getValue().compareTo((T) o.getValue());
	}

	/**
	 * TODO see if the {@linkplain Class type} argument can be resolved from
	 * this method's {@linkplain Method#getReturnType() return type} at runtime
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	public static <S extends Comparable<S>, T extends Identifier<S>> T of(
			final String value, final Class<T> type)
	{
		return of(value, TypeUtil.createBeanProvider(type));
	}

	/**
	 * @param value
	 * @param provider
	 * @return
	 */
	public static <S extends Comparable<S>, T extends Identifier<S>> T of(
			final String value, final Provider<T> provider)
	{
		return of(value, provider.get());
	}

	/**
	 * @param value
	 * @param result
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <S extends Comparable<S>, T extends Identifier<S>> T of(
			final String value, final T result)
	{
		try
		{
			final Class<S> valueType = (Class<S>) TypeUtil.getTypeArguments(
					JsonWrapper.class, result.getClass(),
					JsonWrapper.Util.WRAPPER_TYPE_ARGUMENT_CACHE).get(0);
			result.setValue(JsonUtil.valueOf(value, valueType));
			return result;
		} catch (final Throwable e)
		{
			throw ExceptionBuilder.unchecked("Problem reading value: " + value,
					e).build();
		}
	}

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 * 
	 * @param <S> the {@link Comparable} type of value wrapped as
	 *            {@link Reference}
	 * @param <T> the concrete type of {@link Reference} being built
	 * @param <THIS> the concrete type of {@link Builder}
	 */
	/*protected static abstract class Builder<S extends Comparable<S>, T extends Reference<S>, THIS extends Builder<S, T, THIS>>
			extends JsonDynaBean.Builder<T, THIS>
	{
		@SuppressWarnings("unchecked")
		public THIS withValue(final S value)
		{
			getResult().value = value;
			return (THIS) this;
		}
	}*/
}