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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.quantity.time.TimeQuantities;

/**
 * {@link UomTest}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@Ignore
public class UomTest
{

	/** */
	private static final Logger LOG = LogManager.getLogger(UomTest.class);

	@Test
	public void uomTestJava8()
	{
		LOG.trace("JSR-363 test: "
				+ Quantities.getQuantity(3.2, TimeQuantities.MILLISECOND));
	}
}
