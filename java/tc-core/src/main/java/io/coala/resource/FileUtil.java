/* $Id: 6ff1fbf35cf8a0e61d916b594a95a94e27dff7e0 $
 * $URL: https://dev.almende.com/svn/abms/coala-common/src/main/java/com/almende/coala/util/FileUtil.java $
 *  
 * Part of the EU project All4Green, see http://www.all4green-project.eu/
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
 * Copyright © 2010-2013 Almende B.V.
 */
package io.coala.resource;

import io.coala.error.ExceptionBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link FileUtil} provides some file related utilities
 * 
 * @date $Date: 2014-08-08 07:08:29 +0200 (Fri, 08 Aug 2014) $
 * @version $Revision: 349 $ $Author: krevelen $
 * @author <a href="mailto:rick@almende.org">Rick van Krevelen</a>
 */
public class FileUtil // implements Util
{

	/** */
	private static final Logger LOG = LogManager.getLogger(FileUtil.class);

	/**
	 * {@link FileUtil} constructor
	 */
	private FileUtil()
	{
		// empty
	}

	/**
	 * @param path
	 * @return
	 * @throws A4GException
	 */
	public static InputStream getFileAsInputStream(final File path)
	{
		return getFileAsInputStream(path.getPath());
	}

	/**
	 * @param path
	 * @return
	 * @throws A4GException
	 */
	public static InputStream getFileAsInputStream(final URI path)
	{
		try
		{
			return getFileAsInputStream(path.toURL());
		} catch (final MalformedURLException e)
		{
			throw ExceptionBuilder.unchecked("ILLEGAL: uri " + path, e).build();
		}
	}

	/**
	 * @param path
	 * @return
	 * @throws A4GException
	 */
	public static InputStream getFileAsInputStream(final URL path)
	{
		return getFileAsInputStream(path.toExternalForm());
	}

	/**
	 * Searches the file system first and then the context class path for a file
	 * 
	 * @param path an absolute path in the file system or (context) classpath
	 * @return an {@link InputStream} for the specified {@code path}
	 * @throws A4GException e.g. if the file was not found
	 */
	public static InputStream getFileAsInputStream(final String path)
	{
		if (path == null)
			throw ExceptionBuilder.unchecked("NOT SET: path").build();

		final File file = new File(path);
		if (file.exists())
		{
			LOG.debug("Found '" + path + "' at location: "
					+ file.getAbsolutePath());
			try
			{
				// if (path.exists() && path.isFile())
				return new FileInputStream(file);

			} catch (final FileNotFoundException e)
			{
				throw ExceptionBuilder.unchecked(
						"NOT ALLOWED: " + file.getAbsolutePath(), e).build();
			}
		}

		try
		{
			final URL url = new URL(path);
			LOG.trace("Downloading '" + path + "'");
			return url.openStream();
		} catch (final MalformedURLException e)
		{
			// ignore
		} catch (final IOException e)
		{
			throw ExceptionBuilder.unchecked("NOT ALLOWED: url " + path, e)
					.build();
		}

		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		// FileUtil.class.getClassLoader()
		final URL resourcePath = cl.getResource(path);
		if (resourcePath == null)
		{
			throw ExceptionBuilder.unchecked(
					"NOT ALLOWED: File not found " + path + ", looked in"
							+ file.getAbsolutePath() + " and classpath")
					.build();
		}
		LOG.debug("Found '" + path + "' in classpath: " + resourcePath);
		return cl.getResourceAsStream(path);
	}

	/**
	 * @param path
	 * @return
	 */
	public static OutputStream getFileAsOutputStream(final String path)
	{
		return getFileAsOutputStream(new File(path), true);
	}

	/**
	 * @param path
	 * @return
	 */
	public static OutputStream getFileAsOutputStream(final File file,
			final boolean append)
	{
		if (file == null)
			throw ExceptionBuilder.unchecked("NOT SET: file").build();

		try
		{
			if (file.createNewFile())
				LOG.info("Created '" + file.getName() + "' at location: "
						+ file.getAbsolutePath());
			else
				LOG.debug("Found '" + file.getName() + "' at location: "
						+ file.getAbsolutePath());
			return new FileOutputStream(file, append);
		} catch (final IOException e)
		{
			throw ExceptionBuilder.unchecked("NOT AVAILABLE: file " + file, e)
					.build();
		}
	}

}
