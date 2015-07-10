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

import java.io.PrintStream;
import java.io.PrintWriter;

import com.eaio.uuid.UUID;

/**
 * {@link ManageableException}
 * 
 * TODO set interesting meta data attributes from config/defaults, e.g. error
 * codes, the originator Object, context Thread or stack, number of retries,
 * time-outs, checked/unchecked, etc.
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 */
public interface ManageableException
{
	/** @see Throwable#initCause(Throwable) */
	Throwable initCause(Throwable t);

	/** @see Throwable#getMessage() */
	String getMessage();

	/** @see Throwable#getLocalizedMessage() */
	String getLocalizedMessage();

	/** @see Throwable#getCause() */
	Throwable getCause();

	/** @see Throwable#getStackTrace() */
	StackTraceElement[] getStackTrace();

	/** @see Throwable#fillInStackTrace() */
	Throwable fillInStackTrace();

	/** @see Throwable#printStackTrace() */
	void printStackTrace();

	/** @see Throwable#printStackTrace(PrintStream) */
	void printStackTrace(PrintStream s);

	/** @see Throwable#printStackTrace(PrintWriter) */
	void printStackTrace(PrintWriter s);

	/** @return a JSON representation of this {@linkplain ManageableException} */
	String toJSON();

	/** @return the {@link ExceptionContext} */
	ExceptionContext getContext();

	/** @return the {@link UUID} */
	UUID getUuid();

	// context (application id, error code, ...)

	// origin (originator id, stack, time, thread)

	// trace (handlers, retries, timeout start/duration, ...)

}