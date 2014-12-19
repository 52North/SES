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
