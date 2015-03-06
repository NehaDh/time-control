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

/**
 * {@link TimeControlConstants} encodes constants related to master-slave interaction
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface TimeControlConstants
{

	/** */
	String TIMER_ID_PARAM = "timerID";

	/** */
	String ORIGIN_URI_PARAM = "originURI";

	/** */
	String INTERVAL_MS_PARAM = "intervalMS";

	/** */
	String TIMEOUT_MS_PARAM = "timeoutMS";

	/** */
	String TIMER_BASE_PATH = "{" + TIMER_ID_PARAM + "}";

	/** */
	String SCENARIO_PATH = TIMER_BASE_PATH + "/scenario";

	/** */
	String CLOCK_PATH = TIMER_BASE_PATH + "/clock";

	/** */
	String TRIGGER_PATH = TIMER_BASE_PATH + "/trigger";

	/** */
	String SLAVE_PATH = TIMER_BASE_PATH + "/slave";

	/** */
	String SET_INTERVAL_PATH = TIMER_BASE_PATH + "/setInterval/{"
			+ ORIGIN_URI_PARAM + "}/{" + INTERVAL_MS_PARAM + "}";

	/** */
	String SET_TIMEOUT_PATH = TIMER_BASE_PATH + "/setTimeout/{"
			+ ORIGIN_URI_PARAM + "}/{" + TIMEOUT_MS_PARAM + "}";

}
