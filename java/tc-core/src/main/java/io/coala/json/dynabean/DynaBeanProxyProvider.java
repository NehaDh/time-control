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
package io.coala.json.dynabean;

import io.coala.error.ExceptionBuilder;
import io.coala.json.dynabean.DynaBean.Builder;
import io.coala.util.TypeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Provider;

/**
 * {@link DynaBeanProxyProvider}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 * @param <T>
 */
public class DynaBeanProxyProvider<T> implements Provider<T>
{

	/** */
	private final DynaBean bean;

	/** */
	private final Properties[] imports;

	/**
	 * {@link DynaBeanProxyProvider} constructor
	 * 
	 * @param type
	 */
	public DynaBeanProxyProvider(final DynaBean bean,
			final Properties... imports)
	{
		this.bean = bean;
		this.imports = imports;
	}

	/** cache of type arguments for known {@link Builder} sub-types */
	private static final Map<Class<?>, List<Class<?>>> BUILDER_TYPE_ARGUMENT_CACHE = new HashMap<>();

	@Override
	public T get()
	{
		try
		{
			@SuppressWarnings("unchecked")
			final Class<T> type = (Class<T>) TypeUtil.getTypeArguments(
					DynaBeanProxyProvider.class, getClass(),
					BUILDER_TYPE_ARGUMENT_CACHE).get(0);
			return DynaBean.valueOf(type, this.bean, this.imports);
		} catch (final Throwable t)
		{
			throw ExceptionBuilder.unchecked(
					"Problem providing " + DynaBean.class.getSimpleName()
							+ " proxy using: " + getClass().getName(), t)
					.build();
		}
	}
}