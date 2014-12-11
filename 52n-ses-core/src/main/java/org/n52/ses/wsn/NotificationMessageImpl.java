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
package org.n52.ses.wsn;

import java.io.Serializable;
import java.util.Collection;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.n52.ses.api.ws.INotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


public class NotificationMessageImpl implements INotificationMessage, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7640713398661834738L;
	private static final Logger logger = LoggerFactory.getLogger(NotificationMessageImpl.class);
	
	private transient NotificationMessage message;

	private Element messageXml;

	public NotificationMessageImpl(Object message) {
		if (!(message instanceof NotificationMessage)) {
			throw new IllegalArgumentException("Only NotificationMessage instances allowed.");
		}
		this.message = (NotificationMessage) message;
		this.messageXml = this.message.toXML();
	}
	
	@Override
	public Object getNotificationMessage() {
		synchronized (this) {
			if (this.message == null && this.messageXml != null) {
				try {
					this.message = new SimpleNotificationMessage(this.messageXml);
				} catch (SoapFault e) {
					logger.warn("Could not recreate NotificationMessage", e);
				}
			}
		}
		return this.message;
	}

	@Override
	public String xmlToString() {
		return XmlUtils.toString(((NotificationMessage) getNotificationMessage()).toXML());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof NotificationMessageImpl)) {
			return false;
		}
		
		NotificationMessage thisMessage = (NotificationMessage) this.getNotificationMessage();
		NotificationMessage thatMessage = (NotificationMessage) ((NotificationMessageImpl) obj).getNotificationMessage();
		
		if (thisMessage == null || thatMessage == null) {
			return false;
		}
		
		Collection<?> thisNames = thisMessage.getMessageContentNames();
		Collection<?> thatNames = thatMessage.getMessageContentNames();
		
		for (Object object : thatNames) {
			if (!thisNames.contains(object)) {
				return false;
			}
		}
		
		if (thisMessage.getProducerReference() != null && thatMessage.getProducerReference() != null) {
			if (!thisMessage.getProducerReference().equals(thatMessage.getProducerReference())) {
				return false;
			}
		}
		else if (thatMessage.getProducerReference() == null && thisMessage.getProducerReference() != null) {
			return false;
		}
		else if (thatMessage.getProducerReference() != null && thisMessage.getProducerReference() == null) {
			return false;
		}
		
		if (thisMessage.getSubscriptionReference() != null && thatMessage.getSubscriptionReference() != null) {
			if (!thisMessage.getSubscriptionReference().equals(thatMessage.getSubscriptionReference())) {
				return false;
			}
		}
		else if (thatMessage.getSubscriptionReference() == null && thisMessage.getSubscriptionReference() != null) {
			return false;
		}
		else if (thatMessage.getSubscriptionReference() != null && thisMessage.getSubscriptionReference() == null) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return 1234 + (this.messageXml != null ? this.messageXml.hashCode() : 0);
	}
	
}
