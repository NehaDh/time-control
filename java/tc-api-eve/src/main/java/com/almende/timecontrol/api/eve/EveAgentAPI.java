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
package com.almende.timecontrol.api.eve;

import io.coala.id.Identifier;
import rx.Observable;

import com.almende.eve.agent.AgentInterface;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.timecontrol.entity.ClockConfig.ID;
import com.eaio.uuid.UUID;

/**
 * {@link EveAgentAPI} exposes the agent's internal events (until external event
 * pub/sub is available)
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface EveAgentAPI extends AgentInterface
{

	/** the agent's (internal) events, only directly accessible */
	@Access(AccessType.UNAVAILABLE)
	Observable<AgentEvent> events();

	/**
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	enum AgentEvent
	{
		/** */
		AGENT_INITIALIZED,

		/** */
		AGENT_DESTROYED,

		;
	}

	/**
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class SubscriptionID extends Identifier<UUID>
	{
		public SubscriptionID()
		{
			super();
			setValue(new UUID());
		}

		/** @see org.aeonbits.owner.Converters.CLASS_WITH_VALUE_OF_METHOD */
		public static ID valueOf(final String value)
		{
			return Identifier.valueOf(value, ID.class);
		}
	}

	/**
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class EventWrapper<T> implements Comparable<EventWrapper<T>>
	{
		private final SubscriptionID subID;
		private final T event;

		private EventWrapper(final SubscriptionID subID, final T event)
		{
			this.subID = subID;
			this.event = event;
		}

		public static <T> EventWrapper<T> of(final SubscriptionID subID,
				final T event)
		{
			return new EventWrapper<T>(subID, event);
		}

		/**
		 * @return {@code true} iff the {@link SubscriptionID} matches,
		 *         {@code false} otherwise
		 */
		public boolean fits(final SubscriptionID subID)
		{
			return this.subID.equals(subID);
		}

		/**
		 * @return the observed event
		 */
		public T unwrap()
		{
			return this.event;
		}

		@Override
		public int compareTo(final EventWrapper<T> o)
		{
			return this.subID.compareTo(o.subID);
		}
	}

}
