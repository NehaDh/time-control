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

import java.util.Map;

import com.almende.timecontrol.entity.Clock;
import com.almende.timecontrol.entity.Job;
import com.almende.timecontrol.entity.Scenario;
import com.almende.timecontrol.entity.Slave;
import com.almende.timecontrol.entity.Trigger;

/**
 * {@link TimeControlMasterAPI}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface TimeControlMasterAPI
{

	/**
	 * @return the current state of all {@link Clock}s
	 */
	Map<Scenario.ID, Scenario.ScenarioInfo> getStatus();

	/**
	 * registers/updates a {@link Scenario} which a {@link Slave} may then join.
	 * Starts calling {@link TimeControlSlaveAPI#notify(Clock)} at specified
	 * {@link Scenario#address} <i>iff</i> it is non-null
	 * 
	 * @param replication the {@link Scenario} to (re)register
	 */
	void put(Scenario replication);

	/**
	 * unregisters a {@link Scenario} including all of its {@link Clock} s,
	 * {@link Slave}s, {@link Trigger}s, and {@link Job}s
	 * 
	 * @param name a {@link Scenario.ID reference} to a particular
	 *            {@link Scenario}
	 */
	void remove(Scenario.ID name);

	/**
	 * registers/updates a {@link Slave}, e.g. (time-managed) actors for
	 * business logic, sniffing, logging, statistics, etc.
	 * 
	 * @param scenarioID the {@link Scenario.ID} of the {@link Scenario} to
	 *            (re)register the federate in
	 * @param federate the {@link Slave} to (re)register
	 */
	void put(Scenario.ID scenarioID, Slave federate);

	/**
	 * unregisters a {@link Slave} including all of its {@link Trigger}s and
	 * {@link Job}s
	 * 
	 * @param name a {@link Slave.ID reference} to a particular {@link Slave}
	 */
	void remove(Slave.ID name);

	/**
	 * registers/updates a {@link Clock}, possibly forking a split from its
	 * parent {@link Clock}'s time line
	 * 
	 * @param trigger the new {@link Clock} to register
	 */
	void put(Clock clock);

	/**
	 * unregisters a {@link Clock} including all of its {@link Trigger}s and
	 * {@link Job}s
	 * 
	 * @param name a {@link Clock.ID reference} to a particular {@link Clock}
	 */
	void remove(Clock.ID name);

	/**
	 * registers a new {@link Trigger}, e.g. (time-managed) actors for business
	 * logic, sniffing, logging, statistics, etc.
	 * 
	 * @param trigger the new {@link Trigger} to register
	 */
	void put(Trigger trigger);

	/**
	 * unregisters a {@link Trigger} including all of its {@link Job}s
	 * 
	 * @param name a {@link Trigger.ID reference} to a particular
	 *            {@link Trigger}
	 */
	void remove(Trigger.ID name);
}
