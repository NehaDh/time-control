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

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.timecontrol.api.TimeManagerAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link EveTimeManagerAPI} adds Eve's {@link Name} annotations to
 * {@link TimeManagerAPI} where required
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface EveTimeManagerAPI extends TimeManagerAPI, EveTimeObserverAPI
{

	@Override
	@Access(AccessType.PUBLIC)
	TimerStatus getTimerStatus();

	@Override
	@Access(AccessType.PUBLIC)
	void destroy();

	@Override
	@Access(AccessType.PUBLIC)
	void setTimerConfig(@Name(CONFIG_PARAM) TimerConfig config);

	@Override
	@Access(AccessType.PUBLIC)
	TimerConfig getTimerConfig();

	/**
	 * FIXME rename to match {@link EveTimeManagerAPI#getClock(ClockConfig.ID)}
	 * when Eve allows overloading again
	 */
	@Access(AccessType.PUBLIC)
	JsonNode findClock(@Optional @Name(ID_PARAM) String clockId);

	@Override
	@Access(AccessType.PUBLIC)
	ClockConfig getClock(@Optional @Name(ID_PARAM) ClockConfig.ID clockId);

	@Override
	@Access(AccessType.PUBLIC)
	void updateClock(@Name(CONFIG_PARAM) ClockConfig clock);

	@Override
	@Access(AccessType.UNAVAILABLE)
	void removeClock(ClockConfig.ID clockId);

	@Access(AccessType.PUBLIC)
	void removeClock(@Name(ID_PARAM) String clockId);

}