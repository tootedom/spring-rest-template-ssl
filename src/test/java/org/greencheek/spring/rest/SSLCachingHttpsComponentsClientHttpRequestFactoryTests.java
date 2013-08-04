package org.greencheek.spring.rest;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;

import static org.junit.Assert.fail;

public class SSLCachingHttpsComponentsClientHttpRequestFactoryTests extends
        AbstractJettyHttpsRequestFactoryTestCase {

    private String previousKeyStore;
    private String previousKeyStorePassword;
    private String previousKeyStoreType;
    private String previousTrustStore;
    private String previousTrustStoreType;
    private String previousTrustStorePassword;

    public void baseSetup() {
        previousKeyStore = System.getProperty("javax.net.ssl.keyStore");
        previousKeyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        previousKeyStoreType = System.getProperty("javax.net.ssl.keyStoreType");
        previousTrustStore = System.getProperty("javax.net.ssl.trustStore");
        previousTrustStoreType = System.getProperty("javax.net.ssl.trustStoreType");
        previousTrustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");


        String clientTrustStore;
        String clientKeyStore;
        try {

            clientTrustStore = ResourceUtils.getURL("classpath:sslcerts/client.truststore").getPath();
            clientKeyStore = ResourceUtils.getURL("classpath:sslcerts/client.p12").getPath();


            System.setProperty("javax.net.ssl.keyStore", clientKeyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
            System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
            System.setProperty("javax.net.ssl.trustStore", clientTrustStore);
            System.setProperty("javax.net.ssl.trustStorePassword","changeit");
            System.setProperty("javax.net.ssl.trustStoreType","JKS");

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("cannot locate server certificates");
        }
    }

    public void baseTearDown() {
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

        if(previousTrustStore==null)
        {
            System.clearProperty("javax.net.ssl.trustStore");
        }
        else
        {
            System.setProperty("javax.net.ssl.trustStore",previousTrustStore);
        }

        if(previousTrustStoreType==null)
        {
            System.clearProperty("javax.net.ssl.trustStoreType");
        }
        else
        {
            System.setProperty("javax.net.ssl.trustStoreType",previousTrustStoreType);
        }

        if(previousTrustStorePassword==null)
        {
            System.clearProperty("javax.net.ssl.trustStorePassword");
        }
        else
        {
            System.setProperty("javax.net.ssl.trustStore",previousTrustStorePassword);
        }
    }

	@Override
	protected ClientHttpRequestFactory createRequestFactory() {
		// TODO Auto-generated method stub
		return new SSLCachingHttpComponentsClientHttpRequestFactory(true);
	}

}
