/* $Id$
 * $URL$
 * 
 * Part of the EU project Inertia, see http://www.inertia-project.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2014 Almende B.V. 
 */
package com.almende.timecontrol.eve;

import static org.aeonbits.owner.util.Collections.entry;
import io.coala.util.JsonUtil;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentProxyFactory;
import com.almende.timecontrol.api.TimerAPI;
import com.almende.timecontrol.api.eve.EveTimedAPI;
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

/**
 * {@link SlaveAgent}
 * 
 * @date $Date$
 * @version $Revision$
 * @author <a href="mailto:gebruiker@almende.org">gebruiker</a>
 *
 */
public class SlaveAgent extends Agent implements EveTimedAPI, EveTimerAPI
{

	/** */
	public static final String SLAVE_CONFIG_KEY = "slave-agent-config";

	/** */
	public static final String MASTER_URI_KEY = "master-agent-uri";

	/** */
	private static final Logger LOG = LogManager.getLogger(SlaveAgent.class);

	/** the latest {@link SlaveStatus} received from the {@link #masterProxy} */
	private SlaveStatus status;

	/** */
	private Subject<ClockConfig, ClockConfig> timeUpdates = PublishSubject
			.create();

	/** */
	private Subject<Job, Job> jobUpdates = PublishSubject.create();

	/** */
	private EveTimerAPI masterProxy;

	private boolean initialized = false;

	/**
	 * @return
	 */
	public void initOnce()
	{
		if (this.initialized)
			return;

		final SlaveConfig config = SlaveConfig.Builder.fromJSON(
				getConfig().get(SLAVE_CONFIG_KEY)).build();

		this.status = SlaveStatus.Builder.forSlave(config).build();

		LOG.trace("Initialized! Status: " + this.status);
	}

	@Override
	protected void loadConfig()
	{
		super.loadConfig();
		initOnce();
	}

	/**
	 * @return
	 */
	protected EveTimerAPI getTimerProxy()
	{
		if (this.masterProxy == null)
		{
			final URI timeMasterAgentURI;
			if (getConfig().has(MASTER_URI_KEY))
				timeMasterAgentURI = URI.create(getConfig().get(MASTER_URI_KEY)
						.asText());
			else
				timeMasterAgentURI = URI.create("local:"
						+ this.status.slave().timerId());

			LOG.warn("Connecting to time control master at uri: "
					+ timeMasterAgentURI);
			this.masterProxy = AgentProxyFactory.genProxy(this,
					timeMasterAgentURI, EveTimerAPI.class);

			// send local configuration to (re)connect
			getTimerProxy().updateSlaveConfig(this.status.slave());
		}
		return this.masterProxy;
	}

	// @Override
	public SlaveStatus getSlaveStatus()
	{
		return this.status;
	}

	/**
	 * @return an {@link Observable} of the received {@link ClockConfig} time
	 *         updates, with a replay buffer of 1
	 */
	public Observable<ClockConfig> time()
	{
		return this.timeUpdates.replay(1);
	}

	/** @return an {@link Observable} of the received {@link Job} time updates */
	public Observable<Job> jobs()
	{
		return this.jobUpdates.asObservable();
	}

	@Override
	public void notify(final ClockConfig clock)
	{
		this.timeUpdates.onNext(clock);
	}

	@Override
	public void notify(final Job job)
	{
		this.jobUpdates.onNext(job);
	}

	@Override
	public TimerStatus getStatus()
	{
		return getTimerProxy().getStatus();
	}

	@Override
	public void destroy()
	{
		getTimerProxy().destroy();
	}

	@Override
	public void initialize(final TimerConfig config)
	{
		getTimerProxy().initialize(config);
	}

	@Override
	public void updateSlaveConfig(final SlaveConfig slave)
	{
		getTimerProxy().updateSlaveConfig(slave);
	}

	@Override
	public void removeSlave(final SlaveConfig.ID slaveId)
	{
		getTimerProxy().removeSlave(slaveId);
	}

	@Override
	public void updateClockConfig(final ClockConfig clock)
	{
		getTimerProxy().updateClockConfig(clock);
	}

	@Override
	public void removeClock(final ClockConfig.ID clockId)
	{
		getTimerProxy().removeClock(clockId);
	}

	@Override
	public void updateTrigger(final Trigger trigger)
	{
		getTimerProxy().updateTrigger(trigger);
	}

	@Override
	public void removeTrigger(final Trigger.ID triggerId)
	{
		getTimerProxy().removeTrigger(triggerId);
	}

	/** */
	public static SlaveAgent valueOf(final SlaveConfig config)
	{
		return valueOf(config.id().getValue(),
				entry(SLAVE_CONFIG_KEY, JsonUtil.toTree(config)));
	}

	/** */
	@SafeVarargs
	public static final SlaveAgent valueOf(final String id,
			final Map.Entry<String, ? extends JsonNode>... parameters)
	{
		return EveUtil.valueOf(id, SlaveAgent.class, parameters);
	}

	/** */
	private static final Map<String, SlaveAgent> INSTANCES = new TreeMap<>();

	/** */
	public static TimerAPI getInstance(final String id)
	{
		synchronized (INSTANCES)
		{
			final SlaveAgent result = INSTANCES.get(id);
			if (result == null)
				return getInstance(SlaveConfig.Builder.forID(id).build());
			return result;
		}
	}

	/** */
	public static SlaveAgent getInstance(final SlaveConfig config)
	{
		synchronized (INSTANCES)
		{
			SlaveAgent result = INSTANCES.get(config.id().getValue());
			if (result == null)
			{
				result = SlaveAgent.valueOf(config);
				INSTANCES.put(config.id().getValue(), result);
			}
			return result;
		}
	}

}
