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
package com.almende.timecontrol;

import io.coala.util.JsonUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.measure.unit.Unit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Subscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockEvent;
import com.almende.timecontrol.entity.TriggerConfig;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.entity.ClockConfig.Status;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Instant;
import com.almende.timecontrol.time.TriggerPattern;

/**
 * {@link ClockTuple}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public abstract class ClockTuple
{

	/** */
	private static final Logger LOG = LogManager.getLogger(ClockTuple.class);

	/** */
	protected final Subject<ClockEvent, ClockEvent> events = PublishSubject
			.create();

	/** */
	protected final Subject<PropertyChangeEvent, PropertyChangeEvent> changes = PublishSubject
			.create();

	/** */
	protected final SortedMap<TriggerConfig.ID, TriggerTuple> triggers = Collections
			.synchronizedSortedMap(new TreeMap<TriggerConfig.ID, TriggerTuple>());

	protected final ReentrantReadWriteLock semaphore = new ReentrantReadWriteLock();

	/** */
	protected ClockConfig config;

	/** cached from ClockConfig to increase Owner deserialization performance */
	protected String configID;

	/** cached from ClockConfig to increase Owner deserialization performance */
	protected volatile Duration until;

	/** cached from ClockConfig to increase Owner deserialization performance */
	protected volatile Status status;

	/** cached from ClockConfig to increase Owner deserialization performance */
	protected volatile Double millis;

	/** cached from ClockConfig to increase Owner deserialization performance */
	protected volatile Number drag;

	/** required for wall-clock delay calculations */
	protected volatile long dragWallclockOffset;

	/** required for wall-clock delay calculations */
	protected volatile Double dragSimclockOffset;

	/**
	 * {@link ClockTuple} constructor
	 * 
	 * @param config
	 */
	protected ClockTuple()
	{
	}

	/**
	 * {@link ClockTuple} constructor
	 * 
	 * @param config
	 */
	protected ClockTuple reset(final ClockConfig config)
	{
		this.config = config;
		this.configID = config.id().getValue();
		this.config.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(final PropertyChangeEvent evt)
			{
				LOG.trace("Handling clock config property change: {}",
						JsonUtil.toTree(evt));
				if (evt.getNewValue() == evt.getOldValue()) // same or both null
					return;
				if (evt.getNewValue() != null
						&& evt.getNewValue().equals(evt.getOldValue()))
					return;

				onChange(evt.getPropertyName(), evt.getNewValue());
			}
		});
		return this;
	}

	/**
	 * @param key
	 * @param newValue
	 */
	protected void onChange(final String key, final Object newValue)
	{
		LOG.trace("Handling config change: {} = {}", key, newValue);
		// synchronized (this.config)
		{
			final ClockEvent event = ClockEvent.Builder.fromClockConfig(
					this.config).build();
			LOG.trace("Publishing current clock config: {} [t={}] as event: {}",
					this.config, event.time(), event);
			this.events.onNext(event);

			if (key.equals(TimeControl.UNTIL_KEY))
			{
				this.until = this.config.until();
				LOG.trace("{} now until {}", this.configID, this.until);
			} else if (key.equals(TimeControl.DRAG_KEY))
			{
				this.drag = this.config.drag().doubleValue(Unit.ONE);
				LOG.trace("{} drag now {}", this.configID, this.drag);
			} else if (key.equals(TimeControl.STATUS_KEY))
			{
				final Status oldStatus = this.status;
				this.status = this.config.status();
				try
				{
					if (this.status != Status.RUNNING)
						stop();
					else if (oldStatus != Status.RUNNING)
						start();
				} catch (final Throwable e)
				{
					LOG.error("Problem manipulating simulator", e);
				}
			}
		}
	}

	/**
	 * @param status the new clock status
	 */
	protected void setStatus(final Status status)
	{
		// synchronized (this.config)
		{
			if (this.status == Status.COMPLETED || this.status == Status.FAILED
					|| this.status == status)
			{
				LOG.warn("{} ignoring status update {} => {}", this.configID,
						this.status, status);
				return;
			}
			if (status == Status.RUNNING)
			{
				this.dragWallclockOffset = System.currentTimeMillis();
				this.dragSimclockOffset = this.millis;
			}
			this.config.setProperty(TimeControl.STATUS_KEY, status.name());
		}
	}

	/**
	 * @param the new time in milliseconds
	 */
	protected void setTime(final double millis)
	{
		// synchronized (this.config)
		{
			if (this.status != Status.RUNNING)
				LOG.warn("{} ignoring time update {} with status {}",
						this.configID, millis, this.status);
			else
			{
				this.millis = millis;
				this.config.setProperty(TimeControl.TIME_KEY,
						Double.toString(millis));
			}
			if (this.until != null && millis >= this.until.toMillisLong())
				setStatus(Status.COMPLETED);
		}
	}

	/** clean up */
	protected void destroy()
	{
		// synchronized (this.config)
		{
			this.events.onCompleted();
			this.triggers.clear();
		}
	}

	/**
	 * SCHEDULABLE
	 * 
	 * t_wall0 t_sim0 delay = t_wall0 + (t_sim - t_sim0) * drag - t_wall
	 * 
	 * @param pattern
	 * @param sub
	 */
	protected void onNext(final TriggerPattern pattern, final boolean isLast,
			final Subscriber<? super TriggerEvent> sub)
	{
		final long delayMS;
		final TriggerEvent event;
		// synchronized (this.config)
		{
			delayMS = this.drag == null || this.drag.doubleValue() <= 0 ? 0L
					: (long) (this.dragWallclockOffset + (this.millis - this.dragSimclockOffset)
							* this.drag.doubleValue())
							- System.currentTimeMillis();
			event = TriggerEvent.Builder
					.fromTime(Duration.valueOf(this.millis))
					.withLastCall(isLast).build();
		}
		LOG.trace("[t={}ms] {} triggering for pattern {}, drag: {}ms (actual)",
				this.millis, this.configID, pattern, delayMS);
		if (delayMS > 0)
		{
			try
			{
				Thread.sleep(delayMS);
			} catch (final InterruptedException e)
			{
				LOG.warn("Unexpected interrupt", e);
			}
		}
		sub.onNext(event);
	}

	/**
	 * @param pattern the original {@link TriggerPattern} describing all
	 *            {@link Instant instants}
	 * @param time the {@link Instant} in the {@link TriggerPattern} to schedule
	 * @param isLast {@code true} iff {@code time} is the last {@link Instant}
	 *            in the {@link TriggerPattern}
	 * @param sub the {@link Subscriber} to notify with a {@link TriggerEvent
	 *            events} when specified {@link Instant instant} occurs
	 */
	protected abstract void schedule(final TriggerPattern pattern,
			final Instant time, final boolean isLast,
			final Subscriber<? super TriggerEvent> sub);

	protected abstract void start() throws Exception;

	protected abstract void stop() throws Exception;

}