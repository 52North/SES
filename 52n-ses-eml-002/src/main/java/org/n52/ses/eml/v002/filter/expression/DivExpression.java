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

package org.n52.ses.eml.v002.filter.expression;

import java.util.HashSet;

import org.apache.xmlbeans.XmlObject;



/**
 * Represents an expression for a division
 * 
 * @author Thomas Everding
 *
 */
public class DivExpression extends ABinaryFilterExpression{
	
	private boolean initialized = false;
	
	
//	/**
//	 * 
//	 * Constructor for OGC filter encoding 1 div expressions
//	 *
//	 * @param binaryOP definition of the expression
//	 * @param propertyNames name of the known event properties
//	 */
//	public DivExpression(BinaryOperatorType binaryOP, HashSet<Object > propertyNames) {
//		this.initialize(binaryOP, propertyNames);
//		this.initialized = true;
//	}
	
	
	/**
	 * 
	 * Constructor for OGC filter encoding 2 mul functions
	 * 
	 * @param args arguments of the addition function.
	 * @param propertyNames name of the known event properties
	 */
	public DivExpression(XmlObject[] args, HashSet<Object> propertyNames) {
		if (args.length < 2) {
			throw new RuntimeException("illegal argument count for div function");
		}
		
		//set arguments
		XmlObject firstArg = args[0];
		XmlObject secondArg = args[1];
		
		//initialize
		if (firstArg != null && secondArg != null) {
			this.initialize(firstArg, secondArg, propertyNames);
			this.initialized = true;
		}
	}
	

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		String result = "";
		
		if (this.initialized) {
			result = "("
					+ this.first.createExpressionString(complexPatternGuard)
					+ " / "
					+ this.second.createExpressionString(complexPatternGuard)
					+ ")";
		}
		return result;
	}
	
}
