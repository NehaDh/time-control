/* $Id: a5f93f8617b57246f213fe20eaf0d2b49fd37f00 $
 * $URL: https://dev.almende.com/svn/abms/dsol-util/src/test/java/io/coala/dsol/DsolTest.java $
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
 * Copyright (c) 2010-2014 Almende B.V. 
 */
package com.almende.timecontrol.coala;

import static org.junit.Assert.assertTrue;
import io.coala.agent.AgentID;
import io.coala.agent.AgentStatusObserver;
import io.coala.agent.AgentStatusUpdate;
import io.coala.bind.Binder;
import io.coala.bind.BinderFactory;
import io.coala.capability.admin.CreatingCapability;
import io.coala.capability.plan.ClockStatusUpdate;
import io.coala.capability.replicate.ReplicatingCapability;
import io.coala.capability.replicate.ReplicationConfig;
import io.coala.invoke.ProcedureCall;
import io.coala.invoke.Schedulable;
import io.coala.log.LogUtil;
import io.coala.model.ModelComponent;
import io.coala.name.Identifiable;
import io.coala.time.Instant;
import io.coala.time.SimTime;
import io.coala.time.Trigger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Test;

import rx.Observer;

import com.almende.timecontrol.eve.TimeManagerAgent;

/**
 * {@link CapabilityTest}
 * 
 * @version $Revision: 312 $
 * @author <a href="mailto:Rick@almende.org">Rick</a>
 *
 */
// @Ignore
public class CapabilityTest
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(CapabilityTest.class);

	private static final String DO_SIM_COMPLETE = "doSimComplete";

	@Test
	public void testTimeControl() throws Exception
	{
		LOG.trace("Started " + TimeControlCapabilityImpl.class.getSimpleName()
				+ " test...");

		final String modelname = "testModel" + System.currentTimeMillis();
		final TimeManagerAgent timer = TimeManagerAgent.getInstance(modelname);
		LOG.trace("Started timer at urls: " + timer.getUrls());

		final Binder binder = BinderFactory.Builder
				.fromFile()
				.withProperty(ReplicationConfig.class,
						ReplicationConfig.MODEL_NAME_KEY, modelname).build()
				.create("_unittest_");

		final CreatingCapability booterSvc = binder
				.inject(CreatingCapability.class);

		binder.inject(ReplicatingCapability.class).getStatusUpdates()
				.subscribe(new Observer<ClockStatusUpdate>()
				{
					@Override
					public void onCompleted()
					{
						LOG.trace("Time control COMPLETED");
					}

					@Override
					public void onError(final Throwable e)
					{
						e.printStackTrace();
					}

					@Override
					public void onNext(final ClockStatusUpdate simEvent)
					{
						LOG.trace("Time control now " + simEvent);
					}
				});

		final String[] actorNames = { "actor1", "actor2", "actor3" };

		final CountDownLatch allReady = new CountDownLatch(actorNames.length);

		final CountDownLatch simCompleted = new CountDownLatch(1);

		final CountDownLatch allCompleted = new CountDownLatch(
				actorNames.length);

		final Set<AgentID> ready = new HashSet<>();
		final Set<AgentID> failed = new HashSet<>();
		for (String actorName : actorNames)
		{
			final String myName = actorName;
			booterSvc.createAgent(myName, CapabilityTestAgent.class).subscribe(
					new AgentStatusObserver()
					{

						@Override
						public void onNext(final AgentStatusUpdate update)
						{
							LOG.trace(myName + " ==> " + update.getStatus());

							if (update.getStatus().isPassiveStatus())
							{
								ready.add(update.getAgentID());
								allReady.countDown();
								LOG.trace(myName + " activated, remaining: "
										+ allCompleted.getCount());
							} else if (update.getStatus().isFailedStatus())
							{
								failed.add(update.getAgentID());
								allCompleted.countDown();
								LOG.trace(myName + " failed, remaining: "
										+ allCompleted.getCount());
							} else if (update.getStatus().isFinishedStatus())
							{
								allCompleted.countDown();
								LOG.trace(myName + " finished, remaining: "
										+ allCompleted.getCount());
							}
						}

						@Override
						public void onError(final Throwable e)
						{
							LOG.error("Problem while observing status of "
									+ myName, e);
						}

						@Override
						public void onCompleted()
						{
							LOG.trace(myName + " status updates COMPLETED");
						}
					});
		}

		allReady.await(5, TimeUnit.SECONDS);
		assertTrue(
				"Agent(s) ready: " + ready + ", pending: "
						+ allReady.getCount(), allReady.getCount() == 0);

		final ModelComponent<AgentID> actor = new ModelComponent<AgentID>()
		{
			/** */
			private static final long serialVersionUID = 1L;

			@Override
			public AgentID getID()
			{
				return binder.getID();
			}

			@Override
			public int compareTo(final Identifiable<AgentID> o)
			{
				return getID().compareTo(o.getID());
			}

			@Override
			public AgentID getOwnerID()
			{
				return binder.getID();
			}

			@Override
			public Instant<?> getTime()
			{
				return binder.inject(ReplicatingCapability.class).getTime();
			}

			@Schedulable(DO_SIM_COMPLETE)
			public void doSimulationComplete()
			{
				LOG.trace("Simulation complete");
				simCompleted.countDown();
			}
		};
		final ReplicationConfig cfg = binder.inject(ReplicationConfig.class);
		final ReplicatingCapability sim = binder
				.inject(ReplicatingCapability.class);
		final SimTime endTime = cfg.newTime().create(
				cfg.getInterval().toDurationMillis(),
				io.coala.time.TimeUnit.MILLIS);
		sim.schedule(ProcedureCall.create(actor, actor, DO_SIM_COMPLETE),
				Trigger.createAbsolute(endTime));

		LOG.trace("All agent(s) activated, now starting replication...");
		sim.start();

		simCompleted.await(5, TimeUnit.SECONDS);
		assertTrue("Simulation incomplete", simCompleted.getCount() == 0);

		allCompleted.await(1, TimeUnit.SECONDS);
		assertTrue("Agent(s) failed: " + failed, failed.isEmpty());
		assertTrue("Agent(s) interrupted: " + allCompleted.getCount(),
				allCompleted.getCount() == 0);
		LOG.trace(TimeControlCapabilityImpl.class.getSimpleName()
				+ " test done!");
	}
}
