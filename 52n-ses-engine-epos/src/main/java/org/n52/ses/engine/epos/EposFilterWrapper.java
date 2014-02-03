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
package org.n52.ses.engine.epos;

import java.io.IOException;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.WsnConstants;
import org.n52.epos.filter.EposFilter;
import org.n52.ses.api.common.SesConstants;
import org.n52.ses.api.ws.EngineCoveredFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class EposFilterWrapper implements Filter, EngineCoveredFilter {

	private static final Logger logger = LoggerFactory.getLogger(EposFilterWrapper.class);
	private EposFilter filter;

	public EposFilterWrapper(EposFilter eposf) {
		this.filter = eposf;
	}

	@Override
	public Element toXML() {
		return toXML(XmlUtils.createDocument());
	}

	@Override
	public Element toXML(Document doc) {
		Element filter = XmlUtils.createElement(doc, WsnConstants.FILTER_QNAME);

		String xmlText = "<MessageContent>"+System.getProperty("line.separator");
		xmlText += this.filter.serialize();
		xmlText += System.getProperty("line.separator")+"</MessageContent>";

		Element message = null;
		try {
			Element node = XmlUtils.createDocument(xmlText).getDocumentElement();
			message = XmlUtils.createElement(doc, WsnConstants.MESSAGE_CONTENT_QNAME,
					node);
			message.setAttribute(WsnConstants.DIALECT, SesConstants.SES_FILTER_LEVEL_3_DIALECT);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		} catch (SAXException e) {
			logger.warn(e.getMessage(), e);
		}

		filter.appendChild(message);

		return filter;
	}

	@Override
	public boolean accepts(NotificationMessage message) {
		return true;
	}
	
	@Override
	public Object getEngineSpecificFilter() {
		return this.filter;
	}

}
