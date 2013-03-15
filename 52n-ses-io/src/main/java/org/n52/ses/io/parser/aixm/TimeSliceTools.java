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
package org.n52.ses.io.parser.aixm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.Interval;
import org.n52.oxf.conversion.gml32.xmlbeans.GMLTimeParser;

import aero.aixm.schema.x51.AbstractAIXMFeatureType;
import aero.aixm.schema.x51.AbstractAIXMTimeSliceType;

/**
 * Helper class for extracting timeslices from AIXM features
 */
public class TimeSliceTools {

	private static final Object GET_TIME_SLICE_ARRAY_METHOD_NAME = "getTimeSliceArray";
	private static final CharSequence TIME_SLICE_PORTION = "TimeSlice";
	private static final CharSequence GET_PORTION = "get";

	/**
	 * @param slice the timeslices
	 * @param validDate the time interval
	 * @return true if the timeslices validTime intersects the given time interval
	 */
	public static boolean checkTimeSliceValidForTime(
			AbstractAIXMTimeSliceType slice, Interval validDate) {
		Interval sliceDuration = GMLTimeParser.parseTimePrimitive(slice.getValidTime());

		if (sliceDuration.overlaps(validDate) || sliceDuration.abuts(validDate)) {
			return true;
		}

		return false;
	}
	
	/**
	 * method resolves a timeslice from any given AIXM feature which has timeslices.
	 * 
	 * @param feature the AIXM feature
	 * @param validTime the validTime
	 * @return the timeslice which validTime intersects the given time interval
	 */
	public static AbstractAIXMTimeSliceType resolveTimeSliceFromValidTime(AbstractAIXMFeatureType feature, Date validTime) {
		List<AbstractAIXMTimeSliceType> slices;
		try {
			slices = getSlicesFromFeature(feature);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
		
		Interval validDate = new Interval(validTime.getTime(), validTime.getTime());
		
		for (AbstractAIXMTimeSliceType slice : slices) {
			if (checkTimeSliceValidForTime(slice, validDate)) {
				return slice;
			}
		}
		
		if (slices.size() > 0) {
			return slices.get(0);
		}
		throw new IllegalStateException("Could not find a TimeSlice.");
	}

	private static List<AbstractAIXMTimeSliceType> getSlicesFromFeature(AbstractAIXMFeatureType feature) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		List<AbstractAIXMTimeSliceType> results = new ArrayList<AbstractAIXMTimeSliceType>();
		for (Method method : feature.getClass().getMethods()) {
			if (method.getName().equals(GET_TIME_SLICE_ARRAY_METHOD_NAME) && method.getGenericParameterTypes().length == 0) {
				Object[] objects = (Object[]) method.invoke(feature, new Object[] {});
				for (Object object : objects) {
					for (Method m2 : object.getClass().getMethods()) {
						if (m2.getName().contains(TIME_SLICE_PORTION) && m2.getName().contains(GET_PORTION)) {
							Object resultInstance = m2.invoke(object, new Object[] {});
							if (resultInstance != null && AbstractAIXMTimeSliceType.class.isAssignableFrom(resultInstance.getClass())) {
								results.add((AbstractAIXMTimeSliceType) resultInstance);
								break;
							}
						}
					}
				}
				break;
			}
		}
		return results;
	}

}
