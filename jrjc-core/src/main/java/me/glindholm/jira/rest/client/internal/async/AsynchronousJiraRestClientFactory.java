/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.glindholm.jira.rest.client.internal.async;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.httpclient.api.HttpClient;

import me.glindholm.jira.rest.client.api.AuthenticationHandler;
import me.glindholm.jira.rest.client.api.JiraRestClient;
import me.glindholm.jira.rest.client.api.JiraRestClientFactory;
import me.glindholm.jira.rest.client.auth.BasicHttpAuthenticationHandler;

/**
 * Serves asynchronous implementations of the JiraRestClient.
 *
 * @since v2.0
 */
public class AsynchronousJiraRestClientFactory implements JiraRestClientFactory {

    @Override
    public JiraRestClient create(final URI serverUri, final AuthenticationHandler authenticationHandler) throws URISyntaxException {
        final DisposableHttpClient httpClient = new AsynchronousHttpClientFactory()
                .createClient(serverUri, authenticationHandler);
        return new AsynchronousJiraRestClient(serverUri, httpClient);
    }

    @Override
    public JiraRestClient createWithBasicHttpAuthentication(final URI serverUri, final String username, final String password) throws URISyntaxException {
        return create(serverUri, new BasicHttpAuthenticationHandler(username, password));
    }

    @Override
    public JiraRestClient createWithAuthenticationHandler(final URI serverUri, final AuthenticationHandler authenticationHandler) throws URISyntaxException {
        return create(serverUri, authenticationHandler);
    }

    @Override
    public JiraRestClient create(final URI serverUri, final HttpClient httpClient) throws URISyntaxException {
        final DisposableHttpClient disposableHttpClient = new AsynchronousHttpClientFactory().createClient(httpClient);
        return new AsynchronousJiraRestClient(serverUri, disposableHttpClient);
    }
}
