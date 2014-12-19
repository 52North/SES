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

package org.n52.ses.eml.v002.pattern;

import java.util.HashSet;

import net.opengis.fes.x20.FilterType;

import org.n52.ses.eml.v002.Constants;
import org.n52.ses.eml.v002.filter.StatementFilter;


/**
 * representation of a guard
 * 
 * @author Thomas Everding
 *
 */
public class PatternGuard {
	
	private StatementFilter filter;
	
	private String statement = "";
	
//	private long maxListeningDuration = -1;

	/**
	 * @param filter the filter to set
	 * @param propertyNames all found property names of this pattern
	 */
	public void setFilter(FilterType filter, HashSet<Object> propertyNames) {
		this.filter = new StatementFilter(filter, propertyNames);
	}
	
	
	/**
	 * creates the esper statement for this guard
	 * @param complexPatternGuard if <code>true</code> the property names are used with the event names, else only the
	 * property names are used
	 * 
	 * @return the guard as esper where clause
	 */
	public String createStatement(boolean complexPatternGuard) {
		if (!this.statement.equals("")) {
			//statement already created
			return this.statement;
		}
		
//		if (complexPatternGuard) {
//			//create statement for complex patterns
//			this.statement += Constants.EPL_WHERE 
//							  + " "
//							  + this.filter.createExpressionString(complexPatternGuard);
//			
//			return this.statement;
//		}
		
		//create statement for simple patterns
		this.statement = Constants.EPL_WHERE
						 + " ";
		
//		if (propertyName != null) {
//			String usedEvent = "";
//			String usedField = "";
//			if (propertyName.contains(".")) {
//				usedEvent = propertyName.substring(0, propertyName.indexOf(".")+1);
//				usedField = propertyName.substring(propertyName.indexOf(".")+1, propertyName.length());
//			} else {
//				usedField = propertyName;
//			}
//			
//			this.statement += "(" + MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\")) AND "; 
//		
//		}
		this.statement += this.filter.createExpressionString(complexPatternGuard);
		
		return this.statement;
	}


//	/**
//	 * sets the max Listening Duration
//	 * @param maxListeningDuration maximum duration for listening
//	 */
//	public void setMaxListeningDuration(long maxListeningDuration) {
//		this.maxListeningDuration = maxListeningDuration;
//	}
	
}
