//package com.almende.timecontrol.http;
//
//import io.coala.json.JsonUtil;
//
//import javax.ws.rs.PUT;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.core.Response;
//
//import com.almende.timecontrol.TimerPool;
//import com.almende.timecontrol.api.TimeControlConstants;
//import com.almende.timecontrol.api.TimeControlMasterAPI;
//import com.almende.timecontrol.entity.Slave;
//import com.almende.timecontrol.entity.Trigger;
//
///**
// * {@link TimeControlMasterEcmaRestServlet} is a JSON REST wrapper around a
// * collection of {@link TimeControlMasterAPI} instances
// * 
// * @date $Date$
// * @version $Id$
// * @author <a href="mailto:rick@almende.org">rick</a>
// */
//@Path("/jscript")
//public class TimeControlMasterEcmaRestServlet
//{
//
//	/** */
//	// private static final Logger LOG = LogManager
//	// .getLogger(TimeControlMasterServlet.class);
//
//	@PUT
//	@Path(TimeControlConstants.SET_TIMEOUT_PATH)
//	public Response setTimeout(
//			final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID,
//			final @PathParam(TimeControlConstants.ORIGIN_URI_PARAM) String originURI,
//			final @PathParam(TimeControlConstants.TIMEOUT_MS_PARAM) Number timeoutMS)
//	{
//		// urldecode and register originURI as slave ?
//		final Slave slave = Slave.Builder.fromJSON(originURI).build();
//		TimerPool.getInstance(timerID).put(slave);
//
//		// TODO convert intervalMS to trigger for specified slave
//		final Trigger trigger = JsonUtil.valueOf(timeoutMS.toString(),
//				Trigger.class);// Trigger.Builder.fromJSON(intervalMS).build();
//
//		TimerPool.getInstance(timerID).put(trigger);
//		return Response.ok().build();
//	}
//
//	@PUT
//	@Path(TimeControlConstants.SET_INTERVAL_PATH)
//	public Response setInterval(
//			final @PathParam(TimeControlConstants.TIMER_ID_PARAM) String timerID,
//			final @PathParam(TimeControlConstants.ORIGIN_URI_PARAM) String originURI,
//			final @PathParam(TimeControlConstants.INTERVAL_MS_PARAM) Number intervalMS)
//	{
//		// urldecode and register originURI as slave ?
//		final Slave slave = Slave.Builder.fromJSON(originURI).build();
//		TimerPool.getInstance(timerID).put(slave);
//
//		// TODO convert intervalMS to trigger for specified slave
//		final Trigger trigger = JsonUtil.valueOf(intervalMS.toString(),
//				Trigger.class);// Trigger.Builder.fromJSON(intervalMS).build();
//
//		TimerPool.getInstance(timerID).put(trigger);
//		return Response.ok().build();
//	}
//
//}
