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
package io.coala.config;

import io.coala.resource.FileUtil;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import org.aeonbits.owner.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * {@link YamlConfig}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface YamlConfig extends ConfigParent
{

	/** Configuration key for the YAML 1.1 format configuration location */
	String YAML_URI_KEY = "yaml-uri";

	/** Default (relative path) value for the YAML 1.1 formatted configuration */
	String YAML_URI_DEFAULT = "config.yaml";

	@Key(YAML_URI_KEY)
	@DefaultValue(YAML_URI_DEFAULT)
	String yamlURI();

	@DefaultValue("${" + YAML_URI_KEY + "}")
	@ConverterClass(YamlMapConverter.class)
	Map<String, Object> yamlMap();

	/**
	 * {@link YamlMapConverter} is a provider of {@link Map}s containing objects
	 * as specified in some file in <a href="http://yaml.org/">YAML</a> 1.1
	 * format
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	public class YamlMapConverter implements Converter<Map<String, Object>>
	{

		/** */
		private static final Logger LOG = LogManager
				.getLogger(YamlMapConverter.class);

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Object> convert(final Method targetMethod,
				final String text)
		{
			try
			{
				final InputStream is = FileUtil.getFileAsInputStream(text);
				return (Map<String, Object>) new Yaml().load(is);
			} catch (final Throwable t)
			{
				LOG.warn("Could not read YAML config at " + text, t);
				return Collections.emptyMap();
			}
		}
	}

}