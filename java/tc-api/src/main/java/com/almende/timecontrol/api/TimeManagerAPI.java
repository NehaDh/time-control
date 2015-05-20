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
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.TriggerConfig;

/**
 * {@link TimeManagerAPI}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface TimeManagerAPI extends TimeObserverAPI
{

	/**
	 * initiates/updates a {@link TimerConfig} which a {@link SlaveConfig} may
	 * then join. Starts calling {@link SlaveAPI#notify(ClockConfig)} at
	 * specified {@link TimerConfig#address} <i>iff</i> it is non-null
	 * 
	 * @param config the {@link TimerConfig} to (re)register
	 */
	void setTimerConfig(TimerConfig config);

	/**
	 * @return the current exhaustive {@link TimerStatus} for debugging
	 */
	TimerStatus getTimerStatus();

	/**
	 * @return the current {@link TimerConfig}
	 */
	TimerConfig getTimerConfig();

	/**
	 * unregisters a {@link TimerConfig} including all of its
	 * {@link ClockConfig} s, {@link SlaveConfig}s, {@link TriggerConfig}s, and
	 * {@link TriggerEvent}s
	 * 
	 * @param name a {@link TimerConfig.ID reference} to a particular
	 *            {@link TimerConfig}
	 */
	void destroy();

	/**
	 * @param clockId the {@link ClockConfig.ID}
	 * @return the current {@link ClockConfig} for specified {@code clockId}
	 */
	ClockConfig getClock(ClockConfig.ID clockId);

	/**
	 * registers/updates a {@link ClockConfig}, possibly forking a split from
	 * its parent {@link ClockConfig}'s time line
	 * 
	 * @param trigger the new {@link ClockConfig} to register
	 */
	void updateClock(ClockConfig clock);

	/**
	 * unregisters a {@link ClockConfig} including all of its
	 * {@link TriggerConfig}s and {@link TriggerEvent}s
	 * 
	 * @param name a {@link ClockConfig.ID reference} to a particular
	 *            {@link ClockConfig}
	 */
	void removeClock(ClockConfig.ID clockId);

}
