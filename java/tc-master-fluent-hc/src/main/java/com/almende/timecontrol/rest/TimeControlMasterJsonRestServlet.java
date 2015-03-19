package com.almende.timecontrol.rest;

import javax.ws.rs.Path;

/**
 * {@link TimeControlMasterJsonRestServlet} is a JSON REST decorator of
 * {@link TimeControlMasterAPI} instances
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
@Path("/json")
public class TimeControlMasterJsonRestServlet
{

	// @GET
	// @Path(TimeControlConstants.TIMER_BASE_PATH)
	// @Produces(MediaType.APPLICATION_JSON)
	// public Response getStatus(
	// final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID)
	// {
	// final Timer.ID scenarioId = JsonUtil.valueOf(timerID, Timer.ID.class);
	//
	// final TimerStatusInfo result = TimerPool.getInstance(timerID)
	// .getStatus(scenarioId);
	// return Response.ok(result).build();
	// }
	//
	// @PUT
	// @Path(TimeControlConstants.TRIGGER_PATH)
	// @Consumes(MediaType.APPLICATION_JSON)
	// public Response putTrigger(
	// final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID,
	// final String jsonBody)
	// {
	// final Trigger trigger = JsonUtil.valueOf(jsonBody, Trigger.class);
	// TimerPool.getInstance(timerID).put(trigger);
	//
	// return Response.ok().build();
	// }
	//
	// @PUT
	// @Path(TimeControlConstants.SCENARIO_PATH)
	// @Consumes(MediaType.APPLICATION_JSON)
	// public Response putScenario(
	// final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID,
	// final String jsonBody)
	// {
	// final Timer scenario = JsonUtil.valueOf(jsonBody, Timer.class);
	// TimerPool.getInstance(timerID).put(scenario);
	//
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
