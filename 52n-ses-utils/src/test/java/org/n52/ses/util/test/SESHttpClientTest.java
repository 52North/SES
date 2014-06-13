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
package org.n52.ses.util.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.n52.oxf.util.web.HttpClientException;
import org.n52.ses.common.test.EnvironmentMockup;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.util.http.BasicAuthenticator;
import org.n52.ses.util.http.SESHttpClient;
import org.n52.ses.util.http.SESHttpClient.SESHttpResponse;

public class SESHttpClientTest {

	@Before
	public void setup() throws IOException {
		Assume.assumeTrue(thereIsNetworkConnectivity());

		ConfigurationRegistry.init(
				getClass().getResourceAsStream("ses_config_test.xml"),
				new EnvironmentMockup());
	}

	@Test
	public void testGet() throws IOException, IllegalStateException,
			URISyntaxException, HttpClientException {
		SESHttpClient client = new SESHttpClient();
		client.setAuthentication(new BasicAuthenticator("ich", "du"));
		SESHttpResponse resp = client.sendPost(new URL("http://google.com"),
				"{\"json\":true}", ContentType.APPLICATION_JSON);

		Assert.assertNotNull(resp);
	}

	private boolean thereIsNetworkConnectivity() throws IOException {
		try {
			URL url = new URL("http://www.google.com");

			HttpURLConnection urlConnect = (HttpURLConnection) url
					.openConnection();

			urlConnect.getContent();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
