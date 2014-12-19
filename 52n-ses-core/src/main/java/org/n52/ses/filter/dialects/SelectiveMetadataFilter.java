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
package org.n52.ses.filter.dialects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.WsnConstants;
import org.n52.ses.api.common.SesConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SelectiveMetadataFilter extends DialectConstraintFilter {

	private Element filter;
	private List<QName> excludedQNames = new ArrayList<QName>();

	@Override
	public boolean canHandle(Element filterXml) {
		QName qnEml = new QName("http://www.opengis.net/es-pc/0.0", "FilterWithProjectionClause");
		Element elem = XmlUtils.getElement(filterXml, qnEml);
		return elem != null;
	}

	@Override
	public boolean accepts(NotificationMessage message) {
		return true;
	}

	@Override
	public Element toXML() {
		return toXML(XmlUtils.createDocument());
	}

	@Override
	public Element toXML(Document doc) {
		Element filter = XmlUtils.createElement(doc, WsnConstants.FILTER_QNAME);
		
		Element message = XmlUtils.createElement(doc, WsnConstants.MESSAGE_CONTENT_QNAME);
		message.appendChild(doc.importNode(this.filter, true));
		message.setAttribute(WsnConstants.DIALECT, SesConstants.SES_FILTER_LEVEL_2_DIALECT);

		filter.appendChild(message);

		return filter;
	}

	@Override
	public void initialize(Element filterXml) {
		QName qnEml = new QName("http://www.opengis.net/es-pc/0.0", "FilterWithProjectionClause");
		filter = XmlUtils.getElement(filterXml, qnEml);
		QName propertyExclusionName = new QName("http://www.opengis.net/fes-te/1.0", "PropertyExclusion");
		
		if (this.filter == null) return;
		
		NodeList list = filter.getElementsByTagNameNS(propertyExclusionName.getNamespaceURI(),
				propertyExclusionName.getLocalPart());
		if (list != null) {
			for (int i = 0; i < list.getLength(); i++) {
				NodeList children = list.item(i).getChildNodes();
				
				if (children == null) continue;
				
				for (int j = 0; j < children.getLength(); j++) {
					if (children.item(j).getNodeType() == Node.ELEMENT_NODE &&
							children.item(j).getLocalName().equals("propertyName")) {
						String text = children.item(j).getTextContent();
						resolveAndAddQName(filterXml, text);
					}
				}
				
				
			}
		}
	}
	
	private void resolveAndAddQName(Element filterXml, String text) {
		if (text == null) return;
		
		int index = text.indexOf(":");
		String prefix;
		if (index >= 0) {
			prefix = text.substring(0, index);	
		}
		else {
			prefix = null;
		}
		
		if (prefix != null && !prefix.isEmpty()) {
			String namespace = filterXml.lookupNamespaceURI(prefix);
			excludedQNames.add(new QName(namespace, text.substring(index +1, text.length())));
		}
	}

	public List<QName> getExcludedQNames() {
		return excludedQNames;
	}

	
}
