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

import net.opengis.fes.x20.BinaryComparisonOpType;


/**
 * filters via >
 * 
 * @author Thomas Everding
 * 
 */
public class GreaterThanFilter extends ABinaryComparisonFilter {

	/**
	 * 
	 * Constructor
	 * 
	 * @param binaryOp filter definition
	 * @param propertyNames names of the properties used in this filter / pattern
	 */
	public GreaterThanFilter(BinaryComparisonOpType binaryOp, HashSet<Object> propertyNames) {
		this.initialize(binaryOp, propertyNames);
	}

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		// if (complexPatternGuard) {
		// //build expression for complex patterns
		// // String result = Constants.GUARD_COMPARISON_NAMESPACE
		// // + ":greater("
		// // + this.first.createExpressionString(complexPatternGuard)
		// // + ", "
		// // + this.second.createExpressionString(complexPatternGuard)
		// // + ")";
		// String result = "("
		// + first.createExpressionString(complexPatternGuard)
		// + " > "
		// + second.createExpressionString(complexPatternGuard)
		// + ")";
		// return result;
		// }
		//		
		// //for simple patterns
		String result = "";

		if (this.first.getUsedProperty() != null || this.second.getUsedProperty() != null) {
			
			result += createUsedPropertyString();

		}
		
		result += "(" + this.first.createExpressionString(complexPatternGuard)
		+ " > " + this.second.createExpressionString(complexPatternGuard)
		+ ")";
		return result;
	}

	
}
