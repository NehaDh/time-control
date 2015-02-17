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
package com.almende.timecontrol.entity;

import io.coala.refer.Identifier;

import java.net.URI;
import java.util.SortedMap;

import org.joda.time.Duration;
import org.joda.time.Instant;

import com.almende.timecontrol.entity.Slave.SlaveInfo;

/**
 * {@link Scenario}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface Scenario
{

	/** @return the {@link ID} of this {@link Clock} */
	ID id();

	/** the callback {@link URI} for the owner of this {@link Scenario} */
	URI address();

	/**
	 * a {@link ID} of the (root) {@link ClockData}
	 */
	ID root();

	/**
	 * the simulated time {@link Duration} between <b>discrete</b> ticks of this
	 * {@link Scenario}, or {@code null} for a <b>continuous</b> time scale
	 */
	Duration resolution();

	/**
	 * the requested/recorded wall-clock start {@link Instant}, or {@code null}
	 * for "immediate" (request) or "non-applicable" when time is managed across
	 * federated nodes using the non-null {@code #wallClockRate}, therefore
	 * recorded offset becomes invalid (i.e. shifts) in case of delays
	 */
	Instant wallClockOffset();

	/** the simulated duration */
	Duration duration();

	/**
	 * {@link ID}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class ID extends Identifier<String>
	{
		/** @see org.aeonbits.owner.Converters.CLASS_WITH_VALUE_OF_METHOD */
		public static ID valueOf(final String value)
		{
			return Identifier.of(value, ID.class);
		}
	}

	/**
	 * {@link ScenarioInfo}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	public interface ScenarioInfo
	{
		Scenario scenario();

		SortedMap<ID, Clock> clocks();

		SortedMap<Slave.ID, SlaveInfo> slaves();

	}

}