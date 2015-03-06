package com.almende.timecontrol.eve;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.almende.timecontrol.entity.TimerStatus;

/**
 * {@link TimerAgentTest} tests {@link TimerAgent}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class TimerAgentTest
{

	/** */
	private static final Logger LOG = LogManager
			.getLogger(TimerAgentTest.class);

	@Test
	public void doTest()
	{
		final String timerID = "testTimerID";
		LOG.trace("Getting status for instance: " + timerID);
		final TimerStatus status = TimerAgent.getInstance(timerID).getStatus();
		LOG.trace("Got status: " + status);
	}

}
