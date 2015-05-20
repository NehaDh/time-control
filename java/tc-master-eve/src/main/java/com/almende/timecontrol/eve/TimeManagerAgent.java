package com.almende.timecontrol.eve;

import static org.aeonbits.owner.util.Collections.entry;
import io.coala.util.JsonUtil;
import io.coala.util.LogUtil;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.AgentProxyFactory;
import com.almende.timecontrol.TimeManagerImpl;
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
 * {@link TimerAgentTest} wraps/proxies for a {@link TimeManagerImpl} instance
 * with the JSON-RPC capability provided by this Eve {@link Agent}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class TimeManagerAgent extends Agent implements EveTimeManagerAPI
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(TimeManagerAgent.class);

	/** */
	public static final String MASTER_CONFIG_KEY = "time-manager-config";

	/** */
	private final Subject<AgentEventType, AgentEventType> events = ReplaySubject
			.create();

	@Override
	public Observable<AgentEventType> events()
	{
		return this.events.asObservable();
	}

	private final SortedMap<URI, EveTimeObserverClientAPI> observerProxyCache = new TreeMap<>();

	protected EveTimeObserverClientAPI getObserverProxy(final URI slaveURI)
	{
		synchronized (this.observerProxyCache)
		{
			EveTimeObserverClientAPI result = this.observerProxyCache
					.get(slaveURI);
			if (result == null)
			{
				result = AgentProxyFactory.genProxy(this, slaveURI,
						EveTimeObserverClientAPI.class);
				this.observerProxyCache.put(slaveURI, result);
			}
			return result;
		}
	}

	@Override
	protected void loadConfig()
	{
		super.loadConfig();
		this.events.onNext(AgentEventType.AGENT_INITIALIZED);
	}

	/**
	 * Helper-method FIXME use CDI/injection?
	 * 
	 * @return the {@link TimeManagerAPI} implementation instance wrapped by
	 *         this {@link TimeManagerAgent}
	 */
	protected TimeManagerAPI getTimer()
	{
		LOG.trace("Getting timer instance: {}", getId());
		return TimeManagerImpl.getInstance(getId());
	}

	@Override
	public TimerStatus getTimerStatus()
	{
		return getTimer().getTimerStatus();
	}

	@Override
	public void setTimerConfig(final TimerConfig config)
	{
		getTimer().setTimerConfig(config);
	}

	@Override
	public TimerConfig getTimerConfig()
	{
		return getTimer().getTimerConfig();
	}

	@Override
	public void destroy()
	{
		super.destroy();
		getTimer().destroy();
		this.events.onNext(AgentEventType.AGENT_DESTROYED);
		this.events.onCompleted();
	}

	@Override
	public JsonNode findClock(final String clockId)
	{
		LOG.trace("Finding clock: {}", clockId);
		return JsonUtil.toTree(getClock(clockId == null ? null : ClockConfig.ID
				.valueOf(clockId)));
	}

	@Override
	public ClockConfig getClock(final ClockConfig.ID clockId)
	{
		LOG.trace("Finding clock: {}", clockId);
		return getTimer().getClock(clockId);
	}

	@Override
	public void updateClock(final ClockConfig clock)
	{
		getTimer().updateClock(clock);
	}

	@Override
	public Observable<ClockEvent> observeClock()
	{
		return getTimer().observeClock();
	}

	@Override
	public Observable<ClockEvent> observeClock(final ClockConfig.ID id)
	{
		return getTimer().observeClock(id);
	}

	@Override
	public SubscriptionID observeClockCallback(final String callbackURI)
	{
		return observeClockCallback(null, callbackURI);
	}

	@Override
	public SubscriptionID observeClockCallback(final ClockConfig.ID id,
			final String callbackURI)
	{
		final SubscriptionID callbackID = new SubscriptionID();
		getTimer().observeClock(id).subscribe(new Observer<ClockEvent>()
		{
			private final URI uri = URI.create(callbackURI);

			@Override
			public void onCompleted()
			{
				LOG.trace("Completed clock: {}", id);
			}

			@Override
			public void onError(final Throwable e)
			{
				LOG.error("Problem observing trigger " + id
						+ " for callbackURI " + this.uri, e);
			}

			@Override
			public void onNext(final ClockEvent clock)
			{
				getObserverProxy(this.uri).notifyClock(callbackID, clock);
			}
		});
		return callbackID;
	}

	@Override
	public void removeClock(final String clockId)
	{
		removeClock(ClockConfig.ID.valueOf(clockId));
	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		getTimer().removeClock(clockId);
	}

	@Override
	public SubscriptionID registerTriggerCallback(final TriggerPattern pattern,
			final String callbackURI)
	{
		return registerTriggerCallback(null, pattern, callbackURI);
	}

	@Override
	public Observable<TriggerEvent> registerTrigger(final TriggerPattern pattern)
	{
		return getTimer().registerTrigger(pattern);
	}

	@Override
	public Observable<TriggerEvent> registerTrigger(
			final ClockConfig.ID clockId, final TriggerPattern pattern)
	{
		return getTimer().registerTrigger(clockId, pattern);
	}

	@Override
	public SubscriptionID registerTriggerCallback(final ClockConfig.ID clockId,
			final TriggerPattern pattern, final String callbackURI)
	{
		final SubscriptionID callbackID = new SubscriptionID();
		getTimer().registerTrigger(clockId, pattern).subscribe(
				new Observer<TriggerEvent>()
				{
					private final URI uri = URI.create(callbackURI);

					@Override
					public void onCompleted()
					{
						LOG.trace("Completed trigger pattern: " + pattern);
					}

					@Override
					public void onError(final Throwable e)
					{
						LOG.error("Problem observing trigger pattern: "
								+ pattern + " for callbackURI: " + this.uri, e);
					}

					@Override
					public void onNext(final TriggerEvent job)
					{
						getObserverProxy(this.uri).notifyTrigger(callbackID,
								job);
					}
				});
		return callbackID;
	}

	/**************************************************************************/

	/** */
	private static final Map<String, TimeManagerAgent> INSTANCES = new TreeMap<>();

	/** */
	public static TimeManagerAgent getInstance(final String timerID)
	{
		final AgentConfig cfg = new AgentConfig(timerID);
		cfg.setClassName(TimeManagerAgent.class.getName());
		return getInstance(cfg);
	}

	/** */
	public static TimeManagerAgent getInstance(// final String timerID,
			final AgentConfig agentConfig)
	{
		synchronized (INSTANCES)
		{
			final TimeManagerAgent result = INSTANCES.get(agentConfig.getId());
			if (result != null)
				return result;
			return getInstance(TimerConfig.Builder.forID(agentConfig.getId())
					.build(), agentConfig);
		}
	}

	/** */
	public static TimeManagerAgent getInstance(final TimerConfig config,
			final AgentConfig agentConfig)
	{
		synchronized (INSTANCES)
		{
			TimeManagerAgent result = INSTANCES.get(config.id());
			if (result != null)
				result.setTimerConfig(config);
			else
			{
				LOG.trace("Create master with config: {}", config);
				result = EveUtil.valueOf(
						agentConfig,
						entry(TimeManagerAgent.MASTER_CONFIG_KEY,
								JsonUtil.toTree(config)));
				INSTANCES.put(config.id().getValue(), result);
			}
			return result;
		}
	}

	/**
	 * @return
	 */
	public static Collection<TimeManagerAgent> getInstances()
	{
		synchronized (INSTANCES)
		{
			return Collections
					.unmodifiableCollection(new HashSet<TimeManagerAgent>(
							INSTANCES.values()));
		}
	}

	/**************************************************************************/

	/**
	 * Boot the Multi-Agent System
	 *
	 * @param args the command-line arguments
	 */
	public static void main(final String[] args)
	{
		MAS.main(args);
	}

}
