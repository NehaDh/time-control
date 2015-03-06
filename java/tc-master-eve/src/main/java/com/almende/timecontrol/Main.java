//package com.almende.timecontrol;
//
//import org.apache.wink.server.internal.servlet.RestServlet;
//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.servlet.ServletContextHandler;
//import org.eclipse.jetty.servlet.ServletHolder;
//
//import com.almende.timecontrol.http.TimeControlMasterEcmaRestServlet;
//import com.almende.timecontrol.http.TimeControlMasterJsonRestServlet;
//
///**
// * {@link Main}
// * 
// * @date $Date$
// * @version $Id$
// * @author <a href="mailto:rick@almende.org">rick</a>
// */
//public class Main
//{
//
//	/**
//	 * @param args
//	 * @throws Exception
//	 */
//	public static void main(final String[] args) throws Exception
//	{
//		final Server server = new Server(Integer.valueOf(System.getenv("PORT")));
//		final ServletContextHandler context = new ServletContextHandler(
//				ServletContextHandler.SESSIONS);
//		context.setContextPath("/");
//		server.setHandler(context);
//		System.setProperty(RestServlet.APPLICATION_INIT_PARAM,
//				TimeControlMasterEcmaRestServlet.class.getName());
//		context.addServlet(new ServletHolder(new RestServlet()), "/ecma/*");
//		// TODO check if two REST servlets works
//		System.setProperty(RestServlet.APPLICATION_INIT_PARAM,
//				TimeControlMasterJsonRestServlet.class.getName());
//		context.addServlet(new ServletHolder(new RestServlet()), "/json/*");
//		server.start();
//		server.join();
//	}
//}
