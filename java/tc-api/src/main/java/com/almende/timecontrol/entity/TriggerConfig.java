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

import com.almende.timecontrol.TimeControl;
import com.almende.timecontrol.time.TriggerPattern;
import com.fasterxml.jackson.core.TreeNode;

/**
 * {@link TriggerConfig}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@BeanWrapper(comparableOn = TimeControl.ID_KEY)
public interface TriggerConfig extends Comparable<TriggerConfig> // , Accessible
{

	/** @return the {@link ID} of this {@link TriggerConfig} */
	// @Key(TimeControl.ID_KEY)
	ID id();

	/**
	 * @return
	 */
	// @Key(TimeControl.RECURRENCE_KEY)
	TriggerPattern pattern();

	/** @return the ordinal value by which to replicably order triggers from varying sources */
	//String sourceOrdinal();
	
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
		public static ID valueOf(final String json)
		{
			return Identifier.valueOf(json, ID.class);
		}
	}

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class Builder extends DynaBean.Builder<TriggerConfig, Builder>
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
			return fromJSON(JsonUtil.valueOf(json), imports);
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
			return new Builder(imports).withID(tree.get(TimeControl.ID_KEY))
					.withPattern(tree.get(TimeControl.RECURRENCE_KEY));
		}

		/**
		 * @param id the JSON-formatted identifier value
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromID(final String id,
				final Properties... imports)
		{
			return new Builder(imports).withID(ID.valueOf(id));
		}

		/**
		 * {@link Builder} constructor
		 * 
		 * @param imports optional property defaults
		 */
		public Builder(final Properties... imports)
		{
			super(imports);
		}

		public Builder withID(final TreeNode id)
		{
			return withID(JsonUtil.valueOf(id, ID.class));
		}

		public Builder withID(final ID id)
		{
			with(TimeControl.ID_KEY, id);
			return this;
		}

		public Builder withPattern(final TreeNode pattern)
		{
			return withPattern(JsonUtil.valueOf(pattern,
					TriggerPattern.class));
		}

		public Builder withPattern(final TriggerPattern pattern)
		{
			with(TimeControl.RECURRENCE_KEY, pattern);
			return this;
		}

	}
}