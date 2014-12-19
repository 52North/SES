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

package org.n52.ses.eml.v001.filter.logical;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.n52.ses.eml.v001.filter.IFilterElement;
import org.n52.ses.eml.v001.filter.comparison.AComparisonFilter;
import org.n52.ses.eml.v001.filter.spatial.ASpatialFilter;
import org.n52.ses.eml.v001.filter.temporal.ATemporalFilter;

import net.opengis.fes.x20.BinaryLogicOpType;
import net.opengis.fes.x20.ComparisonOpsType;
import net.opengis.fes.x20.LogicOpsType;
import net.opengis.fes.x20.SpatialOpsType;
import net.opengis.fes.x20.TemporalOpsType;


/**
 * Representation of binary logic filters.
 * 
 * @author Thomas Everding
 *
 */
public abstract class ABinaryLogicFilter extends ALogicFilter {
	
	
	/**
	 * List {@link IFilterElement}s registered to this Filter.
	 */
	protected List<IFilterElement> elements = new ArrayList<IFilterElement>();
	
	/**
	 * initializes the filter
	 * 
	 * @param binaryOp the filter definition
	 */
//	protected void initialize(BinaryLogicOpType binaryOp, HashSet<Object > propertyNames) {
//		if (binaryOp.getLogicOpsArray().length == 2) {
//			//only logical operators
//			first  = ALogicFilter.FACTORY.buildLogicFilter(binaryOp.getLogicOpsArray(0), propertyNames);
//			second = ALogicFilter.FACTORY.buildLogicFilter(binaryOp.getLogicOpsArray(1), propertyNames);
//		}
//		else if (binaryOp.getComparisonOpsArray().length == 2){
//			//only comparison operators
//			first  = AComparisonFilter.FACTORY.buildComparisonFilter(binaryOp.getComparisonOpsArray(0), propertyNames);
//			second = AComparisonFilter.FACTORY.buildComparisonFilter(binaryOp.getComparisonOpsArray(1), propertyNames);
//		}
//		else if (binaryOp.getSpatialOpsArray().length == 2) {
//			first  = ASpatialFilter.FACTORY.buildSpatialFilter(binaryOp.getSpatialOpsArray(0));
//			second = ASpatialFilter.FACTORY.buildSpatialFilter(binaryOp.getSpatialOpsArray(1));
//		}
//		else if (binaryOp.getTemporalOpsArray().length == 2) {
//			first = ATemporalFilter.FACTORY.buildTemporalFilter(binaryOp.getTemporalOpsArray(0));
//			second = ATemporalFilter.FACTORY.buildTemporalFilter(binaryOp.getTemporalOpsArray(1));
//		}
//		else {
//			if (binaryOp.getLogicOpsArray().length == 1) {
//				first  = ALogicFilter.FACTORY.buildLogicFilter(binaryOp.getLogicOpsArray(0), propertyNames);
//			}
//			if (binaryOp.getComparisonOpsArray().length == 1) {
//				if (first != null) {
//					second = AComparisonFilter.FACTORY.buildComparisonFilter(binaryOp.getComparisonOpsArray(0), propertyNames);
//				} else {
//					first = AComparisonFilter.FACTORY.buildComparisonFilter(binaryOp.getComparisonOpsArray(0), propertyNames);
//				}
//			}
//			if (binaryOp.getSpatialOpsArray().length == 1) {
//				if (first != null) {
//					if (second == null) {
//						second = ASpatialFilter.FACTORY.buildSpatialFilter(binaryOp.getSpatialOpsArray(0));
//					}
//				} else {
//					first = ASpatialFilter.FACTORY.buildSpatialFilter(binaryOp.getSpatialOpsArray(0));
//				}
//			}
////			//one of each
////			first  = ALogicFilter.FACTORY.buildLogicFilter(binaryOp.getLogicOpsArray(0), propertyNames);
////			second = AComparisonFilter.FACTORY.buildComparisonFilter(binaryOp.getComparisonOpsArray(0), propertyNames);
//		}
//	}
	
	/**
	 * Init method for this filter.
	 * 
	 * @param binaryOp the operator type
	 * @param propertyNames set of used property names
	 */
	protected void initialize(BinaryLogicOpType binaryOp, HashSet<Object > propertyNames) {
		if (binaryOp.getLogicOpsArray().length > 0) {
			for (LogicOpsType lops : binaryOp.getLogicOpsArray()) {
				this.elements.add(ALogicFilter.FACTORY.buildLogicFilter(lops, propertyNames));
			}
		}
		if (binaryOp.getComparisonOpsArray().length > 0){
			for (ComparisonOpsType cops : binaryOp.getComparisonOpsArray()) {
				this.elements.add(AComparisonFilter.FACTORY.buildComparisonFilter(cops, propertyNames));
			}
		}
		if (binaryOp.getSpatialOpsArray().length > 0) {
			for (SpatialOpsType sops : binaryOp.getSpatialOpsArray()) {
				this.elements.add(ASpatialFilter.FACTORY.buildSpatialFilter(sops));
			}
		}
		if (binaryOp.getTemporalOpsArray().length > 0) {
			for (TemporalOpsType tops : binaryOp.getTemporalOpsArray()) {
				this.elements.add(ATemporalFilter.FACTORY.buildTemporalFilter(tops));
			}
		}
	}
}
