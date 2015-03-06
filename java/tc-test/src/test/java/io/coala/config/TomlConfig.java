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
import java.util.Map;

import org.aeonbits.owner.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.moandjiezana.toml.Toml;

/**
 * {@link TomlConfig}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
public interface TomlConfig extends ConfigParent
{

	/** Configuration key for the TOML format configuration location */
	String TOML_URI_KEY = "toml-uri";

	/** Default (relative path) value for the TOML formatted configuration */
	String TOML_URI_DEFAULT = "config.toml";

	@Key(TOML_URI_KEY)
	@DefaultValue(TOML_URI_DEFAULT)
	String tomlURI();

	@DefaultValue("${" + TOML_URI_KEY + "}")
	@ConverterClass(TomlMapConverter.class)
	Map<String, Object> tomlMap();

	@DefaultValue("${" + TOML_URI_KEY + "}")
	@ConverterClass(TomlConverter.class)
	Toml toml();

	/**
	 * {@link TomlMapConverter} is a provider of {@link Map}s containing objects
	 * as specified in some file in <a
	 * href="https://github.com/toml-lang/toml/tree/v0.3.1">TOML v0.3.1</a>
	 * format
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	public class TomlMapConverter implements Converter<Map<String, Object>>
	{

		/** */
		private static final Logger LOG = LogManager
				.getLogger(TomlMapConverter.class);

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, Object> convert(final Method targetMethod,
				final String text)
		{
			final Toml result = new Toml();
			try
			{
				final InputStream is = FileUtil.toInputStream(text);
				result.parse(is);
			} catch (final Throwable t)
			{
				LOG.warn("Could not read YAML config at " + text, t);
			}
			return (Map<String, Object>) result.to(Map.class);
		}
	}

	/**
	 * {@link TomlConverter} is a provider of {@link Toml} instances containing
	 * objects as specified in some file in <a
	 * href="https://github.com/toml-lang/toml/tree/v0.3.1">TOML v0.3.1</a>
	 * format
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	public class TomlConverter implements Converter<Toml>
	{

		/** */
		private static final Logger LOG = LogManager
				.getLogger(TomlConverter.class);

		@Override
		public Toml convert(final Method targetMethod, final String text)
		{
			final Toml result = new Toml();
			try
			{
				final InputStream is = FileUtil.toInputStream(text);
				result.parse(is);
			} catch (final Throwable t)
			{
				LOG.warn("Could not read YAML config at " + text, t);
			}
			return result;
		}
	}
}