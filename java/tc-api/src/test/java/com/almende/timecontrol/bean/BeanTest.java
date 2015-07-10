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

import io.coala.error.Contextualized;
import io.coala.util.JsonUtil;

import java.util.Properties;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import org.junit.Test;

import rx.Observer;

import com.almende.timecontrol.entity.ClockConfig;

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
		Contextualized.Publisher.asObservable().subscribe(
				new Observer<Throwable>()
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
					public void onNext(final Throwable t)
					{
						LOG.trace("Observed exception: "
								+ ((Contextualized) t).getContext());
						t.printStackTrace();
					}
				});

		LOG.trace("Period json: "
				+ JsonUtil.stringify(Duration.parse("PT123s")));
		// LOG.trace("Period json: "+JsonUtil.toJSON(Period.parse("PT1.4s")));

		final ClockConfig.ID parentID = ClockConfig.ID.valueOf("\"theRoot\""); // TODO
																				// loose
		// quotes?
		final Properties defaults = new Properties();
		defaults.setProperty("id", parentID.toString());
		defaults.setProperty("time", "PT1.2S");
		defaults.setProperty("status", ClockConfig.Status.WAITING.name());
		defaults.setProperty("error", "{}"); // FIXME
		defaults.setProperty("until", "\"P1W1DT0H0M0.000000001S\"");
		defaults.setProperty("forkParent", parentID.toString());
		defaults.setProperty("forkOffset", "10");
		defaults.setProperty("slaveTimeout", "\"PT123.000000001S\"");
		defaults.setProperty("wallClockRate", "1.4");
		LOG.trace("Using defaults: " + defaults);

		LOG.trace("Got defaults: "
				+ ConfigCache.getOrCreate(ClockConfig.class, defaults));

		final ClockConfig clock = JsonUtil.valueOf(
				"{\"id\":\"bla\",\"idasdsd\":[\"bla3w\"]}", ClockConfig.class,
				defaults);

		LOG.trace("Clock: " + clock);
		LOG.trace("Clock id: " + clock.id());
		clock.setProperty("id", "\"myClock\"");
		LOG.trace("Clock: " + clock);
		LOG.trace("Clock id: " + clock.id());

		LOG.trace("Clock until: " + clock.until().toJava8());

		// final Trigger trigger = JsonUtil.valueOf("{\"when\":{\"type\":1}}",
		// Trigger.class);

		// LOG.trace("Trigger: " + trigger + ", when ["
		// + trigger.type().getClass().getSimpleName() + "]: "
		// + trigger.when().type().name());
	}
}
