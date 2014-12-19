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
package org.n52.ses.eml.v001.filter.temporal;

import net.opengis.fes.x20.TemporalOpsType;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.Interval;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.FESParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temporal filter that checks for the temporal 'after' condition.
 *
 */
public class AfterFilter extends ATemporalFilter {

	private static final Logger logger = LoggerFactory
			.getLogger(AfterFilter.class);
	
	/**
	 * 
	 * Constructor
	 *
	 * @param temporalOps FES temporal operator
	 */
	public AfterFilter(TemporalOpsType temporalOps) {
		super(temporalOps);
	}

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		StringBuilder sb = new StringBuilder();
		
		XmlObject[] valRef = this.temporalOp.selectChildren(VALUE_REFERENCE_QNAME);
		
		Interval time = null;
		if (valRef != null) {
			try {
				time = getTimeFromValueReference(valRef[0]);
			} catch (FESParseException e) {
				//TODO log exc and throw
				logger.warn(e.getMessage(), e);
			}
		}
		
		if (time == null) {
			//error while parsing time
			return "";
		}
		
		sb.append(MapEvent.START_KEY +" > "+ time.getEndMillis());
		return sb.toString();
	}


	@Override
	public void setUsedProperty(String nodeValue) {
		/*empty*/
	}

}
