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
package com.almende.timecontrol.coala;

import io.coala.agent.AgentID;
import io.coala.bind.Binder;
import io.coala.capability.BasicCapability;
import io.coala.capability.CapabilityID;
import io.coala.capability.plan.ClockStatusUpdate;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.capability.replicate.ReplicationConfig;
import io.coala.config.CoalaProperty;
import io.coala.error.ExceptionBuilder;
import io.coala.model.ModelComponent;
import io.coala.process.Job;
import io.coala.random.RandomNumberStream;
import io.coala.random.RandomNumberStreamID;
import io.coala.time.ClockID;
import io.coala.time.SimTime;
import io.coala.time.TimeUnit;

import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.almende.timecontrol.entity.SlaveConfig;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.Trigger;
import com.almende.timecontrol.eve.SlaveAgent;
import com.almende.timecontrol.time.Instant;
import com.almende.timecontrol.time.RecurrenceRule;

/**
 * {@link TimeControlCapabilityImpl}
 * 
 * @date $Date$
 * @version $Revision$
 * @author <a href="mailto:gebruiker@almende.org">gebruiker</a>
 *
 */
public class TimeControlCapabilityImpl extends BasicCapability implements
		ReplicatingCapability, ModelComponent<CapabilityID>
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private final SlaveAgent slave;

	/** */
	private final ReplicationConfig config;

	/** */
	private final Subject<ClockStatusUpdate, ClockStatusUpdate> statusUpdates = PublishSubject
			.create();

	/** */
	private final Subject<SimTime, SimTime> timeUpdates = PublishSubject
			.create();

	/** */
	private volatile SimTime time;
	
	/** */
	private static final Logger LOG = LogManager
			.getLogger(TimeControlCapabilityImpl.class);

	/**
	 * {@link TimeControlCapabilityImpl} constructor
	 * 
	 * @param binder
	 */
	@Inject
	protected TimeControlCapabilityImpl(final Binder binder)
	{
		super(binder);
		this.config = getBinder().inject(ReplicationConfig.class);
		final SlaveConfig slaveConfig = SlaveConfig.Builder
				.forID(getID().getOwnerID().getValue())
				.withTimerId(
						TimerConfig.ID.valueOf(this.config.getClockID()
								.getValue()))
				// TODO set slave values
				.build();

		LOG.trace("class path: "+Arrays.asList(
		((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs()).toString().replace(", ", ",\r\n"));
		this.slave = SlaveAgent.getInstance(slaveConfig);
	}

	/** */
	public static final RandomNumberStreamID MAIN_RNG_ID = new RandomNumberStreamID(
			"__MAIN__");

	/** */
	private final Map<RandomNumberStreamID, RandomNumberStream> rngCache = new HashMap<>();

	@Override
	public RandomNumberStream getRNG()
	{
		return getRNG(MAIN_RNG_ID);
	}

	@Override
	public RandomNumberStream getRNG(final RandomNumberStreamID streamID)
	{
		synchronized (this.rngCache)
		{
			RandomNumberStream result = this.rngCache.get(streamID);
			if (result == null)
			{
				result = getBinder().inject(RandomNumberStream.Factory.class)
						.create(streamID,
								CoalaProperty.randomSeed.value().getLong());
				this.rngCache.put(streamID, result);
			}
			return result;
		}
	}

	@Override
	public ClockID getClockID()
	{
		return this.config.getClockID();
	}

	@Override
	public AgentID getOwnerID()
	{
		return getBinder().getID();
	}

	@Override
	public Observable<ClockStatusUpdate> getStatusUpdates()
	{
		return this.statusUpdates.asObservable();
	}

	@Override
	public Observable<SimTime> getTimeUpdates()
	{
		return this.timeUpdates.asObservable();
	}

	protected synchronized void setTime(final SimTime time)
	{
		this.time = time;
		this.timeUpdates.onNext(time);
	}

	@Override
	public synchronized SimTime getTime()
	{
		return this.time;
	}

	private final SortedMap<String, Job<?>> PENDING_JOBS = Collections
			.synchronizedSortedMap(new TreeMap<String, Job<?>>());

	@Override
	public void schedule(final Job<?> job,
			final io.coala.time.Trigger<?> trigger)
	{
		synchronized (PENDING_JOBS)
		{
			final String id = job.getID().toString();
			PENDING_JOBS.put(id, job);
			final RecurrenceRule rule = new RecurrenceRule(trigger, trigger
					.getInstants().map(
							new Func1<io.coala.time.Instant<?>, Instant>()
							{
								@Override
								public Instant call(
										final io.coala.time.Instant<?> t)
								{
									return Instant.valueOf(TimeUnit.MILLIS
											.convertFrom(t).longValue());
								}
							}));
			final Trigger trig = Trigger.Builder.fromID(id)
					.withRecurrence(rule).build();
			this.slave.updateTrigger(trig);
		}
	}

	@Override
	public boolean unschedule(final Job<?> job)
	{
		synchronized (PENDING_JOBS)
		{
			final String id = job.getID().toString();
			this.slave.removeTrigger(Trigger.ID.valueOf(id));
			PENDING_JOBS.remove(id);
			return true; // FIXME get result from slave's proxy
		}
	}

	@Override
	public void start()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRunning()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isComplete()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SimTime getVirtualOffset()
	{
		throw ExceptionBuilder.unchecked("NOT IMPLEMENTED").build();
	}

	@Override
	public SimTime toActualTime(final SimTime virtualTime)
	{
		throw ExceptionBuilder.unchecked("NOT IMPLEMENTED").build();
	}

	@Override
	public SimTime getActualOffset()
	{
		throw ExceptionBuilder.unchecked("NOT IMPLEMENTED").build();
	}

	@Override
	public SimTime toVirtualTime(final SimTime actualTime)
	{
		throw ExceptionBuilder.unchecked("NOT IMPLEMENTED").build();
	}

	@Override
	public Number getApproximateSpeedFactor()
	{
		throw ExceptionBuilder.unchecked("NOT IMPLEMENTED").build();
	}

}
