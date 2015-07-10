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
package com.almende.timecontrol.dsol;

import java.rmi.RemoteException;
import java.util.Calendar;

import javax.inject.Provider;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.eventlists.RedBlackTree;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeCalendarDouble;
import nl.tudelft.simulation.dsol.simtime.UnitTimeDouble;
import nl.tudelft.simulation.dsol.simulators.DEVDESSSimulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Subscriber;

import com.almende.timecontrol.ClockTuple;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockConfig.Status;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Instant;
import com.almende.timecontrol.time.TriggerPattern;

/**
 * {@link DsolClockTuple}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class DsolClockTuple extends ClockTuple
{

	/** */
	private static final Logger LOG = LogManager
			.getLogger(DsolClockTuple.class);

	/** */
	private static final String ON_NEXT = "onNext";

	/** */
	protected DEVDESSSimulator<Calendar, UnitTimeDouble, SimTimeCalendarDouble> scheduler;

	/**
	 * {@link DsolClockTuple} constructor
	 * 
	 * @param config
	 */
	public DsolClockTuple()
	{
		// empty
	}

	@Override
	public ClockTuple reset(final ClockConfig config)
	{
		super.reset(config);
		this.until = config.until();

		final Duration time = config.time();
		this.scheduler = new DEVDESSSimulator.CalendarDouble(DsolUtil.toDSOL(
				time == null ? Duration.ZERO : time, UnitTimeDouble.class))
		/*{

			*//** */
		/*
		private static final long serialVersionUID = 1L;

		@Override
		public void scheduleEvent(
		final SimEventInterface<SimTimeCalendarDouble> event)
		throws SimRuntimeException
		{
		LOG.trace("Scheduling {} for target {}", event.getClass()
			.getSimpleName(),
			((SimEvent<SimTimeCalendarDouble>) event).getTarget()
					.getClass().getName());
		super.scheduleEvent(event);
		}

		@Override
		protected synchronized EventInterface fireEvent(
		final EventListenerInterface listener,
		final EventInterface event) throws RemoteException
		{
		LOG.trace("Notifying listener {} of event {}", listener
			.getClass().getName(), event.getType());
		return super.fireEvent(listener, event);
		}

		}*/;
		DsolUtil.initialize(this.scheduler, this.config);
		LOG.trace("Initialized sim, time = {}",
				this.scheduler.getSimulatorTime());

		this.scheduler.addListener(new EventListenerInterface()
		{
			@Override
			public void notify(final EventInterface event)
					throws RemoteException
			{
				setTime(scheduler.getSimulatorTime().getTimeMsec());
				LOG.info("[t={}] {}", scheduler.getSimulatorTime()
						.getTimeMsec(), event.getType());
			}
		}, SimulatorInterface.TIME_CHANGED_EVENT);

		this.scheduler.addListener(new EventListenerInterface()
		{
			@Override
			public void notify(final EventInterface event)
					throws RemoteException
			{
				LOG.info("[t={}] {}", scheduler.getSimulatorTime()
						.getTimeMsec(), event.getType());
				setStatus(Status.RUNNING);
			}
		}, SimulatorInterface.START_EVENT);

		this.scheduler.addListener(new EventListenerInterface()
		{
			@Override
			public void notify(final EventInterface event)
					throws RemoteException
			{
				LOG.info("[t={}] {}", scheduler.getSimulatorTime()
						.getTimeMsec(), event.getType());
				setStatus(Status.WAITING);
			}
		}, SimulatorInterface.STOP_EVENT);

		this.scheduler.addListener(new EventListenerInterface()
		{
			@Override
			public void notify(final EventInterface event)
					throws RemoteException
			{
				LOG.info("[t={}] {}", scheduler.getSimulatorTime()
						.getTimeMsec(), event.getType());
				setStatus(Status.COMPLETED);
			}
		}, SimulatorInterface.END_OF_REPLICATION_EVENT);
		/*this.sim.addListener(new EventListenerInterface()
		{
			@Override
			public void notify(final EventInterface event)
					throws RemoteException
			{
				setStatus(Status.COMPLETED);
			}
		}, DEVSSimulatorInterface.EVENTLIST_CHANGED_EVENT);*/
		return this;
	}

	/**
	 * @param time
	 * @param sub
	 */
	@Override
	public void schedule(final TriggerPattern pattern, final Instant time,
			final boolean isLast, final Subscriber<? super TriggerEvent> sub)
	{
		// TODO group triggers for same Instant/millis AND "recursiveness" level
		// into same DSOL event
		try
		{
			final SimTimeCalendarDouble cal = DsolUtil.toDSOL(time,
					SimTimeCalendarDouble.class);
			LOG.trace("{} scheduling pattern {} instant {} => {}",
					this.configID, pattern, time, cal);
			this.scheduler.scheduleEvent(DsolUtil.toDSOL(cal,
					SimEventInterface.NORMAL_PRIORITY, this, this, ON_NEXT,
					pattern, sub));
		} catch (final Throwable t)
		{
			sub.onError(t);
		}
	}

	/** clean up */
	@Override
	protected void destroy()
	{
		super.destroy();
		synchronized (this.config)
		{
			if (this.scheduler != null)
			{
				this.scheduler.stop();
				this.scheduler
						.setEventList(new RedBlackTree<SimTimeCalendarDouble>());
			}
		}
	}

	/**
	 * @throws SimRuntimeException
	 * @see com.almende.timecontrol.ClockTuple#start()
	 */
	@Override
	protected void start() throws SimRuntimeException
	{
		if (!this.scheduler.isRunning())
			this.scheduler.start();

		LOG.trace("{} status now: {}, DSOL running: {}", this.configID,
				this.status, this.scheduler.isRunning());
	}

	/** @see com.almende.timecontrol.ClockTuple#stop() */
	@Override
	protected void stop()
	{
		this.scheduler.stop();

		LOG.trace("{} status now: {}, DSOL running: {}", this.configID,
				this.status, this.scheduler.isRunning());
	}

	public static class DefaultProvider implements Provider<ClockTuple>
	{
		@Override
		public ClockTuple get()
		{
			return new DsolClockTuple();
		}
	}
}