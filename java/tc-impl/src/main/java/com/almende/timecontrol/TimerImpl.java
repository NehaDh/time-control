package com.almende.timecontrol;

import io.coala.util.JsonUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.almende.timecontrol.api.TimerAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.Job;
import com.almende.timecontrol.entity.SlaveConfig;
import com.almende.timecontrol.entity.SlaveStatus;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.Trigger;
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
public class TimerImpl implements TimerAPI
{

	/** */
	private static final Logger LOG = LogManager.getLogger(TimerImpl.class);

	/** */
	private final SortedMap<ClockConfig.ID, ClockConfig> clocks = Collections
			.synchronizedSortedMap(new TreeMap<ClockConfig.ID, ClockConfig>());

	/** */
	private final SortedMap<SlaveConfig.ID, SlaveConfig> slaves = Collections
			.synchronizedSortedMap(new TreeMap<SlaveConfig.ID, SlaveConfig>());

	/** */
	private final SortedMap<SlaveConfig.ID, SortedMap<Trigger.ID, Trigger>> triggers = Collections
			.synchronizedSortedMap(new TreeMap<SlaveConfig.ID, SortedMap<Trigger.ID, Trigger>>());

	/** */
	private final SortedMap<SlaveConfig.ID, SortedMap<Trigger.ID, Job>> upcomingJobs = Collections
			.synchronizedSortedMap(new TreeMap<SlaveConfig.ID, SortedMap<Trigger.ID, Job>>());

	/** */
	private final TimerConfig config;

	private static Map<String, TimerAPI> TIMER_CACHE = new HashMap<>();

	public static TimerAPI getInstance(final String id)
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

	public static synchronized TimerAPI getInstance(final TimerConfig config)
	{
		TimerAPI result = TIMER_CACHE.get(config.id().getValue());
		if (result == null)
		{
			result = new TimerImpl();
			result.initialize(config);
			TIMER_CACHE.put(config.id().getValue(), result);
		}
		return result;
	}

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

	protected TimerConfig getConfig()
	{
		return this.config;
	}

	protected Set<SlaveStatus> getSlaveStatus()
	{
		if (this.slaves.isEmpty())
			return Collections.emptySet();

		final SortedSet<SlaveStatus> result = new TreeSet<>();
		synchronized (this.slaves)
		{
			for (SlaveConfig slave : this.slaves.values())
				result.add(getSlaveStatus(slave));
		}
		return result;
	}

	protected SlaveStatus getSlaveStatus(final SlaveConfig slave)
	{
		return SlaveStatus.Builder.forSlave(slave)
				.withTriggers(this.triggers.get(slave.id()).values())
				.withUpcomingJobs(this.upcomingJobs.get(slave.id()).values())
				.build();
	}

	@Override
	public TimerStatus getStatus()
	{
		return new TimerStatus.Builder().withTimer(this.config)
				.withSlaves(getSlaveStatus()).withClocks(this.clocks.values())
				.build();
	}

	@Override
	public void initialize(final TimerConfig config)
	{
		if (config.id() != null)
			getConfig().setProperty(TimeControl.ID_KEY,
					JsonUtil.stringify(config.id()));

		if (config.duration() != null)
			getConfig().setProperty(TimeControl.DURATION_KEY,
					JsonUtil.stringify(config.duration()));

		if (config.clock() != null)
			getConfig().setProperty(TimeControl.CLOCK_KEY,
					JsonUtil.stringify(config.clock()));

		if (config.offset() != null)
			getConfig().setProperty(TimeControl.OFFSET_KEY,
					JsonUtil.stringify(config.offset()));

		// resolution may be set to 'null' for continuous time scales
		getConfig().setProperty(TimeControl.RESOLUTION_KEY,
				JsonUtil.stringify(config.resolution()));
	}

	@Override
	public void updateSlaveConfig(final SlaveConfig slave)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSlave(final SlaveConfig.ID slaveId)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void updateClockConfig(final ClockConfig clock)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void updateTrigger(final Trigger trigger)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void removeTrigger(final Trigger.ID triggerId)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception
	{
		// final Server server = new
		// Server(Integer.valueOf(System.getenv("PORT")));
		// final ServletContextHandler context = new ServletContextHandler(
		// ServletContextHandler.SESSIONS);
		// context.setContextPath("/");
		// server.setHandler(context);
		// System.setProperty(RestServlet.APPLICATION_INIT_PARAM,
		// TimeControlMasterEcmaRestServlet.class.getName());
		// context.addServlet(new ServletHolder(new RestServlet()), "/ecma/*");
		// // TODO check if two REST servlets works
		// System.setProperty(RestServlet.APPLICATION_INIT_PARAM,
		// TimeControlMasterJsonRestServlet.class.getName());
		// context.addServlet(new ServletHolder(new RestServlet()), "/json/*");
		// server.start();
		// server.join();
	}
}
