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
package org.n52.ses.common.integration.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ses.adapter.client.Subscription;
import org.n52.ses.common.test.TestWSNEndpoint;

public class ViewCountInitialMatchSubscriptionIT extends AbstractSubscriptionWorkflow {

	private TestWSNEndpoint endpoint;

	private CountNotificationReceiver notificationReceiver;


	@Test
	public void shouldCompleteRoundtripForNotification() throws IOException, InterruptedException,
				OXFException, ExceptionReport, XmlException, ExecutionException, TimeoutException {
		notificationReceiver = initializeConsumer();
		
		ServiceInstance.getInstance().waitUntilAvailable();
		
		Subscription subscription = subscribe();
		
		Thread.sleep(1000);
		
		notification();
		
		Thread.sleep(1000);
		
		unsubscribe(subscription);
		
		Thread.sleep(1000);
		
		Assert.assertTrue("Noticiation count did not match the expected count!",
				notificationReceiver.hasReceivedExpectedCount());
	}

	private CountNotificationReceiver initializeConsumer() throws IOException, InterruptedException {
		endpoint = TestWSNEndpoint.getInstance(IntegrationTestConfig.getInstance().getConsumerPort());
		CountNotificationReceiver notificationReceiver = new CountNotificationReceiver("initial-count", 1);
		endpoint.addListener(notificationReceiver);
		return notificationReceiver;
	}

	private Subscription subscribe() throws OXFException, ExceptionReport, XmlException, IOException {
		return super.subscribe(endpoint.getPublicURL()+notificationReceiver.getPath(),
				"http://www.opengis.net/ses/filter/level3");
	}


	public List<String> readNotifications() throws XmlException, IOException {
		List<String> result = new ArrayList<String>();
		result.add(readXmlContent("InitialMatch_Notify1.xml"));
		result.add(readXmlContent("InitialMatch_Notify1.xml"));
		return result;
	}

	public String readSubscription() throws XmlException, IOException {
		return readXmlContent("InitialMatch_Subscribe1.xml");
	}


}
