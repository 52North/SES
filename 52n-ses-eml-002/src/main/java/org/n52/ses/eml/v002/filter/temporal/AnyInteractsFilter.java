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
package org.n52.ses.eml.v002.filter.temporal;

import net.opengis.fes.x20.BinaryTemporalOpType;
import net.opengis.fes.x20.TemporalOpsType;

import org.joda.time.Interval;
import org.n52.ses.eml.v002.filterlogic.esper.customFunctions.MethodNames;
import org.n52.ses.eml.v002.filterlogic.esper.customFunctions.TemporalMethods;

/**
 * Implementation of the FES2.0 AnyInteracts filter 
 * that checks for any interactions (intersections)
 * of a time primitive against a time interval.
 * 
 * @author Thomas Everding
 *
 */
public class AnyInteractsFilter extends ATemporalFilter {

	/**
	 * 
	 * Constructor
	 *
	 * @param temporalOp the FES temporal operator
	 */
	public AnyInteractsFilter(TemporalOpsType temporalOp) {
		super(temporalOp);
	}


	@Override
	public String createExpressionString(boolean complexPatternGuard) {

		//get reference interval
		BinaryTemporalOpType anyInteracts = (BinaryTemporalOpType) this.temporalOp;
		Interval intersectsInterval = this.parseGMLTimePeriodFromBinaryTemporalOp(anyInteracts);
		
		//build expression
		StringBuilder sb = new StringBuilder();
		
		//add property check
		sb.append("(");
		sb.append(MethodNames.PROPERTY_EXISTS_NAME);
		sb.append("(this, \"");
		sb.append(anyInteracts.getValueReference());
		sb.append("\") AND "); //property check close
		
		//add any interacts
		sb.append(MethodNames.ANY_INTERACTS_OPERATION);
		sb.append("(this, \"");
		
		//add test time reference
		sb.append(anyInteracts.getValueReference());
		
		//add reference interval
		sb.append("\", \"");
		sb.append(intersectsInterval.getStartMillis());
		sb.append(TemporalMethods.INTERVAL_SEPARATOR);
		sb.append(intersectsInterval.getEndMillis());
		sb.append("\")"); //any interacts close
		
		sb.append(")"); //all close
		
		return sb.toString();
	}


	@Override
	public void setUsedProperty(String nodeValue) {
		/*empty*/
	}

}
