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

import org.joda.time.Instant;

/**
 * {@link Job}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface Job
{

	/** @return the {@link ID} of this {@link Clock} */
	ID id();

	/**
	 * the {@link FederateRef} of the {@link Slave} that registered the
	 * {@link Trigger} for this {@link Job}
	 */
	Slave.ID sourceId();

	/**
	 * the {@link Trigger.ID} of the {@link Trigger} that generated this
	 * {@link Job}
	 */
	Trigger.ID generatorId();

	/** the simulated time {@link Instant} when this {@link Job} occurs */
	Instant executionTime();

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

}