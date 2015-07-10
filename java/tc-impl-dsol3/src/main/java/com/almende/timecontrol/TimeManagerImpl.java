package com.almende.timecontrol;

import io.coala.error.ExceptionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

import com.almende.timecontrol.api.TimeManagerAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockEvent;
import com.almende.timecontrol.entity.ClockStatus;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.entity.TriggerStatus;
import com.almende.timecontrol.rx.RxClock;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Instant;
import com.almende.timecontrol.time.Rate;
import com.almende.timecontrol.time.TriggerPattern;
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

	/** */
	private final Map<ClockConfig.ID, ClockTuple> clocks = Collections
			.synchronizedMap(new TreeMap<ClockConfig.ID, ClockTuple>());

	private final Provider<ClockTuple> clockProvider;

	/**
	 * {@link TimeManagerImpl} constructor
	 * 
	 * @param config
	 */
	protected TimeManagerImpl(final Provider<ClockTuple> clockProvider)
	{
		this.clockProvider = clockProvider;
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
	protected ClockTuple getClockTuple(final ClockConfig.ID clockId)
	{
		final ClockTuple result;
		ClockConfig.ID rootClockID = getTimerConfig().rootClockId();
		if (clockId == null)
			result = rootClockID == null ? null : this.clocks.get(rootClockID);
		else
		{
			if (!this.clocks.containsKey(clockId))
				updateClock(ClockConfig.Builder.forID(clockId.getValue())
						.build());

			result = this.clocks.get(clockId);
		}
		if (result != null)
			return result;

		throw new IllegalStateException("No clock (id: " + clockId
				+ ", root-id: " + rootClockID + ") in timer config: "
				+ getTimerConfig());
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
			result.add(trigger.toStatus());
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
		final ClockConfig result = getClockTuple(clockId).config;
		LOG.trace("Found clock {} config: {}", clockId, result);
		return result;
	}

	@Override
	public void updateClock(final ClockConfig config)
	{
		LOG.trace("{} updating clock {}", this.config.id(), config);
		final ClockConfig.ID clockId = config.id();
		final ClockTuple result;
		synchronized (this.clocks)
		{
			result = this.clocks.get(clockId);
			if (result == null)
			{
				this.clocks
						.put(clockId, this.clockProvider.get().reset(config));
				LOG.trace("{} updating new clock {}", this.config.id(), config);
			}
		}
		if (result != null)
			for (String key : config.propertyNames())
			{
				if (key.equals(TimeControl.ID_KEY))
					continue;
				final String oldValue = this.config.getProperty(key);
				final String newValue = config.getProperty(key);

				// LOG.trace("Checking clock property {}", key);

				// ignore if unchanged
				if (oldValue == newValue
						|| (oldValue != null && oldValue.equals(newValue)))
					return;

				// LOG.trace("Updating clock config {} => {}", key, newValue);

				if (result.config != null)
					result.config.setProperty(key, newValue);
				result.onChange(key, newValue);
				// if (key.equals(TimeControl.DRAG_KEY))
				// reschedule(clockId, config.drag());
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
		final TimerConfig cfg = getTimerConfig();
		if (cfg == null)
		{
			LOG.warn("Clock not yet configured: " + id);
			return Observable.empty();
		}
		LOG.trace("Observing clock " + id + ", config: " + cfg);
		final ClockConfig.ID rootId = cfg.rootClockId();
		LOG.trace("Root clock: " + rootId);
		final ClockConfig.ID clockId = id == null ? rootId : id;
		synchronized (this.clocks)
		{
			ClockTuple clock = this.clocks.get(clockId);
			if (clock == null)
			{
				if (id != null && !getTimerConfig().rootClockId().equals(id))
					throw ExceptionBuilder.unchecked(
							"CLOCK UNKNOWN: " + clockId).build();

				clock = this.clockProvider.get().reset(
						new ClockConfig.Builder().withId(clockId).build());
				this.clocks.put(clockId, clock);
				LOG.warn("Root clock not initialized, using defaults: "
						+ clock.config);

			}
			return clock.events.asObservable();
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

		final ClockTuple clock;
		synchronized (this.clocks)
		{
			clock = this.clocks.get(clockId);
		}
		if (clock == null)
		{
			LOG.warn("Could not schedule trigger, clock not found {}", id);
			return Observable.empty();
		}
		// only schedule pattern if anyone is actually listening
		return Observable.create(new OnSubscribe<TriggerEvent>()
		{
			@Override
			public void call(final Subscriber<? super TriggerEvent> sub)
			{
				pattern.asObservable().subscribe(new Subscriber<Instant>()
				{
					private Instant previous = null;

					@Override
					public void onCompleted()
					{
						if (this.previous != null)
							clock.schedule(pattern, this.previous, true, sub);
					}

					@Override
					public void onError(final Throwable e)
					{
						// if (this.previous != null)
						// clock.schedule(pattern, this.previous, true, sub);
						sub.onError(e);
					}

					@Override
					public void onNext(final Instant time)
					{
						if (this.previous != null)
							clock.schedule(pattern, this.previous, false, sub);
						this.previous = time;
					}
				});
			}
		});
	}

	protected void destroyClock(final ClockConfig.ID clockId)
	{
		synchronized (this.clocks)
		{
			final ClockTuple clock = this.clocks.remove(clockId);
			if (clock != null)
				clock.destroy();
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
				.withDuration(Duration.valueOf("P200D"))
				.withOffset(
						Instant.valueOf(DateTime.now().withTimeAtStartOfDay()))
				.withResolution(Duration.valueOf("PT1H")).withClock(clock)
				.withClockType(RxClock.class).build();
		return getInstance(config);
	}

	public static synchronized TimeManagerAPI getInstance(
			final TimerConfig config)
	{
		TimeManagerAPI cachedResult = TIMER_CACHE.get(config.id().getValue());
		if (cachedResult == null)
		{
			final Class<? extends ClockTuple> clockType = config.clockType()
					.asSubclass(ClockTuple.class);
			cachedResult = new TimeManagerImpl(new Provider<ClockTuple>()
			{
				@Override
				public ClockTuple get()
				{
					try
					{
						return clockType.newInstance();
					} catch (final Exception e)
					{
						throw new RuntimeException(
								"Problem providing clock of type: " + clockType,
								e);
					}
				}
			});
			TIMER_CACHE.put(config.id().getValue(), cachedResult);
			cachedResult.setTimerConfig(config);
			LOG.trace("Cached new timer instance with config: {}", config);
		} else
			cachedResult.setTimerConfig(config);
		return cachedResult;
	}
}
