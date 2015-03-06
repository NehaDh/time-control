/* $Id$
 * $URL$
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
 * Copyright (c) 2015 Almende B.V. 
 */
package com.almende.timecontrol.api;

import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.Job;
import com.almende.timecontrol.entity.SlaveConfig;
import com.almende.timecontrol.entity.Trigger;

/**
 * {@link TimedAPI}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface TimedAPI
{
	/**
	 * @return the latest {@link SlaveConfig} status information for this
	 *         {@link TimeControlSlaveAPI}, useful for reconnecting
	 */
	// SlaveConfig getConfig();

	/**
	 * @return the latest {@link SlaveStatus} status information for this
	 *         {@link TimeControlSlaveAPI}, useful for reconnecting
	 */
	//SlaveStatus getStatus();

	/**
	 * called (asynchronously) in response to
	 * {@link TimerAPI#updateSlaveConfig(SlaveConfig)} <i>iff</i>
	 * {@link SlaveConfig#address()} {@code != null}
	 * 
	 * @param clock the new {@link ClockConfig} state
	 */
	void notify(ClockConfig clock);

	/**
	 * called (asynchronously) as per the recurrence rule of some
	 * {@linkplain Trigger} subscribed using
	 * {@link TimerAPI#updateTrigger(Trigger)}
	 * 
	 * @param job
	 */
	void notify(Job job);
}