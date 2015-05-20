package com.almende.timecontrol.eve;

import io.coala.util.JsonUtil;
import io.coala.util.LogUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;

import rx.Observable;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import com.almende.eve.agent.Agent;
import com.almende.eve.agent.AgentConfig;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Optional;
import com.almende.timecontrol.TimeManagerImpl;
import com.almende.timecontrol.api.eve.EveTimeAgentAPI;
import com.almende.timecontrol.entity.TimerConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * {@link TimerAgentTest} wraps/proxies for a {@link TimeManagerImpl} instance
 * with the JSON-RPC capability provided by this Eve {@link Agent}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class ManagerAgent extends Agent implements EveTimeAgentAPI
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(ManagerAgent.class);

	/**************************************************************************/

	/** */
	private final Subject<AgentEventType, AgentEventType> events = ReplaySubject
			.create();

	@Override
	public Observable<AgentEventType> events()
	{
		return this.events.asObservable();
	}

	@Override
	protected void loadConfig()
	{
		super.loadConfig();
		this.events.onNext(AgentEventType.AGENT_INITIALIZED);
	}

	@Override
	public void destroy()
	{
		super.destroy();
		this.events.onNext(AgentEventType.AGENT_DESTROYED);
		this.events.onCompleted();
	}

	/**************************************************************************/

	@JsonIgnore
	@Access(AccessType.PUBLIC)
	public List<URI> findTimerURIs(
			@Optional @Name(ID_PARAM) final String timerID)
	{
		if (timerID != null)
		{
			final AgentConfig timerCfg = new AgentConfig(timerID, getConfig()
					.with("timerConfig"));

			return TimeManagerAgent.getInstance(timerCfg).getUrls();
		}
		final List<URI> result = new ArrayList<>();
		for (TimeManagerAgent agent : TimeManagerAgent.getInstances())
			result.addAll(agent.getUrls());
		LOG.trace("Found timer URIs: {}", JsonUtil.toJSON(result));
		return result;
	}

	@JsonIgnore
	@Access(AccessType.PUBLIC)
	public List<URI> createTimer(@Name(CONFIG_PARAM) final TimerConfig config)
	{
		final AgentConfig timerCfg = new AgentConfig(config.id().getValue(),
				getConfig().with("timerConfig"));

		final List<URI> result = TimeManagerAgent.getInstance(config, timerCfg)
				.getUrls();
		LOG.trace("Created timer at URIs: {}", JsonUtil.toJSON(result));
		return result;
	}

	/**************************************************************************/

	/**
	 * Boot the Multi-Agent System
	 *
	 * @param args the command-line arguments
	 */
	public static void main(final String[] args)
	{
		MAS.main(args);
	}

}
