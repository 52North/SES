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

import org.apache.xmlbeans.XmlObject;
import org.joda.time.Interval;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.FESParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.fes.x20.TemporalOpsType;

/**
 * Temporal operator that checks the temporal 'met by# condition. 
 *
 */
public class MetByFilter extends ATemporalFilter {

	private static final Logger logger = LoggerFactory
			.getLogger(MetByFilter.class);
	
	/**
	 * 
	 * Constructor
	 *
	 * @param temporalOps FES temporal operator
	 */
	public MetByFilter(TemporalOpsType temporalOps) {
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
				//TODO log exception and throw
				logger.warn(e.getMessage(), e);
			}
		}

		if (time != null) {
			sb.append(MapEvent.START_KEY +" = "+ time.getEndMillis());
		}
		return sb.toString();
	}

	@Override
	public void setUsedProperty(String nodeValue) {
		/*empty*/
	}

}
