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
import io.coala.util.JsonUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link DynaBeanInvocationHandler}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class DynaBeanInvocationHandler implements InvocationHandler
{
	/** */
	private static final Logger LOG = LogManager
			.getLogger(DynaBeanInvocationHandler.class);

	/** */
	private final Class<?> type;

	/** */
	private final Config config;

	/** */
	protected final DynaBean bean;

	/**
	 * {@link DynaBeanInvocationHandler} constructor
	 */
	public DynaBeanInvocationHandler(final Class<?> type, final DynaBean bean,
			final Map<?, ?>... imports)
	{
		this.type = type;
		this.bean = bean;
		// LOG.trace("Using imports: " + Arrays.asList(imports));
		Config config = null;
		if (Config.class.isAssignableFrom(type))
		{
			// always create fresh, never from cache
			config = ConfigFactory.create(type.asSubclass(Config.class),
					imports);
			if (Mutable.class.isAssignableFrom(type))
				((Mutable) config)
						.addPropertyChangeListener(new PropertyChangeListener()
						{
							@Override
							public void propertyChange(
									final PropertyChangeEvent change)
							{
								LOG.trace(type.getSimpleName()
										+ " changed: {} = {} (was {})",
										change.getPropertyName(),
										change.getNewValue(),
										change.getOldValue());

								// remove bean property in favor of changed
								// default config
								bean.remove(change.getPropertyName());

								// FIXME actually parse new value
							}
						});
		}
		this.config = config;

		// TODO use event listeners of Mutable interface to dynamically add
		// Converters at runtime
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable
	{
		// LOG.trace("Calling " + this.type.getSimpleName() + "#"
		// + method.getName() + "()");

		final String beanProp = method.getName();
		switch (args == null ? 0 : args.length)
		{
		case 0:
			if (beanProp.equals("toString"))
			{
				JsonUtil.checkRegistered(JsonUtil.getJOM(), this.type);
				return this.bean.toString();
			}

			if (beanProp.equals("hashCode"))
				return this.bean.hashCode();

			// ! can't intercept call to native method
			// if (method.getName().equals("getClass"))
			// return this.type;

			if (!method.getReturnType().equals(Void.TYPE))
			{
				final Object result = this.bean.any().get(beanProp);
				if (result != null)
					return result;
			}
			break;

		case 1:
			if (beanProp.equals("equals"))
				return this.bean.equals(args[0]);

			final DynaBean.BeanWrapper comparable = this.type
					.getAnnotation(DynaBean.BeanWrapper.class);
			if (beanProp.equals("compareTo") && comparable != null)
				return DynaBean.getComparator(comparable).compare(
						(Comparable) this.bean, (Comparable) args[0]);

			if (method.getReturnType().equals(Void.TYPE)
					&& method.getName().startsWith("set")
					&& method.getParameterTypes().length == 1
					&& method.getParameterTypes()[0].isAssignableFrom(args[0]
							.getClass()))
			{
				this.bean.set(beanProp, args[0]);
				return null; // setters return void
			}
			break;
		}

		if (method.getReturnType().equals(Void.TYPE))
			return null;

		if (this.config != null)
		{
			// LOG.trace("Checking config for " + attribute);
			return method.invoke(this.config, args);
		}

		if (method.getReturnType().isPrimitive())
			throw ExceptionBuilder.unchecked(
					"(primitive) value not set: " + this.type.getSimpleName()
							+ "#" + beanProp + "()").build();

		throw ExceptionBuilder.unchecked(
				"Unhandleable bean method: " + method + " for run-time type: "
						+ this.type).build();
	}
}