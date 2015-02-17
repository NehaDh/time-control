package com.almende.timecontrol.servlet;

import javax.ws.rs.Path;

import com.almende.timecontrol.api.TimeControlMasterAPI;
import com.almende.timecontrol.impl.TimeControlMasterImpl;

/**
 * {@link TimeControlMasterServlet}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
@Path("/time-control")
public class TimeControlMasterServlet 
{

	/** */
	private TimeControlMasterAPI master = null; // TODO inject?
	
	/**
	 * {@link TimeControlMasterServlet} zero-arg constructor
	 */
	private TimeControlMasterServlet()
	{
		this.master = new TimeControlMasterImpl();
	}
	

	// private static Config CONFIG = ConfigCache.getOrCreate(Config.class,
	// Config.IMPORTS);


//	@Path("{group-id}/")
}
