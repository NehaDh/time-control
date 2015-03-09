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
import io.coala.json.dynabean.DynaBean.BeanWrapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aeonbits.owner.Config;

import com.almende.timecontrol.TimeControl;
import com.fasterxml.jackson.core.TreeNode;

/**
 * {@link SlaveStatus}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@BeanWrapper(comparableOn = TimeControl.SLAVE_KEY)
public interface SlaveStatus extends Comparable<SlaveStatus>, Config
{

	/**
	 * @return the currently known slave configuration
	 */
	@Key(TimeControl.SLAVE_KEY)
	SlaveConfig slave();

	/**
	 * @return the currently pending triggers
	 */
	@Key(TimeControl.TRIGGERS_KEY)
	List<Trigger> triggers();

	/**
	 * @return the currently scheduled jobs per virtual time instant
	 */
	@Key(TimeControl.UPCOMING_JOBS_KEY)
	List<Job> upcomingJobs();

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class Builder extends DynaBean.Builder<SlaveStatus, Builder>
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
					.withSlave(tree.get(TimeControl.SLAVE_KEY))
					.withTriggers(tree.get(TimeControl.TRIGGERS_KEY))
					.withUpcomingJobs(tree.get(TimeControl.UPCOMING_JOBS_KEY));
		}

		/**
		 * {@link Builder} factory method
		 * 
		 * @param json the JSON-formatted {@link String}
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder forSlave(final SlaveConfig slave,
				final Properties... imports)
		{
			final Builder result = new Builder(imports);
			return slave == null ? result : result.withSlave(slave);
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

		public Builder withSlave(final TreeNode slave)
		{
			if (slave == null)
				return this;
			return withSlave(JsonUtil.valueOf(slave, SlaveConfig.class));
		}

		public Builder withSlave(final SlaveConfig slave)
		{
			with(TimeControl.SLAVE_KEY, slave);
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
			return withTriggers(JsonUtil.valueOf(clocks, Trigger.class));
		}

		public Builder withTriggers(final Trigger... clocks)
		{
			if (clocks == null || clocks.length == 0)
				return this;

			return withTriggers(Arrays.asList(clocks));
		}

		@SuppressWarnings("unchecked")
		public Builder withTriggers(final Collection<Trigger> clocks)
		{
			Object value = get(TimeControl.TRIGGERS_KEY, Object.class);
			if (value == null)
			{
				value = new TreeSet<Trigger>();
				with(TimeControl.TRIGGERS_KEY, value);
			}

			if (clocks != null && clocks.size() != 0)
			{
				final SortedSet<Trigger> map = (SortedSet<Trigger>) value;
				for (Trigger clock : clocks)
					map.add(clock);
			}
			return this;
		}

		public Builder withUpcomingJobs(final TreeNode jobs)
		{
			if (jobs == null)
				return this;
			if (jobs.isArray())
			{
				for (int i = 0; i < jobs.size(); i++)
					withUpcomingJobs(jobs.get(i));
				return this;
			}
			return withUpcomingJobs(JsonUtil.valueOf(jobs, Job.class));
		}

		public Builder withUpcomingJobs(final Job... jobs)
		{
			if (jobs == null || jobs.length == 0)
				return this;

			return withUpcomingJobs(Arrays.asList(jobs));
		}

		/**
		 * @param values
		 * @return
		 */
		public Builder withUpcomingJobs(final Collection<Job> jobs)
		{
			Object value = get(TimeControl.UPCOMING_JOBS_KEY, Object.class);
			if (value == null)
			{
				value = new TreeSet<Job>();
				with(TimeControl.UPCOMING_JOBS_KEY, value);
			}
			if (jobs != null && jobs.size() != 0)
			{
				@SuppressWarnings("unchecked")
				final SortedSet<Job> map = (SortedSet<Job>) value;
				for (Job job : jobs)
					map.add(job);
			}
			return this;
		}
	}
}