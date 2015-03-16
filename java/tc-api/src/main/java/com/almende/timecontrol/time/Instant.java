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

import io.coala.json.JsonWrapper;
import io.coala.json.JsonWrapper.JsonPolymorphic;
import io.coala.util.JsonUtil;

import java.math.BigDecimal;
import java.util.Date;

import javax.measure.DecimalMeasure;
import javax.measure.Measurable;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.joda.time.ReadableInstant;
import org.threeten.bp.temporal.ChronoField;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain Instant} wraps a {@linkplain TimeSpan} that is
 * {@linkplain JsonPolymorphic} (measuring the duration since the EPOCH,
 * 1970-01-01T00:00:00Z) and provides a {@link #valueOf(String)} method for
 * loading as configured value {@link Converters#CLASS_WITH_VALUE_OF_METHOD}
 * <p>
 * We considered various temporal measure implementations, including
 * <dl>
 * <dt>Java's native utilities</dt>
 * <dd>{@link java.util.Date} and {@link java.util.GregorianCalendar}</dd>
 * <dt></dt>
 * <dt>The JSR-275 {@code javax.measure} reference implementation v4.3.1 from <a
 * href="http://jscience.org/">jscience.org</a></dt>
 * <dd>
 * <li>takes any value type (e.g. {@linkplain Number}) or granularity (e.g.
 * {@link SI#NANO(javax.measure.unit.Unit nano)} or
 * {@link SI#PICO(javax.measure.unit.Unit) pico})</li></dd>
 * <dt>The JSR-310 {@code javax.time} Java8 extension back-port from <a
 * href="http://www.threeten.org/">threeten.org</a>:</dt>
 * <dd>
 * <li>supports nanosecond precision,</li>
 * <li>{@linkplain org.threeten.bp.Instant} parses strictly ISO8601 format
 * (millis/nanos) only</li>
 * <dt>Joda's time API from <a href="http://www.joda.org/">joda.org</a></dt>
 * <dd>
 * <li>Allows lenient (lower and upper case) ISO8601 format strings</li>
 * <li>Joda time offers this <a
 * href="https://github.com/FasterXML/jackson-datatype-joda">datatype extension
 * for Jackson</a>.</li>
 * <li>offers many nice calendar and formatter implementations</li>
 * <li>will <a href="https://github.com/JodaOrg/joda-time/issues/52">not support
 * microsecond or nanosecond precision</a></li></dd>
 * <dt>Apache {@code commons-lang3} Date Utilities</dt>
 * <dd>
 * limitations similar to Joda's time API (millisecond precision only)</dd>
 * </dl>
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@JsonPolymorphic
public class Instant implements JsonWrapper<TimeSpan>
{

	private TimeSpan value;

	@Override
	public TimeSpan getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(final TimeSpan value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return getValue().toString();
	}

	@Override
	public int hashCode()
	{
		return getValue().hashCode();
	}

	/** @see Converters.CLASS_WITH_VALUE_OF_METHOD */
	@JsonCreator
	public static Instant valueOf(final String json)
	{
		return JsonUtil.valueOf(json, Instant.class);
	}

	/** */
	public static Instant valueOf(final long millis)
	{
		return valueOf(new TimeSpan(millis));
	}

	/** */
	public static Instant valueOf(final ReadableInstant joda)
	{
		return valueOf(joda.getMillis());
	}

	/** */
	public static Instant valueOf(final org.threeten.bp.Instant temporal)
	{
		return valueOf(TimeSpan.valueOf(DecimalMeasure.valueOf(
				BigDecimal.valueOf(temporal.get(ChronoField.NANO_OF_SECOND))
						.add(BigDecimal.valueOf(
								temporal.get(ChronoField.INSTANT_SECONDS))
								.multiply(BigDecimal.TEN.pow(9))), SI
						.NANO(SI.SECOND))));
	}

	/** */
	public static Instant valueOf(final TimeSpan value)
	{
		return new Instant()
		{
			{
				setValue(value);
			}
		};
	}

	@JsonIgnore
	public long millis()
	{
		return getValue().longValue(SI.MILLI(SI.SECOND));
	}

	@JsonIgnore
	public long nanos()
	{
		return getValue().longValue(SI.NANO(SI.SECOND));
	}

	@JsonIgnore
	public Date toDate()
	{
		return new Date(millis());
	}

	/** @return the Joda {@link ReadableInstant} implementation of an instant */
	@JsonIgnore
	public ReadableInstant toJoda()
	{
		return new org.joda.time.Instant(millis());
	}

	/**
	 * @return the JSR-310 {@link org.threeten.bp.Instant} implementation of an
	 *         instant
	 */
	@JsonIgnore
	public org.threeten.bp.Instant toJava8()
	{
		return org.threeten.bp.Instant.ofEpochMilli(millis());
	}

	/** @return the JSR-275 {@link Measurable} implementation of an instant */
	@JsonIgnore
	public Measurable<Duration> toMeasure()
	{
		return getValue();
	}
}