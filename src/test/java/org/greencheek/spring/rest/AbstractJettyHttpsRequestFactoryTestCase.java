package org.greencheek.spring.rest;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractJettyHttpsRequestFactoryTestCase extends AbstractHttpRequestFactoryTestCase
{
    static final private JettyHttpsTestServer testHttpsServer = new JettyHttpsTestServer();

	@BeforeClass
	public static void setUp() throws Exception
	{

		testHttpsServer.startSSLServer();
	}
	
	@AfterClass
	public static void tearDown()
	{
		testHttpsServer.stopHttpsServer();

	}

	@Override
	public String getBaseUrl() {
		return testHttpsServer.getBaseUrl();
	}

}