/* $Id$
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
package com.almende.timecontrol.bean;

import io.coala.error.ExceptionBuilder;
import io.coala.error.ManagedException;
import io.coala.json.JsonUtil;

import java.util.Arrays;
import java.util.Properties;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import rx.Observer;

import com.almende.timecontrol.entity.Clock;
import com.almende.timecontrol.entity.Trigger;

/**
 * {@link BeanTest}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class BeanTest
{

	/** */
	private static final Logger LOG = LogManager.getLogger(BeanTest.class);

	@Test
	public void marshalBeanTest()
	{
		ExceptionBuilder.getObservable().subscribe(
				new Observer<ManagedException>()
				{
					@Override
					public void onCompleted()
					{
						LOG.trace("No more exceptions?");
					}

					@Override
					public void onError(final Throwable e)
					{
						LOG.error("Problem while observing exceptions", e);
					}

					@Override
					public void onNext(final ManagedException t)
					{
						LOG.trace("Observed exception: " + t.getContext());
						// t.printStackTrace();
					}
				});

		final Clock.ID parentID = Clock.ID.valueOf("\"theRoot\""); // TODO loose
																	// quotes?
		final Properties imports = new Properties();
		imports.setProperty("forkParent", parentID.toString());
		imports.setProperty("wallClockRate", "1.4");
		imports.setProperty("time", "PT1.2S"); // TODO accept Number type?
		// imports.setProperty("error", "{}"); // FIXME
		// TODO check parseable JSON
		final Clock config = ConfigCache.getOrCreate(Clock.class, imports);
		LOG.trace("Config id: " + config.id());
		LOG.trace("Config forkParent: " + config.forkParent());
		LOG.trace("Config forkOffset: " + config.forkOffset());
		LOG.trace("Config slaveTimeout: " + config.slaveTimeout());
		LOG.trace("Config status: " + config.status());
		LOG.trace("Config time: " + config.time());
		LOG.trace("Config until: " + config.until());
		LOG.trace("Config error: " + config.error());
		LOG.trace("Config wallClockRate: " + config.wallClockRate());
		final Clock clock = JsonUtil.valueOf(
				"{\"id\":\"bla\",\"idasdsd\":[\"bla3w\"]}", Clock.class,
				imports);
		// clock.setName(new ClockRef.Builder().withValue("myClock").build());
		LOG.trace("Clock: " + clock);
		LOG.trace("Clock methods: "
				+ Arrays.asList(clock.getClass().getDeclaredMethods()));
		LOG.trace("Clock.name: " + clock.id() + ", type: "
				+ clock.id().getClass().getSimpleName());

		LOG.trace("Clock.forkParent: " + clock.forkParent());
		final Trigger trigger = JsonUtil.valueOf("{\"when\":{\"type\":1}}",
				Trigger.class);

		LOG.trace("Trigger: " + trigger + ", when ["
				+ trigger.when().type().getClass().getSimpleName() + "]: "
				+ trigger.when().type().name());
	}
}
