/* $Id$
 * $URL$
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
package com.almende.timecontrol.api;

import org.joda.time.Instant;

import com.almende.timecontrol.entity.Clock;
import com.almende.timecontrol.entity.Job;
import com.almende.timecontrol.entity.Slave;
import com.almende.timecontrol.entity.Trigger;

/**
 * {@link TimeControlSlaveAPI}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface TimeControlSlaveAPI
{
	/**
	 * @return the latest {@link Slave} status information for this
	 *         {@link TimeControlSlaveAPI}, useful for reconnecting
	 */
	Slave getStatus();

	/**
	 * called (asynchronously) in response to
	 * {@link TimeControlMasterAPI#put(Slave)} <i>iff</i>
	 * {@link Slave#getAddress()} {@code != null}
	 * 
	 * @param clock the new {@link Clock} state
	 * @return the {@link Instant} at which the next (local) event is scheduled
	 *         for this {@link TimeControlSlaveAPI}, or {@code null} if unknown
	 *         or irrelevant
	 */
	Instant notify(Clock clock);

	/**
	 * called (asynchronously) as caused by the {@link Trigger} subscribed using
	 * {@link TimeControlMasterAPI#put(Trigger)}
	 * 
	 * @param job
	 */
	void notify(Job job);
}