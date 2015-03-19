package com.almende.timecontrol.rest;

import javax.ws.rs.Path;

/**
 * {@link TimeControlMasterEcmaRestServlet} is a JSON REST wrapper around a
 * collection of {@link TimeControlMasterAPI} instances
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
@Path("/ecma")
public class TimeControlMasterEcmaRestServlet
{

	/** */
	// private static final Logger LOG = LogManager
	// .getLogger(TimeControlMasterServlet.class);

	// @PUT
	// @Path(TimeControlConstants.SET_TIMEOUT_PATH)
	// public Response setTimeout(
	// final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID,
	// final @PathParam(TimeControlConstants.ORIGIN_URI_PARAM) String originURI,
	// final @PathParam(TimeControlConstants.TIMEOUT_MS_PARAM) Number timeoutMS)
	// {
	// // urldecode and register originURI as slave ?
	// final Slave slave = Slave.Builder.fromJSON(originURI).build();
	// TimerPool.getInstance(timerID).put(slave);
	//
	// // TODO convert intervalMS to trigger for specified slave
	// final Trigger trigger = JsonUtil.valueOf(timeoutMS.toString(),
	// Trigger.class);// Trigger.Builder.fromJSON(intervalMS).build();
	//
	// TimerPool.getInstance(timerID).put(trigger);
	// return Response.ok().build();
	// }
	//
	// @PUT
	// @Path(TimeControlConstants.SET_INTERVAL_PATH)
	// public Response setInterval(
	// final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID,
	// final @PathParam(TimeControlConstants.ORIGIN_URI_PARAM) String originURI,
	// final @PathParam(TimeControlConstants.INTERVAL_MS_PARAM) Number
	// intervalMS)
	// {
	// // urldecode and register originURI as slave ?
	// final Slave slave = Slave.Builder.fromJSON(originURI).build();
	// TimerPool.getInstance(timerID).put(slave);
	//
	// // TODO convert intervalMS to trigger for specified slave
	// final Trigger trigger = JsonUtil.valueOf(intervalMS.toString(),
	// Trigger.class);// Trigger.Builder.fromJSON(intervalMS).build();
	//
	// TimerPool.getInstance(timerID).put(trigger);
	// return Response.ok().build();
	// }

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception
	{
		// final Server server = new
		// Server(Integer.valueOf(System.getenv("PORT")));
		// final ServletContextHandler context = new ServletContextHandler(
		// ServletContextHandler.SESSIONS);
		// context.setContextPath("/");
		// server.setHandler(context);
		// System.setProperty(RestServlet.APPLICATION_INIT_PARAM,
		// TimeControlMasterEcmaRestServlet.class.getName());
		// context.addServlet(new ServletHolder(new RestServlet()), "/ecma/*");
		// // TODO check if two REST servlets works
		// System.setProperty(RestServlet.APPLICATION_INIT_PARAM,
		// TimeControlMasterJsonRestServlet.class.getName());
		// context.addServlet(new ServletHolder(new RestServlet()), "/json/*");
		// server.start();
		// server.join();
	}

}
