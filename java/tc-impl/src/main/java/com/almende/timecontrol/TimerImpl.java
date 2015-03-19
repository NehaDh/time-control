package com.almende.timecontrol;

import io.coala.util.JsonUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import rx.Observable;

import com.almende.timecontrol.api.TimeManagerAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockStatus;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.TriggerConfig;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.entity.TriggerStatus;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Instant;
import com.almende.timecontrol.time.Rate;

/**
 * {@link TimerImpl}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class TimerImpl implements TimeManagerAPI
{

	/** */
	private static final Logger LOG = LogManager.getLogger(TimerImpl.class);

	/** */
	private final TimerConfig config;

	/** */
	private final SortedMap<ClockConfig.ID, ClockStatus> clocks = Collections
			.synchronizedSortedMap(new TreeMap<ClockConfig.ID, ClockStatus>());

	/** all triggers collected for all Clocks */
	private final SortedMap<TriggerConfig.ID, TriggerStatus> triggers = Collections
			.synchronizedSortedMap(new TreeMap<TriggerConfig.ID, TriggerStatus>());

	/**
	 * {@link TimerImpl} constructor
	 * 
	 * @param config
	 */
	protected TimerImpl()
	{
		this.config = TimerConfig.Builder.forID("_NOID_").build();
		config.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(final PropertyChangeEvent evt)
			{
				LOG.trace("New timer config value for {}: {} --> {}",
						evt.getPropertyName(), evt.getOldValue(),
						evt.getNewValue());
			}
		});
	}

	@Override
	public TimerConfig getTimerConfig()
	{
		return this.config;
	}

	@Override
	public TimerStatus getTimerStatus()
	{
		return new TimerStatus.Builder().withConfig(this.config)
				.withClocks(this.clocks.values())
				.build();
	}

	@Override
	public void initialize(final TimerConfig config)
	{
		if (config.id() != null)
			getTimerConfig().setProperty(TimeControl.ID_KEY,
					JsonUtil.stringify(config.id()));

		if (config.duration() != null)
			getTimerConfig().setProperty(TimeControl.DURATION_KEY,
					JsonUtil.stringify(config.duration()));

		if (config.clock() != null)
			getTimerConfig().setProperty(TimeControl.CLOCK_KEY,
					JsonUtil.stringify(config.clock()));

		if (config.offset() != null)
			getTimerConfig().setProperty(TimeControl.OFFSET_KEY,
					JsonUtil.stringify(config.offset()));

		// resolution may be set to 'null' for continuous time scales
		getTimerConfig().setProperty(TimeControl.RESOLUTION_KEY,
				JsonUtil.stringify(config.resolution()));
	}

	@Override
	public void updateClock(final ClockConfig clock)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Observable<ClockConfig> observeClock(final ClockConfig.ID id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void updateTrigger(final TriggerConfig trigger)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Observable<TriggerEvent> observeTrigger(final TriggerConfig.ID triggerId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeTrigger(final TriggerConfig.ID triggerId)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy()
	{
		// TODO Auto-generated method stub

	}

	private static Map<String, TimeManagerAPI> TIMER_CACHE = new HashMap<>();

	public static TimeManagerAPI getInstance(final String id)
	{
		final ClockConfig clock = ClockConfig.Builder.forID("clock1")
				.withPace(Rate.valueOf(100)).build();
		final TimerConfig config = TimerConfig.Builder
				.forID(id)
				.withDuration(Duration.valueOf("\"P200D\""))
				.withOffset(
						Instant.valueOf(DateTime.now().withTimeAtStartOfDay()))
				.withResolution(Duration.valueOf("\"PT1H\"")).withClock(clock)
				.build();
		return getInstance(config);
	}

	public static synchronized TimeManagerAPI getInstance(final TimerConfig config)
	{
		TimeManagerAPI result = TIMER_CACHE.get(config.id().getValue());
		if (result == null)
		{
			result = new TimerImpl();
			result.initialize(config);
			TIMER_CACHE.put(config.id().getValue(), result);
		}
		return result;
	}
}
