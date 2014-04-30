/**
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.ses.util.http;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

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

	private BasicAuthenticator authenticator;
	private Boolean useGzip = null;
	private int timeout;

	public SESHttpClient() {
		//init is done at time of first request
		//due to unavailability of resources
	}


	public SESHttpResponse sendPost(URL destination, String content, ContentType contentType) throws Exception {
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

	public void initialize() {
		checkSetup();		
	}

}
