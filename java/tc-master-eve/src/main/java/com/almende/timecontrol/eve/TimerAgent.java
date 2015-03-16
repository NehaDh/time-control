package com.almende.timecontrol.eve;

import static org.aeonbits.owner.util.Collections.entry;
import io.coala.util.JsonUtil;

import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.almende.eve.agent.Agent;
import com.almende.timecontrol.TimerImpl;
import com.almende.timecontrol.api.TimerAPI;
import com.almende.timecontrol.api.eve.EveTimerAPI;
import com.almende.timecontrol.api.eve.EveUtil;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.SlaveConfig;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.entity.Trigger;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link TimerAgentTest} wraps a {@link TimerImpl} instance with the JSON-RPC
 * capability provided by an Eve {@link Agent}
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

	/**
	 * Helper-method
	 * 
	 * @return the {@link TimerAPI} implementation instance wrapped by this
	 *         {@link TimerAgent}
	 */
	protected TimerAPI getTimer()
	{
		return TimerImpl.getInstance(getId());
	}

	@Override
	public TimerStatus getStatus()
	{
		return getTimer().getStatus();
	}

	@Override
	public void initialize(final TimerConfig config)
	{
		getTimer().initialize(config);
	}

	@Override
	public void destroy()
	{
		getTimer().destroy();
	}

	@Override
	public void updateSlaveConfig(final SlaveConfig slave)
	{
		getTimer().updateSlaveConfig(slave);
	}

	@Override
	public void removeSlave(final SlaveConfig.ID slaveId)
	{
		getTimer().removeSlave(slaveId);
	}

	@Override
	public void updateClockConfig(final ClockConfig clock)
	{
		getTimer().updateClockConfig(clock);
	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		getTimer().removeClock(clockId);
	}

	@Override
	public void updateTrigger(final Trigger trigger)
	{
		getTimer().updateTrigger(trigger);
	}

	@Override
	public void removeTrigger(final Trigger.ID triggerId)
	{
		getTimer().removeTrigger(triggerId);
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
