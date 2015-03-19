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

import java.net.URI;

import rx.Observable;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.timecontrol.api.TimeManagerAPI;
import com.almende.timecontrol.api.TimeObserverAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.entity.TriggerConfig;

/**
 * {@link EveTimeObserverAPI} adds Eve's {@link Name} annotations to
 * {@link TimeManagerAPI} where required
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface EveTimeObserverAPI extends TimeObserverAPI, EveAgentAPI
{

	/** @see EveTimeObserverClientAPI#notifyClock(ClockConfig) */
	@Override
	@Access(AccessType.UNAVAILABLE)
	Observable<ClockConfig> observeClock(ClockConfig.ID id);

	@Override
	@Access(AccessType.UNAVAILABLE)
	void updateTrigger(TriggerConfig trigger);

	/** @see #updateTrigger(TriggerConfig) */
	@Access(AccessType.PUBLIC)
	void updateTrigger(@Name("callbackURI") URI callbackURI,
			@Name("trigger") TriggerConfig trigger);

	/** @see EveTimeObserverClientAPI#notifyTrigger(TriggerEvent) */
	@Override
	@Access(AccessType.UNAVAILABLE)
	Observable<TriggerEvent> observeTrigger(TriggerConfig.ID triggerId);

	@Override
	@Access(AccessType.PUBLIC)
	void removeTrigger(@Name("triggerId") TriggerConfig.ID triggerId);

}