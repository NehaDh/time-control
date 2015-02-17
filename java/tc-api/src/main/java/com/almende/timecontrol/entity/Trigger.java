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

import java.util.List;

import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * {@link Trigger}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface Trigger
{

	/** @return the {@link ID} of this {@link Trigger} */
	ID id();

	/**
	 * @return
	 */
	Slave.ID originId();

	/**
	 * @return
	 */
	List<Slave.ID> destinationIds();

	/**
	 * @return
	 */
	RecurrenceExpression when();

	/**
	 * {@link ExpressionFormat}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	enum ExpressionFormat
	{
		/** a (non-recurring) instant in {@link ISODateTimeFormat} */
		ISO_8601("iso"),

		/** */
		CRON_RULE("cron"),

		/** */
		ICAL_RULE("ical"),

		;

		/** */
		private final String jsonValue;

		/**
		 * {@link ExpressionFormat} enum constant constructor
		 * 
		 * @param jsonValue
		 */
		private ExpressionFormat(final String jsonValue)
		{
			this.jsonValue = jsonValue;
		}

		@JsonValue
		private final String value()
		{
			return this.jsonValue;
		}
	}

	/**
	 * {@link RecurrenceExpression}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	interface RecurrenceExpression
	{
		/**
		 * @return
		 */
		ExpressionFormat type();

		/**
		 * @return
		 */
		String expression();
	}

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