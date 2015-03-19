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

import io.coala.error.ExceptionBuilder;
import io.coala.util.JsonUtil;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableDateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.ical.compat.jodatime.DateTimeIteratorFactory;

/**
 * {@link RecurrenceRule} TODO test!!
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@JsonSerialize(using = RecurrenceRule.JsonSerializer.class)
@JsonDeserialize(using = RecurrenceRule.JsonDeserializer.class)
public class RecurrenceRule // implements JsonWrapper<String>//
							// Iterable<Instant>
{
	/** */
	private static final Pattern dtStartTimePattern = Pattern
			.compile("DTSTART[.]*:(.*)");

	/** */
	private static final Pattern dtStartZonePattern = Pattern
			.compile("TZID=([^:;]*)");

	/** */
	private Object value;

	/** */
	@JsonIgnore
	private final Observable<Instant> values;

	/**
	 * @param measure
	 * @return
	 * 
	 * @see CronScheduleBuilder#cronSchedule(String)
	 * @see DateTimeIteratorFactory#createDateTimeIterable(String,
	 *      ReadableDateTime, DateTimeZone, boolean)
	 */
	public static final Observable<Instant> parseInstantOrIntervalOrRule(
			final String json)
	{
		try
		{
			// example: "0/20 * * * * ?"
			final CronTrigger trigger = TriggerBuilder.newTrigger()
					.withSchedule(CronScheduleBuilder.cronSchedule(json))
					.build();
			return Observable.create(new Observable.OnSubscribe<Instant>()
			{
				@Override
				public void call(final Subscriber<? super Instant> sub)
				{
					Date current = trigger.getStartTime();
					while (current != null)
					{
						sub.onNext(Instant.valueOf(current.getTime()));
						current = trigger.getFireTimeAfter(current);
					}
				}
			});
		} catch (final Exception e)
		{
			try
			{
				final boolean strict = false;
				// String ical =
				// "DTSTART;TZID=US-Eastern:19970902T090000\r\n"+"RRULE:FREQ=DAILY;"
				// + "UNTIL=20130430T083000Z;"
				// + "INTERVAL=1;";

				// FIXME parse DTSTART (see
				// http://www.kanzaki.com/docs/ical/rrule.html)
				final DateTimeZone zone = DateTimeZone.forID(dtStartZonePattern
						.matcher(json).group());
				final DateTime start = DateTime.parse(
						dtStartTimePattern.matcher(json).group())
						.withZone(zone);

				// convert DateTime to Instant
				return Observable.from(
						DateTimeIteratorFactory.createDateTimeIterable(json,
								start, zone, strict)).map(
						new Func1<DateTime, Instant>()
						{
							@Override
							public Instant call(final DateTime dt)
							{
								return Instant.valueOf(dt);
							}
						});
			} catch (final Exception e1)
			{
				return Observable.just(Instant.valueOf(json));
			}
		}
	}

	/**
	 * {@link RecurrenceRule} constructor for "natural" polymorphic Jackson bean
	 * deserialization
	 * 
	 * @see com.fasterxml.jackson.databind.deser.BeanDeserializer
	 */
	public RecurrenceRule(final String json)
	{
		this(json, parseInstantOrIntervalOrRule(json));
	}

	/**
	 * {@link RecurrenceRule} constructor for "natural" polymorphic Jackson bean
	 * deserialization
	 * 
	 * @see com.fasterxml.jackson.databind.deser.BeanDeserializer
	 */
	public RecurrenceRule(final double absoluteInstantMS)
	{
		// TODO convert to default time unit (other than MILLIS)?
		this(absoluteInstantMS, Observable.just(Instant
				.valueOf((long) absoluteInstantMS)));
	}

	/**
	 * {@link RecurrenceRule} constructor for "natural" polymorphic Jackson bean
	 * deserialization
	 * 
	 * @see com.fasterxml.jackson.databind.deser.BeanDeserializer
	 */
	public RecurrenceRule(final int absoluteInstantMS)
	{
		this(absoluteInstantMS, Observable.just(Instant
				.valueOf(absoluteInstantMS)));
	}

	/**
	 * {@link RecurrenceRule} constructor
	 * 
	 * @param values
	 * @param type
	 */
	public RecurrenceRule(final Object value, final Observable<Instant> values)
	{
		this.value = value;
		this.values = values;
	}

	public Object getValue()
	{
		return this.value;
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
	
	/**
	 * @return
	 */
	@JsonIgnore
	public Observable<Instant> asObservable()
	{
		return this.values;
	}

	public static Iterable<Instant> createIterableInstant(
			final CronTrigger trigger)
	{
		return new Iterable<Instant>()
		{
			@Override
			public Iterator<Instant> iterator()
			{
				return new Iterator<Instant>()
				{
					/** */
					private Date current = trigger.getStartTime();

					@Override
					public boolean hasNext()
					{
						return this.current != null;
					}

					@Override
					public Instant next()
					{
						final Date result = this.current;
						this.current = trigger.getFireTimeAfter(this.current);
						return Instant.valueOf(result.getTime());
					}

					@Override
					public void remove()
					{
						throw ExceptionBuilder.unchecked("NOT SUPPORTED")
								.build();
					}
				};
			}
		};
	}

	/**
	 * @param jsonRecurrence
	 * @return
	 */
	public static RecurrenceRule valueOf(final String json)
	{
		return JsonUtil.valueOf(json, RecurrenceRule.class);
	}

	/** */
	private static final Logger LOG = LogManager
			.getLogger(RecurrenceRule.class);

	public static class JsonSerializer extends
			com.fasterxml.jackson.databind.JsonSerializer<RecurrenceRule>
	{
		public void serialize(final RecurrenceRule value,
				final JsonGenerator gen, final SerializerProvider serializers)
				throws IOException, JsonProcessingException
		{
			LOG.trace("Serializing " + value);
			gen.writeString(value.toString());
		}
	}

	public static class JsonDeserializer extends
			com.fasterxml.jackson.databind.JsonDeserializer<RecurrenceRule>
	{
		@Override
		public RecurrenceRule deserialize(final JsonParser p,
				final DeserializationContext ctxt) throws IOException,
				JsonProcessingException
		{
			LOG.trace("Deserializing " + p.getText());
			return p.getCurrentToken().isNumeric() ? new RecurrenceRule(p
					.getNumberValue().doubleValue()) : new RecurrenceRule(
					p.getText());
		}
	}

}