package com.almende.timecontrol.eve;

import static org.aeonbits.owner.util.Collections.entry;
import static org.aeonbits.owner.util.Collections.map;
import io.coala.json.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.almende.timecontrol.api.eve.EveAgentConfig;
import com.almende.timecontrol.api.eve.EveTimerAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.Job;
import com.almende.timecontrol.entity.SlaveConfig;
import com.almende.timecontrol.entity.SlaveStatus;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.Trigger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
	protected void loadConfig(final boolean onBoot)
	{
		super.loadConfig(onBoot);

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

		if (!equals(current.duration(), config.duration()))
		{
			// TODO apply changes
		}

		if (!equals(current.resolution(), config.resolution()))
		{
			// TODO apply changes
		}

		if (!equals(current.clock(), config.clock()))
		{
			// TODO apply changes
		}

		if (!equals(current.offset(), config.offset()))
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
		@SuppressWarnings("unchecked")
		final EveAgentConfig cfg = ConfigFactory.create(
				EveAgentConfig.class,
				EveAgentConfig.DEFAULT_VALUES,
				map(entry(EveAgentConfig.AGENT_CLASS_KEY,
						TimerAgent.class.getName())));

		final InputStream is = cfg.agentConfigStream();
		if (is != null)
		{
			final Config config = YamlReader.load(is).expand();
			try
			{
				is.close();
			} catch (final IOException ignore)
			{
				// empty
			}

			for (final JsonNode agent : (ArrayNode) config.get("agents"))
			{
				final AgentConfig agentConfig = new AgentConfig(
						(ObjectNode) agent);
				if (parameters != null && parameters.length != 0)
					for (Map.Entry<String, ? extends JsonNode> param : parameters)
						agentConfig.set(param.getKey(), param.getValue());

				final JsonNode idNode = agent.get("id");
				if (idNode != null && !idNode.asText().equals(id))
					continue;

				final Agent result = new AgentBuilder().with(agentConfig)
						.build();
				LOG.info("Created agent {} from config at {}: {}", id,
						cfg.agentConfigUri(), agentConfig);
				return (TimerAgent) result;
			}
		}

		LOG.info("Using default config for timer agent: {}", id);
		final AgentConfig agentConfig = cfg.agentConfig();
		if (parameters != null && parameters.length != 0)
			for (Map.Entry<String, ? extends JsonNode> param : parameters)
				agentConfig.set(param.getKey(), param.getValue());
		final Agent result = new AgentBuilder().with(agentConfig).build();
		LOG.trace("Created agent {} from default config: {}", id, agentConfig);
		return (TimerAgent) result;
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
