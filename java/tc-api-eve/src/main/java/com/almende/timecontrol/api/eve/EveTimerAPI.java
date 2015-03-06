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
package com.almende.timecontrol.api.eve;

import com.almende.eve.transform.rpc.annotation.Name;
import com.almende.timecontrol.api.TimerAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.SlaveConfig;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.Trigger;

/**
 * {@link EveTimerAPI} adds {@link Name} annotations to {@link TimerAPI} where
 * required
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface EveTimerAPI extends TimerAPI
{

	// @Override
	// TimerStatus getStatus(); // Timer.ID timerId

	@Override
	void initialize(@Name("config") TimerConfig config);

	// @Override
	// void destroy();

	@Override
	void updateSlaveConfig(@Name("slave") SlaveConfig slave);

	@Override
	void removeSlave(@Name("slaveId") SlaveConfig.ID slaveId);

	@Override
	void updateClockConfig(@Name("clock") ClockConfig clock);

	@Override
	void removeClock(@Name("clockId") ClockConfig.ID clockId);

	@Override
	void updateTrigger(@Name("trigger") Trigger trigger);

	@Override
	void removeTrigger(@Name("triggerId") Trigger.ID triggerId);
}
