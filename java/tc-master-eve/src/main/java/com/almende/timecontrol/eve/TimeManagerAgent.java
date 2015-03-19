package com.almende.timecontrol.eve;

import static org.aeonbits.owner.util.Collections.entry;
import io.coala.util.JsonUtil;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentProxyFactory;
import com.almende.timecontrol.TimerImpl;
import com.almende.timecontrol.api.TimeManagerAPI;
import com.almende.timecontrol.api.eve.EveTimeObserverClientAPI;
import com.almende.timecontrol.api.eve.EveTimeManagerAPI;
import com.almende.timecontrol.api.eve.EveUtil;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.TriggerConfig;
import com.almende.timecontrol.entity.TriggerConfig.ID;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link TimerAgentTest} wraps/proxies for a {@link TimerImpl} instance with
 * the JSON-RPC capability provided by this Eve {@link Agent}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class TimeManagerAgent extends Agent implements EveTimeManagerAPI
{

	/** */
	private static final Logger LOG = LogManager
			.getLogger(TimeManagerAgent.class);

	/** */
	public static final String MASTER_CONFIG_KEY = "time-manager-config";

	/** */
	private final Subject<String, String> events = ReplaySubject.create();

	@Override
	public Observable<String> events()
	{
		return this.events.asObservable();
	}

	@Override
	protected void loadConfig()
	{
		super.loadConfig();
		this.events.onNext(AGENT_INITIALIZED);
	}

	/**
	 * Helper-method FIXME use CDI/injection?
	 * 
	 * @return the {@link TimeManagerAPI} implementation instance wrapped by
	 *         this {@link TimeManagerAgent}
	 */
	protected TimeManagerAPI getTimer()
	{
		return TimerImpl.getInstance(getId());
	}

	@Override
	public TimerStatus getTimerStatus()
	{
		return getTimer().getTimerStatus();
	}

	@Override
	public void initialize(final TimerConfig config)
	{
		getTimer().initialize(config);
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
		this.events.onNext(AGENT_DESTROYED);
		this.events.onCompleted();
	}

	// @Override
	// public void updateSlave(final SlaveConfig slave)
	// {
	// getTimer().updateSlave(slave);
	// }

	// @Override
	// public void removeSlave(final SlaveConfig.ID slaveId)
	// {
	// getTimer().removeSlave(slaveId);
	// }

	@Override
	public void updateClock(final ClockConfig clock)
	{
		getTimer().updateClock(clock);
	}

	@Override
	public Observable<ClockConfig> observeClock(final ClockConfig.ID id)
	{
		throw new IllegalStateException("NOT SUPPORTED VIA JSON-RPC");
	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		getTimer().removeClock(clockId);
	}

	@Override
	public void updateTrigger(final TriggerConfig trigger)
	{
		throw new IllegalStateException("NOT SUPPORTED VIA JSON-RPC");
	}

	private final SortedMap<URI, EveTimeObserverClientAPI> slaveProxyCache = new TreeMap<>();

	protected EveTimeObserverClientAPI getSlaveProxy(final URI slaveURI)
	{
		synchronized (this.slaveProxyCache)
		{
			EveTimeObserverClientAPI result = this.slaveProxyCache.get(slaveURI);
			if (result == null)
			{
				result = AgentProxyFactory.genProxy(this, slaveURI,
						EveTimeObserverClientAPI.class);
				this.slaveProxyCache.put(slaveURI, result);
			}
			return result;
		}
	}

	private SortedMap<TriggerConfig.ID, Subscription> jobSubscriptions = Collections
			.synchronizedSortedMap(new TreeMap<TriggerConfig.ID, Subscription>());

	@Override
	public void updateTrigger(final URI callbackURI, final TriggerConfig trigger)
	{
		final TriggerConfig.ID id = trigger.id();
		if (id == null)
			throw new NullPointerException("Trigger ill defined: " + trigger);
		if (!this.jobSubscriptions.containsKey(id))
		{
			LOG.trace(callbackURI + " added new trigger: " + trigger);
			this.jobSubscriptions.put(id, getTimer().observeTrigger(id)
					.subscribe(new Observer<TriggerEvent>()
					{
						@Override
						public void onCompleted()
						{
							LOG.trace("Unregistering completed trigger: " + id);
							jobSubscriptions.remove(id);
						}

						@Override
						public void onError(final Throwable e)
						{
							LOG.error("Problem observing trigger: " + id
									+ " for uri: " + callbackURI, e);
						}

						@Override
						public void onNext(final TriggerEvent job)
						{
							getSlaveProxy(callbackURI).notifyTrigger(job);
						}
					}));
		}
		getTimer().updateTrigger(trigger);
	}

	@Override
	public Observable<TriggerEvent> observeTrigger(ID triggerId)
	{
		throw new IllegalStateException("NOT SUPPORTED VIA JSON-RPC");
	}

	@Override
	public void removeTrigger(final TriggerConfig.ID triggerId)
	{
		getTimer().removeTrigger(triggerId);
	}

	/** */
	public static TimeManagerAgent valueOf(final TimerConfig config)
	{
		return valueOf(
				config.id().getValue(),
				entry(TimeManagerAgent.MASTER_CONFIG_KEY,
						JsonUtil.toTree(config)));
	}

	/** */
	@SafeVarargs
	public static TimeManagerAgent valueOf(final String id,
			final Map.Entry<String, ? extends JsonNode>... parameters)
	{
		return EveUtil.valueOf(id, TimeManagerAgent.class, parameters);
	}

	/** */
	private static final Map<String, TimeManagerAgent> INSTANCES = new TreeMap<>();

	/** */
	public static TimeManagerAgent getInstance(final String timerID)
	{
		synchronized (INSTANCES)
		{
			final TimeManagerAgent result = INSTANCES.get(timerID);
			if (result == null)
				return getInstance(TimerConfig.Builder.forID(timerID).build());
			return result;
		}
	}

	/** */
	public static TimeManagerAgent getInstance(final TimerConfig config)
	{
		synchronized (INSTANCES)
		{
			TimeManagerAgent result = INSTANCES.get(config.id());
			if (result == null)
			{
				LOG.trace("Create master with config: {}", config);
				result = TimeManagerAgent.valueOf(config);
				INSTANCES.put(config.id().getValue(), result);
			}
			return result;
		}
	}

}
