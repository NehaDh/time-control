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
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.timecontrol.api.TimeManagerAPI;
import com.almende.timecontrol.api.TimeObserverAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockEvent;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.time.TriggerPattern;

/**
 * {@link EveTimeObserverAPI} adds Eve's {@link Name} annotations to
 * {@link TimeManagerAPI} where required
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface EveTimeObserverAPI extends TimeObserverAPI, EveTimeAgentAPI
{

	/** for JSON-RPC of {@link Observable} {@link #observeClock()} */
	@Access(AccessType.PUBLIC)
	SubscriptionID observeClockCallback(@Optional @Sender String callbackURI);

	/**
	 * for JSON-RPC, use method pair {@link #observeClockCallback(String)} and
	 * {@link EveTimeObserverClientAPI#notifyClock(SubscriptionID,ClockConfig)}
	 */
	@Override
	@Access(AccessType.UNAVAILABLE)
	Observable<ClockEvent> observeClock();

	/** for JSON-RPC of {@link Observable} {@link #observeClock(ClockConfig.ID)} */
	@Access(AccessType.PUBLIC)
	SubscriptionID observeClockCallback(
			@Optional @Name(ID_PARAM) ClockConfig.ID clockId,
			@Optional @Sender String callbackURI);

	/**
	 * for JSON-RPC, use method pair
	 * {@link #observeClockCallback(ClockConfig.ID,String)} and
	 * {@link EveTimeObserverClientAPI#notifyClock(SubscriptionID,ClockConfig)}
	 */
	@Override
	@Access(AccessType.UNAVAILABLE)
	Observable<ClockEvent> observeClock(ClockConfig.ID id);

	/**
	 * for JSON-RPC of {@link Observable}
	 * {@link #registerTrigger(TriggerPattern)}
	 */
	@Access(AccessType.PUBLIC)
	SubscriptionID registerTriggerCallback(
			@Name(PATTERN_PARAM) TriggerPattern pattern,
			@Optional @Sender String callbackURI);

	/**
	 * for JSON-RPC, use method pair
	 * {@link #registerTriggerCallback(TriggerPattern,String)} and
	 * {@link EveTimeObserverClientAPI#notifyTrigger(SubscriptionID,TriggerEvent)}
	 */
	@Override
	@Access(AccessType.UNAVAILABLE)
	Observable<TriggerEvent> registerTrigger(TriggerPattern pattern);

	/**
	 * for JSON-RPC of {@link Observable}
	 * {@link #registerTrigger(ClockConfig.ID,TriggerPattern)}
	 */
	@Access(AccessType.PUBLIC)
	SubscriptionID registerTriggerCallback(
			@Optional @Name(ID_PARAM) ClockConfig.ID clockId,
			@Name(PATTERN_PARAM) TriggerPattern pattern,
			@Optional @Sender String callbackURI);

	/**
	 * for JSON-RPC, use method pair
	 * {@link #registerTriggerCallback(ClockConfig.ID,TriggerPattern,String)}
	 * and
	 * {@link EveTimeObserverClientAPI#notifyTrigger(SubscriptionID,TriggerEvent)}
	 */
	@Override
	@Access(AccessType.UNAVAILABLE)
	Observable<TriggerEvent> registerTrigger(ClockConfig.ID clockId,
			TriggerPattern pattern);

	// @Override
	// @Access(AccessType.PUBLIC)
	// void unregisterTrigger(@Name("triggerId") TriggerConfig.ID triggerId);

}