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
