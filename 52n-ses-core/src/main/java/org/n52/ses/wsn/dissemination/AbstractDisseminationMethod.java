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
package org.n52.ses.wsn.dissemination;

import org.apache.muse.util.messages.Messages;
import org.apache.muse.util.messages.MessagesFactory;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.impl.SimpleSubscriptionManager;
import org.apache.muse.ws.notification.remote.NotificationConsumerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for dissemination methods.
 * 
 * @author matthes rieke
 *
 */
public abstract class AbstractDisseminationMethod {
	
	//
	// Used to lookup all exception messages
	//
	protected static Messages _MESSAGES = MessagesFactory.get(SimpleSubscriptionManager.class);
	protected int numberOfTries;
	private static final Logger logger = LoggerFactory.getLogger(AbstractDisseminationMethod.class);
	
	public abstract boolean newMessage(NotificationMessage message, NotificationConsumerClient client,
			EndpointReference subscriptionReference, EndpointReference producerReference,
			EndpointReference consumerReference);

	public void setNumberOfTries(int numberOfTries) {
		this.numberOfTries = numberOfTries;
	}
	
	public boolean sendMessage(NotificationMessage message,
			NotificationConsumerClient client,
			EndpointReference consumerReference, int numberOfTries,
			EndpointReference producerReference, EndpointReference subscriptionReference) {

		message.setProducerReference(producerReference);
		message.setSubscriptionReference(subscriptionReference);
		
		for (int n = 0; n < numberOfTries; ++n) {
			if (logger.isDebugEnabled())
				logger.debug("(Try #{}) Sending matched message to: {}", n+1, consumerReference.getAddress().toString());
			try	{
				client.notify(new NotificationMessage[] {message});
				return true;
			}

			catch (SoapFault error) {
				logger.warn("LastPublishFailed: Could not send message to consumer at {}.",
						client.getConsumerReference().getAddress().toString());
			}
		}
		return false;
	}

	public abstract void shutdown();
	
}
