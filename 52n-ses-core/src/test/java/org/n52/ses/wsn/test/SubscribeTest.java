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
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.NotificationProducer;
import org.apache.muse.ws.notification.Policy;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.notification.impl.TopicFilter;
import org.junit.Test;
import org.n52.ses.common.test.ConfigurationRegistryMockup;
import org.n52.ses.wsn.dissemination.DisseminationMethodFactory;
import org.n52.ses.wsn.dissemination.updateinterval.NoNewMessagesMessage;
import org.n52.ses.wsn.dissemination.updateinterval.UpdateIntervalDisseminationMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SubscribeTest {
	
	@Test
	public void testSubscribeWorkflow() throws Exception {
		ConfigurationRegistryMockup.init();
		
		NotificationProducer prod = new NotificationProducerMockup();
		
		EndpointReference er = new EndpointReference(new URI("http://uri.test"));
		TopicFilter filter = new TopicFilter(NotificationProducerMockup.TOPIC);
		Date date = new Date();
		Policy policy = new PolicyMockup();
		new NoNewMessagesMessage(er);
		prod.subscribe(er, filter, date, policy);
	}
	
	private class PolicyMockup implements Policy {
		
		@Override
		public Element toXML(Document doc) {
			Element esPolicy = createElementFromQName(DisseminationMethodFactory.EVENT_SERVICE_POLICY_QNAME, doc);
			
			Element updateInterval = createElementFromQName(UpdateIntervalDisseminationMethod.UPDATE_INTERVAL_NAME, doc);
			Element duration = createElementFromQName(UpdateIntervalDisseminationMethod.INTERVAL_DURATION_NAME, doc);
			Element method = createElementFromQName(UpdateIntervalDisseminationMethod.DISSEMINATION_METHOD_NAME, doc);
			Element nonRelated = createElementFromQName(UpdateIntervalDisseminationMethod.NON_RELATED_NAME, doc);
			
			duration.setTextContent("PT10M");
			method.setTextContent(UpdateIntervalDisseminationMethod.DisseminationMethod.latest.toString());
			nonRelated.setTextContent(UpdateIntervalDisseminationMethod.NonRelatedEventTreatment.ignore.toString());
			
			updateInterval.appendChild(duration);
			updateInterval.appendChild(method);
			updateInterval.appendChild(nonRelated);
			
			esPolicy.appendChild(updateInterval);
			
			Element wsntPolicy = createElementFromQName(WsnConstants.POLICY_QNAME, doc);
			wsntPolicy.appendChild(esPolicy);
			doc.appendChild(wsntPolicy);
			return doc.getDocumentElement();
		}
		
		private Element createElementFromQName(QName qn, Document doc) {
			return doc.createElementNS(qn.getNamespaceURI(), qn.getLocalPart());
		}

		@Override
		public Element toXML() {
			return toXML(XmlUtils.createDocument());
		}
	}
}
