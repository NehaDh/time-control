package com.almende.timecontrol.rest;
//package com.almende.timecontrol.http;
//
//import io.coala.json.JsonUtil;
//
//import javax.ws.rs.Consumes;
//import javax.ws.rs.GET;
//import javax.ws.rs.PUT;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//
//import com.almende.timecontrol.TimerPool;
//import com.almende.timecontrol.api.TimeControlConstants;
//import com.almende.timecontrol.api.TimeControlMasterAPI;
//import com.almende.timecontrol.entity.Timer;
//import com.almende.timecontrol.entity.TimerStatusInfo;
//import com.almende.timecontrol.entity.Trigger;
//
///**
// * {@link TimeControlMasterJsonRestServlet} is a JSON REST decorator of
// * {@link TimeControlMasterAPI} instances
// * 
// * @date $Date$
// * @version $Id$
// * @author <a href="mailto:rick@almende.org">rick</a>
// */
//@Path("/jscript")
//public class TimeControlMasterJsonRestServlet
//{
//
//	@GET
//	@Path(TimeControlConstants.TIMER_BASE_PATH)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getStatus(
//			final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID)
//	{
//		final Timer.ID scenarioId = JsonUtil.valueOf(timerID, Timer.ID.class);
//
//		final TimerStatusInfo result = TimerPool.getInstance(timerID)
//				.getStatus(scenarioId);
//		return Response.ok(result).build();
//	}
//
//	@PUT
//	@Path(TimeControlConstants.TRIGGER_PATH)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response putTrigger(
//			final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID,
//			final String jsonBody)
//	{
//		final Trigger trigger = JsonUtil.valueOf(jsonBody, Trigger.class);
//		TimerPool.getInstance(timerID).put(trigger);
//
//		return Response.ok().build();
//	}
//
//	@PUT
//	@Path(TimeControlConstants.SCENARIO_PATH)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response putScenario(
//			final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID,
//			final String jsonBody)
//	{
//		final Timer scenario = JsonUtil.valueOf(jsonBody, Timer.class);
//		TimerPool.getInstance(timerID).put(scenario);
//
//		return Response.ok().build();
//	}
//}
