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
package com.almende.timecontrol.rx;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Subscriber;

import com.almende.timecontrol.ClockTuple;
import com.almende.timecontrol.TimeControl;
import com.almende.timecontrol.TriggerTuple;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockConfig.Status;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Instant;
import com.almende.timecontrol.time.TriggerPattern;

/**
 * {@link RxClock}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class RxClock extends ClockTuple implements Runnable
{

	/** */
	private static final Logger LOG = LogManager.getLogger(RxClock.class);

	protected final ScheduledExecutorService worker = Executors
			.newSingleThreadScheduledExecutor();

	protected final NavigableMap<Duration, Map<TriggerTuple, Boolean>> pending = new ConcurrentSkipListMap<>();

	protected Instant offset;

	protected double untilMS;

	protected volatile Duration now = null;

	protected volatile boolean continuing = false;

	/**
	 * {@link RxClock} constructor
	 * 
	 * @param config
	 */
	public RxClock()
	{
		setTime(Duration.ZERO);
	}

	@Override
	public ClockTuple reset(final ClockConfig config)
	{
		super.reset(config);

		this.until = config.until();
		this.untilMS = this.until == null ? Double.MAX_VALUE : this.until
				.getValue().doubleValue(TimeControl.MILLIS);
		this.offset = config.offset();
		final Duration now = config.time();
		setTime(now == null ? Duration.ZERO : now);
		return this;
	}

	@Override
	public void run()
	{
		try
		{
			LOG.trace("Simulator is {}, t={} < end={}, #pending={}, #locks={}",
					this.continuing ? "STARTING" : "<?>", this.millis,
					this.untilMS, this.pending.size(),
					this.semaphore.getReadLockCount());

			while (this.continuing && !this.pending.isEmpty()
					&& (this.millis == null || this.millis < this.untilMS))
			{
				this.semaphore.writeLock().lock();
				setTime(this.pending.firstKey());
				final Map<TriggerTuple, Boolean> next = this.pending
						.remove(this.now);
				if (next == null || next.isEmpty())
				{
					LOG.trace("No triggers remaining for time: {}", this.now);
					return;
				} else
					LOG.trace("{} triggers for time: {}", next.size(), this.now);
				for (Map.Entry<TriggerTuple, Boolean> entry : next.entrySet())
				{
					try
					{
						entry.getKey().onNext(this.now, entry.getValue());
					} catch (final Throwable e)
					{
						entry.getKey().onError(e);
					}
				}
				this.semaphore.writeLock().unlock();
			}
			LOG.trace("Simulator is {}, t_last={}, end={}, #pending={}",
					this.continuing ? "COMPLETED" : "WAITING", this.now,
					this.until, this.pending.size());
			// this.semaphore.writeLock().lock();
			if (this.until != null)
				setTime(this.until);
			// else
			// setTime(this.untilMS);
			if (this.continuing)// this.millis == null || this.millis <
								// this.untilMS)
				setStatus(Status.COMPLETED);
			else
				setStatus(Status.WAITING);
			this.continuing = false;
			// this.semaphore.writeLock().unlock();
		} catch (final Throwable e)
		{
			setStatus(Status.FAILED);
			e.printStackTrace();
		}
	}

	/**
	 * @param the new time in milliseconds
	 */
	protected void setTime(final Duration time)
	{
		this.now = time;
		if (this.status != Status.RUNNING)
			LOG.warn("{} ignoring time update {} with status {}",
					this.configID, millis, this.status);
		else
		{
			this.millis = time.getValue().doubleValue(TimeControl.MILLIS);
			this.config.setProperty(TimeControl.TIME_KEY, time.toString());
		}
		if (this.until != null && millis >= this.until.toMillisLong())
			setStatus(Status.COMPLETED);
	}

	/**
	 * @param instant
	 * @param sub
	 */
	@Override
	public void schedule(final TriggerPattern pattern, final Instant instant,
			final boolean isLast, final Subscriber<? super TriggerEvent> sub)
	{
		LOG.trace("Scheduling instant {}, last={}, locks={}", instant, isLast,
				this.semaphore.getReadLockCount());
		final Duration absTime = instant.toDuration(this.offset);
		// this.semaphore.writeLock().lock();
		Map<TriggerTuple, Boolean> triggers = this.pending.get(absTime);
		if (triggers == null)
		{
			triggers = new HashMap<>();
			this.pending.put(absTime, triggers);
		}
		triggers.put(new TriggerTuple(pattern, sub), isLast);
		LOG.trace("Registered t={} ({}) for {}", absTime, instant, pattern);
		// this.semaphore.writeLock().unlock();
	}

	@Override
	protected void start() throws IllegalStateException
	{
		if (this.continuing)
			throw new IllegalStateException("Already started!");

		this.continuing = true;
		this.worker.execute(this);
	}

	@Override
	protected void stop()
	{
		this.continuing = false;
	}

	/** clean up */
	@Override
	protected void destroy()
	{
		super.destroy();
		this.semaphore.writeLock().lock();
		this.pending.clear();
		this.now = null;
		this.semaphore.writeLock().unlock();
	}
}