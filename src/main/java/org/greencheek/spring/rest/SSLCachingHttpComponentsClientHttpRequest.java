/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.greencheek.spring.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.client.protocol.ClientContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

/**
 * {@link org.springframework.http.client.ClientHttpRequest} implementation that uses
 * Apache HttpComponents HttpClient to execute requests.
 *
 * <p>Created via the {@link SSLCachingHttpComponentsClientHttpRequestFactory}.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @since 3.1
 * @see SSLCachingHttpComponentsClientHttpRequestFactory#createRequest(URI, HttpMethod)
 */
final class SSLCachingHttpComponentsClientHttpRequest extends AbstractClientHttpRequest {

    private final HttpClient httpClient;

    private final HttpUriRequest httpRequest;

    private final HttpContext httpContext;

    private final AtomicReference<Principal> sslTokenCache;

    private final boolean useSSLSessionCaching;

    private ByteArrayOutputStream bufferedOutput = new ByteArrayOutputStream();

    public SSLCachingHttpComponentsClientHttpRequest(HttpClient httpClient, HttpUriRequest httpRequest,
                                                     HttpContext httpContext, AtomicReference<Principal> sslTokenCache,
                                                     boolean useSSLSessionCaching) {
        this.httpClient = httpClient;
        this.httpRequest = httpRequest;
        this.httpContext = httpContext;
        this.sslTokenCache = sslTokenCache;
        this.useSSLSessionCaching = useSSLSessionCaching;
    }


    public HttpMethod getMethod() {
        return HttpMethod.valueOf(this.httpRequest.getMethod());
    }

    public URI getURI() {
        return this.httpRequest.getURI();
    }

    /**
     * Set if the client should follow redirects
     * @param followRedirects
     */
    public void setFollowRedirects(boolean followRedirects) {
        httpClient.getParams().setBooleanParameter("http.protocol.handle-redirects", followRedirects);
    }


    protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
        return this.bufferedOutput;
    }


    protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
        byte[] bytes = this.bufferedOutput.toByteArray();
        if (headers.getContentLength() == -1) {
            headers.setContentLength(bytes.length);
        }
        ClientHttpResponse result = executeInternal(headers, bytes);
        this.bufferedOutput = null;
        return result;
    }

    protected ClientHttpResponse executeInternal(HttpHeaders headers, byte[] bufferedOutput) throws IOException {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            if (!headerName.equalsIgnoreCase(HTTP.CONTENT_LEN) &&
                    !headerName.equalsIgnoreCase(HTTP.TRANSFER_ENCODING)) {
                for (String headerValue : entry.getValue()) {
                    this.httpRequest.addHeader(headerName, headerValue);
                }
            }
        }
        if (this.httpRequest instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) this.httpRequest;
            HttpEntity requestEntity = new ByteArrayEntity(bufferedOutput);
            entityEnclosingRequest.setEntity(requestEntity);
        }

        setSSLPrinciple(this.httpContext);
        HttpResponse httpResponse = this.httpClient.execute(this.httpRequest, this.httpContext);
        saveSSLPrinciple(this.httpContext);
        return new SSLCachingHttpComponentsClientHttpResponse(httpResponse,this.httpContext);
    }

    private void setSSLPrinciple(HttpContext context) {
        Principal sslToken = sslTokenCache.get();
        if(sslToken!=null && useSSLSessionCaching) {
            context.setAttribute(ClientContext.USER_TOKEN,sslToken);
        }
    }

    private void saveSSLPrinciple(HttpContext context) {
        Principal current = (Principal) context.getAttribute(ClientContext.USER_TOKEN);
        if(current!=null)
            sslTokenCache.compareAndSet(null, current);
    }

}
