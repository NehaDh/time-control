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

import io.coala.json.DynaBean;
import io.coala.json.DynaBean.BeanWrapper;
import io.coala.util.JsonUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import com.almende.timecontrol.TimeControl;
import com.fasterxml.jackson.core.TreeNode;

/**
 * {@link TimerStatus}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@BeanWrapper(comparableOn = TimeControl.CONFIG_KEY)
public interface TimerStatus extends Comparable<TimerStatus>
{

	/**
	 * @return
	 */
	TimerConfig config();

	/**
	 * @return the {@link ClockConfig}s currently managed by the
	 *         {@link #timer()}
	 */
	List<ClockStatus> clocks();

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class Builder extends DynaBean.Builder<TimerStatus, Builder>
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
			final TreeNode tree = JsonUtil.toTree(json);
			return tree == null ? new Builder(imports)
					: fromJSON(tree, imports);
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
			return new Builder(imports).withConfig(
					tree.get(TimeControl.CONFIG_KEY)).withClocks(
					tree.get(TimeControl.CLOCKS_KEY));
		}

		/**
		 * @param config the {@link TimerConfig}
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromConfig(final TimerConfig config,
				final Properties... imports)
		{
			return new Builder(imports).withConfig(config);
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

		public Builder withConfig(final TreeNode timer)
		{
			if (timer == null)
				return this;
			return withConfig(JsonUtil.valueOf(timer, TimerConfig.class));
		}

		public Builder withConfig(final TimerConfig timer)
		{
			with(TimeControl.CONFIG_KEY, timer);
			return this;
		}

		public Builder withClocks(final TreeNode clocks)
		{
			if (clocks == null)
				return this;
			if (clocks.isArray())
			{
				for (int i = 0; i < clocks.size(); i++)
					withClocks(clocks.get(i));
				return this;
			}
			return withClocks(JsonUtil.valueOf(clocks, ClockStatus.class));
		}

		public Builder withClocks(final ClockStatus... clocks)
		{
			if (clocks == null || clocks.length == 0)
				return this;

			return withClocks(Arrays.asList(clocks));
		}

		@SuppressWarnings("unchecked")
		public Builder withClocks(final Collection<ClockStatus> clocks)
		{
			Object value = get(TimeControl.CLOCKS_KEY, Object.class);
			if (value == null)
			{
				// FIXME use more efficient collection, e.g. arraylist, hashset?
				value = new TreeSet<ClockStatus>();
				with(TimeControl.CLOCKS_KEY, value);
			}

			if (clocks != null)
				((Collection<ClockStatus>) value).addAll(clocks);
			return this;
		}

	}

}