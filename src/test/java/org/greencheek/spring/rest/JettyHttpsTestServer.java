package org.greencheek.spring.rest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.greencheek.spring.rest.servletmocks.*;
import org.greencheek.utils.SocketUtil;

import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;

import static org.junit.Assert.fail;

public class JettyHttpsTestServer {
	
	private Server jettyServer;

    private int HTTPS_PORT_NUM;
    
	public String clientKeyStore = null;
	public String clientTrustStore = null;

    private String serverKeyStore = null;
    private String serverTrustStore = null;

		
	private String previousKeyStore;
	private String previousKeyStorePassword;
	private String previousKeyStoreType;
	private String previousTrustStore;
	
	private String baseUrl;
	private String rootUrl;
	private String url;
	
	public JettyHttpsTestServer() {
		setRootUrl("https://localhost:");
	}
	
	public JettyHttpsTestServer(String rootUrl){
		setRootUrl(rootUrl);
	}
	
	
	public void startSSLServer() throws Exception
	{				
		previousKeyStore = System.getProperty("javax.net.ssl.keyStore");
		previousKeyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
		previousKeyStoreType = System.getProperty("javax.net.ssl.keyStoreType");
		previousTrustStore = System.getProperty("javax.net.ssl.trustStore");
		
    	HTTPS_PORT_NUM = SocketUtil.findUnusedPort();

        setBaseUrl(getRootUrl() + HTTPS_PORT_NUM);


		try {

			clientTrustStore = ResourceUtils.getURL("classpath:sslcerts/client.truststore").getPath();
			clientKeyStore = ResourceUtils.getURL("classpath:sslcerts/client.p12").getPath();
            serverTrustStore = ResourceUtils.getURL("classpath:sslcerts/server.truststore").getPath();
            serverKeyStore = ResourceUtils.getURL("classpath:sslcerts/server.p12").getPath();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("cannot locate server certificates");
		}
				
		// 
		// Set up the server
		//
		jettyServer = createAndStartLocalHttpsJettyServer(HTTPS_PORT_NUM,serverTrustStore,serverKeyStore,"changeit","PKCS12");
		
		
		url = "https://localhost:" + HTTPS_PORT_NUM +"/";

		System.out.println("SERVER_STARTED at " + url);

	}
	
	public void stopHttpsServer()
	{
		try
		{
			if(jettyServer!=null) jettyServer.stop();
		}
		catch(Exception e)
		{
			System.out.println("ERROR Shutting down jetty server 1");
			e.printStackTrace();
		}
		
		if(previousKeyStore==null)
		{
			System.clearProperty("javax.net.ssl.keyStore");
		}
		else
		{
			System.setProperty("javax.net.ssl.keyStore",previousKeyStore);
		}
		
		if(previousKeyStorePassword==null)
		{
			System.clearProperty("javax.net.ssl.keyStorePassword");
		}
		else
		{
			System.setProperty("javax.net.ssl.keyStorePassword",previousKeyStorePassword);
		}
		
		if(previousKeyStoreType==null)
		{
			System.clearProperty("javax.net.ssl.keyStoreType");
		}
		else
		{
			System.setProperty("javax.net.ssl.keyStoreType",previousKeyStoreType);
		}
		
		if(previousTrustStore==null)
		{
			System.clearProperty("javax.net.ssl.trustStore");
		}
		else
		{
			System.setProperty("javax.net.ssl.trustStore",previousTrustStore);
		}
		
	}
//	
//	
	private Server createAndStartLocalHttpsJettyServer(int portNum, String trustStore,
			   String keyStore,String pass,String keyStoreType) throws Exception
	{
		
		Server jettyServer = new Server();
        ServletContextHandler jettyContext = new ServletContextHandler(jettyServer, "/");

// If you wanted http		
//		SocketConnector plainSocketConnector = new SocketConnector();
//		plainSocketConnector.setPort(httpPortNum);
//		jettyServer.addConnector(plainSocketConnector);

        SslContextFactory factory = new SslContextFactory();
        factory.setKeyStorePath(keyStore);
        factory.setKeyStorePassword(pass);
        factory.setKeyStoreType(keyStoreType);
        factory.setTrustStore(trustStore);
        factory.setTrustStoreType("JKS");
        factory.setTrustStorePassword(pass);
        factory.setWantClientAuth(true);
        factory.setNeedClientAuth(true);

        SslConnector sslConnector = new SslSelectChannelConnector(factory);
		sslConnector.setPort(portNum);


        jettyContext.addServlet(new ServletHolder(new MockEchoHttpHandler()), "/echo");
        jettyContext.addServlet(new ServletHolder(new MockEchoHttpHandler()), "/echo/*");
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


        jettyServer.addConnector(sslConnector);
		jettyServer.start();


		return jettyServer;
	}
	

	
	public String getBaseUrl() {
		// TODO Auto-generated method stub
		return baseUrl;
	}
	
	private void setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

	public String getRootUrl() {
		return rootUrl;
	}

}
