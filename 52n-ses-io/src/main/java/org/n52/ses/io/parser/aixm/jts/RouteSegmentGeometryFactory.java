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
package org.n52.ses.io.parser.aixm.jts;

import java.util.List;

import org.n52.oxf.conversion.gml32.geometry.AltitudeLimits;
import org.n52.oxf.conversion.gml32.geometry.GeometryWithInterpolation;
import org.n52.oxf.conversion.gml32.geometry.RouteSegmentWithAltitudeLimits;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
import org.n52.oxf.conversion.unit.UOMTools;
import org.n52.ses.io.parser.aixm.AltitudeTools;
import org.n52.ses.io.parser.aixm.XBeansUOMTools;

import aero.aixm.schema.x51.CurvePropertyType;
import aero.aixm.schema.x51.RouteSegmentTimeSliceType;

/**
 * Class provides static methods for parsing AIXM RouteSegments.
 */
public class RouteSegmentGeometryFactory {

	/**
	 * @param routeSegmentTimeSlice the aixm timeslice
	 * @return a RouteSegment with interpolation methods and altitudes defined.
	 */
	public static RouteSegmentWithAltitudeLimits parseRouteSegment(
			RouteSegmentTimeSliceType routeSegmentTimeSlice) {
		if (!routeSegmentTimeSlice.isSetCurveExtent())
			throw new UnsupportedOperationException("Could not find a CurveExtent in this RouteSegment.");

		CurvePropertyType curve = routeSegmentTimeSlice.getCurveExtent();

		List<GeometryWithInterpolation> curveAsLineString = AIXMGeometryFactory.createCurve(curve);

		double width = calculateWidth(routeSegmentTimeSlice);

		AltitudeLimits altitude = AltitudeTools.createAltitueLimits(routeSegmentTimeSlice.getLowerLimit(),
				routeSegmentTimeSlice.getLowerLimitReference(), routeSegmentTimeSlice.getUpperLimit(),
				routeSegmentTimeSlice.getUpperLimitReference());
		
		GMLGeometryFactory.checkAndApplyInterpolation(curveAsLineString);

		return new RouteSegmentWithAltitudeLimits(curveAsLineString, altitude, width);
	}
	

	private static double calculateWidth(
			RouteSegmentTimeSliceType routeSegmentTimeSlice) {
		double left = Double.NaN;
		if (routeSegmentTimeSlice.isSetWidthLeft()) {
			left = XBeansUOMTools.parseValDistance(routeSegmentTimeSlice.getWidthLeft(), UOMTools.METER_UOM);
		}
		double right = Double.NaN;
		if (routeSegmentTimeSlice.isSetWidthRight()) {
			right = XBeansUOMTools.parseValDistance(routeSegmentTimeSlice.getWidthRight(), UOMTools.METER_UOM);
		}

		double width;
		if (!Double.isNaN(left)) {
			if (!Double.isNaN(right)) {
				width = left+right;
			} else {
				width = left*2;
			}
		} else {
			if (!Double.isNaN(right)) {
				width = right*2;	
			} else {
				//default
				width = 1000;
			}

		}

		return width;
	}
	
}
