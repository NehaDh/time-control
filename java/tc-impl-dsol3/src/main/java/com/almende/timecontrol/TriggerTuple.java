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

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import com.almende.timecontrol.entity.TriggerConfig;
import com.almende.timecontrol.entity.TriggerEvent;
import com.almende.timecontrol.entity.TriggerStatus;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.TriggerPattern;

/**
 * {@link TriggerTuple}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class TriggerTuple implements Observer<TriggerEvent>
{

	private final List<TriggerEvent> events = new ArrayList<>();

	private final Subject<TriggerEvent, TriggerEvent> eventPublisher = PublishSubject
			.create();

	private final TriggerPattern pattern;

	private final TriggerConfig.ID id;

	public TriggerTuple(final TriggerPattern pattern,
			final Subscriber<? super TriggerEvent> subscriber)
	{
		this.pattern = pattern;
		this.id = new TriggerConfig.ID();
		this.eventPublisher.subscribe(subscriber);
	}

	/**
	 * @return the {@link TriggerConfig}
	 */
	public TriggerConfig toConfig()
	{
		return new TriggerConfig.Builder().withID(this.id)
				.withPattern(this.pattern).build();
	}

	public TriggerStatus toStatus()
	{
		return new TriggerStatus.Builder().withConfig(toConfig())
				.withJobs(getEvents()).build();
	}

	/**
	 * @return the events published so far
	 */
	public List<TriggerEvent> getEvents()
	{
		return this.events;
	}

	/**
	 * @return an {@link Observable} of the {@link TriggerEvent}s
	 */
	public Observable<TriggerEvent> asObservable()
	{
		return this.eventPublisher.asObservable();
	}

	/**
	 * @param event the {@link TriggerEvent} to publish
	 */
	public void onNext(final Duration time, final boolean isLast)
	{
		onNext(TriggerEvent.Builder.fromTime(time).withTriggerID(this.id)
				.withLastCall(isLast).build());
		if (isLast)
			onCompleted();
	}

	@Override
	public void onCompleted()
	{
		this.eventPublisher.onCompleted();
	}

	@Override
	public void onError(final Throwable e)
	{
		this.eventPublisher.onError(e);
	}

	@Override
	public void onNext(final TriggerEvent event)
	{
		this.events.add(event);
		this.eventPublisher.onNext(event);
	}
}