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
package com.almende.timecontrol.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Separator;
import org.aeonbits.owner.Config.Sources;

/**
 * {@link ConfigParent} annotates possible configuration file {@link Sources},
 * as specified with the {@link Config#CONFIG_URI_KEY} System property name, or
 * otherwise to find the default {@link ConfigConstants#CONFIG_URI_DEFAULT} file
 * name in either the current working directory, user home directory, or the
 * class path
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@LoadPolicy(LoadType.MERGE)
@Sources({ "file:${" + ConfigConstants.CONFIG_URI_KEY + "}",
		"classpath:${" + ConfigConstants.CONFIG_URI_KEY + "}",
		"file:${user.dir}/" + ConfigConstants.CONFIG_URI_DEFAULT,
		"file:~/" + ConfigConstants.CONFIG_URI_DEFAULT,
		"classpath:" + ConfigConstants.CONFIG_URI_DEFAULT })
@Separator(",")
public interface ConfigParent extends Config
{
 // to be
}