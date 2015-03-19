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

/**
 * {@link TimeControl} specifies common domain-specific JSON constants
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface TimeControl
{

	/** */
	String ID_KEY = "id";

	/** */
	String CONFIG_KEY = "config";

	/** */
	String TIMER_KEY = "timer";

	/** */
	String TIMER_ID_KEY = "timerId";

	/** */
	String CLOCK_KEY = "clock";

	/** */
	String CLOCKS_KEY = "clocks";

	/** */
	String SLAVE_KEY = "slave";

	/** */
	String SLAVES_KEY = "slaves";

	/** */
	String TRIGGER_ID_KEY = "triggerId";

	/** */
	String LAST_CALL_KEY = "lastCall";

	/** */
	String LAST_TRIGGER_ID_KEY = "lastTriggerId";

	/** */
	String LAST_JOB_ID_KEY = "lastJobId";

	/** */
	String TRIGGER_KEY = "trigger";

	/** */
	String TRIGGERS_KEY = "triggers";

	/** */
	String UPCOMING_JOBS_KEY = "upcomingJobs";

	/** */
	String SUBSCRIBERS_KEY = "subscribers";

	/** */
	String RESOLUTION_KEY = "resolution";

	/** */
	String OFFSET_KEY = "offset";

	/** */
	String DURATION_KEY = "duration";

	/** */
	String RECURRENCE_KEY = "recurrence";

	/** */
	String FORK_PARENT_ID_KEY = "forkParentID";

	/** */
	String FORK_TIME_KEY = "forkTime";

	/** */
	String STATUS_KEY = "status";

	/** */
	String PACE_KEY = "pace";

	/** */
	String TIME_KEY = "time";

	/** */
	String UNTIL_KEY = "until";

}