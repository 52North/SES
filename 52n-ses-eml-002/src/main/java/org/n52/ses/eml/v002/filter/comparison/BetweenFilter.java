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

package org.n52.ses.eml.v002.filter.comparison;

import java.util.HashSet;

import org.n52.ses.eml.v002.filter.expression.AFilterExpression;
import org.n52.ses.eml.v002.filterlogic.esper.customFunctions.MethodNames;

import net.opengis.fes.x20.PropertyIsBetweenType;


/**
 * Filter to compare a value and a range.
 * 
 * @author Thomas Everding
 *
 */
public class BetweenFilter extends AComparisonFilter{
	
	private AFilterExpression lower;
	
	private AFilterExpression upper;
	
	private AFilterExpression test;
	
	
	/**
	 * 
	 * Constructor
	 *
	 * @param betweenOp filter definition
	 * @param propertyNames names of the properties used in this filter / pattern
	 */
	public BetweenFilter(PropertyIsBetweenType betweenOp, HashSet<Object > propertyNames) {
		//TODO parse expression
		
		this.test  = AFilterExpression.FACTORY.buildFilterExpression(betweenOp.getExpression(), propertyNames, this);
		this.lower = AFilterExpression.FACTORY.buildFilterExpression(betweenOp.getLowerBoundary().getExpression(), propertyNames, this);
		this.upper = AFilterExpression.FACTORY.buildFilterExpression(betweenOp.getUpperBoundary().getExpression(), propertyNames, this);
	}

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		String result = "";
		
		if (this.lower.getUsedProperty() != null || this.upper.getUsedProperty() != null || this.test.getUsedProperty() != null) {
			result += "(";
			
			boolean first = true;
			String usedProp;
			String usedEvent = "";
			String usedField = "";
			if (this.lower.getUsedProperty() != null) {
				usedProp = this.lower.getUsedProperty();
				if (usedProp.contains(".")) {
					usedEvent = usedProp.substring(0, usedProp.indexOf(".")+1);
					usedField = usedProp.substring(usedProp.indexOf(".")+1, usedProp.length());
				} else {
					usedField = usedProp;
				}
				
				result += MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
				first = false;
			}
			
			if (this.upper.getUsedProperty() != null) {
				
				if (!first) {
					result += "AND ";
				}
				
				usedProp = this.upper.getUsedProperty();
				if (usedProp.contains(".")) {
					usedEvent = usedProp.substring(0, usedProp.indexOf(".")+1);
					usedField = usedProp.substring(usedProp.indexOf(".")+1, usedProp.length());
				} else {
					usedField = usedProp;
				}
				
				result += MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
				first = false;
			}
			
			if (this.test.getUsedProperty() != null) {
				
				if (!first) {
					result += "AND ";
				}
				
				
				usedProp = this.test.getUsedProperty();
				if (usedProp.contains(".")) {
					usedEvent = usedProp.substring(0, usedProp.indexOf(".")+1);
					usedField = usedProp.substring(usedProp.indexOf("."), usedProp.length());
				} else {
					usedField = usedProp;
				}
				
				result += MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
				first = false;
			}
			
			result += ") AND ";
		}
		
		result += "("
						+ this.test.createExpressionString(complexPatternGuard)
						+ " between "
						+ this.lower.createExpressionString(complexPatternGuard)
						+ " and "
						+ this.upper.createExpressionString(complexPatternGuard)
						+ ")";
		return result;
	}
}
