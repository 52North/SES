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

import javax.xml.namespace.QName;

import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.Policy;
import org.apache.muse.ws.notification.remote.NotificationConsumerClient;
import org.n52.ses.wsn.dissemination.updateinterval.UpdateIntervalDisseminationMethod;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Factory class providing implementations of {@link AbstractDisseminationMethod}
 * based on xml contents.
 * 
 * @author matthes rieke
 *
 */
public class DisseminationMethodFactory {

	public static final String SUBSCRIPTION_POLICY_NAMESPACE = "http://www.opengis.net/es-sp/0.0";
	public static final QName EVENT_SERVICE_POLICY_QNAME = new QName(SUBSCRIPTION_POLICY_NAMESPACE, "EventServiceSubscriptionPolicy");

	public static AbstractDisseminationMethod createDisseminationMethodFromPolicy(
			Policy subscriptionPolicy, NotificationConsumerClient notificationConsumerClient,
			EndpointReference subscriptionReference) {
		Node policyXml = extractInnerPolicy(subscriptionPolicy);

		if (policyXml == null) return null;

		Node inlinePolicy = extractFirstElement(policyXml);
		
		if (inlinePolicy == null) return null;
		
		QName inlineQName = new QName(inlinePolicy.getNamespaceURI(), inlinePolicy.getLocalName());

		if (inlineQName.equals(UpdateIntervalDisseminationMethod.UPDATE_INTERVAL_NAME)) {
			return new UpdateIntervalDisseminationMethod(inlinePolicy, notificationConsumerClient,
					subscriptionReference);
		}

		return null;
	}

	private static Node extractFirstElement(Node policyXml) {
		NodeList children = policyXml.getChildNodes();
		Node child;
		for (int i = 0; i < children.getLength(); i++) {
			child = children.item(i);
			if (!(child instanceof Element)) continue;
			return child;
		}
		return null;
	}

	private static Node extractInnerPolicy(Policy subscriptionPolicy) {
		Element outer = subscriptionPolicy.toXML();

		if (outer == null) return null;

		NodeList children = outer.getChildNodes();
		Node child;
		QName policyQName;
		for (int i = 0; i < children.getLength(); i++) {
			child = children.item(i);
			if (!(child instanceof Element)) continue;
			
			policyQName = new QName(child.getNamespaceURI(), child.getLocalName());
			if (policyQName.equals(EVENT_SERVICE_POLICY_QNAME)) {
				return child;
			}
		}
		return null;
	}

}
