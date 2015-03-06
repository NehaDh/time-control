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
package com.almende.timecontrol.time;

import java.math.BigDecimal;

import javax.measure.DecimalMeasure;
import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

/**
 * {@link Rate} extends {@link DecimalMeasure} with
 * {@link #valueOf(String)} for {@link Converters#CLASS_WITH_VALUE_OF_METHOD}.
 * <p>
 * Assumes {@linkplain Double#NaN} as value for illegal/empty value types
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class Rate extends DecimalMeasure<Dimensionless>
{

	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link Rate} constructor for "natural" polymorphic Jackson bean
	 * deserialization
	 * 
	 * TODO handle {@link Double#NaN} etc.
	 * 
	 * @see com.fasterxml.jackson.databind.deser.BeanDeserializer
	 */
	public Rate(final String measure)
	{
		this(DecimalMeasure.valueOf(measure));
	}

	/**
	 * {@link Rate} constructor for "natural" polymorphic Jackson bean
	 * deserialization
	 * 
	 * @see com.fasterxml.jackson.databind.deser.BeanDeserializer
	 */
	public Rate(final double rate)
	{
		this(BigDecimal.valueOf(rate), Unit.ONE);
	}

	/**
	 * {@link Rate} constructor for "natural" polymorphic Jackson bean
	 * deserialization
	 * 
	 * @see com.fasterxml.jackson.databind.deser.BeanDeserializer
	 */
	public Rate(final int rate)
	{
		this(BigDecimal.valueOf(rate), Unit.ONE);
	}

	/**
	 * {@link Rate} constructor
	 * 
	 * @param measure
	 */
	public Rate(final Measure<BigDecimal, ?> measure)
	{
		this(measure.getValue(), measure.getUnit().asType(Dimensionless.class));
	}

	/**
	 * {@link Rate} constructor
	 * 
	 * @param value
	 * @param unit
	 */
	public Rate(final BigDecimal value, final Unit<Dimensionless> unit)
	{
		super(value, unit);
	}

	/**
	 * {@link Rate} static factory method
	 * 
	 * @param value
	 */
	public static Rate valueOf(final BigDecimal value)
	{
		return new Rate(value, Unit.ONE);
	}

	/**
	 * {@link Rate} static factory method
	 * 
	 * @param value
	 */
	public static Rate valueOf(final Number value)
	{
		return new Rate(value.doubleValue());
	}

	/**
	 * {@link Rate} static factory method
	 * 
	 * @param measure
	 */
	public static <V extends Number, Q extends Quantity> Rate valueOf(
			final Measure<V, Q> measure)
	{
		return new Rate(BigDecimal.valueOf(measure.getValue()
				.doubleValue()), measure.getUnit().asType(Dimensionless.class));
	}

	/**
	 * {@link Rate} static factory method
	 * 
	 * @see Converters.CLASS_WITH_VALUE_OF_METHOD
	 */
	public static Rate valueOf(final String value)
	{
		return new Rate(value);
	}
}