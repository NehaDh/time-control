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
import io.coala.util.JsonUtil;

import javax.measure.Measurable;
import javax.measure.unit.SI;

import org.joda.time.ReadableDuration;
import org.threeten.bp.temporal.TemporalAmount;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@linkplain Duration} wraps an {@linkplain TimeSpan} that is
 * {@linkplain JsonPolymorphic} and provides a {@link #valueOf(String)} method
 * for loading as configured value {@link Converters#CLASS_WITH_VALUE_OF_METHOD}
 * <p>
 * We considered various temporal measure implementations, including
 * <dl>
 * <dt>Java's native utilities</dt>
 * <dd>Offers no standard tuple combining a {@link java.lang.Number} and
 * {@link java.util.concurrent.TimeUnit}</dd>
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
 * <li>{@linkplain org.threeten.bp.Duration} parses strictly 'PTx.xS' (upper
 * case) ISO8601 format only</li>
 * <li>{@linkplain org.threeten.bp.temporal.TemporalAmount} does not align with
 * earlier JSR-275 {@link javax.measure.quantity.Duration}</li></dd>
 * <dt>Joda's time API from <a href="http://www.joda.org/">joda.org</a></dt>
 * <dd>
 * <li>Allows lenient (lower and upper case) ISO8601 format strings</li>
 * <li>{@link org.joda.time.Duration} implements {@link Comparable} whereas
 * {@link org.joda.time.Period} does not.</li>
 * <li>Joda time offers this <a
 * href="https://github.com/FasterXML/jackson-datatype-joda">datatype extension
 * for Jackson</a>.</li>
 * <li>offers many nice calendar and formatter implementations</li>
 * <li>will <a href="https://github.com/JodaOrg/joda-time/issues/52">not support
 * microsecond or nanosecond precision</a></li></dd>
 * <dt>Apache {@code commons-lang3} Date Utilities</dt>
 * <dd>
 * limitations similar to Joda's time API (millisecond precision only)</dd>
 * <dt>Guava in the Google Web Toolkit from <a
 * href="https://github.com/google/guava">github.com/google/guava</a></dt>
 * <dd>
 * extends relevant Java types only with a (time-line offset) interval (
 * {@code Range}) API, not an (free floating) duration quantity</dd>
 * <dt>DESMO-J's TimeSpan API from <a
 * href="http://desmoj.sf.net/">desmoj.sf.net</a></dt>
 * <dd>limited to Java's standard TimeUnit</dd>
 * <dt>DSOL3's UnitTime API from <a
 * href="http://simulation.tudelft.nl/">simulation.tudelft.nl</a></dt>
 * <dd>no Javadoc available</dd>
 * </dl>
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class Duration implements JsonWrapper<TimeSpan>
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

	/** @return the Joda {@link ReadableDuration} implementation of a time span */
	@JsonIgnore
	public ReadableDuration toJoda()
	{
		return org.joda.time.Duration.millis(millis());
	}

	/**
	 * @return a JSR-310 {@link TemporalAmount} implementation of a time span,
	 *         either time-based ({@link org.threeten.bp.Duration}) or
	 *         date-based ({@link org.threeten.bp.Period})
	 */
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public <T extends TemporalAmount & Comparable<T>> T toJava8()
	{
		return (T) org.threeten.bp.Duration.ofNanos(nanos());
	}

	/** @return the JSR-275 {@link Measurable} implementation of a time span */
	@JsonIgnore
	public Measurable<javax.measure.quantity.Duration> toMeasure()
	{
		return getValue();
	}

	/** @see Converters.CLASS_WITH_VALUE_OF_METHOD */
	public static Duration valueOf(final String json)
	{
		return JsonUtil.valueOf(json, Duration.class);
	}

	/** */
	public static final Duration ZERO = Duration.valueOf("0");
}