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
import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Period;
import org.joda.time.ReadableDuration;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.TemporalAmount;

/**
 * {@link TimeSpan} extends {@link DecimalMeasure} with {@link #valueOf(String)}
 * for {@link Converters#CLASS_WITH_VALUE_OF_METHOD}.
 * <p>
 * Assumes {@linkplain Double#NaN} as value for illegal/empty value types
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class TimeSpan extends DecimalMeasure<Duration>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private static final Logger LOG = LogManager.getLogger(TimeSpan.class);

	/**
	 * @param measure
	 * @return
	 * 
	 * @see org.threeten.bp.Duration#parse(String)
	 * @see org.joda.time.format.ISOPeriodFormat#standard()
	 * @see DecimalMeasure
	 */
	public static final Measure<BigDecimal, Duration> parsePeriodOrMeasure(
			final String measure)
	{
		try
		{
			// final long millis = Period.parse(measure).getMillis();
			// return DecimalMeasure.valueOf(BigDecimal.valueOf(millis),
			// SI.MILLI(SI.SECOND));
			final org.threeten.bp.Duration temp = org.threeten.bp.Duration
					.parse(measure);
			return DecimalMeasure.valueOf(
					BigDecimal.valueOf(temp.getSeconds())
							.multiply(BigDecimal.TEN.pow(9))
							.add(BigDecimal.valueOf(temp.getNano())),
					SI.NANO(SI.SECOND));
		} catch (final Exception e)
		{
			try
			{
				final Period joda = Period.parse(measure);
				return DecimalMeasure
						.valueOf(BigDecimal.valueOf(joda.toStandardDuration()
								.getMillis()), SI.MILLI(SI.SECOND));
			} catch (final Exception e1)
			{
				LOG.trace("Joda failed", e1);
				return DecimalMeasure.valueOf(measure);
			}
		}
	}

	/**
	 * {@link TimeSpan} constructor for "natural" polymorphic Jackson bean
	 * deserialization
	 * 
	 * TODO allow ISO Period formats
	 * 
	 * @see com.fasterxml.jackson.databind.deser.BeanDeserializer
	 */
	public TimeSpan(final String measure)
	{
		this(parsePeriodOrMeasure(measure));
	}

	/**
	 * {@link TimeSpan} constructor for "natural" polymorphic Jackson bean
	 * deserialization
	 * 
	 * @see com.fasterxml.jackson.databind.deser.BeanDeserializer
	 */
	public TimeSpan(final double millis)
	{
		this(BigDecimal.valueOf(millis), SI.MILLI(SI.SECOND));
	}

	/**
	 * {@link TimeSpan} constructor for "natural" polymorphic Jackson bean
	 * deserialization
	 * 
	 * @see com.fasterxml.jackson.databind.deser.BeanDeserializer
	 */
	public TimeSpan(final int millis)
	{
		this(BigDecimal.valueOf(millis), SI.MILLI(SI.SECOND));
	}

	/**
	 * {@link TimeSpan} constructor
	 * 
	 * @param measure
	 * @param unit
	 */
	public TimeSpan(final Measure<BigDecimal, ?> measure)
	{
		this(measure.getValue(), measure.getUnit().asType(Duration.class));
	}

	/**
	 * {@link TimeSpan} constructor
	 * 
	 * @param value
	 * @param unit
	 */
	public TimeSpan(final BigDecimal value, final Unit<Duration> unit)
	{
		super(value, unit);
	}

	/**
	 * added for erasure-compatibility with
	 * {@link DecimalMeasure#valueOf(CharSequence)}
	 * 
	 * @see Converters.CLASS_WITH_VALUE_OF_METHOD
	 */
	// @SuppressWarnings("unchecked")
	// public static MeasurableDuration valueOf(final CharSequence value)
	// {
	// return valueOf(value.toString());
	// }

	/**
	 * {@link TimeSpan} static factory method
	 * 
	 * @param temporal
	 */
	public static Measurable<Duration> valueOf(final TemporalAmount temporal)
	{
		return new TimeSpan(BigDecimal.valueOf(temporal.get(ChronoUnit.NANOS))
				.add(BigDecimal.valueOf(temporal.get(ChronoUnit.MILLIS))
						.multiply(BigDecimal.TEN.pow(6))), SI.NANO(SI.SECOND));
	}

	/**
	 * {@link TimeSpan} static factory method
	 * 
	 * @param value
	 */
	public static TimeSpan valueOf(final ReadableDuration value)
	{
		return new TimeSpan(BigDecimal.valueOf(value.getMillis()),
				SI.MILLI(SI.SECOND));
	}

	/**
	 * {@link TimeSpan} static factory method
	 * 
	 * @param measure
	 */
	public static <V extends Number, Q extends Quantity> TimeSpan valueOf(
			final Measure<V, Q> measure)
	{
		return new TimeSpan(
				BigDecimal.valueOf(measure.getValue().doubleValue()), measure
						.getUnit().asType(Duration.class));
	}

	/**
	 * for "natural" Config value conversion
	 * 
	 * @see Converters.CLASS_WITH_VALUE_OF_METHOD
	 */
	public static TimeSpan valueOf(final String measure)
	{
		return new TimeSpan(measure);
	}

}