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

import io.coala.util.JsonUtil;

/**
 * {@link UncheckedException}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public class UncheckedException extends RuntimeException implements
		ManagedException
{

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	private ExceptionContext context = null;

	/**
	 * {@link UncheckedException} zero-arg bean constructor for JSON-RPC
	 */
	public UncheckedException()
	{
		// empty
	}
	
	/**
	 * {@link UncheckedException} constructor
	 */
	public UncheckedException(final ExceptionContext context,
			final String message)
	{
		super(message); // cause is initialized as "self"
		context.lock();
		this.context = context;
	}

	/**
	 * {@link UncheckedException} constructor
	 */
	public UncheckedException(final ExceptionContext context,
			final String message, final Throwable cause)
	{
		super(message, cause);
		context.lock();
		this.context = context;
	}

	@Override
	public String toJSON()
	{
		return JsonUtil.toJSON(this);
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
	public static class Builder extends ExceptionBuilder<Builder>
	{

		/**
		 * {@link Builder} constructor
		 * 
		 * @param message the detailed description of the
		 *            {@link UncheckedException}
		 * @param cause the {@link Throwable} causing the new
		 *            {@link UncheckedException}, or {@code null} if none
		 */
		public Builder(final String message, final Throwable cause)
		{
			super(message, cause);
		}

		@Override
		public UncheckedException build()
		{
			return this.cause == null ? published(new UncheckedException(
					this.context, this.message))
					: published(new UncheckedException(this.context,
							this.message, this.cause));
		}

	}

}