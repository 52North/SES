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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.filter.dialects.SelectiveMetadataFilter;
import org.n52.ses.wsn.contentfilter.PropertyExclusionContentFilter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PropertyExclusionTest {

	@Test
	public void testPropertyExclusionContentFilter() throws IOException, SAXException {
		NotificationMessage message = createMessage();
		
		SelectiveMetadataFilter smf = new SelectiveMetadataFilter();
		smf.initialize(readFilter());
		
		PropertyExclusionContentFilter filter = new PropertyExclusionContentFilter(smf.getExcludedQNames());
		filter.filterMessage(message);
		checkForQNames(smf.getExcludedQNames(), message);
	}
	
	private Element readFilter() throws IOException, SAXException {
		return XmlUtils.createDocument(getClass().getResourceAsStream("propertyExclusionFilter.xml")).getDocumentElement();
	}

	private void checkForQNames(List<QName> qnames, NotificationMessage message) {
		Collection<?> contents = message.getMessageContentNames();
		for (Object object : contents) {
			QName qn = (QName) object;
			Element content = message.getMessageContent(qn);
			for (QName excludedQN : qnames) {
				NodeList list = content.getElementsByTagNameNS(excludedQN.getNamespaceURI(), excludedQN.getLocalPart());
				Assert.assertTrue("Content not removed!", list.getLength() == 0);
			}
		}
	}

	private NotificationMessage createMessage() throws IOException, SAXException {
		SimpleNotificationMessage result = new SimpleNotificationMessage();
		result.addMessageContent(createAIXMBasicMessage());
		return result;
	}

	private Element createAIXMBasicMessage() throws IOException, SAXException {
		return XmlUtils.createDocument(getClass().getResourceAsStream("aixmBasicMessage_propertyExclusion.xml")).getDocumentElement();
	}
	
}
