package com.almende.timecontrol.eve;

import io.coala.util.FileUtil;
import io.coala.util.LogUtil;

import java.io.InputStream;

import org.apache.logging.log4j.Logger;

import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.almende.eve.deploy.Boot;

/**
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class MAS
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(MAS.class);

	/** */
	private static final String EVE_YAML_DEFAULT = "eve.yaml";

	/**
	 * Boot the Multi-Agent System
	 *
	 * @param args the command-line arguments
	 */
	public static void main(final String[] args)
	{
		final String yamlFileName = args == null || args.length == 0 ? EVE_YAML_DEFAULT
				: args[0];

		try (final InputStream is = FileUtil.toInputStream(yamlFileName))
		{
			final Config config = YamlReader.load(is).expand();
			LOG.trace("Starting MAS for " + MAS.class.getSimpleName()
					+ " with config:\n" + config);
			Boot.boot(config);
		} catch (final Throwable t)
		{
			LOG.error("Problem starting MAS for " + MAS.class.getSimpleName(),
					t);
			System.exit(1);
		}
	}

}
