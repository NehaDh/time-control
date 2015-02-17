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

import io.coala.type.DynaBean;

import java.util.Map;

/**
 * {@link ExceptionContext}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class ExceptionContext extends DynaBean
{

	@Override
	public Map<String, Object> any()
	{
		return super.any();
	}

	@Override
	protected Object set(final String name, final Object value)
	{
		return super.set(name, value);
	}

	@Override
	protected void lock()
	{
		super.lock();
	}
}