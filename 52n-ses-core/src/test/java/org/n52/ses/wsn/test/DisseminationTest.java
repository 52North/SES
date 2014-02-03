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
package org.n52.ses.wsn.test;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.apache.muse.ws.notification.remote.NotificationConsumerClient;
import org.junit.Test;
import org.n52.ses.common.environment.SESSoapClient;
import org.n52.ses.common.test.ConfigurationRegistryMockup;
import org.n52.ses.wsn.dissemination.DefaultDisseminationMethod;

public class DisseminationTest {

	@Test
	public void testHttpPostDissemination() throws URISyntaxException {
		String url = "http://www.google.com";
		DefaultDisseminationMethod method = new DefaultDisseminationMethod();
		method.setNumberOfTries(5);
		
		EndpointReference consumer = new EndpointReference(new URI(url));
		NotificationConsumerClient client = new NotificationConsumerClient(consumer, new EndpointReference(new URI("http://www.w3.org/2005/08/addressing/role/anonymous")), new SESSoapClient());
		
		ConfigurationRegistryMockup.init();
		
		Assert.assertTrue("Can't connect to "+url, method.newMessage(new SimpleNotificationMessage(), client, null, null, consumer));
	}
}
