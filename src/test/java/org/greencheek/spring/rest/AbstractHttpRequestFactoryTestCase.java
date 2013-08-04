package org.greencheek.spring.rest;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FileCopyUtils;


import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.*;


/**
 * All extensions shoud call {@link #createFactory} in the before method,
 * and implement the createRequestFactoryMethod
 * 
 * @author dominict
 *
 */
public abstract class AbstractHttpRequestFactoryTestCase {
	private ClientHttpRequestFactory factory;


    public static final byte[] body;
    static {
        byte [] b;
        try {
            b = "Hello World".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            b = "Hello World".getBytes();
        }
        body = b;
    }

	public abstract String getBaseUrl();
	
	public void baseSetup() {

    }

	@Before
	public final void createFactory() {
        baseSetup();
		factory = createRequestFactory();
	}


    public void baseTearDown() {

    }

    @After
    public final void basicTearDown() {
        baseTearDown();
    }

	protected abstract ClientHttpRequestFactory createRequestFactory();

	@Test
	public void testStatusNotFound() throws Exception {
		ClientHttpRequest request =
				factory.createRequest(new URI(getBaseUrl() + "/status/notfound"), HttpMethod.GET);
		assertEquals("Invalid HTTP method", HttpMethod.GET, request.getMethod());
		ClientHttpResponse response = request.execute();
		assertEquals("Invalid status code", HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void testEcho() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/echo"), HttpMethod.PUT);
		assertEquals("Invalid HTTP method", HttpMethod.PUT, request.getMethod());
        String headerName = "MyHeader";
        String headerValue1 = "value1";
        String headerValue2 = "value2";

        addHeader(request,headerName, headerValue1);
        addHeader(request,headerName, headerValue2);

		FileCopyUtils.copy(body, request.getBody());
		ClientHttpResponse response = request.execute();
		assertEquals("Invalid status code", HttpStatus.OK, response.getStatusCode());
		assertTrue("Header not found", response.getHeaders().containsKey(headerName));
		
		
		assertTrue("Header value not found", StringUtils.join(response.getHeaders().get(headerName), ", ").contains(headerValue1));
        assertTrue("Header value not found", StringUtils.join(response.getHeaders().get(headerName), ", ").contains(headerValue2));

		byte[] result = FileCopyUtils.copyToByteArray(response.getBody());
		assertTrue("Invalid body", Arrays.equals(body, result));
    }

    public void addHeader(ClientHttpRequest request, String name, String value) {
        request.getHeaders().add(name, value);

    }

	@Test(expected = IllegalStateException.class)
	public void testSettingBodyMultipleTimeThrowsException() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/echo"), HttpMethod.POST);
		FileCopyUtils.copy(body, request.getBody());
		ClientHttpResponse response = request.execute();
		try {
			FileCopyUtils.copy(body, request.getBody());
		}
		finally {
			response.close();
		}
	}
	
	@Test
	public void testPostBinaryContent() throws Exception {
		ClientHttpResponse response = null;
	
		try
		{
			ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/echo"), HttpMethod.POST);			
			FileCopyUtils.copy(body, request.getBody());
			response = request.execute();
			byte[] bodyResponse = FileCopyUtils.copyToByteArray(response.getBody());
			
			assertArrayEquals(body, bodyResponse);		
		}
		finally {
			if(response!=null) {
				response.close();
			}
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testFailsToAddHeadersAfterExecute() throws Exception {
		ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/echo"), HttpMethod.POST);
		addHeader(request, "MyHeader", "value");
		FileCopyUtils.copy(body, request.getBody());
		ClientHttpResponse response = request.execute();
		try {
			request.getHeaders().add("MyHeader", "value");
		}
		finally {
			response.close();
		}
		
		
	}

	@Test
	public void testHttpMethods() throws Exception {
		assertHttpMethod("get", HttpMethod.GET);
		assertHttpMethod("head", HttpMethod.HEAD);
		assertHttpMethod("post", HttpMethod.POST);
		assertHttpMethod("put", HttpMethod.PUT);
		assertHttpMethod("options", HttpMethod.OPTIONS);
		assertHttpMethod("delete", HttpMethod.DELETE);
	}

	private void assertHttpMethod(String path, HttpMethod method) throws Exception {
		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/methods/" + path), method);
			response = request.execute();
            assertEquals("Header not set to attempted method",method.name(),response.getHeaders().get("X-METHOD").get(0));
			assertEquals("Invalid method", path.toUpperCase(Locale.ENGLISH), request.getMethod().name());
		}
		finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	@Test
	public void testPermRedirectIsFollowed() throws Exception {
		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/redirect"), HttpMethod.PUT);
			response = request.execute();
			if(response instanceof SSLCachingHttpComponentsClientHttpResponse)
			{
                SSLCachingHttpComponentsClientHttpResponse urlFetcherResponse = (SSLCachingHttpComponentsClientHttpResponse)response;
				assertFalse("Invalid Location value", StringUtils.isBlank(urlFetcherResponse.getFinalUri()));
				assertEquals("Invalid Location value", getBaseUrl() + "/redirect", urlFetcherResponse.getFinalUri());

			}

		} finally {
			if (response != null) {
				response.close();
				response = null;
			}
		}
		try {
			ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/redirect"), HttpMethod.GET);
			response = request.execute();
			if(response instanceof SSLCachingHttpComponentsClientHttpResponse)
			{
                SSLCachingHttpComponentsClientHttpResponse urlFetcherResponse = (SSLCachingHttpComponentsClientHttpResponse)response;
				assertEquals("Invalid Location value", getBaseUrl() + "/status/ok", urlFetcherResponse.getFinalUri());
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	@Test
	public void testTemporaryRedirectSeeOtherIsFollowed() throws Exception {
		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/seeother"), HttpMethod.PUT);
			response = request.execute();
			if(request instanceof SSLCachingHttpComponentsClientHttpRequest)
			{
                SSLCachingHttpComponentsClientHttpResponse urlFetcherResponse = (SSLCachingHttpComponentsClientHttpResponse)response;
				assertEquals("Invalid Location value", getBaseUrl() + "/status/ok", urlFetcherResponse.getFinalUri());
			}

		} finally {
			if (response != null) {
				response.close();
				response = null;
			}
		}
		try {
			ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/seeother"), HttpMethod.GET);
			response = request.execute();
			if(request instanceof SSLCachingHttpComponentsClientHttpRequest)
			{
                SSLCachingHttpComponentsClientHttpResponse urlFetcherResponse = (SSLCachingHttpComponentsClientHttpResponse)response;
				assertEquals("Invalid Location value", getBaseUrl() + "/status/ok", urlFetcherResponse.getFinalUri());
			}

		} finally {
			if (response != null) {
				response.close();
			}
		}
	}


    @Test
    public void testPermRedirectIsNotFollowed() throws Exception {
        ClientHttpResponse response = null;
        try {
            ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/redirect"), HttpMethod.PUT);

            if(request instanceof SSLCachingHttpComponentsClientHttpRequest) {
                ((SSLCachingHttpComponentsClientHttpRequest)request).setFollowRedirects(false);
            }

            response = request.execute();
            if(response instanceof SSLCachingHttpComponentsClientHttpResponse)
            {
                SSLCachingHttpComponentsClientHttpResponse urlFetcherResponse = (SSLCachingHttpComponentsClientHttpResponse)response;
                assertFalse("Invalid Location value", StringUtils.isBlank(urlFetcherResponse.getFinalUri()));
                assertEquals("Invalid Location value",getBaseUrl() + "/redirect", urlFetcherResponse.getFinalUri());

            }

        } finally {
            if (response != null) {
                response.close();
                response = null;
            }
        }
        try {
            ClientHttpRequest request = factory.createRequest(new URI(getBaseUrl() + "/redirect"), HttpMethod.GET);
            response = request.execute();
            if(response instanceof SSLCachingHttpComponentsClientHttpResponse)
            {
                SSLCachingHttpComponentsClientHttpResponse urlFetcherResponse = (SSLCachingHttpComponentsClientHttpResponse)response;
                assertEquals("Invalid Location value",getBaseUrl() + "/redirect", urlFetcherResponse.getFinalUri());
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }


}
