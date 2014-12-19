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
package org.n52.ses.storedfilters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.esSf.x00.FilterExpressionTextDocument.FilterExpressionText;
import net.opengis.esSf.x00.ParameterValueDocument.ParameterValue;
import net.opengis.esSf.x00.StoredFilterDescriptionDocument;
import net.opengis.esSf.x00.StoredFilterSubscription;
import net.opengis.esSf.x00.StoredFilterSubscriptionDocument;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.faults.InvalidFilterFault;
import org.apache.muse.ws.notification.impl.FilterCollection;
import org.apache.muse.ws.notification.impl.FilterFactory;
import org.apache.muse.ws.notification.impl.FilterFactoryHandler;
import org.apache.muse.ws.resource.basefaults.BaseFault;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StoredFilterHandler implements FilterFactoryHandler {

	@Override
	public boolean accepts(QName filterName, String filterDialect) {
		return filterDialect.equals(StoredFilterInstance.STORED_FILTER_NAMESPACE);
	}

	@Override
	public Filter newInstance(Element filterXML) throws BaseFault {
		StoredFilterSubscription sub = null;
		try {
			NodeList list = filterXML.getElementsByTagNameNS(StoredFilterInstance.STORED_FILTER_NAMESPACE, "StoredFilterSubscription");
			if (list.getLength() == 0) throw new InvalidFilterFault("Could not find StoredFilterSubscription");
			sub = StoredFilterSubscriptionDocument.Factory.parse(list.item(0)).getStoredFilterSubscription();
		} catch (XmlException e) {
			throw new InvalidFilterFault(e);
		}
		
		return createFilter(sub);
	}

	private Filter createFilter(StoredFilterSubscription sub) throws BaseFault {
		ParameterValue[] parameters = sub.getParameterValueArray();
		
		String template;
		try {
			template = getFilterTemplate(sub.getId());
		} catch (XmlException e) {
			throw new InvalidFilterFault(e);
		}
		
		for (ParameterValue parameterValue : parameters) {
			template = template.replace("${"+parameterValue.getName().trim()+"}", XmlUtil.stripText(parameterValue));
		}
		
		try {
			return findFilterForContent(incorporateNamespacesIntoTemplate(template, sub));
		} catch (IOException e) {
			throw new InvalidFilterFault(e);
		} catch (SAXException e) {
			throw new InvalidFilterFault(e);
		} catch (XmlException e) {
			throw new InvalidFilterFault(e);
		}
	}

	private String incorporateNamespacesIntoTemplate(String template, StoredFilterSubscription sub) throws XmlException {
		Map<String, String> prefixMap = new HashMap<String, String>();
		NamedNodeMap attributes = sub.getDomNode().getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			if (attributes.item(i).getNodeType() == Node.ATTRIBUTE_NODE) {
				if (attributes.item(i).getPrefix().equals("xmlns")) {
					prefixMap.put(attributes.item(i).getLocalName(), attributes.item(i).getNodeValue());
				}
			}
		}
		
		XmlObject templateObject = XmlObject.Factory.parse(template);
		XmlCursor cur = templateObject.newCursor();
		cur.toFirstChild();
		for (String prefix : prefixMap.keySet()) {
			cur.insertNamespace(prefix, prefixMap.get(prefix));
		}
		
		return cur.getObject().xmlText(new XmlOptions().setSaveOuter());
	}

	private Filter findFilterForContent(String template) throws IOException, SAXException, BaseFault {
		Document doc = XmlUtils.createDocument(template);
		Filter filter = FilterFactory.getInstance().newInstance(doc.getDocumentElement());
		
		if (filter instanceof FilterCollection) {
			if (((FilterCollection) filter).getFilters().size() == 1)
				return (Filter) ((FilterCollection) filter).getFilters().iterator().next();
		}
		
		return filter;
	}

	private String getFilterTemplate(String id) throws XmlException {
		StoredFilterInstance instance = StoredFilterInstance.getByID(id);
		StoredFilterDescriptionDocument description = StoredFilterDescriptionDocument.Factory.parse(instance.getStoredFilterDescription());
		return resolveChildContents(description.getStoredFilterDescription().getFilterExpressionText());
	}

	private String resolveChildContents(
			XmlObject xobj) {
		XmlCursor cur = xobj.newCursor();
		
		XmlOptions opts = new XmlOptions();
		opts.setSaveOuter();
		TokenType type = cur.toNextToken();
		while (cur.hasNextToken()) {
			if (type == TokenType.START) {
				return cur.getObject().xmlText(opts);
			}
			else if (type == TokenType.TEXT) {
				return cur.getChars().trim();
			}
			type = cur.toNextToken();
		}

		return xobj.xmlText(opts);
	}

	
	public static void main(String[] args) {
		StoredFilterDescriptionDocument doc = StoredFilterDescriptionDocument.Factory.newInstance();
		FilterExpressionText expr = doc.addNewStoredFilterDescription().addNewFilterExpressionText();
		expr.set(StoredFilterSubscription.Factory.newInstance());

		System.out.println(doc);
		
		StoredFilterHandler sfh = new StoredFilterHandler();
		System.out.println(sfh.resolveChildContents(doc));
	}
}
