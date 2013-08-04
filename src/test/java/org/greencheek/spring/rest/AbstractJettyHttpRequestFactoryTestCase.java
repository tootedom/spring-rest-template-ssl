package org.greencheek.spring.rest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.greencheek.spring.rest.servletmocks.*;
import org.greencheek.utils.SocketUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;




public abstract class AbstractJettyHttpRequestFactoryTestCase extends
        AbstractHttpRequestFactoryTestCase {

	private static Server jettyServer;

	private static String BASE_URL = "http://localhost:";		

    private static int JETTY_PORT_NUM;
    
    private String baseUrl = BASE_URL; 
    
	
	@BeforeClass
	public static void startJettyServer() throws Exception {	
		JETTY_PORT_NUM = SocketUtil.findUnusedPort();
        BASE_URL += JETTY_PORT_NUM;
		jettyServer = new Server(JETTY_PORT_NUM);
        ServletContextHandler jettyContext = new ServletContextHandler(jettyServer, "/");
		jettyContext.addServlet(new ServletHolder(new MockEchoHttpHandler()), "/echo");
		jettyContext.addServlet(new ServletHolder(new MockStatusHttpHandler(200)), "/status/ok");
		jettyContext.addServlet(new ServletHolder(new MockStatusHttpHandler(404)), "/status/notfound");
		jettyContext.addServlet(new ServletHolder(new MockMethodHttpHandler("DELETE")), "/methods/delete");
		jettyContext.addServlet(new ServletHolder(new MockMethodHttpHandler("GET")), "/methods/get");
		jettyContext.addServlet(new ServletHolder(new MockMethodHttpHandler("HEAD")), "/methods/head");
		jettyContext.addServlet(new ServletHolder(new MockMethodHttpHandler("OPTIONS")), "/methods/options");
		jettyContext.addServlet(new ServletHolder(new MockMethodHttpHandler("POST")), "/methods/post");
		jettyContext.addServlet(new ServletHolder(new MockMethodHttpHandler("PUT")), "/methods/put");
		jettyContext.addServlet(new ServletHolder(new MockRedirectHttpHandler("/status/ok")), "/redirect");
		jettyContext.addServlet(new ServletHolder(new MockRedirectSeeOtherServlet("/status/ok")), "/seeother");
		jettyServer.start();
	}
	

	@AfterClass
	public static void stopJettyServer() throws Exception {
		if (jettyServer != null) {
			jettyServer.stop();
		}
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}

	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}



	

}
