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
package io.coala.util;

import org.apache.logging.log4j.Logger;

/**
 * {@link LogUtil}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public class LogUtil
{

//	static
//	{
//		// divert java.util.logging to Log4J
//		System.setProperty("java.util.logging.manager",
//				org.apache.logging.log4j.jul.LogManager.class.getName());
//	}

	/** singleton design pattern constructor */
	private LogUtil()
	{
		// singleton design pattern
	}

	public static Logger getLogger(final Class<?> type)
	{
		return org.apache.logging.log4j.LogManager.getLogger(type);
	}
}
