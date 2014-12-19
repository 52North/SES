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
package org.n52.ses.filter.epl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.opengis.ses.x00.EPLFilterDocument.EPLFilter;
import net.opengis.ses.x00.EPLFiltersDocument;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.notification.faults.SubscribeCreationFailedFault;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.parser.XMLHandlingException;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.common.SesConstants;
import org.n52.ses.api.ws.IConstraintFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Filter class for pure EPL statement subscriptions.
 * 
 * @author matthes rieke <m.rieke@52north.org>
 *
 */
public class EPLFilterImpl implements Filter, IConstraintFilter {

	private static final Logger logger = LoggerFactory
			.getLogger(EPLFilterImpl.class);
	
	private EPLFiltersDocument filterXml;
	private Map<EPLFilterInstance, String> eplFilters = new HashMap<EPLFilterInstance, String>();

	private String externalInputName;
	

	public EPLFilterImpl(XmlObject obj) throws SubscribeCreationFailedFault {
		if (obj instanceof EPLFiltersDocument) {
			this.filterXml = (EPLFiltersDocument) obj;
			EPLFilter[] filters = ((EPLFiltersDocument) obj).getEPLFilters().getEPLFilterArray();
			
			for (EPLFilter epl : filters) {
				String stmt = StringEscapeUtils.unescapeXml(
						epl.getStatement().getStringValue()).trim();
				int fromIndex = StringUtils.indexOfIgnoreCase(stmt, " from ");
				String subFrom = stmt.substring(fromIndex).trim();
				subFrom = subFrom.substring(subFrom.indexOf(" "), subFrom.length()).trim();
				
				fromIndex = subFrom.indexOf(" ");
				if (fromIndex > 0) {
					subFrom = subFrom.substring(0, fromIndex).trim();	
				}
				
				String streamName;
				if (subFrom.contains(".") || subFrom.contains(":")) {
					int i = Math.min(subFrom.indexOf("."), subFrom.indexOf(":"));
					streamName = subFrom.substring(0, i);
				} else {
					streamName = subFrom;
				}
				
				String statement = StringEscapeUtils.unescapeXml(
						epl.getStatement().getStringValue()).trim();
				
				String newEventName = null;
				if (epl.isSetNewEventName()) {
					newEventName = epl.getNewEventName();
				}
				
				boolean doOutput = false;
				if (epl.getStatement().isSetDoOutput()) {
					doOutput = epl.getStatement().getDoOutput();
				}
				
				boolean external = epl.getStatement().getExternalInput();
				if (external) {
					if (this.externalInputName != null && !this.externalInputName.equals(streamName)) {
						throw new SubscribeCreationFailedFault("Multiple externalInputs set for one EPLFilter subscription");
					} else {
						this.externalInputName = streamName;
					}
				}
				
				this.eplFilters.put(new EPLFilterInstance(statement, newEventName, doOutput,
						external), streamName);
			}
		} else {
			throw new SubscribeCreationFailedFault("Could not parse EPLFilter markup.");
		}
	}

	@Override
	public Element toXML() {
		return toXML(XmlUtils.createDocument());
	}

	@Override
	public Element toXML(Document doc) {
		Element filter = XmlUtils.createElement(doc, WsnConstants.FILTER_QNAME);

		//hack to get the <Filter> Element of OGCFilter
		String xmlText = "<MessageContent>"+System.getProperty("line.separator");
		try {
			xmlText += XmlUtil.objectToString(this.filterXml.getEPLFilters(), true, true);
		} catch (XMLHandlingException e1) {
			logger.warn(e1.getMessage(), e1);
		}
		xmlText += System.getProperty("line.separator")+"</MessageContent>";

		Element message = null;
		try {
			Element node = XmlUtils.createDocument(xmlText).getDocumentElement();
			message = XmlUtils.createElement(doc, WsnConstants.MESSAGE_CONTENT_QNAME,
					node);
			message.setAttribute(WsnConstants.DIALECT, SesConstants.EPL_PURE_DIALECT);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		} catch (SAXException e) {
			logger.warn(e.getMessage(), e);
		}

		filter.appendChild(message);

		return filter;
	}

	
	

	public Map<EPLFilterInstance, String> getEplFilters() {
		return this.eplFilters;
	}

	@Override
	public String toString() {
		return XmlUtils.toString(toXML(), false).trim();
	}

	

	public XmlObject getFilterXml() {
		return this.filterXml;
	}

	@Override
	public boolean accepts(NotificationMessage arg0) {
		//always return false as matching is done inside esper
		return false;
	}
	
	public String getExternalInputName() {
		return this.externalInputName;
	}

	
	/*
	 * 
	 * Helper class for representing a single filter statement
	 * 
	 */
	public class EPLFilterInstance {

		private String statement;
		private String newEventName;
		private boolean doOutput;
		private boolean externalInput;

		public EPLFilterInstance(String statement, String newEventName,
				boolean doOutput, boolean externalInput) {
			this.statement = statement;
			this.newEventName = newEventName;
			this.doOutput = doOutput;
			this.externalInput = externalInput;
		}

		
		public boolean isExternalInput() {
			return this.externalInput;
		}


		public String getStatement() {
			return this.statement;
		}

		public String getNewEventName() {
			return this.newEventName;
		}

		public boolean isDoOutput() {
			return this.doOutput;
		}
		
	}



}
