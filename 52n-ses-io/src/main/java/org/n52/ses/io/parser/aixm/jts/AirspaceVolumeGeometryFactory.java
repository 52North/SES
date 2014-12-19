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
package org.n52.ses.io.parser.aixm.jts;

import java.util.ArrayList;
import java.util.List;

import net.opengis.gml.x32.AbstractSurfacePatchType;
import net.opengis.gml.x32.PolygonPatchType;
import net.opengis.gml.x32.RectangleType;

import org.n52.oxf.conversion.gml32.geometry.AirspaceVolumeWithAltitudeLimits;
import org.n52.oxf.conversion.gml32.geometry.AltitudeLimits;
import org.n52.oxf.conversion.gml32.geometry.GeometryWithInterpolation;
import org.n52.oxf.conversion.gml32.srs.SRSUtils;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.PolygonFactory;
import org.n52.ses.io.parser.aixm.AltitudeTools;

import aero.aixm.schema.x51.AirspaceVolumeType;
import aero.aixm.schema.x51.CurvePropertyType;
import aero.aixm.schema.x51.SurfacePropertyType;
import aero.aixm.schema.x51.SurfaceType;
import aero.aixm.schema.x51.ValDistanceType;

/**
 * Class provides static methods for parsing AIXM
 * AirspaceVolumes.
 */
public class AirspaceVolumeGeometryFactory {

	/**
	 * @param volume the airspace volume
	 * @return an airspacevolume with possible multiple geometries and altitudes defined.
	 */
	public static AirspaceVolumeWithAltitudeLimits parseVolume(AirspaceVolumeType volume) {
		if (!volume.isSetHorizontalProjection() && !volume.isSetCentreline()) throw new UnsupportedOperationException("No horizontal projection or centreline was found.");
		
		List<GeometryWithInterpolation> horiz;
		if (volume.isSetHorizontalProjection()) {
			horiz = parseHorizontalProjection(volume.getHorizontalProjection());
		} else {
			horiz = parseCentreLine(volume.getCentreline(), volume.getWidth());
		}
		
		AltitudeLimits altitude = AltitudeTools.createAltitueLimits(volume.getLowerLimit(), volume.getLowerLimitReference(),
				volume.getUpperLimit(), volume.getUpperLimitReference());
		
		return new AirspaceVolumeWithAltitudeLimits(horiz, altitude);
	}

	private static List<GeometryWithInterpolation> parseCentreLine(CurvePropertyType centreline,
			ValDistanceType width) {
		throw new UnsupportedOperationException("centreline is currently not supported.");
	}

	private static List<GeometryWithInterpolation> parseHorizontalProjection(
			SurfacePropertyType surfaceProperty) {
		SurfaceType surface = surfaceProperty.getSurface();
		
		String srs;
		if (surface.isSetSrsName()) {
			srs = surface.getSrsName();
		}
		else {
			srs = SRSUtils.DEFAULT_SRS;
		}
		
		List<GeometryWithInterpolation> result = new ArrayList<GeometryWithInterpolation>();
		for (AbstractSurfacePatchType patch : surface.getPatches().getAbstractSurfacePatchArray()) {
			if (patch instanceof PolygonPatchType) {
				result.add(PolygonFactory.createPolygonPatch((PolygonPatchType) patch, srs));
			} else if (patch instanceof RectangleType) {
				result.add(GMLGeometryFactory.createRectangle((RectangleType) patch, srs));
			} else {
				throw new UnsupportedOperationException("Only Polygon and Rectangle patches supported.");
			}
		}
		return result;
	}
	
}
