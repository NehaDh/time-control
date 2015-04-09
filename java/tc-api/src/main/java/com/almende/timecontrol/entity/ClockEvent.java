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
import io.coala.json.dynabean.DynaBean;
import io.coala.json.dynabean.DynaBean.BeanWrapper;
import io.coala.util.JsonUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import org.joda.time.Interval;

import rx.Observer;

import com.almende.timecontrol.TimeControl;
import com.almende.timecontrol.entity.ClockConfig.Status;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Rate;
import com.eaio.uuid.UUID;
import com.fasterxml.jackson.core.TreeNode;

/**
 * {@link ClockEvent}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 * 
 * @see java.beans.PropertyChangeEvent
 */
@BeanWrapper(comparableOn = TimeControl.ID_KEY)
public interface ClockEvent extends Comparable<ClockEvent>
{

	/** @return the {@link ID} of this {@link ClockEvent} */
	ID id();

	/** @return the {@link ClockConfig.ID} of the source {@link ClockConfig} */
	ClockConfig.ID clockId();

	/** @return the {@link Status} of the source {@link ClockConfig} */
	Status status();

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
	Rate drag();

	/**
	 * @return the current simulated time as relative duration since
	 *         {@link Interval#getStart() interval#getStart()}, so at the start
	 *         of the simulation {@code time.equals(Duration.ZERO)==true} and at
	 *         the end of the simulation
	 *         {@code time.equals(interval.toDuration())==true}
	 */
	Duration time();

	/**
	 * @return the next simulated time when this {@link ClockConfig} will pause,
	 *         as relative duration since {@link Interval#getStart()
	 *         Replication.interval#getStart()}, unless preceded by
	 *         {@link Interval#toDuration() Replication.interval#toDuration()},
	 *         default = {@link Interval#toDuration()
	 *         Replication.interval#toDuration()}, default = {@code null}
	 */
	Duration until();

	/**
	 * {@link ID}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class ID extends Identifier<UUID>
	{
		public ID()
		{
			this(new UUID());
		}

		public ID(final UUID value)
		{
			setValue(value);
		}

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
	class Builder extends DynaBean.Builder<ClockEvent, Builder>
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
			return fromJSON(JsonUtil.valueOf(json));
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
					.withClockId(tree.get(TimeControl.CLOCK_ID_KEY))
					.withStatus(tree.get(TimeControl.STATUS_KEY))
					.withDrag(tree.get(TimeControl.DRAG_KEY))
					.withTime(tree.get(TimeControl.TIME_KEY))
					.withUntil(tree.get(TimeControl.UNTIL_KEY));
		}

		/**
		 * @param oldConfig
		 * @param newConfig
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromClockId(final ClockConfig.ID clockId,
				final Properties... imports)
		{
			return new Builder(imports).withId(new ID()).withClockId(clockId);
		}

		/**
		 * {@link Builder} constructor, to be extended by a public zero-arg
		 * constructor in concrete sub-types
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

		public Builder withClockId(final TreeNode id)
		{
			return withClockId(JsonUtil.valueOf(id, ClockConfig.ID.class));
		}

		public Builder withClockId(final ClockConfig.ID id)
		{
			with(TimeControl.CLOCK_ID_KEY, id);
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

	/**
	 * @param events
	 * @return
	 */
	class PropertyChangeListenerFilter
	{
		public static PropertyChangeListener forObserver(
				final ClockConfig.ID clockID, final Observer<ClockEvent> events)
		{
			return new PropertyChangeListener()
			{
				@Override
				public void propertyChange(final PropertyChangeEvent evt)
				{
					final Builder builder = ClockEvent.Builder
							.fromClockId(clockID);
					if (evt.getPropertyName().equals(TimeControl.STATUS_KEY))
						events.onNext(builder.withStatus(
								(ClockConfig.Status) evt.getNewValue()).build());
					else if (evt.getPropertyName().equals(TimeControl.TIME_KEY))
						events.onNext(builder.withTime(
								(Duration) evt.getNewValue()).build());
					else if (evt.getPropertyName()
							.equals(TimeControl.UNTIL_KEY))
						events.onNext(builder.withUntil(
								(Duration) evt.getNewValue()).build());
					else if (evt.getPropertyName().equals(TimeControl.DRAG_KEY))
						events.onNext(builder
								.withDrag((Rate) evt.getNewValue()).build());
				}
			};
		}
	}
}