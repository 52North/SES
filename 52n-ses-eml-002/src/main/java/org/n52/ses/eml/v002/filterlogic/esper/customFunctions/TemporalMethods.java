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
package org.n52.ses.eml.v002.filterlogic.esper.customFunctions;

import org.n52.ses.api.common.GlobalConstants;
import org.n52.ses.api.event.MapEvent;



/**
 * provides special temporal methods
 * 
 * @author Thomas Everding
 *
 */
public class TemporalMethods {
	
	/**
	 * String that separates two long values in an interval string
	 */
	public static final String INTERVAL_SEPARATOR = GlobalConstants.TEMPORAL_INTERVAL_SEPARATOR;
	
	
	/**
	 * This method tests if a time lies within an interval
	 * (borders included). It implements the FES2.0 AnyInteracts
	 * operator.
	 * @param eventObj the event whose time is checked for interactions
	 * @param timeProperty the property name of the time 
	 * @param intersectionInterval the time interval on which the interaction is tested, format: long|long
	 * 
	 * @return true if the testTime lies (partly) in the interval
	 */
	public static boolean anyInteracts (Object eventObj, Object timeProperty, Object intersectionInterval) {
		MapEvent event = (MapEvent) eventObj;
		
		String test = event.get(timeProperty).toString();
		
		if (test.contains(INTERVAL_SEPARATOR)) {
			//interval vs. interval
			String[] startEnd = test.split(INTERVAL_SEPARATOR);
			long testLS = Long.parseLong(startEnd[0]);
			long testLE = Long.parseLong(startEnd[1]);
			
			startEnd = intersectionInterval.toString().split(INTERVAL_SEPARATOR);
			long intervalS = Long.parseLong(startEnd[0]);
			long intervalE = Long.parseLong(startEnd[1]);
			
			return intersectsIntervals(testLS, testLE, intervalS, intervalE);
		}
		//instant vs. interval
		long testL = Long.parseLong(test);
		
		String[] startEnd = intersectionInterval.toString().split(INTERVAL_SEPARATOR);
		long intervalS = Long.parseLong(startEnd[0]);
		long intervalE = Long.parseLong(startEnd[1]);
		
		return intersectsInstant(testL, intervalS, intervalE);
	}

	private static boolean intersectsInstant(long test, long intervalS, long intervalE) {
		if (test >= intervalS && test <= intervalE) {
			//test lies within the interval
			return true;
		}
		return false;
	}

	private static boolean intersectsIntervals(long testS, long testE, long intervalS, long intervalE) {
		if (testE <= intervalS) {
			//test interval ended too early
			return false;
		}
		if (testS >= intervalE) {
			//test interval starts too late
			return false;
		}
		
		//test interval intersect the other interval
		return true;
	}
}
