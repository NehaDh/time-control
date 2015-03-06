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
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.SlaveConfig;
import com.almende.timecontrol.entity.Trigger;

/**
 * {@link TimerAPI}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface TimerAPI
{

	/**
	 * @return the current state of all {@link ClockConfig}s
	 */
	TimerStatus getStatus(); // Timer.ID timerId

	/**
	 * initiates/updates a {@link TimerConfig} which a {@link SlaveConfig} may
	 * then join. Starts calling {@link TimedAPI#notify(ClockConfig)} at
	 * specified {@link TimerConfig#address} <i>iff</i> it is non-null
	 * 
	 * @param config the {@link TimerConfig} to (re)register
	 */
	void initialize(TimerConfig config);

	/**
	 * unregisters a {@link TimerConfig} including all of its
	 * {@link ClockConfig} s, {@link SlaveConfig}s, {@link Trigger}s, and
	 * {@link Job}s
	 * 
	 * @param name a {@link TimerConfig.ID reference} to a particular
	 *            {@link TimerConfig}
	 */
	void destroy();

	/**
	 * registers/updates a {@link SlaveConfig}, e.g. (time-managed) actors for
	 * business logic, sniffing, logging, statistics, etc.
	 * 
	 * @param slave the {@link SlaveConfig} to (re)register
	 */
	void updateSlaveConfig(SlaveConfig slave);

	/**
	 * unregisters a {@link SlaveConfig} including all of its {@link Trigger}s
	 * and {@link Job}s
	 * 
	 * @param name a {@link SlaveConfig.ID reference} to a particular
	 *            {@link SlaveConfig}
	 */
	void removeSlave(SlaveConfig.ID slaveId);

	/**
	 * registers/updates a {@link ClockConfig}, possibly forking a split from
	 * its parent {@link ClockConfig}'s time line
	 * 
	 * @param trigger the new {@link ClockConfig} to register
	 */
	void updateClockConfig(ClockConfig clock);

	/**
	 * unregisters a {@link ClockConfig} including all of its {@link Trigger}s
	 * and {@link Job}s
	 * 
	 * @param name a {@link ClockConfig.ID reference} to a particular
	 *            {@link ClockConfig}
	 */
	void removeClock(ClockConfig.ID clockId);

	/**
	 * registers a new {@link Trigger}, e.g. (time-managed) actors for business
	 * logic, sniffing, logging, statistics, etc.
	 * 
	 * @param trigger the new {@link Trigger} to register
	 */
	void updateTrigger(Trigger trigger);

	/**
	 * unregisters a {@link Trigger} including all of its {@link Job}s
	 * 
	 * @param name a {@link Trigger.ID reference} to a particular
	 *            {@link Trigger}
	 */
	void removeTrigger(Trigger.ID triggerId);
}
