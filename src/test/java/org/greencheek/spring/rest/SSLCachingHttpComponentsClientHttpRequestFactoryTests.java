package org.greencheek.spring.rest;

import org.springframework.http.client.ClientHttpRequestFactory;

public class SSLCachingHttpComponentsClientHttpRequestFactoryTests extends
        AbstractJettyHttpRequestFactoryTestCase {

	@Override
	protected ClientHttpRequestFactory createRequestFactory() {
		// TODO Auto-generated method stub
		return new SSLCachingHttpComponentsClientHttpRequestFactory(true);
	}

}
