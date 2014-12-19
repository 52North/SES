/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * icense version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.ses.util.http;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.n52.oxf.util.web.BasicAuthenticationHttpClient;
import org.n52.oxf.util.web.GzipEnabledHttpClient;
import org.n52.oxf.util.web.HttpClient;
import org.n52.oxf.util.web.PoolingConnectionManagerHttpClient;
import org.n52.oxf.util.web.PreemptiveBasicAuthenticationHttpClient;
import org.n52.ses.util.common.ConfigurationRegistry;

public class SESHttpClient {

	private BasicAuthenticationHttpClient httpClient;
	private BasicAuthenticator authenticator;
	private AtomicBoolean firstRun = new AtomicBoolean(true);

	public SESHttpClient() {
		//init is done at time of first request
		//due to unavailability of resources
	}


	public SESHttpResponse sendPost(URL destination, String content, ContentType contentType) throws Exception {
		checkSetup();
		
		if (this.authenticator != null) {
			URI uri = destination.toURI();
			HttpHost host = new HttpHost(uri.getHost(), uri.getPort());
			this.httpClient.provideAuthentication(host, this.authenticator.getUsername(),
					this.authenticator.getPassword());
		}
		
		HttpResponse postResponse = this.httpClient.executePost(destination.toString(),
				content, contentType);

		SESHttpResponse response = null;
		if (postResponse != null && postResponse.getEntity() != null) {
			response = new SESHttpResponse(postResponse.getEntity().getContent(),
					postResponse.getEntity().getContentType().getValue());
		} else if (postResponse != null &&
				postResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
			return SESHttpResponse.NO_CONTENT_RESPONSE;
		}

		return response;
	}


	private void checkSetup() {
		if (firstRun.getAndSet(false)) {
			ConfigurationRegistry conf = ConfigurationRegistry.getInstance();
			
			int timeout = Integer.parseInt(conf.getPropertyForKey(
					ConfigurationRegistry.NOTIFY_TIMEOUT));
			
			HttpClient client = new PoolingConnectionManagerHttpClient(timeout);
			if (Boolean.parseBoolean(ConfigurationRegistry.getInstance().getPropertyForKey(ConfigurationRegistry.USE_GZIP))) {
				client = new GzipEnabledHttpClient(client);
			}
			
			this.httpClient = new PreemptiveBasicAuthenticationHttpClient(client);
		}
	}


	public void setAuthentication(BasicAuthenticator basicAuthenticator) {
		this.authenticator = basicAuthenticator;
	}

	public static class SESHttpResponse {

		public static final SESHttpResponse NO_CONTENT_RESPONSE = new SESHttpResponse(null, null);
		private InputStream content;
		private String contentType;

		public SESHttpResponse(InputStream content, String value) {
			this.content = content;
			this.contentType = value;
		}

		public InputStream getContent() {
			return content;
		}

		public String getContentType() {
			return contentType;
		}

	}

	public void initialize() {
		checkSetup();		
	}

}
