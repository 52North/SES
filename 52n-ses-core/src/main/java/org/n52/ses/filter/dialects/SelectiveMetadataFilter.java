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
