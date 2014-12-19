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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v002.filter.logical;

import java.util.HashSet;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.fes.x20.BinaryLogicOpType;
import net.opengis.fes.x20.LogicOpsType;
import net.opengis.fes.x20.UnaryLogicOpType;


/**
 * Builds logic filters.
 * 
 * @author Thomas Everding
 *
 */
public class LogicFilterFactory {
	
	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(LogicFilterFactory.class);
	
	private static final QName AND_QNAME = new QName("http://www.opengis.net/fes/2.0", "And");
	
	private static final QName OR_QNAME = new QName("http://www.opengis.net/fes/2.0", "Or");
	
	private static final QName NOT_QNAME = new QName("http://www.opengis.net/fes/2.0", "Not");
	
	/**
	 * Builds a new logic filter
	 * 
	 * @param logicOp definition of the filter
	 * @param propertyNames names of the properties used in this filter / pattern
	 * 
	 * @return the new {@link ALogicFilter}
	 */
	public ALogicFilter buildLogicFilter(LogicOpsType logicOp, HashSet<Object > propertyNames) {
		//TODO
		QName loQName = logicOp.newCursor().getName();
		
		/*
		 * non binary operators 
		 */
		
		//check Not
		if (NOT_QNAME.equals(loQName)) {
			//create new NotFilter
			UnaryLogicOpType unaryOp = (UnaryLogicOpType) logicOp;
			return new NotFilter(unaryOp, propertyNames);
		}
		
		/*
		 * binary operators
		 */
		BinaryLogicOpType binaryOp = (BinaryLogicOpType) logicOp;
		
		//check And
		if (AND_QNAME.equals(loQName)) {
			//create new AndFilter
			return new AndFilter(binaryOp, propertyNames);
		}
		
		//check Or
		else if (OR_QNAME.equals(loQName)) {
			//create new OrFilter
			return new OrFilter(binaryOp, propertyNames);
		}
		
		logger.warn("unable to build filter expression for '" + loQName.toString() + "'");
		return null;
	}
	
}
