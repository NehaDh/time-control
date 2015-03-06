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

import io.coala.json.JsonUtil;
import io.coala.json.dynabean.DynaBean;
import io.coala.json.dynabean.DynaBean.ComparableProperty;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aeonbits.owner.Config;

import com.almende.timecontrol.TimeControl;
import com.fasterxml.jackson.core.TreeNode;

/**
 * {@link TimerStatus}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@ComparableProperty(TimeControl.TIMER_KEY)
public interface TimerStatus extends Comparable<TimerStatus>, Config
{

	/**
	 * @return
	 */
	@Key(TimeControl.TIMER_KEY)
	TimerConfig timer();

	/**
	 * @return the {@link ClockConfig}s currently managed by the
	 *         {@link #timer()}
	 */
	@Key(TimeControl.CLOCKS_KEY)
	List<ClockConfig> clocks();

	/**
	 * @return the {@link SlaveStatus}s of {@link SlaveConfig}s currently
	 *         managed by the {@link #timer()}
	 */
	@Key(TimeControl.SLAVES_KEY)
	List<SlaveStatus> slaves();

	/** the callback {@link URI}s for the listeners of the {@link #timer()} */
	@Key(TimeControl.LISTENERS_KEY)
	List<URI> listeners();

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
			final TreeNode tree = JsonUtil.valueOf(json);
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
			return new Builder(imports)
					.withTimer(tree.get(TimeControl.TIMER_KEY))
					.withClocks(tree.get(TimeControl.CLOCKS_KEY))
					.withSlaves(tree.get(TimeControl.SLAVES_KEY));
		}

		/**
		 * @param timer the {@link TimerConfig}
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromTimer(final TimerConfig timer,
				final Properties... imports)
		{
			return new Builder(imports).withTimer(timer);
		}

		/** FIXME replace by a natural ordering, using Comparable<> interface */
		private static final Comparator<SlaveStatus> SLAVE_STATUS_COMPARATOR = new Comparator<SlaveStatus>()
		{
			@Override
			public int compare(final SlaveStatus o1, final SlaveStatus o2)
			{
				return o1.slave().id().compareTo(o2.slave().id());
			}
		};

		/**
		 * {@link Builder} constructor
		 * 
		 * @param imports optional property defaults
		 */
		public Builder(final Properties... imports)
		{
			super(imports);
		}

		public Builder withTimer(final TreeNode timer)
		{
			if (timer == null)
				return this;
			return withTimer(JsonUtil.valueOf(timer, TimerConfig.class));
		}

		public Builder withTimer(final TimerConfig timer)
		{
			with(TimeControl.TIMER_KEY, timer);
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
			return withClocks(JsonUtil.valueOf(clocks, ClockConfig.class));
		}

		public Builder withClocks(final ClockConfig... clocks)
		{
			if (clocks == null || clocks.length == 0)
				return this;

			return withClocks(Arrays.asList(clocks));
		}

		@SuppressWarnings("unchecked")
		public Builder withClocks(final Collection<ClockConfig> clocks)
		{
			Object value = get(TimeControl.CLOCKS_KEY, Object.class);
			if (value == null)
			{
				value = new TreeSet<ClockConfig>();
				with(TimeControl.CLOCKS_KEY, value);
			}

			if (clocks != null && clocks.size() != 0)
			{
				final SortedSet<ClockConfig> set = (SortedSet<ClockConfig>) value;
				for (ClockConfig clock : clocks)
					set.add(clock);
			}
			return this;
		}

		public Builder withSlaves(final TreeNode slaves)
		{
			if (slaves == null)
				return this;
			if (slaves.isArray())
			{
				for (int i = 0; i < slaves.size(); i++)
					withSlaves(slaves.get(i));
				return this;
			}
			return withSlaves(JsonUtil.valueOf(slaves, SlaveStatus.class));
		}

		public Builder withSlaves(final SlaveStatus... slaves)
		{
			if (slaves == null || slaves.length == 0)
				return this;

			return withSlaves(Arrays.asList(slaves));
		}

		/**
		 * @param values
		 * @return
		 */
		public Builder withSlaves(final Collection<SlaveStatus> slaves)
		{
			Object value = get(TimeControl.SLAVES_KEY, Object.class);
			if (value == null)
			{
				value = new TreeSet<SlaveStatus>(SLAVE_STATUS_COMPARATOR);
				with(TimeControl.SLAVES_KEY, value);
			}
			if (slaves != null && slaves.size() != 0)
			{
				@SuppressWarnings("unchecked")
				final SortedSet<SlaveStatus> set = (SortedSet<SlaveStatus>) value;
				for (SlaveStatus slave : slaves)
					set.add(slave);
			}
			return this;
		}

		public Builder withListeners(final TreeNode json)
		{
			if (json == null)
				return this;
			if (json.isArray())
			{
				for (int i = 0; i < json.size(); i++)
					withListeners(json.get(i));
				return this;
			}
			return withListeners(JsonUtil.valueOf(json, URI.class));
		}

		@SuppressWarnings("unchecked")
		public Builder withListeners(final URI... uris)
		{
			Object value = get(TimeControl.LISTENERS_KEY, Object.class);
			if (value == null)
			{
				value = new TreeSet<URI>();
				with(TimeControl.LISTENERS_KEY, value);
			}

			if (uris != null && uris.length != 0)
			{
				final SortedSet<URI> list = (SortedSet<URI>) value;
				for (URI uri : uris)
					list.add(uri);
			}
			return this;
		}

	}

}