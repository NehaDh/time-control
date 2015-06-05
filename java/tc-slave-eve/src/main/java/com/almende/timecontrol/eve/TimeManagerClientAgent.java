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
import rx.functions.Action0;
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

	/** */
	private final Subject<EventWrapper<ClockEvent>, EventWrapper<ClockEvent>> clockEvents = PublishSubject
			.create();

	/** */
	private final Subject<EventWrapper<TriggerEvent>, EventWrapper<TriggerEvent>> triggerEvents = PublishSubject
			.create();

	/** */
	private final Map<ClockConfig.ID, Observable<ClockEvent>> clockObservableCache = new HashMap<>();

	/** */
	private final Map<ClockConfig.ID, Map<TriggerPattern, Observable<TriggerEvent>>> triggerObservableCache = new HashMap<>();

	/** */
	private final Subject<AgentEventType, AgentEventType> events = ReplaySubject
			.create();

	/** */
	private volatile EveTimeManagerAPI timerProxy;

	/** */
	private volatile boolean initialized = false;

	/** */
	private volatile TimerConfig timerConfig = null;

	/** */
	private volatile ClockConfig rootClock = null;

	@Override
	public Observable<AgentEventType> events()
	{
		return this.events.asObservable();
	}

	/**
	 * clean up {@link #clockObservableCache}
	 * 
	 * @param clockId
	 */
	protected void cleanClockObservableCache(final ClockConfig.ID clockId)
	{
		synchronized (this.clockObservableCache)
		{
			final Observable<ClockEvent> empty = Observable.empty();
			this.clockObservableCache.put(clockId, empty);
		}
	}

	/**
	 * clean up {@link #triggerObservableCache}
	 * 
	 * @param clockId
	 */
	protected void cleanTriggerObservableCache(final ClockConfig.ID clockId)
	{
		synchronized (this.triggerObservableCache)
		{
			final Map<TriggerPattern, Observable<TriggerEvent>> observables = this.triggerObservableCache
					.get(clockId);
			if (observables == null)
				return;

			for (TriggerPattern pattern : observables.keySet())
				cleanTriggerObservableCache(clockId, pattern);
		}
	}

	/**
	 * clean up {@link #triggerObservableCache}
	 * 
	 * @param clockId
	 * @param pattern
	 */
	protected void cleanTriggerObservableCache(final ClockConfig.ID clockId,
			final TriggerPattern pattern)
	{
		synchronized (this.triggerObservableCache)
		{
			final Map<TriggerPattern, Observable<TriggerEvent>> observables = this.triggerObservableCache
					.get(clockId);
			if (observables == null)
				return;

			final Observable<TriggerEvent> empty = Observable.empty();
			observables.put(pattern, empty);
		}
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

		this.timerConfig = this.timerProxy.getTimerConfig();
		this.rootClock = this.timerProxy.getClock(this.timerConfig
				.rootClockId());
		LOG.warn(
				"Connected to time control master at uri: {}, got timer config: {}",
				timerAgentURI, this.timerConfig);

		this.initialized = true;
		this.events.onNext(AgentEventType.AGENT_INITIALIZED);
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
		return this.timerConfig;
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
	public TimerStatus getTimerStatus()
	{
		return getTimerProxy().getTimerStatus();
	}

	@Override
	public void destroy()
	{
		super.destroy();
		this.events.onNext(AgentEventType.AGENT_DESTROYED);
		this.events.onCompleted();
	}

	@Override
	public ClockConfig getClock(final ClockConfig.ID clockId)
	{
		return getTimerProxy().getClock(clockId);
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

	@Override
	public Observable<ClockEvent> observeClock(final ClockConfig.ID id)
	{
		final ClockConfig.ID clockId = id != null ? id
				: this.rootClock != null ? this.rootClock.id() : null;

		synchronized (this.clockObservableCache)
		{
			Observable<ClockEvent> cachedResult = this.clockObservableCache
					.get(clockId);

			if (cachedResult == null)
			{
				final SubscriptionID subID = getTimerProxy()
						.observeClockCallback(clockId, null);
				cachedResult = this.clockEvents.filter(
						new Func1<EventWrapper<ClockEvent>, Boolean>()
						{
							@Override
							public Boolean call(
									final EventWrapper<ClockEvent> wrapper)
							{
								return wrapper.fits(subID);
							}
						}).map(
						new Func1<EventWrapper<ClockEvent>, ClockEvent>()
						{
							@Override
							public ClockEvent call(
									final EventWrapper<ClockEvent> wrapper)
							{
								return wrapper.unwrap();
							}
						});

				cachedResult.finallyDo(new Action0()
				{
					@Override
					public void call()
					{
						cleanClockObservableCache(clockId);
						cleanTriggerObservableCache(clockId);
					}
				});
				this.clockObservableCache.put(clockId, cachedResult);
			}
			return cachedResult;
		}
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
		LOG.trace("Notify trigger event: {} {}", subscriptionID, job);
		this.triggerEvents.onNext(EventWrapper.of(subscriptionID, job));
	}

	@Override
	public Observable<TriggerEvent> registerTrigger(final TriggerPattern pattern)
	{
		return registerTrigger(null, pattern);
	}

	@Override
	public Observable<TriggerEvent> registerTrigger(final ClockConfig.ID id,
			final TriggerPattern pattern)
	{
		final ClockConfig.ID clockId = id != null ? id
				: this.rootClock != null ? this.rootClock.id() : null;

		synchronized (this.triggerObservableCache)
		{
			Map<TriggerPattern, Observable<TriggerEvent>> observables = this.triggerObservableCache
					.get(clockId);
			if (observables == null)
			{
				observables = new HashMap<>();
				this.triggerObservableCache.put(clockId, observables);
			}
			Observable<TriggerEvent> cachedResult = this.triggerObservableCache
					.get(clockId).get(pattern);
			if (cachedResult == null)
			{
				final SubscriptionID subID = getTimerProxy()
						.registerTriggerCallback(clockId, pattern, null);
				LOG.trace("Received trigger {} pattern: {}", subID, pattern);
				cachedResult = this.triggerEvents
						.filter(new Func1<EventWrapper<TriggerEvent>, Boolean>()
						{
							@Override
							public Boolean call(
									final EventWrapper<TriggerEvent> wrapper)
							{
								final boolean result = wrapper.fits(subID);
								LOG.trace("Fits {} == {}: {}", wrapper.subID,
										subID, result);
								return result;
							}
						})
						.map(new Func1<EventWrapper<TriggerEvent>, TriggerEvent>()
						{
							@Override
							public TriggerEvent call(
									final EventWrapper<TriggerEvent> wrapper)
							{
								final TriggerEvent result = wrapper.unwrap();
								LOG.trace("Unwrapped event: {}", result);
								return result;
							}
						}).takeWhile(new Func1<TriggerEvent, Boolean>()
						{
							@Override
							public Boolean call(final TriggerEvent event)
							{
								final boolean result = !event
										.lastCall();
								LOG.trace("notLast: {}", result);
								return result;
							}
						});// TODO
							// .takeUntil(observeClock(clockId).takeLast(1));

				/* TODO
				cachedResult.finallyDo(new Action0()
				{
					@Override
					public void call()
					{
						cleanTriggerObservableCache(clockId, pattern);
					}
				});*/
				observables.put(pattern, cachedResult);
			}
			return cachedResult;
		}
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
