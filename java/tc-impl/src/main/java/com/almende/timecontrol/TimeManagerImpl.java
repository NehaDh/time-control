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
	public TimerConfig getTimerConfig()
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
	public void setTimerConfig(final TimerConfig config)
	{
		this.config = config;
	}

	@Override
	public void updateClock(final ClockConfig config)
	{
		final ClockConfig.ID clockId = config.id() == null ? getTimerConfig()
				.clock().id() : config.id();
		synchronized (this.clocks)
		{
			ClockTuple result = this.clocks.get(clockId);
			if (result == null)
			{
				result = new ClockTuple(config);
				this.clocks.put(config.id(), result);
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
							reschedule(config.id(), config.drag());
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
		final ClockConfig.ID clockId = id == null ? getTimerConfig().clock()
				.id() : id;
		synchronized (this.clocks)
		{
			final ClockTuple result = this.clocks.get(clockId);
			if (result == null)
				throw ExceptionBuilder.unchecked("CLOCK UNKNOWN: " + id)
						.build();
			return result.events.asObservable();
		}
	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		if (getTimerConfig().clock().id().equals(clockId))
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
		final ClockConfig.ID clockId = id == null ? getTimerConfig().clock()
				.id() : id;
		LOG.trace("Registering trigger pattern {} on clock {}", pattern,
				clockId);

		return Observable.create(new OnSubscribe<TriggerEvent>()
		{
			@Override
			public void call(final Subscriber<? super TriggerEvent> sub)
			{
				// TODO start scheduling at specified clock according to
				// specified pattern

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

	public static TimeManagerAPI getInstance(final String id)
	{
		final ClockConfig clock = ClockConfig.Builder.forID("clock1")
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
		TimeManagerAPI result = TIMER_CACHE.get(config.id().getValue());
		if (result == null)
		{
			result = new TimeManagerImpl();
			result.setTimerConfig(config);
			TIMER_CACHE.put(config.id().getValue(), result);
		}
		return result;
	}
}
