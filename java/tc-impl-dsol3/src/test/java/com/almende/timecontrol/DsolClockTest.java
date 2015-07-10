package com.almende.timecontrol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import rx.Observer;

import com.almende.timecontrol.api.TimeManagerAPI;
import com.almende.timecontrol.dsol.DsolClockTuple;
import com.almende.timecontrol.entity.ClockConfig;
import com.almende.timecontrol.entity.ClockConfig.Status;
import com.almende.timecontrol.entity.ClockEvent;
import com.almende.timecontrol.time.Rate;

/**
 * {@link DsolClockTest} tests {@link TimeManagerAgent}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class DsolClockTest
{

	/** */
	private static final Logger LOG = LogManager.getLogger(DsolClockTest.class);

	@Test
	public void doTest()
	{
		final TimeManagerAPI mgr = new TimeManagerImpl(
				new DsolClockTuple.DefaultProvider());
		final ClockConfig.ID id = ClockConfig.ID.valueOf("clk1");
		final ClockConfig clk1 = mgr.getClock(id);
		LOG.trace("Created clock, status: " + clk1);
		mgr.updateClock(ClockConfig.Builder.forID(id).withDrag(Rate.valueOf(2))
				.build());
		
		mgr.observeClock(id).subscribe(new Observer<ClockEvent>()
		{
			@Override
			public void onCompleted()
			{
				LOG.trace(id + " is done");
			}

			@Override
			public void onError(final Throwable e)
			{
				LOG.warn(id + " failed", e);
				Assert.fail(e.getMessage());
			}

			@Override
			public void onNext(final ClockEvent t)
			{
				LOG.trace(id + " event: " + t);
			}
		});
		LOG.trace("Set pace, status: " + clk1);
		mgr.updateClock(ClockConfig.Builder.forID(id)
				.withStatus(Status.RUNNING).build());
		LOG.trace("Set running, status: " + clk1);
		mgr.updateClock(ClockConfig.Builder.forID(id)
				.withStatus(Status.COMPLETED).build());
		LOG.trace("Set completed, status: " + clk1);
		mgr.updateClock(ClockConfig.Builder.forID(id)
				.withStatus(Status.WAITING).build());
		LOG.trace("Set waiting, status: " + clk1);
	}

}
