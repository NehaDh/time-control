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
package com.almende.timecontrol.entity;

import io.coala.refer.Identifier;

import java.math.BigDecimal;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;
import org.joda.time.Duration;
import org.joda.time.Interval;

import com.almende.timecontrol.entity.Trigger.ExpressionFormat;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * {@link Clock} configuration
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface Clock extends Mutable, Reloadable // optional extensions
{

	/** @return the {@link ID} of this {@link Clock} */
	ID id();

	/**
	 * @return the {@link ID} of the original forking (parent) {@link Clock}, or
	 *         {@code null} if this is the {@link Replication}'s root
	 *         {@link Clock}
	 */
	// @DefaultValue("")
	@Key("forkParent")
	ID forkParent();

	/**
	 * @return the simulated time when this clock was forked as relative
	 *         duration since {@link Interval#getStart()
	 *         Replication#.interval.getStart()}, so at the start of the
	 *         replication {@code time.equals(Duration.ZERO)==true} and at the
	 *         end of the replication
	 *         {@code time.equals(interval.toDuration())==true}
	 */
	// TODO use javax.time.Duration, to handle nanoseconds?
	Duration forkOffset();

	/** @return the {@link Status} of this {@link ClockData} */
	Status status();

	/**
	 * @return the (last occurred) {@link Error} of this {@link Clock} if
	 *         {@link #status} {@code ==} {@link ClockStatus#FAILED}, or
	 *         {@code null} if none
	 */
	Error error();

	/**
	 * @return the rate at which to proceed w.r.t. wall-clock time. For example:
	 *         <p>
	 *         <dl>
	 *         <dt>{@code null} or {@code <=0}</dt>
	 *         <dd>"as-fast-as-the-slowest-node-allows"</dd>
	 *         <dt>&isin; (0.0, 1.0)</dt>
	 *         <dd>"faster-than-real-time"</dd>
	 *         <dt>=1.0</dt>
	 *         <dd>"real-time" speed</dd>
	 *         <dt>
	 *         &isin; (1.0, inf)</dt>
	 *         <dd>"slower-than-real-time")</dd>
	 *         </dl>
	 */
	WallClockRate wallClockRate();

	/**
	 * @return the current simulated time as relative duration since
	 *         {@link Interval#getStart() interval#getStart()}, so at the start
	 *         of the simulation {@code time.equals(Duration.ZERO)==true} and at
	 *         the end of the simulation
	 *         {@code time.equals(interval.toDuration())==true}
	 */
	// TODO use javax.time.Duration, to handle nanoseconds?
	Duration time();

	/**
	 * @return the next simulated time when this {@link Clock} will pause, as
	 *         relative duration since {@link Interval#getStart()
	 *         Replication.interval#getStart()}, unless preceded by
	 *         {@link Interval#toDuration() Replication.interval#toDuration()},
	 *         default = {@link Interval#toDuration()
	 *         Replication.interval#toDuration()}, default = {@code null}
	 */
	// TODO use javax.time.Duration, to handle nanoseconds?
	Duration until();

	/**
	 * @return the minimum wall-clock duration to wait (>0) before timing out
	 *         and unregistering a non-responsive {@link Slave}, default: <= 0
	 *         (never)
	 */
	// TODO use javax.time.Duration, to handle nanoseconds?
	Duration slaveTimeout();

	/**
	 * {@link Status}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	enum Status
	{
		/** */
		RUNNING("running"),

		/** */
		WAITING("waiting"),

		/** */
		COMPLETED("completed"),

		/** */
		FAILED("failed"),

		/** for JSON compatibility */
		_UNDEFINED("undefined"),

		;

		/** */
		private final String jsonValue;

		/**
		 * {@link ExpressionFormat} enum constant constructor
		 * 
		 * @param jsonValue
		 */
		private Status(final String jsonValue)
		{
			this.jsonValue = jsonValue;
		}

		@JsonValue
		private final String value()
		{
			return this.jsonValue;
		}
	}

	/**
	 * {@link WallClockRate} extends {@link DecimalMeasure} with
	 * {@link #valueOf(String)} for
	 * {@link org.aeonbits.owner.Converters#CLASS_WITH_VALUE_OF_METHOD}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class WallClockRate extends DecimalMeasure<Dimensionless>
	{

		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * {@link WallClockRate} constructor
		 * 
		 * @param value
		 * @param unit
		 */
		private WallClockRate(final BigDecimal value)
		{
			super(value, Unit.ONE);
		}

		/** @see org.aeonbits.owner.Converters.CLASS_WITH_VALUE_OF_METHOD */
		public static WallClockRate valueOf(final String value)
		{
			return new WallClockRate(BigDecimal.valueOf(Double.valueOf(value)));
		}
	}

	/**
	 * {@link ID} provides {@link #valueOf(String)} for
	 * {@link org.aeonbits.owner.Converters#CLASS_WITH_VALUE_OF_METHOD}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class ID extends Identifier<String>
	{
		/** @see org.aeonbits.owner.Converters.CLASS_WITH_VALUE_OF_METHOD */
		public static ID valueOf(final String value)
		{
			return Identifier.of(value, ID.class);
		}
	}
}