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

import io.coala.id.Identifier;
import io.coala.json.DynaBean;
import io.coala.json.DynaBean.BeanWrapper;
import io.coala.util.JsonUtil;

import java.lang.reflect.Method;
import java.util.Properties;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Converter;
import org.aeonbits.owner.Mutable;
import org.joda.time.Interval;

import com.almende.timecontrol.TimeControl;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Instant;
import com.almende.timecontrol.time.Rate;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;

/**
 * {@link ClockConfig} configuration
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@BeanWrapper(comparableOn = TimeControl.ID_KEY)
public interface ClockConfig extends Comparable<ClockConfig>, Mutable,
		Accessible
{

	/** @return the {@link ID} of this {@link ClockConfig} */
	@Key(TimeControl.ID_KEY)
	@DefaultValue("rootClock")
	ID id();

	/**
	 * @return the {@link ID} of the original forking (parent)
	 *         {@link ClockConfig}, or {@code null} if this is the
	 *         {@link Replication}'s root {@link ClockConfig}
	 */
	@Key(TimeControl.FORK_PARENT_ID_KEY)
	ID forkParentID();

	/**
	 * @return the simulated time when this clock was forked as relative
	 *         duration since {@link Interval#getStart()
	 *         Replication#.interval.getStart()}, so at the start of the
	 *         replication {@code time.equals(Duration.ZERO)==true} and at the
	 *         end of the replication
	 *         {@code time.equals(interval.toDuration())==true}
	 */
	@Key(TimeControl.FORK_TIME_KEY)
	Duration forkTime();

	/** @return the {@link Status} of this {@link ClockData} */
	@Key(TimeControl.STATUS_KEY)
	@ConverterClass(Status.MyConverter.class)
	Status status();

	/**
	 * @return the (last occurred) {@link Error} of this {@link ClockConfig} if
	 *         {@link #status} {@code ==} {@link ClockStatus#FAILED}, or
	 *         {@code null} if none
	 */
	// @Key(TimeControl.ERROR_KEY)
	// Error error();

	/**
	 * @return the rate at which to proceed w.r.t. wall-clock time. For example:
	 *         <p>
	 *         <dl>
	 *         <dt>{@code null} or {@code <=0}</dt>
	 *         <dd>"as-fast-as-the-slowest-node-allows"</dd>
	 *         <dt>&isin; (0.0, 1.0)</dt>
	 *         <dd>"faster-than-real-time" speed</dd>
	 *         <dt>=1.0</dt>
	 *         <dd>"real-time" speed</dd>
	 *         <dt>
	 *         &isin; (1.0, inf)</dt>
	 *         <dd>"slower-than-real-time" speed</dd>
	 *         </dl>
	 */
	@Key(TimeControl.DRAG_KEY)
	// @DefaultValue("")
	Rate drag();

	/**
	 * @return the current simulated time as relative duration since
	 *         {@link Interval#getStart() interval#getStart()}, so at the start
	 *         of the simulation {@code time.equals(Duration.ZERO)==true} and at
	 *         the end of the simulation
	 *         {@code time.equals(interval.toDuration())==true}
	 */
	@Key(TimeControl.TIME_KEY)
	// @DefaultValue("")
	Duration time();

	/**
	 * @return the current simulated time as relative duration since
	 *         {@link Interval#getStart() interval#getStart()}, so at the start
	 *         of the simulation {@code time.equals(Duration.ZERO)==true} and at
	 *         the end of the simulation
	 *         {@code time.equals(interval.toDuration())==true}
	 */
	@Key(TimeControl.OFFSET_KEY)
	// @DefaultValue("")
	Instant offset();

	/**
	 * @return the next simulated time when this {@link ClockConfig} will pause,
	 *         as relative duration since {@link Interval#getStart()
	 *         Replication.interval#getStart()}, unless preceded by
	 *         {@link Interval#toDuration() Replication.interval#toDuration()},
	 *         default = {@link Interval#toDuration()
	 *         Replication.interval#toDuration()}, default = {@code null}
	 */
	@Key(TimeControl.UNTIL_KEY)
	// @DefaultValue("")
	Duration until();

	/**
	 * {@linkplain Status} of a {@linkplain ClockConfig} with JSON
	 * {@linkplain #value()} tokens. Note that {@link JsonParser} accepts
	 * {@linkplain #ordinal()} as well.
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	enum Status
	{
		/** */
		WAITING("waiting"),

		/** */
		RUNNING("running"),

		/** */
		COMPLETED("completed"),

		/** */
		FAILED("failed"),

		/** for JSON compatibility */
		// _UNDEFINED("undefined"),

		;

		/** */
		private final String jsonValue;

		/**
		 * {@link DurationType} enum constant constructor
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

		public static class MyConverter implements Converter<Status>
		{
			@Override
			public Status convert(final Method method, final String input)
			{
				for (Status value : values())
					if (value.value().equalsIgnoreCase(input))
						return value;
				throw new IllegalArgumentException(Status.class.getSimpleName()
						+ " unknown: " + input);
			}
		}
	}

	/**
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class ID extends Identifier<String>
	{
		/** @see org.aeonbits.owner.Converters.CLASS_WITH_VALUE_OF_METHOD */
		public static ID valueOf(final String value)
		{
			return Identifier.valueOf(value, ID.class);
		}
	}

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class Builder extends DynaBean.Builder<ClockConfig, Builder>
	{

		/**
		 * {@link Builder} factory method
		 * 
		 * @param json the JSON-formatted {@link String}
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromJSON(final String json,
				final Properties... imports)
		{
			return fromJSON(JsonUtil.toTree(json));
		}

		/**
		 * {@link Builder} factory method
		 * 
		 * @param tree the partially parsed JSON object
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromJSON(final TreeNode tree,
				final Properties... imports)
		{
			return new Builder(imports).withId(tree.get(TimeControl.ID_KEY))
					.withForkParentID(tree.get(TimeControl.FORK_PARENT_ID_KEY))
					.withForkTime(tree.get(TimeControl.FORK_TIME_KEY))
					.withDrag(tree.get(TimeControl.DRAG_KEY))
					.withStatus(tree.get(TimeControl.STATUS_KEY))
					.withTime(tree.get(TimeControl.TIME_KEY))
					.withUntil(tree.get(TimeControl.UNTIL_KEY));
		}

		/**
		 * @param id the JSON-formatted identifier value
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder forID(final String id,
				final Properties... imports)
		{
			return forID(ID.valueOf(id));
		}

		/**
		 * @param id the JSON-formatted identifier value
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder forID(final ID id, final Properties... imports)
		{
			return new Builder(imports).withId(id);
		}

		/**
		 * {@link Builder} constructor
		 */
		public Builder(final Properties... imports)
		{
			super(imports);
		}

		public Builder withId(final TreeNode id)
		{
			return withId(JsonUtil.valueOf(id, ID.class));
		}

		public Builder withId(final ID id)
		{
			with(TimeControl.ID_KEY, id);
			return this;
		}

		public Builder withForkParentID(final TreeNode tree)
		{
			return withForkParentID(JsonUtil
					.valueOf(tree, ClockConfig.ID.class));
		}

		public Builder withForkParentID(final ClockConfig.ID forkParentID)
		{
			with(TimeControl.FORK_PARENT_ID_KEY, forkParentID);
			return this;
		}

		public Builder withForkTime(final TreeNode tree)
		{
			return withForkTime(JsonUtil.valueOf(tree, Duration.class));
		}

		public Builder withForkTime(final Duration forkTime)
		{
			with(TimeControl.FORK_TIME_KEY, forkTime);
			return this;
		}

		public Builder withOffset(final TreeNode tree)
		{
			return withOffset(JsonUtil.valueOf(tree, Instant.class));
		}

		public Builder withOffset(final Instant offset)
		{
			with(TimeControl.OFFSET_KEY, offset);
			return this;
		}

		public Builder withStatus(final TreeNode tree)
		{
			return withStatus(JsonUtil.valueOf(tree, Status.class));
		}

		public Builder withStatus(final Status status)
		{
			with(TimeControl.STATUS_KEY, status);
			return this;
		}

		public Builder withDrag(final TreeNode tree)
		{
			return withDrag(JsonUtil.valueOf(tree, Rate.class));
		}

		public Builder withDrag(final Rate drag)
		{
			with(TimeControl.DRAG_KEY, drag);
			return this;
		}

		public Builder withTime(final TreeNode tree)
		{
			return withTime(JsonUtil.valueOf(tree, Duration.class));
		}

		public Builder withTime(final Duration time)
		{
			with(TimeControl.TIME_KEY, time);
			return this;
		}

		public Builder withUntil(final TreeNode tree)
		{
			return withUntil(JsonUtil.valueOf(tree, Duration.class));
		}

		public Builder withUntil(final Duration until)
		{
			with(TimeControl.UNTIL_KEY, until);
			return this;
		}
	}
}