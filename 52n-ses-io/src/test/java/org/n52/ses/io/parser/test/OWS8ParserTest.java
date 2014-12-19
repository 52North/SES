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
package org.n52.ses.io.parser.test;

import java.io.IOException;
import java.util.List;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.io.parser.OWS8Parser;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class OWS8ParserTest {

	@Test
	public void testParsing() throws Exception {
		NotificationMessage message = createMessage();
		OWS8Parser parser = new OWS8Parser();
		List<MapEvent> result = parser.parse(message);
		Assert.assertTrue("no events parsed!", result != null && !result.isEmpty());
	}
	
	private NotificationMessage createMessage() throws IOException, SAXException {
		SimpleNotificationMessage result = new SimpleNotificationMessage();
		result.addMessageContent(createAIXMBasicMessage());
		return result;
	}

	private Element createAIXMBasicMessage() throws IOException, SAXException {
		return XmlUtils.createDocument(getClass().getResourceAsStream("aixmBasicMessage.xml")).getDocumentElement();
	}
	
}
