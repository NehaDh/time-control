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
package com.almende.timecontrol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.almende.timecontrol.api.eve.EveUtil;
import com.almende.timecontrol.entity.SlaveConfig;
import com.almende.timecontrol.entity.TimerConfig;
import com.almende.timecontrol.entity.TimerStatus;
import com.almende.timecontrol.eve.SlaveAgent;
import com.almende.timecontrol.eve.TimerAgent;

/**
 * {@link SimpleExample}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class SimpleExample
{
	/** */
	private static final Logger LOG = LogManager.getLogger(SimpleExample.class);

	/**
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception
	{
		final String masterId = "exampleTimer1";

		final TimerConfig timerConfig = TimerConfig.Builder.forID(masterId)
				.build();
		LOG.info("Starting master with config: {}", timerConfig);
		final TimerAgent master = TimerAgent.getInstance(masterId);
		master.initialize(timerConfig);

		final SlaveConfig slaveConfig = SlaveConfig.Builder
				.forID("exampleSlave1")
				.withTimerId(TimerConfig.ID.valueOf(masterId)).build();

		LOG.info("Connecting slave with config: {}", slaveConfig);
		final SlaveAgent slave = SlaveAgent.getInstance(slaveConfig);
		slave.initOnce();
		final TimerStatus status = slave.getStatus();
		LOG.info("Connected to master, got status: {}", status);

		EveUtil.stop();
		// System.exit(0);
	}
}
