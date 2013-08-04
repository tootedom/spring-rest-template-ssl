package org.greencheek.spring.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.greencheek.utils.ClientSSLSetupUtil;
import org.junit.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SSLCachingRestTemplateTest {

    static final private JettyHttpsTestServer testHttpsServer = new JettyHttpsTestServer();
    private static String BASE_URL;
    private RestTemplate restTemplate;
    private final ClientSSLSetupUtil SSLSetupUtil = new ClientSSLSetupUtil();
	
	@BeforeClass
	public static void startJettyServer() throws Exception {
        testHttpsServer.startSSLServer();
        BASE_URL = testHttpsServer.getBaseUrl();
	}
	
	@Before
	public void setup()
	{
        SSLSetupUtil.setup();
		RestTemplate template = new RestTemplate();
		template.setRequestFactory(new SSLCachingHttpComponentsClientHttpRequestFactory(true));
		setRestTemplate(template);
	}
	
	@AfterClass
	public static void stopJettyServer() throws Exception {
        testHttpsServer.startSSLServer();
	}

    @After
    public void tearDown()
    {
        SSLSetupUtil.tearDown();
    }
	
	@Test
	public void testPost()
	{
		RestTemplate template = getRestTemplate();
		
		String returnData = template.postForObject(BASE_URL + "/echo/{saying}", new String("Body Content"), String.class,"URIPATH");
		
		assertEquals("Return Object is not correct","Body Content",returnData);
		
		HttpHeaders headers = template.headForHeaders(BASE_URL + "/echo/{saying}","HEADER_VALUE");
		
		List<String> values = headers.get("X-PATH");
		
		assertEquals("Should be one header value",values.size(),1);
		
		assertEquals("Header value is incorrect","HEADER_VALUE",values.get(0));
		
		
	}

    @Test
    public void testExchange() {
        RestTemplate template = getRestTemplate();

        ResponseEntity<String> entity = template.exchange(BASE_URL + "/methods/{saying}", HttpMethod.DELETE, new HttpEntity<String>(new String("Body Content"), null), String.class, "delete");

        HttpHeaders headers = entity.getHeaders();

        List<String> values = headers.get("X-METHOD");

        assertEquals("Should be one header value",values.size(),1);

        assertEquals("Header value is incorrect","DELETE",values.get(0));
    }

    @Test
    public void testGet() {
        RestTemplate template = getRestTemplate();

        String entity = template.getForObject(BASE_URL + "/echo/{saying}", String.class,"URIPATH");

        assertEquals("Return Object is not correct","",entity);

    }

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}


}
