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
