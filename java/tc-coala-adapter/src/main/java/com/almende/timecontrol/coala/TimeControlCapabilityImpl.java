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
import io.coala.exception.CoalaExceptionFactory;
import io.coala.model.ModelComponent;
import io.coala.process.Job;
import io.coala.random.RandomNumberStream;
import io.coala.random.RandomNumberStreamID;
import io.coala.time.ClockID;
import io.coala.time.SimTime;
import io.coala.time.SimTimeFactory;
import io.coala.time.TimeUnit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.aeonbits.owner.ConfigFactory;
import org.apache.log4j.Logger;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.almende.timecontrol.api.eve.EveAgentAPI;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockConfig.Status;
import com.almende.timecontrol.entity.ClockEvent;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TriggerConfig;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.eve.TimeManagerClientAgent;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.TriggerPattern;

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
	private static final Logger LOG = Logger
			.getLogger(TimeControlCapabilityImpl.class);

	/** */
	private final TimeManagerClientAgent timeManagerClient;

	/** */
	private final ReplicationConfig config;

	/** */
	private final SimTimeFactory newTime;

	/** */
	private final TimeUnit baseTimeUnit;

	/** */
	private final Subject<ClockStatusUpdate, ClockStatusUpdate> statusUpdates = PublishSubject
			.create();

	/** */
	private final Subject<SimTime, SimTime> timeUpdates = PublishSubject
			.create();

	/** */
	private final TimerConfig timerConfig;

	/** */
	private volatile SimTime time = SimTime.ZERO;

	/** */
	private volatile ClockEvent clock = null;

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
		this.newTime = this.config.newTime();
		this.baseTimeUnit = this.config.getBaseTimeUnit();

		// ConfigFactory.create(SlaveConfig.class);
		// final SlaveConfig slaveConfig =
		// ConfigFactory.create(SlaveConfig.class);
		// slaveConfig.setProperty(TimeControl.ID_KEY,
		// this.config.getClockName());
		// slaveConfig.setProperty(TimeControl.TIMER_ID_KEY,
		// this.config.getModelName());

		/*SlaveConfig.Builder
				.forID(getID().getOwnerID().getValue())
				.withTimerId(
						TimerConfig.ID.valueOf(this.config.getClockID()
								.getValue()))
				// TODO set slave values
				.build();*/

		final String clientID = binder.getID() + "-timer";// this.config.getClockName();
		final String timerID = this.config.getModelName();
		this.timeManagerClient = TimeManagerClientAgent.getInstance(timerID,
				clientID);
		this.timeManagerClient.events().subscribe(
				new Observer<EveAgentAPI.AgentEvent>()
				{
					@Override
					public void onCompleted()
					{
						LOG.warn("Client agent events completed");
					}

					@Override
					public void onError(final Throwable e)
					{
						LOG.error("Problem observing proxy events", e);
					}

					@Override
					public void onNext(final EveAgentAPI.AgentEvent event)
					{
						LOG.info("Proxy status: " + event);
					}
				});
		final CountDownLatch clockReadyLatch = new CountDownLatch(1);
		this.timeManagerClient.observeClock(ClockConfig.ID.valueOf(clientID))
				.subscribe(new Observer<ClockEvent>()
				{
					@Override
					public void onCompleted()
					{
						LOG.warn("Clock status completed");
					}

					@Override
					public void onError(final Throwable e)
					{
						LOG.error("Problem reading clock status", e);
					}

					@Override
					public void onNext(final ClockEvent update)
					{
						setClock(update);
						clockReadyLatch.countDown();
					}
				});
		// may be redundant
		final TimerConfig config = ConfigFactory.create(TimerConfig.class);
		LOG.trace("Initializing timer with config: " + config);
		// timer.setProperty(TimeControl.ID_KEY, timerID);
		this.timeManagerClient.setTimerConfig(config);
		this.timerConfig = this.timeManagerClient.getTimerConfig();
		LOG.trace("Initialized timer with config: " + this.timerConfig);
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

	protected void setClock(final ClockEvent clock)
	{
		synchronized (this.statusUpdates)
		{
			this.clock = clock;
		}
		setTime(clock.time());
	}

	protected synchronized void setTime(final Duration time)
	{
		setTime(this.newTime.create(time.nanos(), TimeUnit.NANOS).toUnit(
				this.baseTimeUnit));
	}

	protected synchronized void setTime(final SimTime time)
	{
		if (time.isBefore(this.time))
			throw CoalaExceptionFactory.VALUE_NOT_ALLOWED.createRuntime("time",
					time, "can't rewind time, currently: " + this.time);
		this.time = time;
		this.timeUpdates.onNext(time);
	}

	@Override
	public synchronized SimTime getTime()
	{
		return this.time;
	}

	private final SortedMap<TriggerConfig.ID, Job<?>> PENDING_JOBS = Collections
			.synchronizedSortedMap(new TreeMap<TriggerConfig.ID, Job<?>>());

	@Override
	public void schedule(final Job<?> job,
			final io.coala.time.Trigger<?> trigger)
	{
		synchronized (PENDING_JOBS)
		{
			final String id = job.getID().toString();
			// FIXME translate trigger into representative iCal or Cron rule
			final SimTime instant = this.newTime
					.create(trigger.getStartTime().toUnit(this.baseTimeUnit)
							.doubleValue(), this.baseTimeUnit);
			final TriggerPattern rule = /*new RecurrenceRule(trigger, trigger
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
										}));*/
			new TriggerPattern(instant.getIsoTime().getTime());
			final TriggerConfig trig = TriggerConfig.Builder.fromID(id)
					.withPattern(rule).build();
			PENDING_JOBS.put(trig.id(), job);
			this.timeManagerClient.registerTrigger(rule).subscribe(
					this.jobObserver);
		}
	}

	private final Observer<com.almende.timecontrol.entity.TriggerEvent> jobObserver = new Observer<com.almende.timecontrol.entity.TriggerEvent>()
	{

		@Override
		public void onCompleted()
		{
			LOG.trace("No more jobs, simulation ended?");
		}

		@Override
		public void onError(final Throwable e)
		{
			LOG.error("Problem observing triggered jobs", e);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onNext(final TriggerEvent job)
		{
			setTime(job.time());
			synchronized (PENDING_JOBS)
			{
				final Job<?> todo = job.lastCall() ? PENDING_JOBS.remove(job
						.triggerId()) : PENDING_JOBS.get(job.triggerId());
				if (todo == null)
					LOG.warn("trigger no longer available: " + job.triggerId());
				else if (!Callable.class.isAssignableFrom(todo.getClass()))
					throw new NullPointerException(
							"UNEXPECTED: job not Callable: " + job.getClass());
				else
					try
					{
						((Callable<Void>) todo).call();
					} catch (final Exception e)
					{
						LOG.error("Problem executing job: " + todo);
					}
			}
		}
	};

	@Override
	public boolean unschedule(final Job<?> job)
	{
		synchronized (PENDING_JOBS)
		{
			final TriggerConfig.ID id = TriggerConfig.ID.valueOf(job.getID()
					.toString());
			// this.timeManagerClient.unregisterTrigger(id);
			PENDING_JOBS.remove(id);
			return true;
		}
	}

	@Override
	public void start()
	{
		this.timeManagerClient.updateClock(ClockConfig.Builder
				.forID(this.config.getClockName()).withStatus(Status.RUNNING)
				.build());
	}

	@Override
	public void pause()
	{
		this.timeManagerClient.updateClock(ClockConfig.Builder
				.forID(this.config.getClockName()).withStatus(Status.WAITING)
				.build());
	}

	@Override
	public boolean isRunning()
	{
		synchronized (this.statusUpdates)
		{
			return this.clock.status() == Status.RUNNING;
		}
	}

	@Override
	public synchronized boolean isComplete()
	{
		synchronized (this.statusUpdates)
		{
			return this.clock.status() == Status.COMPLETED;
		}
	}

	@Override
	public SimTime getVirtualOffset()
	{
		throw CoalaExceptionFactory.OPERATION_FAILED
				.createRuntime("NOT IMPLEMENTED");
	}

	@Override
	public SimTime toActualTime(final SimTime virtualTime)
	{
		throw CoalaExceptionFactory.OPERATION_FAILED
				.createRuntime("NOT IMPLEMENTED");
	}

	@Override
	public SimTime getActualOffset()
	{
		throw CoalaExceptionFactory.OPERATION_FAILED
				.createRuntime("NOT IMPLEMENTED");
	}

	@Override
	public SimTime toVirtualTime(final SimTime actualTime)
	{
		throw CoalaExceptionFactory.OPERATION_FAILED
				.createRuntime("NOT IMPLEMENTED");
	}

	@Override
	public Number getApproximateSpeedFactor()
	{
		throw CoalaExceptionFactory.OPERATION_FAILED
				.createRuntime("NOT IMPLEMENTED");
	}

}
