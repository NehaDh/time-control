package com.almende.timecontrol;

import io.coala.error.ExceptionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.almende.timecontrol.api.TimeManagerAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockEvent;
import com.almende.timecontrol.entity.ClockStatus;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.TriggerConfig;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.entity.TriggerStatus;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Instant;
import com.almende.timecontrol.time.Rate;
import com.almende.timecontrol.time.TriggerPattern;
import com.eaio.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link TimeManagerImpl}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class TimeManagerImpl implements TimeManagerAPI
{

	/** */
	private static final Logger LOG = LogManager
			.getLogger(TimeManagerImpl.class);

	/** */
	private TimerConfig config = null;

	static class TriggerTuple
	{
		final TriggerConfig config;

		final List<TriggerEvent> events = new ArrayList<>();

		final Subject<TriggerEvent, TriggerEvent> eventPublisher = PublishSubject
				.create();

		TriggerTuple(final TriggerPattern pattern)
		{
			this.config = TriggerConfig.Builder.fromID(new UUID().toString())
					.withPattern(pattern).build();
		}
	}

	static class ClockTuple
	{
		final ClockConfig config;

		final Subject<ClockEvent, ClockEvent> events = PublishSubject.create();

		final SortedMap<TriggerConfig.ID, TriggerTuple> triggers = Collections
				.synchronizedSortedMap(new TreeMap<TriggerConfig.ID, TriggerTuple>());

		ClockTuple(final ClockConfig config)
		{
			this.config = config;
			this.config
					.addPropertyChangeListener(ClockEvent.PropertyChangeListenerFilter
							.forObserver(config.id(), this.events));
		}
	}

	/** */
	private final Map<ClockConfig.ID, ClockTuple> clocks = Collections
			.synchronizedMap(new TreeMap<ClockConfig.ID, ClockTuple>());

	/** all triggers collected for all Clocks */
	// private final SortedMap<TriggerConfig.ID, Subject<TriggerEvent,
	// TriggerEvent>> triggers = Collections
	// .synchronizedSortedMap(new TreeMap<TriggerConfig.ID,
	// Subject<TriggerEvent, TriggerEvent>>());

	/**
	 * {@link TimeManagerImpl} constructor
	 * 
	 * @param config
	 */
	protected TimeManagerImpl()
	{
	}

	@Override
	public synchronized void setTimerConfig(final TimerConfig config)
	{
		this.config = config;
	}

	@Override
	public synchronized TimerConfig getTimerConfig()
	{
		return this.config;
	}

	@JsonIgnore
	protected List<ClockStatus> getClocksStatus()
	{
		final List<ClockStatus> result = new ArrayList<>();
		for (ClockTuple clock : this.clocks.values())
			result.add(ClockStatus.Builder.fromConfig(clock.config)
					.withTriggers(getTriggersStatus(clock.config.id())).build());
		return result;
	}

	@JsonIgnore
	protected List<TriggerStatus> getTriggersStatus(final ClockConfig.ID id)
	{
		final List<TriggerStatus> result = new ArrayList<>();
		for (TriggerTuple trigger : this.clocks.get(id).triggers.values())
			result.add(TriggerStatus.Builder.fromConfig(trigger.config).build());
		return result;
	}

	@Override
	public TimerStatus getTimerStatus()
	{
		return TimerStatus.Builder.fromConfig(this.config)
				.withClocks(getClocksStatus()).build();
	}

	@Override
	public ClockConfig getClock(final ClockConfig.ID clockId)
	{
		final ClockTuple result;
		if (clockId == null)
			result = this.clocks.get(getTimerConfig().rootClockId());
		else
		{
			if (!this.clocks.containsKey(clockId))
				updateClock(ClockConfig.Builder.forID(clockId.getValue())
						.build());

			result = this.clocks.get(clockId);
		}
		if (result == null)
		{
			LOG.error("No clock {} set or no root clock in timer config: {}",
					clockId, getTimerConfig());
			return null;
		}
		LOG.trace("Found clock {} config: {}", clockId, result == null ? null
				: result.config);
		return result.config;
	}

	@Override
	public void updateClock(final ClockConfig config)
	{
		final ClockConfig.ID clockId = config.id();
		LOG.trace("Updating clock: " + clockId);
		synchronized (this.clocks)
		{
			ClockTuple result = this.clocks.get(clockId);
			if (result == null)
			{
				result = new ClockTuple(config);
				this.clocks.put(clockId, result);
			} else
			{
				for (String key : config.propertyNames())
				{
					if (key.equals(TimeControl.ID_KEY))
						continue;
					final String oldValue = this.config.getProperty(key);
					final String newValue = config.getProperty(key);
					if ((oldValue == null && newValue != null)
							|| (oldValue != null && !oldValue.equals(newValue)))
					{
						result.config.setProperty(key, newValue);
						if (key.equals(TimeControl.DRAG_KEY))
							reschedule(clockId, config.drag());
					}
				}
			}
		}
	}

	@Override
	public Observable<ClockEvent> observeClock()
	{
		return observeClock(null);
	}

	@Override
	public Observable<ClockEvent> observeClock(final ClockConfig.ID id)
	{
		LOG.trace("Observing clock " + id + ", config: " + getTimerConfig());
		LOG.trace("Root clock: " + getTimerConfig().rootClockId());
		final ClockConfig.ID clockId = id == null ? getTimerConfig()
				.rootClockId() : id;
		synchronized (this.clocks)
		{
			ClockTuple tuple = this.clocks.get(clockId);
			if (tuple == null)
			{
				if (id != null && !getTimerConfig().rootClockId().equals(id))
					throw ExceptionBuilder.unchecked(
							"CLOCK UNKNOWN: " + clockId).build();

				tuple = new ClockTuple(new ClockConfig.Builder()
						.withId(clockId).build());
				this.clocks.put(clockId, tuple);
				LOG.warn("Root clock not initialized, using defaults: "
						+ tuple.config);

			}
			return tuple.events.asObservable();
		}
	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		if (getTimerConfig().rootClockId().equals(clockId))
			throw ExceptionBuilder.unchecked(
					"Can't remove root clock: " + clockId).build();
		destroyClock(clockId);
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
		final ClockConfig.ID clockId = id == null ? getTimerConfig()
				.rootClockId() : id;
		LOG.trace("Registering trigger pattern {} on clock {}", pattern,
				clockId);

		return Observable.create(new OnSubscribe<TriggerEvent>()
		{
			@Override
			public void call(final Subscriber<? super TriggerEvent> sub)
			{
				// TODO start scheduling according to
				// specified pattern and clock pace

			}
		});
	}

	protected void reschedule(final ClockConfig.ID id, final Rate drag)
	{
		LOG.trace("Rescheduling clock {} for new drag rate {}", id, drag);
		// TODO start scheduling at specified clock according to
		// specified drag
	}

	protected void destroyClock(final ClockConfig.ID clockId)
	{
		synchronized (this.clocks)
		{
			this.clocks.remove(clockId);
			// TODO unschedule clock's respective triggers, subscriptions etc.
		}
	}

	@Override
	public void destroy()
	{
		// destroy all clocks
		synchronized (this.clocks)
		{
			for (ClockConfig.ID id : this.clocks.keySet())
				destroyClock(id);
		}
	}

	private static Map<String, TimeManagerAPI> TIMER_CACHE = new HashMap<>();

	public static synchronized TimeManagerAPI getInstance(final String id)
	{
		TimeManagerAPI cachedResult = TIMER_CACHE.get(id);
		if (cachedResult != null)
		{
			LOG.trace("Return cached timer instance: {}", id);
			return cachedResult;
		}

		LOG.warn("Initializing default config for timer: " + id);
		final ClockConfig clock = ClockConfig.Builder.forID(id)
				.withDrag(Rate.valueOf(100)).build();
		final TimerConfig config = TimerConfig.Builder
				.forID(id)
				.withDuration(Duration.valueOf("\"P200D\""))
				.withOffset(
						Instant.valueOf(DateTime.now().withTimeAtStartOfDay()))
				.withResolution(Duration.valueOf("\"PT1H\"")).withClock(clock)
				.build();
		return getInstance(config);
	}

	public static synchronized TimeManagerAPI getInstance(
			final TimerConfig config)
	{
		TimeManagerAPI cachedResult = TIMER_CACHE.get(config.id().getValue());
		if (cachedResult == null)
		{
			cachedResult = new TimeManagerImpl();
			TIMER_CACHE.put(config.id().getValue(), cachedResult);
			cachedResult.setTimerConfig(config);
			LOG.trace("Cached new timer instance with config: {}", config);
		} else
			cachedResult.setTimerConfig(config);
		return cachedResult;
	}
}
