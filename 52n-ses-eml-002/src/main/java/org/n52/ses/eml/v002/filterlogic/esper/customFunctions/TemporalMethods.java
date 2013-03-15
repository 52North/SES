/**
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
