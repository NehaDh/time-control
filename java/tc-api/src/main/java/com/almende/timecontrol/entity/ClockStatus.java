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
 * {@link ClockStatus}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@BeanWrapper
// (comparableOn = TimeControl.CONFIG_KEY)
public interface ClockStatus // extends Comparable<ClockStatus> , Accessible
{

	/**
	 * @return
	 */
	ClockConfig config();

	/** the callback {@link URI}s for the listeners of the {@link #timer()} */
	// List<URI> subscribers();

	/**
	 * @return the {@link SlaveStatus}s of {@link SlaveConfig}s currently
	 *         managed by the {@link #timer()}
	 */
	List<TriggerStatus> triggers();

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class Builder extends DynaBean.Builder<ClockStatus, Builder>
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
					tree.get(TimeControl.CONFIG_KEY)).withTriggers(
					tree.get(TimeControl.TRIGGERS_KEY))
			// .withSubscribers(tree.get(TimeControl.SUBSCRIBERS_KEY))
			;
		}

		/**
		 * @param config the {@link TimerConfig}
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromConfig(final ClockConfig config,
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
			return withConfig(JsonUtil.valueOf(timer, ClockConfig.class));
		}

		public Builder withConfig(final ClockConfig timer)
		{
			with(TimeControl.CONFIG_KEY, timer);
			return this;
		}

		public Builder withTriggers(final TreeNode clocks)
		{
			if (clocks == null)
				return this;
			if (clocks.isArray())
			{
				for (int i = 0; i < clocks.size(); i++)
					withTriggers(clocks.get(i));
				return this;
			}
			return withTriggers(JsonUtil.valueOf(clocks, TriggerStatus.class));
		}

		public Builder withTriggers(final TriggerStatus... clocks)
		{
			if (clocks == null || clocks.length == 0)
				return this;

			return withTriggers(Arrays.asList(clocks));
		}

		@SuppressWarnings("unchecked")
		public Builder withTriggers(final Collection<TriggerStatus> clocks)
		{
			Object value = get(TimeControl.CLOCKS_KEY, Object.class);
			if (value == null)
			{
				// FIXME use more efficient collection, e.g. arraylist, hashset?
				value = new TreeSet<TriggerStatus>();
				with(TimeControl.CLOCKS_KEY, value);
			}

			if (clocks != null)
				((Collection<TriggerStatus>) value).addAll(clocks);
			return this;
		}

/*		public Builder withSubscribers(final TreeNode json)
		{
			if (json == null)
				return this;
			if (json.isArray())
			{
				for (int i = 0; i < json.size(); i++)
					withSubscribers(json.get(i));
				return this;
			}
			return withSubscribers(JsonUtil.valueOf(json, URI.class));
		}

		public Builder withSubscribers(final URI... uris)
		{
			if (uris == null || uris.length == 0)
				return this;

			return withSubscribers(Arrays.asList(uris));
		}

		@SuppressWarnings("unchecked")
		public Builder withSubscribers(final Collection<URI> uris)
		{
			Object value = get(TimeControl.SUBSCRIBERS_KEY, Object.class);
			if (value == null)
			{
				value = new TreeSet<URI>();
				with(TimeControl.SUBSCRIBERS_KEY, value);
			}

			if (!uris.isEmpty())
			{
				final SortedSet<URI> list = (SortedSet<URI>) value;
				for (URI uri : uris)
					list.add(uri);
			}
			return this;
		}
*/
	}

}