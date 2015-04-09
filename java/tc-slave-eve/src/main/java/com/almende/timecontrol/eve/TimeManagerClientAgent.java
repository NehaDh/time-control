/* $Id$
 * $URL$
 * 
 * Part of the EU project Inertia, see http://www.inertia-project.eu/
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
 * Copyright (c) 2014 Almende B.V. 
 */
package com.almende.timecontrol.eve;

import static org.aeonbits.owner.util.Collections.entry;
import io.coala.util.JsonUtil;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentProxyFactory;
import com.almende.timecontrol.api.TimeManagerAPI;
import com.almende.timecontrol.api.eve.EveTimeManagerAPI;
import com.almende.timecontrol.api.eve.EveTimeObserverClientAPI;
import com.almende.timecontrol.api.eve.EveUtil;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockEvent;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.time.TriggerPattern;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link TimeManagerClientAgent} is a simple client of some
 * {@link EveTimeManagerAPI} instance
 * 
 * @date $Date$
 * @version $Revision$
 * @author <a href="mailto:gebruiker@almende.org">gebruiker</a>
 *
 */
public class TimeManagerClientAgent extends Agent implements
		EveTimeObserverClientAPI, TimeManagerAPI
{

	/** */
	public static final String CLIENT_CONFIG_KEY = "time-manager-client-agent-config";

	/** */
	public static final String MASTER_URI_KEY = "time-manager-agent-uri";

	/** */
	private static final Logger LOG = LogManager
			.getLogger(TimeManagerClientAgent.class);

	/** the latest {@link SlaveStatus} received from the {@link #timerProxy} */
	// private SlaveStatus status;

	/** */
	private Subject<EventWrapper<ClockEvent>, EventWrapper<ClockEvent>> clockEvents = PublishSubject
			.create();

	/** */
	private Subject<EventWrapper<TriggerEvent>, EventWrapper<TriggerEvent>> triggerEvents = PublishSubject
			.create();

	/** */
	private EveTimeManagerAPI timerProxy;

	private boolean initialized = false;

	/** */
	private final Subject<AgentEvent, AgentEvent> events = ReplaySubject
			.create();

	@Override
	public Observable<AgentEvent> events()
	{
		return this.events.asObservable();
	}

	@Override
	protected void loadConfig()
	{
		super.loadConfig();

		if (this.initialized)
			return;

		final JsonNode tree = getConfig().get(CLIENT_CONFIG_KEY);
		LOG.trace("Read slave config tree: " + tree);
		final TimerConfig.ID timerID = TimerConfig.ID.valueOf(tree);

		// this.status = SlaveStatus.Builder.forSlave(config).build();
		// LOG.trace("Initialized! Status: " + this.status);

		final URI timerAgentURI;
		if (getConfig().has(MASTER_URI_KEY))
			timerAgentURI = URI
					.create(getConfig().get(MASTER_URI_KEY).asText());
		else
			timerAgentURI = URI.create("local:" + timerID);

		this.timerProxy = AgentProxyFactory.genProxy(this, timerAgentURI,
				EveTimeManagerAPI.class);

		final TimerConfig config = this.timerProxy.getTimerConfig();
		LOG.warn(
				"Connected to time control master at uri: {}, got timer config: {}",
				timerAgentURI, config);

		this.initialized = true;
		this.events.onNext(AgentEvent.AGENT_INITIALIZED);
	}

	/**
	 * @return
	 */
	protected EveTimeManagerAPI getTimerProxy()
	{
		return this.timerProxy;
	}

	@Override
	public TimerConfig getTimerConfig()
	{
		return getTimerProxy().getTimerConfig();
	}

	@Override
	public TimerStatus getTimerStatus()
	{
		return getTimerProxy().getTimerStatus();
	}

	@Override
	public void destroy()
	{
		super.destroy();
		this.events.onNext(AgentEvent.AGENT_DESTROYED);
		this.events.onCompleted();
	}

	@Override
	public void setTimerConfig(final TimerConfig config)
	{
		try
		{
			getTimerProxy().setTimerConfig(config);
		} catch (final Throwable t)
		{
			LOG.error("Problem in JSON-RPC", t);
		}
	}

	@Override
	public void updateClock(final ClockConfig clock)
	{
		getTimerProxy().updateClock(clock);
	}

	@Override
	public void notifyClock(final SubscriptionID callbackID,
			final ClockEvent clock)
	{
		this.clockEvents.onNext(EventWrapper.of(callbackID, clock));
	}

	@Override
	public Observable<ClockEvent> observeClock()
	{
		return observeClock(null);
	}

	private final Map<ClockConfig.ID, Observable<ClockEvent>> clockObservables = new HashMap<>();

	@Override
	public Observable<ClockEvent> observeClock(final ClockConfig.ID id)
	{
		Observable<ClockEvent> cachedResult = this.clockObservables.get(id);
		if (cachedResult == null)
		{
			final SubscriptionID subID = getTimerProxy().observeClockCallback(
					id, null);
			cachedResult = this.clockEvents.filter(
					new Func1<EventWrapper<ClockEvent>, Boolean>()
					{
						@Override
						public Boolean call(
								final EventWrapper<ClockEvent> wrapper)
						{
							return wrapper.fits(subID);
						}
					}).map(new Func1<EventWrapper<ClockEvent>, ClockEvent>()
			{
				@Override
				public ClockEvent call(final EventWrapper<ClockEvent> wrapper)
				{
					return wrapper.unwrap();
				}
			});
			this.clockObservables.put(id, cachedResult);
		}
		return cachedResult;
	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		getTimerProxy().removeClock(clockId);
	}

	@Override
	public void notifyTrigger(final SubscriptionID subscriptionID,
			final TriggerEvent job)
	{
		this.triggerEvents.onNext(EventWrapper.of(subscriptionID, job));
	}

	@Override
	public Observable<TriggerEvent> registerTrigger(final TriggerPattern pattern)
	{
		return registerTrigger(null, pattern);
	}

	@Override
	public Observable<TriggerEvent> registerTrigger(
			final ClockConfig.ID clockId, final TriggerPattern pattern)
	{
		// TODO add caching/forking for similar patterns?

		final SubscriptionID subID = getTimerProxy().registerTriggerCallback(
				clockId, pattern, null);
		return this.triggerEvents.filter(
				new Func1<EventWrapper<TriggerEvent>, Boolean>()
				{
					@Override
					public Boolean call(final EventWrapper<TriggerEvent> wrapper)
					{
						return wrapper.fits(subID);
					}
				}).map(new Func1<EventWrapper<TriggerEvent>, TriggerEvent>()
		{
			@Override
			public TriggerEvent call(final EventWrapper<TriggerEvent> wrapper)
			{
				return wrapper.unwrap();
			}
		});
	}

	/**************************************************************************/

	/** */
	public static TimeManagerClientAgent valueOf(final TimerConfig.ID timerId,
			final String slaveId)
	{
		LOG.trace("Writing slave config for {}: {}", slaveId, timerId);
		return valueOf(slaveId,
				entry(CLIENT_CONFIG_KEY, JsonUtil.toTree(timerId)));
	}

	/** */
	@SafeVarargs
	public static final TimeManagerClientAgent valueOf(final String id,
			final Map.Entry<String, ? extends JsonNode>... parameters)
	{
		return EveUtil.valueOf(id, TimeManagerClientAgent.class, parameters);
	}

	/** */
	private static final Map<String, TimeManagerClientAgent> INSTANCES = new TreeMap<>();

	/** */
	public static TimeManagerClientAgent getInstance(final String timerId,
			final String slaveId)
	{
		return getInstance(TimerConfig.ID.valueOf(timerId), slaveId);
	}

	/** */
	public static TimeManagerClientAgent getInstance(
			final TimerConfig.ID timerId, final String slaveId)
	{
		synchronized (INSTANCES)
		{
			TimeManagerClientAgent result = INSTANCES.get(slaveId);
			if (result == null)
			{
				result = TimeManagerClientAgent.valueOf(timerId, slaveId);
				INSTANCES.put(slaveId, result);
			}
			return result;
		}
	}

}
