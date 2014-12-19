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

import java.util.HashSet;

import org.n52.ses.eml.v001.filter.IFilterElement;
import org.n52.ses.eml.v001.filter.comparison.AComparisonFilter;
import org.n52.ses.eml.v001.filter.spatial.ASpatialFilter;
import org.n52.ses.eml.v001.filter.temporal.ATemporalFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.fes.x20.UnaryLogicOpType;


/**
 * Representation of not filters.
 * 
 * @author Thomas Everding
 *
 */
public class NotFilter extends ALogicFilter{
	
	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(NotFilter.class);
	
	private IFilterElement element;
	
	
	/**
	 * 
	 * Constructor
	 *
	 * @param unaryOp filter definition
	 * @param propertyNames names of the properties used in this filter / pattern
	 */
	public NotFilter(UnaryLogicOpType unaryOp, HashSet<Object > propertyNames) {
		if (unaryOp.isSetComparisonOps()) {
			//element is comparison operator
			this.element = AComparisonFilter.FACTORY.buildComparisonFilter(unaryOp.getComparisonOps(), propertyNames);
		}
		else if (unaryOp.isSetLogicOps()) {
			//element is logical operator
			this.element = ALogicFilter.FACTORY.buildLogicFilter(unaryOp.getLogicOps(), propertyNames);
		}
		else if (unaryOp.isSetSpatialOps()) {
			//element is spatial operator
			this.element = ASpatialFilter.FACTORY.buildSpatialFilter(unaryOp.getSpatialOps());
		}
		else if (unaryOp.isSetTemporalOps()) {
			//element is temporal operator
			this.element = ATemporalFilter.FACTORY.buildTemporalFilter(unaryOp.getTemporalOps());
		}
		else {
			//not supported
			NotFilter.logger.warn("the operator type is not supported");
			throw new RuntimeException("the operator type is not supported");
		}
	}


	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		String result = "( not ("
						+ this.element.createExpressionString(complexPatternGuard)
						+ "))";
		return result;
	}
}
