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
package com.almende.timecontrol.api.eve;

import static org.aeonbits.owner.util.Collections.entry;
import static org.aeonbits.owner.util.Collections.map;
import io.coala.util.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;

import rx.Observer;

import com.almende.eve.agent.AgentBuilder;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.almende.eve.transport.http.embed.JettyLauncher;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * {@link EveUtil}
 * 
 * @date $Date$
 * @version $Revision$
 * @author <a href="mailto:gebruiker@almende.org">gebruiker</a>
 *
 */
public class EveUtil
{

	/** */
	private static final Logger LOG = LogManager.getLogger(EveUtil.class);

	public static void checkRegistered(final Class<?> type)
	{
		final ObjectMapper om = JOM.getInstance();
		final Package tcEntities = com.almende.timecontrol.entity.TimerConfig.class
				.getPackage();
		// final Properties[] imports = null;
		for (Method method : type.getMethods())
		{
			if (method.getReturnType() != Void.TYPE
					&& method.getReturnType().getPackage() == tcEntities)
				JsonUtil.checkRegistered(om, method.getReturnType());
			// else
			// LOG.trace("Ignoring de/serializers for "
			// + method.getReturnType().getName());
			for (Class<?> paramType : method.getParameterTypes())
				if (paramType.getPackage() == tcEntities)
					JsonUtil.checkRegistered(om, paramType);
			// else
			// LOG.trace("Ignoring de/serializers for "
			// + paramType.getName());
		}
	}

	private static boolean registered = false;

	/** */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static final <T extends EveAgentAPI> T valueOf(
			final AgentConfig agentConfig, final Class<T> agentType,
			final Map.Entry<String, ? extends JsonNode>... parameters)
	{
		synchronized (EveUtil.class)
		{
			if (!registered)
			{
				checkRegistered(EveTimeManagerAPI.class);
				registered = true;
			}
		}
		if (parameters != null && parameters.length != 0)
			for (Map.Entry<String, ? extends JsonNode> param : parameters)
			{
				LOG.trace("Importing agent parameter: " + param);
				agentConfig.set(param.getKey(), param.getValue());
			}

		final T result = (T) new AgentBuilder().with(agentConfig).build();
		final CountDownLatch waitUntilInit = new CountDownLatch(1);
		// Schedulers.newThread().createWorker().schedule(new Action0()
		// {
		// @Override
		// public void call()
		// {
		result.events().subscribe(new Observer<String>()
		{
			@Override
			public void onCompleted()
			{
				LOG.error("Completed observing events for agent: "
						+ agentConfig.getId());
			}

			@Override
			public void onError(final Throwable e)
			{
				LOG.error("Problem while observing events for agent: "
						+ agentConfig.getId(), e);
			}

			@Override
			public void onNext(final String event)
			{
				LOG.trace("Agent {} produced event: {}", agentConfig.getId(),
						event);
				if (event.equals(EveAgentAPI.AGENT_INITIALIZED))
					waitUntilInit.countDown();
			}
		});
		// }
		// });
		try
		{
			LOG.trace("Waiting for agent to load config: {}", agentConfig);
			waitUntilInit.await();
		} catch (final InterruptedException e)
		{
			LOG.warn("Interrupted while awaiting agent to load config");
		}
		return result;
	}

	/** */
	@SafeVarargs
	public static final <T extends EveAgentAPI> T valueOf(final String id,
			final Class<T> agentType,
			final Map.Entry<String, ? extends JsonNode>... parameters)
	{
		@SuppressWarnings("unchecked")
		final EveAgentConfig cfg = ConfigFactory.create(
				EveAgentConfig.class,
				EveAgentConfig.DEFAULT_VALUES,
				map(entry(EveAgentConfig.AGENT_CLASS_KEY, agentType.getName()),
						entry(EveAgentConfig.AGENT_ID_KEY, id),
						entry(EveAgentConfig.AGENT_ID_KEY, id)));

		final InputStream is = cfg.agentConfigStream();
		if (is != null)
		{
			final Config config = YamlReader.load(is).expand();
			try
			{
				is.close();
			} catch (final IOException ignore)
			{
				// empty
			}

			for (final JsonNode agent : (ArrayNode) config.get("agents"))
			{
				final JsonNode idNode = agent.get("id");
				if (idNode != null && !idNode.asText().equals(id))
					continue;

				LOG.info("Creating agent {} from config at {}", id,
						cfg.agentConfigUri());
				return valueOf(new AgentConfig((ObjectNode) agent), agentType,
						parameters);
			}
		}
		LOG.info("No config found at {} for agent: {}. "
				+ "Using default config", cfg.agentConfigUri(), id);
		return valueOf(cfg.agentConfig(), agentType, parameters);
	}

	public static void stop() throws Exception
	{
		// LOG.trace("Stopping Jetty server...");
		final Field field = JettyLauncher.class.getDeclaredField("server");
		field.setAccessible(true);
		((Server) field.get(null)).stop();
		// LOG.trace("Stopped Jetty server");

		// LOG.trace("Stopping RunQueue server...");
		// final RunQueue pool = (RunQueue) ThreadPool.getPool();
		// pool.shutdownNow();
		// //pool.awaitTermination(1, TimeUnit.MILLISECONDS);
		// LOG.trace("Stopped RunQueue");
	}

}
