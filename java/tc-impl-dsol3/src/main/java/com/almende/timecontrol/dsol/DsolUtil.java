/* $Id: cccb3d9313ff65e159321e3a8283c101ef6a24b3 $
 * 
 * Part of the EU project Adapt4EE, see http://www.adapt4ee.eu/
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
 * Copyright (c) 2015 Almende B.V. 
 */
package com.almende.timecontrol.dsol;

import io.coala.util.LogUtil;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.naming.Context;
import javax.naming.InitialContext;

import nl.tudelft.simulation.dsol.ModelInterface;
import nl.tudelft.simulation.dsol.experiment.Experiment;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.experiment.Treatment;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTime;
import nl.tudelft.simulation.dsol.simtime.SimTimeCalendarDouble;
import nl.tudelft.simulation.dsol.simtime.TimeUnit;
import nl.tudelft.simulation.dsol.simtime.UnitTimeDouble;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.Simulator;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.apache.logging.log4j.Logger;

import com.almende.timecontrol.TimeControl;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.time.Duration;
import com.almende.timecontrol.time.Instant;
import com.almende.timecontrol.time.TimeSpan;

/**
 * {@link DsolUtil}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class DsolUtil
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(DsolUtil.class);

	/** */
	private static final TimeUnit DEFAULT_UNIT = TimeUnit.MILLISECOND;

	/** */
	// private static final UnitTimeDouble ZERO = new UnitTimeDouble(0d,
	// DEFAULT_UNIT);

	/** singleton design pattern constructor */
	private DsolUtil()
	{
		// singleton design pattern
	}

	/**
	 * @param time
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number & Comparable<T>> T toDSOL(
			final Duration time, final Class<T> timeUnit)
	{
		if (time == null)
			return null;

		final TimeSpan value = time.getValue();
		if (value.getUnit().equals(TimeControl.MILLIS))
		{
			if (UnitTimeDouble.class.isAssignableFrom(timeUnit))
				return (T) new UnitTimeDouble(
						value.doubleValue(TimeControl.MILLIS),
						TimeUnit.MILLISECOND);
		}

		if (value.getUnit().equals(NonSI.YEAR)
				|| value.getUnit().equals(NonSI.YEAR_CALENDAR)
				|| value.getUnit().equals(NonSI.YEAR_SIDEREAL))
			if (UnitTimeDouble.class.isAssignableFrom(timeUnit))
				return (T) new UnitTimeDouble(value.doubleValue(NonSI.DAY),
						TimeUnit.YEAR);

		if (value.getUnit().equals(NonSI.WEEK))
			if (UnitTimeDouble.class.isAssignableFrom(timeUnit))
				return (T) new UnitTimeDouble(value.doubleValue(NonSI.DAY),
						TimeUnit.WEEK);

		if (value.getUnit().equals(NonSI.DAY))
			if (UnitTimeDouble.class.isAssignableFrom(timeUnit))
				return (T) new UnitTimeDouble(value.doubleValue(NonSI.DAY),
						TimeUnit.DAY);

		if (value.getUnit().equals(NonSI.HOUR))
			if (UnitTimeDouble.class.isAssignableFrom(timeUnit))
				return (T) new UnitTimeDouble(value.doubleValue(NonSI.DAY),
						TimeUnit.HOUR);

		if (value.getUnit().equals(NonSI.MINUTE))
			if (UnitTimeDouble.class.isAssignableFrom(timeUnit))
				return (T) new UnitTimeDouble(value.doubleValue(NonSI.DAY),
						TimeUnit.MINUTE);

		if (value.getUnit().equals(SI.SECOND))
			if (UnitTimeDouble.class.isAssignableFrom(timeUnit))
				return (T) new UnitTimeDouble(value.doubleValue(NonSI.DAY),
						TimeUnit.SECOND);

		LOG.warn("Unknown time unit for {}, assuming {}", time, DEFAULT_UNIT);
		if (UnitTimeDouble.class.isAssignableFrom(timeUnit))
			return (T) new UnitTimeDouble(value.getValue().doubleValue(),
					DEFAULT_UNIT);

		throw new IllegalArgumentException("Unsupported time unit:"
				+ timeUnit.getName());
	}

	/**
	 * @param time
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends SimTime<?, ?, T>> T toDSOL(final Instant time,
			final Class<T> simTimeUnit)
	{
		if (time == null)
			return null;

		if (SimTimeCalendarDouble.class.isAssignableFrom(simTimeUnit))
			return (T) new SimTimeCalendarDouble(time.getValue().doubleValue(
					TimeControl.MILLIS));

		throw new IllegalArgumentException("Unsupported time unit:"
				+ simTimeUnit.getName());
	}

	/**
	 * @param absoluteTime
	 * @param priority
	 * @param source
	 * @param target
	 * @param method
	 * @param args
	 * @return
	 */
	public static <T extends SimTime<?, ?, T>> SimEventInterface<T> toDSOL(
			final T absoluteTime, final short priority, final Object source,
			final Object target, final String method, final Object... args)
	{
		return new SimEvent<T>(absoluteTime, priority, source, target, method,
				args);
	}

	/**
	 * Get the underlying class for a type, or null if the type is a variable
	 * type. See <a
	 * href="http://www.artima.com/weblogs/viewpost.jsp?thread=208860"
	 * >description</a>
	 * 
	 * @param type the type
	 * @return the underlying class
	 */
	public static Class<?> getClass(final Type type)
	{
		if (type instanceof Class)
		{
			// LOG.trace("Type is a class/interface: "+type);
			return (Class<?>) type;
		}

		if (type instanceof ParameterizedType)
		{
			// LOG.trace("Type is a ParameterizedType: "+type);
			return getClass(((ParameterizedType) type).getRawType());
		}

		if (type instanceof GenericArrayType)
		{
			// LOG.trace("Type is a GenericArrayType: "+type);
			final Type componentType = ((GenericArrayType) type)
					.getGenericComponentType();
			final Class<?> componentClass = getClass(componentType);
			if (componentClass != null)
				return Array.newInstance(componentClass, 0).getClass();
		}
		return null;
	}

	/**
	 * Get the actual type arguments a child class has used to extend a generic
	 * base class. See <a
	 * href="http://www.artima.com/weblogs/viewpost.jsp?thread=208860"
	 * >description</a>
	 * <p>
	 * FIXME detect arguments of implemented super interface (e.g.
	 * {@link SimulatorInterface})
	 * 
	 * @param genericAncestorType the base class
	 * @param concreteDescendantType the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static <T> List<Class<?>> getTypeArguments(
			final Class<T> genericAncestorType,
			final Class<? extends T> concreteDescendantType)
	{
		// sanity check
		if (genericAncestorType == null)
			throw new NullPointerException("genericAncestorType not set");
		if (concreteDescendantType == null)
			throw new NullPointerException("concreteDescendantType not set");

		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type type = concreteDescendantType;
		Class<?> typeClass = getClass(type);

		// start walking up the inheritance hierarchy until we hit parentClass
		while (!genericAncestorType.equals(typeClass))
		{
			if (type instanceof Class)
			{
				// there is no useful information for us in raw types, so just
				// keep going.

				if (genericAncestorType.isInterface())
				{
					Type intfType = null;
					for (Type intf : typeClass.getGenericInterfaces())
					{
						if (intf instanceof ParameterizedType
								&& genericAncestorType
										.equals(((ParameterizedType) intf)
												.getRawType()))
						{
							intfType = intf;
							break;
						}
					}
					if (intfType == null)
						type = typeClass.getGenericSuperclass();
					else
						type = intfType;
				} else
					type = typeClass.getGenericSuperclass();

				if (type == null)
				{
					if (!typeClass.isInterface())
					{
						LOG.warn("No generic super classes found for child class: "
								+ typeClass
								+ " of parent: "
								+ genericAncestorType);
						return Collections.emptyList();
					}
					for (Type intf : typeClass.getGenericInterfaces())
					{
						if (intf instanceof ParameterizedType)
						{
							type = intf;
							// TODO try other interfaces if this one fails?
							break;
						}
					}
					if (type == null)
					{
						LOG.warn("No generic ancestors found for child interface: "
								+ typeClass
								+ " of parent: "
								+ genericAncestorType);
						return Collections.emptyList();
					}
				}
				// LOG.trace(String.format("Trying generic super of %s: %s",
				// typeClass.getSimpleName(), type));
			} else
			{
				final ParameterizedType parameterizedType = (ParameterizedType) type;
				final Class<?> rawType = (Class<?>) parameterizedType
						.getRawType();

				final Type[] actualTypeArguments = parameterizedType
						.getActualTypeArguments();
				final TypeVariable<?>[] typeParameters = rawType
						.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++)
				{
					resolvedTypes
							.put(typeParameters[i], actualTypeArguments[i]);
				}

				if (!genericAncestorType.equals(rawType))
				{
					type = rawType.getGenericSuperclass();
					// LOG.trace(String.format(
					// "Trying generic super of child %s: %s", rawType,
					// type));
				}
				// else // done climbing the hierarchy
				// LOG.trace("Matched generic " + type + " to ancestor: "
				// + genericAncestorType);
			}
			typeClass = getClass(type);
			// LOG.trace("Trying generic " + typeClass + " from: "
			// + Arrays.asList(typeClass.getGenericInterfaces()));
		}

		// finally, for each actual type argument provided to baseClass,
		// determine (if possible)
		// the raw class for that type argument.
		final Type[] actualTypeArguments;
		if (type instanceof Class)
		{
			actualTypeArguments = typeClass.getTypeParameters();
		} else
		{
			actualTypeArguments = ((ParameterizedType) type)
					.getActualTypeArguments();
		}

		// resolve types by chasing down type variables.
		final List<Class<?>> parentTypeArguments = new ArrayList<Class<?>>();
		for (Type baseType : actualTypeArguments)
		{
			while (resolvedTypes.containsKey(baseType))
				baseType = resolvedTypes.get(baseType);

			parentTypeArguments.add(getClass(baseType));
		}
		// LOG.trace(String.format(
		// "Got child %s's type arguments for %s: %s",
		// childClass.getName(), parentClass.getSimpleName(),
		// parentTypeArguments));
		return parentTypeArguments;
	}

	@SuppressWarnings("unchecked")
	public static <A extends Comparable<A>, R extends Number & Comparable<R>, T extends SimTime<A, R, T>> void initialize(
			final Simulator<A, R, T> scheduler, final ClockConfig config)
	{
		final ModelInterface<A, R, T> model = new ModelInterface<A, R, T>()
		{
			/** */
			private static final long serialVersionUID = 1L;

			@Override
			public void constructModel(
					final SimulatorInterface<A, R, T> simulator)
			{
				// empty
			}

			@Override
			public SimulatorInterface<A, R, T> getSimulator()
			{
				return scheduler;
			}
		};

		try
		{
			final List<Class<?>> typeArgs = getTypeArguments(Simulator.class,
					scheduler.getClass());
			LOG.trace("Simulator type {} args: {}",
					Arrays.asList(scheduler.getClass().getClasses()), typeArgs);
			final Class<R> timeUnit = (Class<R>) typeArgs.get(1);
			final Class<T> simTimeUnit = (Class<T>) typeArgs.get(2);

			// offset is irrelevant
			final T startTime = toDSOL(Instant.valueOf(0L), simTimeUnit);
			// warmupTime is irrelevant
			final R warmupTime = toDSOL(Duration.ZERO, timeUnit);
			// sanity check
			final Duration until = config.until();
			// if (until == null)
			// throw new NullPointerException("Run length not specified: "
			// + config);
			final R runLength = toDSOL(
					until == null ? Duration.valueOf(Integer.MAX_VALUE) : until,
					timeUnit);

			final String treatmentID = config.id().getValue();
			final Context context = new InitialContext()
					.createSubcontext(treatmentID);

			final ReplicationMode mode = ReplicationMode.STEADY_STATE;
			final Experiment<A, R, T> experiment = new Experiment<A, R, T>(
					context);
			experiment.setModel(model);
			experiment.setTreatment(new Treatment<A, R, T>(experiment,
					treatmentID, startTime, warmupTime, runLength, mode));

			scheduler.initialize(new Replication<A, R, T>(context, experiment),
					mode);

			if (scheduler instanceof DEVSSimulator)
				((DEVSSimulator<A, R, T>) scheduler).scheduleEvent(toDSOL(
						startTime, SimEventInterface.NORMAL_PRIORITY,
						DsolUtil.class, new Object()
						{
							@SuppressWarnings("unused")
							public void setThreadName(final String name)
							{
								final String oldName = Thread.currentThread()
										.getName();
								Thread.currentThread().setName(name);
								LOG.trace(
										"Simulator thread renamed to: {}, was: {}",
										name, oldName);
							}
						}, SET_THREAD_NAME, treatmentID));
		} catch (final Throwable t)
		{
			throw new RuntimeException(
					"Problem initializing simulator, config {}" + config, t);
		}
	}

	/** */
	private static final String SET_THREAD_NAME = "setThreadName";

}
