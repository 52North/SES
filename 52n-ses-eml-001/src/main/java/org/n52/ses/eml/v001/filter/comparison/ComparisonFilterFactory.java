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
package org.n52.ses.eml.v001.filter.comparison;

import java.util.HashSet;

import javax.xml.namespace.QName;

import org.n52.ses.eml.v001.filter.logical.ALogicFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.fes.x20.BinaryComparisonOpType;
import net.opengis.fes.x20.ComparisonOpsType;
import net.opengis.fes.x20.PropertyIsBetweenType;


/**
 * Builds comparison filters.
 * 
 * @author Thomas Everding
 *
 */
public class ComparisonFilterFactory {
	
	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(ComparisonFilterFactory.class);
	
	private static final QName BETWEEN_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyIsBetween");
	
	private static final QName EQUAL_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyIsEqualTo");
	
	private static final QName GREATER_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyIsGreaterThan");
	
	private static final QName GREATER_OR_EQUAL_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyIsGreaterThanOrEqualTo");
	
	private static final QName LESS_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyIsLessThan");
	
	private static final QName LESS_OR_EQUAL_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyIsLessThanOrEqualTo");
	
	private static final QName NOT_EQUAL_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyIsNotEqualTo");
	
	/**
	 * Builds a new comparison filter
	 * 
	 * @param comparisonOp definition of the filter
	 * @param propertyNames names of the properties used in this filter / pattern
	 * 
	 * @return the new {@link ALogicFilter}
	 */
	public AComparisonFilter buildComparisonFilter(ComparisonOpsType comparisonOp, HashSet<Object > propertyNames) {
		//TODO
		
		QName coQName = comparisonOp.newCursor().getName();
		
		/*
		 * non binary operators
		 */
		
		//check between
		if (BETWEEN_QNAME.equals(coQName)) {
			PropertyIsBetweenType betweenOp = (PropertyIsBetweenType) comparisonOp;
			return new BetweenFilter(betweenOp, propertyNames);
		}
		
		/*
		 * binary operators
		 */
		BinaryComparisonOpType binaryOp = (BinaryComparisonOpType) comparisonOp;
		
		//check equal
		if (EQUAL_QNAME.equals(coQName)) {
			return new EqualToFilter(binaryOp, propertyNames);
		}
		
		//check not equal
		else if (NOT_EQUAL_QNAME.equals(coQName)) {
			return new NotEqualToFilter(binaryOp, propertyNames);
		}
		
		//check greater
		else if (GREATER_QNAME.equals(coQName)) {
			return new GreaterThanFilter(binaryOp, propertyNames);
		}
		
		//check greater or equal
		else if (GREATER_OR_EQUAL_QNAME.equals(coQName)) {
			return new GreaterThanOrEqualToFilter(binaryOp, propertyNames);
		}
		
		//check less
		else if (LESS_QNAME.equals(coQName)) {
			return new LessThanFilter(binaryOp, propertyNames);
		}
		
		//check less or equal
		else if (LESS_OR_EQUAL_QNAME.equals(coQName)) {
			return new LessThanOrEqualToFilter(binaryOp, propertyNames);
		}
		
		logger.warn("unable to build comparison filter for '" + comparisonOp.newCursor().getName().toString() + "'");
		return null;
	}
	
}
