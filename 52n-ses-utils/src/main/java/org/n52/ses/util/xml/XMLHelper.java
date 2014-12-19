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
package org.n52.ses.util.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;


/**
 * Provides helper methods for XML handling
 * 
 * @author Thomas Everding
 *
 */
public class XMLHelper {
	
	private static final Logger logger = LoggerFactory
			.getLogger(XMLHelper.class);
	
	
	/**
	 * Builds an rg.w3c.dom.Node from a {@link XmlObject}.
	 * 
	 * ATTENTION: This solution looks strange but it is 
	 * necessary to do it this way. When trying to call
	 * getDomNode() on the incoming XmlObject you will
	 * get an Exception (DOM Level 3 not implemented).
	 * 
	 * @param input the event document
	 * @deprecated Use {@link org.n52.oxf.xmlbeans.tools.XmlUtil} instead.
	 * @return a org.w3c.dom.Node representation
	 */
	public static Node getDomNode (XmlObject input) {
		try {
			//XPath expression to get the root node
			String xQuery = "/*";
			
			//get the root node
			XmlObject xmlObject = XmlUtil.selectPath(xQuery, input)[0];
			
			//get the DOM Node
			Node node = xmlObject.getDomNode();
						
			return node;
		}
		catch (Throwable t) {
			logger.warn(t.getMessage());
		}
		
		//some error occurred
		return null;
	}
	
	
//	/**
//	 * test main
//	 * @param args args
//	 */
//	public static void main(String[] args) {
//		String xmlText = "<SESEvent xmlns=\"http://www.opengis.net/ses/0.0\" xmlns:ns=\"http://www.opengis.net/eml/0.0.1\" xmlns:ns1=\"http://www.opengis.net/gml/3.2\"><ns:Event><ns:content> <ns:EventCharacteristics> <ns:eventTime> <ns1:TimeInstant> <ns1:timePosition>2009-12-17T10:51:44.934+01:00</ns1:timePosition> </ns1:TimeInstant> </ns:eventTime>  <ns:causalVector/>  <ns:attributes> <ns:EventAttribute> <ns:name>value</ns:name> <ns:value>null</ns:value> </ns:EventAttribute> <ns:EventAttribute> <ns:name>stringValue</ns:name> <ns:value>null</ns:value> </ns:EventAttribute></ns:attributes> </ns:EventCharacteristics> </ns:content>  </ns:Event></SESEvent>";
//		logger.info("xml text:\r" + xmlText);
//		
//		try {
//			SESEventDocument eventDoc = SESEventDocument.Factory.parse(xmlText);
//			
//			Node n2 = XMLHelper.getDomNode(eventDoc);
//			logger.info("");
//			logger.info("n2:\r" + n2.toString());
//		}
//		catch (XmlException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}


	/**
	 * Removes gml:id duplications.
	 * 
	 * @param doc document to check
	 * @return XML with unique gml:id tags
	 */
	public static XmlObject removeIDDublications(XmlObject doc) {
		//other possible strategies?
		return removeIDDuplicationsByRenaming(doc);
	}
	
	/**
	 * Removes gml:id duplications by changing the ID.
	 * 
	 * @param doc document to check
	 * @return XML with unique gml:id tags
	 */
	private static XmlObject removeIDDuplicationsByRenaming(XmlObject doc) {
		//list of used IDs
		List<String> usedIDs = new ArrayList<String>();
		
		//String version of the document
		String docString = doc.toString();
		
		//split by id tags
		String sep = "id=\"";
		String[] parts = docString.split(sep);
		
		//check all parts
		String subSep ="\"";
		String id;
		String[] subParts;
		StringBuilder sb;
		for (int i = 1 /*ignore first*/; i < parts.length; i++) {
			//isolate ID
			subParts = parts[i].split(subSep);
			id = subParts[0];
			
			//check if already used
			if (usedIDs.contains(id)) {
				//if in use alter ID to unused one
				while (usedIDs.contains(id)) {
					id = id + "-" + i;
				}
				
				//restore subParts with altered ID
				sb = new StringBuilder();
				sb.append(id);
				for (int j = 1 /*first is id (above)*/; j < subParts.length; j++) {
					sb.append(subSep + subParts[j]);
				}
				//set altered ID in parts array
				parts[i] = sb.toString();
			}
			//store ID as in use
			usedIDs.add(id);
		}
		
		//rebuild docString from parts
		sb = new StringBuilder();
		sb.append(parts[0]);
		for (int i = 1 /*first already handled*/; i < parts.length; i++) {
			sb.append(sep + parts[i]);
		}
		docString = sb.toString();
		
		//rebuild and return XML doc from docString
		try {
			return XmlObject.Factory.parse(docString);
		}
		catch (XmlException e) {
			//log exception
			logger.warn(e.getMessage());
			
			StringBuilder log = new StringBuilder();
			
			for (StackTraceElement ste : e.getStackTrace()) {
				log.append("\n" + ste.toString());
			}
			
			logger.warn(log.toString());

			return doc;
		}
	}
	
	/**
	 * Strips out the text of an xml-element and returns as a String.
	 * @param elems array of elements
	 * @return the string value of the first element
	 * @deprecated Use {@link org.n52.oxf.xmlbeans.tools.XmlUtil} instead.
	 */
	public static String stripText(XmlObject[] elems) {
		if (elems != null && elems.length > 0) {
			return stripText(elems[0]);
		}
		return null;
	}

	/**
	 * @param elem the text-containing element
	 * @return the text value
	 * @deprecated Use {@link org.n52.oxf.xmlbeans.tools.XmlUtil} instead.
	 */
	public static String stripText(XmlObject elem) {
		if (elem != null) {
			Node child = elem.getDomNode().getFirstChild();
			if (child != null) {
				return XmlUtils.toString(child).trim();
			}
		}
		return null;
	}

	
}
