/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.n52.oxf.util.web.BasicAuthenticationHttpClient;
import org.n52.oxf.util.web.GzipEnabledHttpClient;
import org.n52.oxf.util.web.HttpClient;
import org.n52.oxf.util.web.HttpClientException;
import org.n52.oxf.util.web.PoolingConnectionManagerHttpClient;
import org.n52.oxf.util.web.PreemptiveBasicAuthenticationHttpClient;
import org.n52.ses.util.common.ConfigurationRegistry;

public class SESHttpClient {

	private BasicAuthenticator authenticator;
	private Boolean useGzip = null;
	private int timeout;

	public SESHttpClient() {
		//init is done at time of first request
		//due to unavailability of resources
	}


	public SESHttpResponse sendPost(URL destination, String content, ContentType contentType) throws URISyntaxException, HttpClientException, IllegalStateException, IOException {
		BasicAuthenticationHttpClient httpClient = checkSetup();
		
		if (this.authenticator != null) {
			URI uri = destination.toURI();
			HttpHost host = new HttpHost(uri.getHost(), uri.getPort());
			httpClient.provideAuthentication(host, this.authenticator.getUsername(),
					this.authenticator.getPassword());
		}
		
		HttpResponse postResponse = httpClient.executePost(destination.toString(),
				content, contentType);

		SESHttpResponse response = null;
		if (postResponse != null && postResponse.getEntity() != null) {
			/*
			 * some WSN Consumers might return 0 content on HTTP OK
			 */
			if (postResponse.getEntity().getContent() == null ||
					postResponse.getEntity().getContentLength() == 0) {
				return SESHttpResponse.NO_CONTENT_RESPONSE;	
			}
			
			response = new SESHttpResponse(postResponse.getEntity().getContent(),
					postResponse.getEntity().getContentType().getValue());
		} else if (postResponse != null &&
				postResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
			return SESHttpResponse.NO_CONTENT_RESPONSE;
		}

		return response;
	}


	private BasicAuthenticationHttpClient checkSetup() {
		ConfigurationRegistry conf = ConfigurationRegistry.getInstance();
		
		if (useGzip == null) {
			useGzip = Boolean.parseBoolean(ConfigurationRegistry.getInstance().getPropertyForKey(ConfigurationRegistry.USE_GZIP));
			timeout = Integer.parseInt(conf.getPropertyForKey(
					ConfigurationRegistry.NOTIFY_TIMEOUT));
		}
		
		
		HttpClient client = new PoolingConnectionManagerHttpClient(timeout);
		if (useGzip) {
			client = new GzipEnabledHttpClient(client);
		}
		
		return new PreemptiveBasicAuthenticationHttpClient(client);
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

}
