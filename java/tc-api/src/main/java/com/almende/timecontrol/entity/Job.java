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

import org.aeonbits.owner.Config;
import org.joda.time.Instant;

import com.almende.timecontrol.TimeControl;
import com.almende.timecontrol.time.Duration;
import com.fasterxml.jackson.core.TreeNode;

/**
 * {@link Job}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@BeanWrapper(comparableOn = TimeControl.ID_KEY)
public interface Job extends Comparable<Job>, Config
{

	/** @return the {@link ID} of this {@link Job} */
	@Key(TimeControl.ID_KEY)
	ID id();

	/** the simulated time {@link Instant} when this {@link Job} occurs */
	@Key(TimeControl.TIME_KEY)
	Duration time();

	/**
	 * the {@link Trigger.ID} of the {@link Trigger} that generated this
	 * {@link Job}
	 */
	@Key(TimeControl.TRIGGER_ID_KEY)
	Trigger.ID triggerId();

	/**
	 * @return {@code true} iff this is the last {@link Job} created by its
	 *         {@link Trigger}, {@code false} otherwise
	 */
	@Key(TimeControl.LAST_CALL_KEY)
	boolean lastCall();

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
	}

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class Builder extends DynaBean.Builder<Job, Builder>
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
			return new Builder(imports).id(tree.get(TimeControl.ID_KEY));
		}

		/**
		 * @param id the JSON-formatted identifier value
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromID(final String id,
				final Properties... imports)
		{
			return new Builder(imports).id(ID.valueOf(id));
		}

		/**
		 * {@link Builder} constructor, to be extended by a public zero-arg
		 * constructor in concrete sub-types
		 */
		public Builder(final Properties... imports)
		{
			super(imports);
		}

		public Builder id(final TreeNode id)
		{
			return id(JsonUtil.valueOf(id, ID.class));
		}

		public Builder id(final ID id)
		{
			with(TimeControl.ID_KEY, id);
			return this;
		}

		public Builder time(final Duration time)
		{
			with(TimeControl.TIME_KEY, time);
			return this;
		}

		public Builder triggerID(final Trigger.ID triggerId)
		{
			with(TimeControl.TRIGGER_ID_KEY, triggerId);
			return this;
		}

		public Builder lastCall(final boolean lastCall)
		{
			with(TimeControl.LAST_CALL_KEY, lastCall);
			return this;
		}

	}

}