/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.ses.common.integration.test.concurrency;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.ses.adapter.ISESRequestBuilder;
import org.n52.oxf.ses.adapter.SESAdapter;
import org.n52.oxf.ses.adapter.SESRequestBuilderFactory;
import org.n52.oxf.ses.adapter.SESRequestBuilder_00;
import org.n52.oxf.ses.adapter.client.Subscription;
import org.n52.ses.common.integration.test.IntegrationTestConfig;
import org.n52.ses.common.integration.test.ServiceInstance;
import org.n52.ses.common.test.TestWSNEndpoint;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.oasisOpen.docs.wsn.b2.UnsubscribeResponseDocument.UnsubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

public class ConcurrentSubscriptionsIT {
	
	private static final Logger logger = LoggerFactory.getLogger(ConcurrentSubscriptionsIT.class);
	
	private TestWSNEndpoint endpoint;
	private String subscribeBConsumer;
	private String subscribeAConsumer;
	private Subscription subscriptionB;
	private Subscription subscriptionA;

	private ConcurrentNotificationReceiver notificationReceiver;

	@Before
	public void
	ensureConcurrentMessageHandling() throws IOException {
		try {
			Assume.assumeTrue(Boolean.parseBoolean(IntegrationTestConfig.getInstance().getConfigPropertyOfDeployedService(
					ConfigurationRegistry.USE_CONCURRENT_ORDERED_HANDLING)));			
		} catch (Exception e) {
			logger.warn("Skipping test due to exception: "+ e.getMessage());
			Assume.assumeNoException(e);
		}
	}
	
	@Test
	public void
	testConcurrentSubscriptionWorkflow()
			throws IOException, InterruptedException, OXFException, ExceptionReport, XmlException {
		initializeConsumer();
		
		ServiceInstance.getInstance().waitUntilAvailable();
		
		subscribeA();
		subscribeB();
		
		notifications();
		
		waitForNotificationArrival();
		
		String error = evaluate();
		Assert.assertNull(error, error);
		
		unsubscribe();
	}

	
	private void unsubscribe() throws OXFException, ExceptionReport, XmlException, IOException {
		unsubscribeById(subscriptionA.getResourceID(), subscriptionA.getManager().getHost());
		unsubscribeById(subscriptionB.getResourceID(), subscriptionB.getManager().getHost());
	}

	private void unsubscribeById(String resourceID, URL host) throws OXFException, ExceptionReport, XmlException, IOException {
		SESAdapter adapter = new SESAdapter("0.0.0");

		Operation op = new Operation(SESAdapter.UNSUBSCRIBE, null, host.toExternalForm());

		ParameterContainer parameter = new ParameterContainer();
		parameter.addParameterShell(ISESRequestBuilder.UNSUBSCRIBE_SES_URL, host.toExternalForm());
		
		parameter.addParameterShell(SESRequestBuilder_00.UNSUBSCRIBE_REFERENCE, resourceID);

		OperationResult opResult = adapter.doOperation(op, parameter);
		EnvelopeDocument env = EnvelopeDocument.Factory.parse(opResult.getIncomingResultAsStream());
		XmlCursor cur = env.getEnvelope().getBody().newCursor();
		cur.toFirstChild();
		if (!(cur.getObject() instanceof UnsubscribeResponse)) {
			Assert.fail("Unsubscribe failed! Received an unexpected respone: "+ cur.getObject().xmlText(new XmlOptions().setSaveOuter()));
		}
	}

	private String evaluate() {
		Map<String, List<String>> received = notificationReceiver.getUriToNotifications();
		
		if (!received.containsKey(subscribeAConsumer))
			return "Did not receive anything for Subscription A";
		
		if (!received.containsKey(subscribeBConsumer))
			return "Did not receive anything for Subscription B";
		
		List<String> subANotifies = received.get(subscribeAConsumer);
		List<String> subBNotifies = received.get(subscribeBConsumer);
		
		if (subANotifies.size() != 3)
			return "Expected count for Subscription A was 3. Instead received "+subANotifies.size(); 
		
		if (subBNotifies.size() != 3)
			return "Expected count for Subscription B was 3. Instead received "+subBNotifies.size();
		
		return checkContents(subANotifies, subBNotifies);
	}

	private String checkContents(List<String> subANotifies,
			List<String> subBNotifies) {
		if (!subANotifies.get(0).contains("4.476")) {
			return "Wrong contents for first Notification of Subscribption A";
		}
		
		if (!subANotifies.get(1).contains("4.176")) {
			return "Wrong contents for second Notification of Subscribption A";
		}
		
		if (!subANotifies.get(2).contains("4.476")) {
			return "Wrong contents for first Notification of Subscribption A";
		}
		
		if (!subBNotifies.get(0).contains("4.476")) {
			return "Wrong contents for first Notification of Subscribption B";
		}
		
		if (!subBNotifies.get(1).contains("4.176")) {
			return "Wrong contents for second Notification of Subscribption B";
		}
		
		if (!subBNotifies.get(2).contains("4.476")) {
			return "Wrong contents for first Notification of Subscribption B";
		}
		
		return null;
	}

	private void waitForNotificationArrival() throws InterruptedException {
		long start = System.currentTimeMillis();
		while (!allArrived() && System.currentTimeMillis() - start < IntegrationTestConfig.getInstance().getNotificationTimeout()) {
			logger.info("waiting for all notifications to arrive...");
			Thread.sleep(1000);
		}
		
	}

	private boolean allArrived() {
		Map<String, List<String>> received = notificationReceiver.getUriToNotifications();
		
		if (!received.containsKey(subscribeAConsumer) || !received.containsKey(subscribeBConsumer))
			return false;
		
		return received.get(subscribeAConsumer).size() + received.get(subscribeBConsumer).size() >= 6;
	}

	private void notifications() throws XmlException, IOException, OXFException, ExceptionReport {
		List<String> notificiations = readNotifications();
		for (int i = 0; i < 2; i++) {
			for (String n : notificiations) {
				sendNotify(n);
			}
		}
	}

	private void sendNotify(String n) throws OXFException, ExceptionReport {
		SESAdapter adapter = new SESAdapter("0.0.0");

		Operation op = new Operation(SESAdapter.NOTIFY, null, ServiceInstance.getInstance().getHost().toExternalForm());

		ParameterContainer parameter = new ParameterContainer();
		parameter.addParameterShell(ISESRequestBuilder.NOTIFY_SES_URL, ServiceInstance.getInstance().getHost().toExternalForm());
		parameter.addParameterShell(ISESRequestBuilder.NOTIFY_XML_MESSAGE, n);

		OperationResult opResult = adapter.doOperation(op, parameter);
		
		if (opResult != null) {
			Assert.fail("Could not sent notification: "+new String(opResult.getIncomingResult()));
		}
	}

	private List<String> readNotifications() throws XmlException, IOException {
		List<String> result = new ArrayList<String>();
		result.add(readXmlContent("Notification1.xml"));
		result.add(readXmlContent("Notification2.xml"));
		return result;
	}

	private void subscribeA() throws OXFException, ExceptionReport, XmlException, IOException {
		this.subscribeAConsumer = "/A";		
		this.subscriptionA = subscribe(endpoint.getPublicURL()+this.subscribeAConsumer);
		checkForException(this.subscriptionA);
	}
	
	private void checkForException(Subscription sub) {
		if (sub.isFailed()) {
			Assert.fail("Subscription failed: "+ sub.getExceptionText());
		}		
	}

	private void subscribeB() throws OXFException, ExceptionReport, XmlException, IOException {
		this.subscribeBConsumer = "/B";
		this.subscriptionB = subscribe(endpoint.getPublicURL()+this.subscribeBConsumer);
		checkForException(this.subscriptionB);
	}

	private Subscription subscribe(String consumer) throws OXFException, ExceptionReport, XmlException, IOException {
		SESAdapter adapter = new SESAdapter("0.0.0");

		Operation op = new Operation(SESAdapter.SUBSCRIBE, null, ServiceInstance.getInstance().getHost().toExternalForm());

		ParameterContainer parameter = new ParameterContainer();
		parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_SES_URL, ServiceInstance.getInstance().getHost().toExternalForm());
		parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_CONSUMER_REFERENCE_ADDRESS,
				consumer);
		parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_FILTER_MESSAGE_CONTENT_DIALECT,
				"http://www.opengis.net/ses/filter/level3");
		parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_FILTER_MESSAGE_CONTENT, readSubscription());

		logger.info(SESRequestBuilderFactory.generateRequestBuilder("0.0.0").buildSubscribeRequest(parameter));

		OperationResult opResult = adapter.doOperation(op, parameter);
		XmlObject xo = XmlObject.Factory.parse(opResult.getIncomingResultAsStream());
		logger.info(xo.xmlText());
		Subscription sub = new Subscription(null);
		sub.parseResponse(xo);
		return sub;
	}

	
	private void initializeConsumer() throws IOException, InterruptedException {
		endpoint = TestWSNEndpoint.getInstance(IntegrationTestConfig.getInstance().getConsumerPort());
		notificationReceiver = new ConcurrentNotificationReceiver();
		endpoint.addListener(notificationReceiver);
	}
	
	private String readSubscription() throws XmlException, IOException {
		return readXmlContent("Subscription.xml");
	}


	private String readXmlContent(String string) throws XmlException, IOException {
		XmlObject xo = XmlObject.Factory.parse(getClass().getResourceAsStream(string));
		return xo.xmlText(new XmlOptions().setSavePrettyPrint());
	}

}
