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

import java.util.Properties;

import org.aeonbits.owner.Accessible;

import com.almende.timecontrol.TimeControl;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Instant;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link TimerConfig}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@BeanWrapper(comparableOn = TimeControl.ID_KEY)
public interface TimerConfig extends Comparable<TimerConfig>, Accessible
{

	/** @return the {@link ID} of this {@link TimerConfig} */
	@Key(TimeControl.ID_KEY)
	ID id();

	/**
	 * the (root) {@link ClockConfig}
	 */
	@Key(TimeControl.CLOCK_KEY)
	@DefaultValue("rootClock")
	ClockConfig.ID rootClockId();

	/**
	 * the simulated time {@link Duration} between <b>discrete</b> ticks of this
	 * {@link TimerConfig}, or {@code null} for a <b>continuous</b> time scale
	 */
	@Key(TimeControl.RESOLUTION_KEY)
	Duration resolution();

	/**
	 * the requested/recorded wall-cloc0k (actual) start {@link Instant}, or
	 * {@code null} for "immediate" (request) or "non-applicable" when time is
	 * managed across federated nodes using the non-null {@code #wallClockRate},
	 * therefore recorded offset becomes invalid (i.e. shifts) in case of delays
	 */
	@Key(TimeControl.OFFSET_KEY)
	Instant offset();

	/** the simulated duration */
	@Key(TimeControl.DURATION_KEY)
	Duration duration();

	/**
	 * @return the minimum wall-clock duration to wait (>0) before timing out
	 *         and unregistering a non-responsive {@link TriggerConfig}
	 *         subscriber, default: <= 0 (never)
	 */
	// @Key(TimeControl.SLAVE_TIMEOUT_KEY)
	// Duration slaveTimeout();

	/**
	 * {@link ID}
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
			return Identifier.valueOf(value, ID.class);
		}

		/**
		 * @param tree
		 * @return
		 */
		public static ID valueOf(final JsonNode tree)
		{
			return Identifier.valueOf(tree.asText(), ID.class);
		}
	}

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class Builder extends DynaBean.Builder<TimerConfig, Builder>
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
			return new Builder(imports).withID(tree.get(TimeControl.ID_KEY));
		}

		/**
		 * @param id the JSON-formatted identifier value
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder forID(final String id,
				final Properties... imports)
		{
			return new Builder(imports).withID(ID.valueOf(id));
		}

		/**
		 * {@link Builder} constructor, to be extended by a public zero-arg
		 * constructor in concrete sub-types
		 */
		public Builder(final Properties... imports)
		{
			super(imports);
		}

		public Builder withID(final TreeNode json)
		{
			return withID(JsonUtil.valueOf(json, ID.class));
		}

		public Builder withID(final ID id)
		{
			with(TimeControl.ID_KEY, id);
			return this;
		}

		public Builder withClock(final ClockConfig clock)
		{
			with(TimeControl.CLOCK_KEY, clock);
			return this;
		}

		public Builder withResolution(final Duration resolution)
		{
			with(TimeControl.RESOLUTION_KEY, resolution);
			return this;
		}

		public Builder withDuration(final Duration duration)
		{
			with(TimeControl.DURATION_KEY, duration);
			return this;
		}

		public Builder withOffset(final Instant offset)
		{
			with(TimeControl.OFFSET_KEY, offset);
			return this;
		}
	}

}