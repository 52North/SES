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
