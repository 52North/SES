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
package org.n52.ses.io.parser.test;

import java.io.IOException;
import java.util.List;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.io.parser.OM20Parser;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class OM20ParserTest {

	@Test
	public void testParsing() throws Exception {
		NotificationMessage message = createMessage();
		OM20Parser parser = new OM20Parser();
		Assert.assertTrue("Parser does not support payload!", parser.accept(message));
		List<MapEvent> result = parser.parse(message);
		Assert.assertTrue("no events parsed!", result != null && !result.isEmpty());
		double doubleResult = (Double) result.get(0).get("http://www.52north.org/test/observableProperty/1");
		Assert.assertTrue("value not parsed", doubleResult == 0.28);
	}
	
	private NotificationMessage createMessage() throws IOException, SAXException {
		SimpleNotificationMessage result = new SimpleNotificationMessage();
		result.addMessageContent(createObservation());
		return result;
	}

	private Element createObservation() throws IOException, SAXException {
		return XmlUtils.createDocument(getClass().getResourceAsStream("om20-observation.xml")).getDocumentElement();
	}
	
}
