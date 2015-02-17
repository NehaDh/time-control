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
package io.coala.error;

import io.coala.json.JsonUtil;

/**
 * {@link CheckedException}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class CheckedException extends Exception implements ManagedException
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private final ExceptionContext context;

	/**
	 * {@link CheckedException} constructor
	 */
	public CheckedException(final ExceptionContext context, final String message)
	{
		super(message); // cause is initialized as "self"
		context.lock();
		this.context = context;
	}

	/**
	 * {@link CheckedException} constructor
	 */
	public CheckedException(final ExceptionContext context,
			final String message, final Throwable cause)
	{
		super(message, cause);
		context.lock();
		this.context = context;
	}

	@Override
	public String toJSON()
	{
		return JsonUtil.toPrettyJSON(this);
	}

	@Override
	public ExceptionContext getContext()
	{
		return this.context;
	}

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:Rick@almende.org">Rick</a>
	 */
	public static class Builder extends ExceptionBuilder
	{
		/** */
		private final String message;

		/** */
		private final Throwable cause;

		/** */
		private final ExceptionContext context = new ExceptionContext();

		/**
		 * {@link Builder} constructor
		 * 
		 * @param message the detailed description of the
		 *            {@link CheckedException}
		 * @param cause the {@link Throwable} causing the new
		 *            {@link CheckedException}, or {@code null} if none
		 */
		public Builder(final String message, final Throwable cause)
		{
			this.message = message;
			this.cause = cause;
			with("message", message);
			with("cause", cause == null ? null : cause.getClass().getName());
			with("trace", cause == null ? null : cause.getStackTrace());
		}

		/**
		 * @param key the context entry key
		 * @param value the context entry value
		 * @return this {@link Builder}
		 */
		public Builder with(final String key, final Object value)
		{
			this.context.set(key, value);
			return this;
		}

		@Override
		public CheckedException build()
		{
			return this.cause == null ? published(new CheckedException(
					this.context, this.message))
					: published(new CheckedException(this.context,
							this.message, this.cause));
		}

	}

}