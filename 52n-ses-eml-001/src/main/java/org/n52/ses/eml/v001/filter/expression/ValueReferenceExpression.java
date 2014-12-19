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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v001.filter.expression;

import java.util.HashSet;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;




/**
 * Represents an expression for a property name
 * 
 * @author Thomas Everding
 * 
 */
public class ValueReferenceExpression extends AFilterExpression {
	
	private String valueReference;
	
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param expressionType name of a property
	 * @param propertyNames hash map with the known property names
	 */
	public ValueReferenceExpression(XmlObject expressionType, HashSet<Object> propertyNames) {
		Element elem = (Element) expressionType.getDomNode();
		String name = XmlUtils.toString(elem.getFirstChild()).trim();
		//replaceAll(":", "__")
		this.valueReference = name.replaceAll("/", ".");
		
		if (!propertyNames.contains(this.valueReference)) {
			propertyNames.add(this.valueReference);
		}
	}
	

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		if (complexPatternGuard) {
//			StringBuilder log = new StringBuilder();
//			log.append("ValueReference in complex pattern guard found");
//			log.append("\n\t value reference: " + this.valueReference);
//			logger.info(log.toString());
			
			String result = this.valueReference;//.replaceAll("\\.", ":");
			return result;
		}
		
		//remove event name part
		int i;
		if (((i = this.valueReference.indexOf(".")) > 0)) {
			return this.valueReference.substring(i + 1);
		}
		
		return this.valueReference;
	}
	
}
