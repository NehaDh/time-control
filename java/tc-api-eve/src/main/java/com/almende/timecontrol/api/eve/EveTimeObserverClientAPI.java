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

import rx.Observable;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockEvent;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.time.TriggerPattern;

/**
 * {@link EveTimeObserverClientAPI} adds {@link Name} annotations to
 * {@link SlaveAPI}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface EveTimeObserverClientAPI extends EveTimeAgentAPI
{

	/**
	 * for JSON-RPC of {@link Observable}
	 * {@link EveTimeManagerAPI#observeClock(ClockConfig.ID)}
	 */
	@Access(AccessType.PUBLIC)
	void notifyClock(@Name("callbackId") SubscriptionID callbackId,
			@Name("clock") ClockEvent clock);

	/**
	 * for JSON-RPC of {@link Observable}
	 * {@link EveTimeObserverAPI#registerTrigger(TriggerPattern)} and
	 * {@link EveTimeObserverAPI#registerTriggerCallback(ClockConfig.ID, TriggerPattern)}
	 */
	@Access(AccessType.PUBLIC)
	void notifyTrigger(@Name("callbackId") SubscriptionID callbackId,
			@Name("job") TriggerEvent job);

}