package com.almende.timecontrol.eve;

import static org.aeonbits.owner.util.Collections.entry;
import io.coala.json.JsonUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.almende.eve.agent.Agent;
import com.almende.timecontrol.api.eve.EveTimerAPI;
import com.almende.timecontrol.api.eve.EveUtil;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.Job;
import com.almende.timecontrol.entity.SlaveConfig;
import com.almende.timecontrol.entity.SlaveStatus;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.Trigger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * {@link TimerAgentTest extends Agent}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class TimerAgent extends Agent implements EveTimerAPI
{

	/** */
	private static final Logger LOG = LogManager.getLogger(TimerAgent.class);

	/** */
	public static final String MASTER_CONFIG_KEY = "master-agent-config";

	/** */
	private TimerConfig config;

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

	@Override
	public void init(final ObjectNode params, final boolean onBoot)
	{
		super.init(params, onBoot);

		this.config = TimerConfig.Builder.forID(getId()).build();
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

	protected <T> boolean equals(final T o1, final T o2)
	{
		if ((o1 == null && o2 != null) || (o1 != null && !o1.equals(o2)))
		{
			LOG.trace(String.format("New value: %s -> %s", o1, o2));
			return false;
		}
		return true;
	}

	@Override
	public void initialize(final TimerConfig config)
	{
		final TimerConfig current = this.config;

		if (current == null || !equals(current.duration(), config.duration()))
		{
			// TODO apply changes
		}

		if (current == null
				|| !equals(current.resolution(), config.resolution()))
		{
			// TODO apply changes
		}

		if (current == null || !equals(current.clock(), config.clock()))
		{
			// TODO apply changes
		}

		if (current == null || !equals(current.offset(), config.offset()))
		{
			// TODO apply changes
		}
	}

	@Override
	public void destroy()
	{
		// TODO apply changes

	}

	@Override
	public void updateSlaveConfig(final SlaveConfig slave)
	{
		// TODO apply changes

	}

	@Override
	public void removeSlave(final SlaveConfig.ID slaveId)
	{
		// TODO apply changes

	}

	@Override
	public void updateClockConfig(final ClockConfig clock)
	{
		// TODO apply changes

	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		// TODO apply changes

	}

	@Override
	public void updateTrigger(final Trigger trigger)
	{
		// TODO apply changes

	}

	@Override
	public void removeTrigger(final Trigger.ID triggerId)
	{
		// TODO apply changes

	}

	/** */
	public static TimerAgent valueOf(final TimerConfig config)
	{
		return valueOf(config.id().getValue(),
				entry(TimerAgent.MASTER_CONFIG_KEY, JsonUtil.toTree(config)));
	}

	/** */
	@SafeVarargs
	public static TimerAgent valueOf(final String id,
			final Map.Entry<String, ? extends JsonNode>... parameters)
	{
		return EveUtil.valueOf(id, TimerAgent.class, parameters);
	}

	/** */
	private static final Map<String, TimerAgent> INSTANCES = new TreeMap<>();

	/** */
	public static TimerAgent getInstance(final String timerID)
	{
		synchronized (INSTANCES)
		{
			final TimerAgent result = INSTANCES.get(timerID);
			if (result == null)
				return getInstance(TimerConfig.Builder.forID(timerID).build());
			return result;
		}
	}

	/** */
	public static TimerAgent getInstance(final TimerConfig config)
	{
		synchronized (INSTANCES)
		{
			TimerAgent result = INSTANCES.get(config.id());
			if (result == null)
			{
				LOG.trace("Create master with config: {}", config);
				result = TimerAgent.valueOf(config);
				INSTANCES.put(config.id().getValue(), result);
			}
			return result;
		}
	}

}
