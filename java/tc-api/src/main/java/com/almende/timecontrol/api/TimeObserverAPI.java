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

import rx.Observable;

import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockEvent;
import com.almende.timecontrol.entity.TriggerConfig;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.time.TriggerPattern;

/**
 * {@link TimeObserverAPI}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface TimeObserverAPI
{

	/**
	 * @return an {@link Observable} of the root {@link ClockConfig} updates
	 */
	Observable<ClockEvent> observeClock();

	/**
	 * @param id the observable {@link ClockConfig}'s {@link ClockConfig.ID}
	 * @return an {@link Observable} of the respective {@link ClockConfig}
	 *         updates
	 */
	Observable<ClockEvent> observeClock(ClockConfig.ID id);

	/**
	 * registers a new {@link TriggerPattern} for time-managed clients performing
	 * business logic, sniffing, logging, statistics, etc.
	 * 
	 * @param pattern the {@link TriggerPattern} for the new Trigger
	 * @return an {@link Observable} of the respective {@link TriggerConfig}'s
	 *         {@link TriggerEvent} updates
	 */
	Observable<TriggerEvent> registerTrigger(TriggerPattern pattern);

	/**
	 * registers a new {@link TriggerPattern} for time-managed clients performing
	 * business logic, sniffing, logging, statistics, etc.
	 * 
	 * @param clockId the {@link ClockConfig.ID} of the triggering clock
	 * @param pattern the {@link TriggerPattern} for the new Trigger
	 * @return an {@link Observable} of the respective {@link TriggerConfig}'s
	 *         {@link TriggerEvent} updates
	 */
	Observable<TriggerEvent> registerTrigger(ClockConfig.ID clockId,
			TriggerPattern pattern);

	/**
	 * unregisters a {@link TriggerConfig} including all of its
	 * {@link TriggerEvent}s
	 * 
	 * @param name a {@link TriggerConfig.ID reference} to a particular
	 *            {@link TriggerConfig}
	 */
	// void unregisterTrigger(TriggerConfig.ID triggerId);
}
