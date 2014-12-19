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
package org.n52.ses.wsn.dissemination.updateinterval;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.namespace.QName;

import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.remote.NotificationConsumerClient;
import org.joda.time.Period;
import org.n52.ses.wsn.dissemination.AbstractDisseminationMethod;
import org.n52.ses.wsn.dissemination.DisseminationMethodFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Update Interval dissemination method.
 * 
 * This implementation collects all messages within a given period
 * and batches all messages in one or only sends the latest messages,
 * depending on the policy configuration.
 * 
 * @author matthes rieke
 *
 */
public class UpdateIntervalDisseminationMethod extends AbstractDisseminationMethod {

	public static final QName UPDATE_INTERVAL_NAME = new QName(DisseminationMethodFactory.SUBSCRIPTION_POLICY_NAMESPACE, "UpdateInterval");
	public static final QName INTERVAL_DURATION_NAME = new QName(DisseminationMethodFactory.SUBSCRIPTION_POLICY_NAMESPACE, "IntervalDuration");
	public static final QName DISSEMINATION_METHOD_NAME = new QName(DisseminationMethodFactory.SUBSCRIPTION_POLICY_NAMESPACE, "DisseminationMethod");
	public static final QName NON_RELATED_NAME = new QName(DisseminationMethodFactory.SUBSCRIPTION_POLICY_NAMESPACE, "NonRelatedEventTreatment");
	public static final QName NO_NEW_MESSAGES_NAME = new QName(DisseminationMethodFactory.SUBSCRIPTION_POLICY_NAMESPACE, "NoNewMessages");

	public static enum DisseminationMethod { batching, latest };
	public static enum NonRelatedEventTreatment { separate, ignore }; 
	
	private static final Logger logger = LoggerFactory.getLogger(UpdateIntervalDisseminationMethod.class);
	
	private long duration;
	private MessageCollector messageCollector;
	private boolean ignoreNonRelatedEvents;
	private DisseminationMethod disseminationMethod;
	private AtomicBoolean firstRun = new AtomicBoolean(true);
	private NotificationConsumerClient soapClient;
	private EndpointReference subscription;
	private EndpointReference producer;
	private EndpointReference consumer;
	private Timer timer;
	
	
	public UpdateIntervalDisseminationMethod(Node updateInterval, NotificationConsumerClient client,
			EndpointReference subscriptionReference) {
		List<Node> children = getChildElements(updateInterval);
		for (int i = 0; i < children.size(); i++) {
			parseChild(children.get(i));
		}
		
		this.soapClient = client;
		storeReferences(client, subscriptionReference, client.getProducerReference(),
				client.getConsumerReference());
		this.messageCollector = createMessageCollector();
		createIntervalThread();
	}

	private List<Node> getChildElements(Node updateInterval) {
		List<Node> result = new ArrayList<Node>();
		NodeList children = updateInterval.getChildNodes();
		Node child;
		for (int i = 0; i < children.getLength(); i++) {
			child = children.item(i);
			if (child instanceof Element) result.add(child);
		}
		return result;
	}

	private void parseChild(Node item) {
		QName qn = new QName(item.getNamespaceURI(), item.getLocalName());
		if (qn.equals(INTERVAL_DURATION_NAME)) {
			this.duration = parseDuration(item.getTextContent());
		} else if (qn.equals(DISSEMINATION_METHOD_NAME)) {
			this.disseminationMethod = parseDisseminationMethod(item.getTextContent());
		} else if (qn.equals(NON_RELATED_NAME)) {
			this.ignoreNonRelatedEvents = isIgnoreNonRelatedEventsBehaviour(item.getTextContent());
		}
	}

	private DisseminationMethod parseDisseminationMethod(String textContent) {
		if (textContent.equals(DisseminationMethod.batching.toString())) {
//			throw new UnsupportedOperationException("<DisseminationMethod>batching</DisseminationMethod> for Update Intervals is currently not supported.");
			return DisseminationMethod.batching;
		}
		return DisseminationMethod.latest;
	}

	private boolean isIgnoreNonRelatedEventsBehaviour(String nonRelated) {
		if (nonRelated.equals(NonRelatedEventTreatment.ignore.toString())) {
			 return true; 
		}
		throw new UnsupportedOperationException("<NonRelatedEventTreatment>separate</NonRelatedEventTreatment> for Update Intervals is currently not supported.");
	}

	private MessageCollector createMessageCollector() {
		return new MessageCollector(this.ignoreNonRelatedEvents, this.disseminationMethod == DisseminationMethod.batching);
	}

	private long parseDuration(String textContent) {
		return new Period(textContent).toStandardSeconds().getSeconds() * 1000;
	}

	@Override
	public boolean newMessage(NotificationMessage message,
			NotificationConsumerClient client,
			EndpointReference subscriptionReference,
			EndpointReference producerReference,
			EndpointReference consumerReference) {
		synchronized (this) {
			if (firstRun.getAndSet(false)) {
				storeReferences(client, subscriptionReference, producerReference, consumerReference);
			}	
		}
		
		if (!validateReferences(client, subscriptionReference, producerReference, consumerReference)) {
			logger.warn("Not the same reference objects for Update Interval dissemination! Skipping message.");
			return false;
		}
		
		this.messageCollector.newMessage(message);
		
		return true;
	}

	private boolean validateReferences(NotificationConsumerClient client,
			EndpointReference subscriptionReference,
			EndpointReference producerReference,
			EndpointReference consumerReference) {
		return this.soapClient.equals(client) && this.subscription.equals(subscriptionReference) &&
			this.producer.equals(producerReference) && this.consumer.equals(consumerReference);
	}

	private void storeReferences(NotificationConsumerClient client,
			EndpointReference subscriptionReference,
			EndpointReference producerReference,
			EndpointReference consumerReference) {
		this.soapClient = client;
		this.subscription = subscriptionReference;
		this.producer = producerReference;
		this.consumer = consumerReference;
	}
	
	
	public MessageCollector getMessageCollector() {
		return messageCollector;
	}

	
	
	
	private void createIntervalThread() {
		this.timer = new Timer();
		 
	    timer.schedule( new IntervalCycle(), this.duration, this.duration); 
	}

	private class IntervalCycle extends TimerTask {

		@Override
		public void run() {
			NotificationMessage message = messageCollector.pullMessageForLastInterval();
			
			if (message == null) {
				message = createNoNewMessagesSystemMessage();
				if (logger.isInfoEnabled())
					logger.debug("Sending system message for no new messages within update interval.");
			} else {
				if (logger.isInfoEnabled())
					logger.debug("Sending update interval message.");
			}
			
			sendMessage(message, soapClient, consumer, numberOfTries, producer, subscription);	
		}

		private NotificationMessage createNoNewMessagesSystemMessage() {
			return new NoNewMessagesMessage(subscription);
		}
		
	}

	@Override
	public void shutdown() {
		if (this.timer != null) this.timer.cancel();
		if (this.messageCollector != null) this.messageCollector.shutdown();
	}
}
