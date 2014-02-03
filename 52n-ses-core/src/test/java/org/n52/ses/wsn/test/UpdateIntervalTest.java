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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.namespace.QName;


import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.apache.muse.ws.notification.remote.NotificationConsumerClient;
import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.wsn.dissemination.updateinterval.MessageCollector;
import org.n52.ses.wsn.dissemination.updateinterval.UpdateIntervalDisseminationMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class UpdateIntervalTest {


	@Test
	public void testUpdateInterval() throws Exception {
		EndpointReference consumer = new EndpointReference(new URI("http://52north.org"));
		NotificationConsumerClient client = createClient(consumer);
		
		UpdateIntervalDisseminationMethod diss = new UpdateIntervalDisseminationMethod(createUpdateInterval(),
				client, new EndpointReference(new URI("http://52north.org")));
		
		diss.newMessage(createMessage(), client, consumer, consumer, consumer);
		diss.newMessage(createSecondMessage(), client, consumer, consumer, consumer);

		NotificationMessage msg = waitForResult(diss.getMessageCollector());

		Assert.assertNotNull("Could not retrieve message within the given time interval.", msg);
		
		Collection<?> names = msg.getMessageContentNames();
		boolean hasExpectedResult = false;
		String content;
		for (Object object : names) {
			content = XmlUtils.toString(msg.getMessageContent((QName) object));
			if (content.contains("AirportHeliport")) {
				hasExpectedResult = true;
			}
		}

		diss.shutdown();
		Assert.assertTrue("Could not find expected element <AirportHeliport> in pulled message!", hasExpectedResult);
		
	}

	private NotificationMessage createSecondMessage() throws IOException, SAXException {
		SimpleNotificationMessage result = new SimpleNotificationMessage();
		result.addMessageContent(createSecondAIXMBasicMessage());
		return result;
	}

	private Element createSecondAIXMBasicMessage() throws IOException, SAXException {
		return XmlUtils.createDocument(getClass().getResourceAsStream("aixmBasicMessage2.xml")).getDocumentElement();
	}

	private NotificationConsumerClient createClient(EndpointReference ref) {
		return new NotificationConsumerClient(ref);
	}

	private NotificationMessage createMessage() throws IOException, SAXException {
		SimpleNotificationMessage result = new SimpleNotificationMessage();
		result.addMessageContent(createAIXMBasicMessage());
		return result;
	}

	private Element createAIXMBasicMessage() throws IOException, SAXException {
		return XmlUtils.createDocument(getClass().getResourceAsStream("aixmBasicMessage.xml")).getDocumentElement();
	}

	private Node createUpdateInterval() {
		Document doc = XmlUtils.createDocument();

		Element updateInterval = XmlUtils.createElement(doc, UpdateIntervalDisseminationMethod.UPDATE_INTERVAL_NAME);
		Element duration = XmlUtils.createElement(doc, UpdateIntervalDisseminationMethod.INTERVAL_DURATION_NAME);
		Element method = XmlUtils.createElement(doc, UpdateIntervalDisseminationMethod.DISSEMINATION_METHOD_NAME);
		Element nonRelated = XmlUtils.createElement(doc, UpdateIntervalDisseminationMethod.NON_RELATED_NAME);

		duration.setTextContent("PT1S");
		method.setTextContent(UpdateIntervalDisseminationMethod.DisseminationMethod.batching.toString());
		nonRelated.setTextContent(UpdateIntervalDisseminationMethod.NonRelatedEventTreatment.ignore.toString());

		updateInterval.appendChild(duration);
		updateInterval.appendChild(method);
		updateInterval.appendChild(nonRelated);

		return updateInterval;
	}

	private NotificationMessage waitForResult(final MessageCollector messageCollector) throws InterruptedException, ExecutionException, TimeoutException {
		Callable<NotificationMessage> runnable = new Callable<NotificationMessage>() {
			@Override
			public NotificationMessage call() {
				NotificationMessage result = null;
				
				while (result == null) {
					result = messageCollector.pullMessageForLastInterval();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				return result;
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<NotificationMessage> retriever = executor.submit(runnable);

		return retriever.get(5, TimeUnit.SECONDS);
	}
}
