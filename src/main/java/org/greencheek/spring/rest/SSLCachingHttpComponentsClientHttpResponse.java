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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.protocol.HttpContext;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import java.security.Principal;

/**
 * {@link org.springframework.http.client.ClientHttpResponse} implementation that uses
 * Apache HttpComponents HttpClient to execute requests.
 *
 * <p>Created via the {@link SSLCachingHttpComponentsClientHttpRequest}.
 *
 * @author Oleg Kalnichevski
 * @author Arjen Poutsma
 * @since 3.1
 * @see SSLCachingHttpComponentsClientHttpRequest#execute()
 */
final class SSLCachingHttpComponentsClientHttpResponse implements ClientHttpResponse {

    private final HttpResponse httpResponse;
    private final HttpContext httpContext;
    private final AtomicReference<String> finalUri = new AtomicReference<String>();

    private HttpHeaders headers;


    SSLCachingHttpComponentsClientHttpResponse(HttpResponse httpResponse, HttpContext httpContext) {
        this.httpResponse = httpResponse;
        this.httpContext = httpContext;
    }


    public HttpStatus getStatusCode() throws IOException {
        return HttpStatus.valueOf(getRawStatusCode());
    }

    public int getRawStatusCode() throws IOException {
        return this.httpResponse.getStatusLine().getStatusCode();
    }

    public String getStatusText() throws IOException {
        return this.httpResponse.getStatusLine().getReasonPhrase();
    }

    public HttpHeaders getHeaders() {
        if (this.headers == null) {
            this.headers = new HttpHeaders();
            for (Header header : this.httpResponse.getAllHeaders()) {
                this.headers.add(header.getName(), header.getValue());
            }
        }
        return this.headers;
    }

    public String getFinalUri() {
        String url = finalUri.get();
        if(url!=null) return url;

        HttpHost host = (HttpHost)this.httpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        HttpUriRequest finalRequest = (HttpUriRequest) this.httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);

        StringBuilder finalUriString = new StringBuilder();

        finalUriString.append(host.getSchemeName()).append("://");
        finalUriString.append(host.getHostName());
        if (host.getPort() != -1) {
            finalUriString.append(':').append(host.getPort());
        }
        finalUriString.append(finalRequest.getURI().normalize().toString());
        finalUri.compareAndSet(null,finalUriString.toString());
        return finalUri.get();
    }

    public InputStream getBody() throws IOException {
        HttpEntity entity = this.httpResponse.getEntity();
        return entity != null ? entity.getContent() : null;
    }

    public void close() {
        HttpEntity entity = this.httpResponse.getEntity();
        if (entity != null) {
            try {
                // Release underlying connection back to the connection manager
                EntityUtils.consume(entity);
            }
            catch (IOException e) {
                // ignore
            }
        }
    }

}
