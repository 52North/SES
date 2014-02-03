/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.opengis.gml.x32.AbstractCurveSegmentType;
import net.opengis.gml.x32.BoundingShapeType;

import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.conversion.gml32.geometry.GeometryWithInterpolation;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.PointFactory;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.PolygonFactory;
import org.n52.oxf.conversion.unit.UOMTools;
import org.n52.ses.io.parser.aixm.ElevatedSurfaceGeometry;
import org.n52.ses.io.parser.aixm.TimeSliceTools;
import org.n52.ses.io.parser.aixm.XBeansUOMTools;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import aero.aixm.schema.x51.AirportHeliportTimeSliceType;
import aero.aixm.schema.x51.AirportHeliportType;
import aero.aixm.schema.x51.ApronElementTimeSliceType;
import aero.aixm.schema.x51.ApronElementType;
import aero.aixm.schema.x51.CurvePropertyType;
import aero.aixm.schema.x51.ElevatedPointType;
import aero.aixm.schema.x51.ElevatedSurfaceType;
import aero.aixm.schema.x51.RunwayElementTimeSliceType;
import aero.aixm.schema.x51.RunwayElementType;
import aero.aixm.schema.x51.TaxiwayElementTimeSliceType;
import aero.aixm.schema.x51.TaxiwayElementType;

/**
 * Class providing static methods for parsing AIXM
 * and GML geometry abstractions.
 *
 */
public class AIXMGeometryFactory {


	/**
	 * @param curve the aixm curve element
	 * @return a list of geometries representing all abstract curve segments
	 * as LineStrings with an interpolation method defined.
	 */
	public static List<GeometryWithInterpolation> createCurve(CurvePropertyType curve) {
		List<GeometryWithInterpolation> result = new ArrayList<GeometryWithInterpolation>();

		String srs;
		if (curve.getCurve().isSetSrsName()) {
			srs = curve.getCurve().getSrsName().trim();
		} else {
			srs = "urn:ogc:def:crs:EPSG::4326";
		}

		for (AbstractCurveSegmentType segment : curve.getCurve().getSegments().getAbstractCurveSegmentArray()) {
			result.add(GMLGeometryFactory.createCurve(segment, srs));
		}

		return result;
	}
	
	public static GeometryWithInterpolation createElevatedPoint(ElevatedPointType point) {
		Point geom = PointFactory.createPoint(point.getPos(), point.getSrsName());
		return new GeometryWithInterpolation(geom, null);
	}

	public static List<GeometryWithInterpolation> parseGeometry(XmlObject object) {
		if (object instanceof ElevatedPointType) {
			return Collections.singletonList(createElevatedPoint((ElevatedPointType) object));
		}
		else if (object instanceof CurvePropertyType) {
			return createCurve((CurvePropertyType) object);
		}
		
		return null;
	}

	public static ElevatedSurfaceGeometry resolveTaxiwayElementGeometry(
			TaxiwayElementType te, Date validTime) {
		TaxiwayElementTimeSliceType slice = (TaxiwayElementTimeSliceType) TimeSliceTools.resolveTimeSliceFromValidTime(te, validTime);
		
		if (slice.isSetExtent()) {
			return createElevatedSurface(slice.getExtent().getElevatedSurface());
		}
		return null;
	}
	
	public static ElevatedSurfaceGeometry resolveRunwayElementGeometry(
			RunwayElementType re, Date validTime) {
		RunwayElementTimeSliceType slice = (RunwayElementTimeSliceType) TimeSliceTools.resolveTimeSliceFromValidTime(re, validTime);
		
		if (slice.isSetExtent()) {
			return createElevatedSurface(slice.getExtent().getElevatedSurface());
		}
		return null;
	}
	
	public static ElevatedSurfaceGeometry resolveApronElementGeometry(
			ApronElementType ae, Date validTime) {
		ApronElementTimeSliceType slice = (ApronElementTimeSliceType) TimeSliceTools.resolveTimeSliceFromValidTime(ae, validTime);
		if (slice.isSetExtent()) {
			return createElevatedSurface(slice.getExtent().getElevatedSurface());
		}
		
		return null;
	}

	public static ElevatedSurfaceGeometry createElevatedSurface(ElevatedSurfaceType elevatedSurface) {
		if (elevatedSurface == null) return null;
		
		String srs = elevatedSurface.getSrsName();
		
		List<GeometryWithInterpolation> geom = GMLGeometryFactory.createMultiPolygonPatch(
				elevatedSurface.getPatches(), srs);
		
		return new ElevatedSurfaceGeometry(geom,
				XBeansUOMTools.parseValDistance(elevatedSurface.getElevation(), UOMTools.METER_UOM));
	}

	public static GeometryWithInterpolation resolveAirportHeliportGeometry(
			AirportHeliportType ah, Date validTime) {
		AirportHeliportTimeSliceType slice = (AirportHeliportTimeSliceType) TimeSliceTools.resolveTimeSliceFromValidTime(ah, validTime);
		
		if (slice.isSetARP()) {
			return createElevatedPoint(slice.getARP().getElevatedPoint());
		}
		
		if (slice.isSetAviationBoundary()) {
			ElevatedSurfaceGeometry result = createElevatedSurface(slice.getAviationBoundary().getElevatedSurface());
			if (result.getGeometries().size() > 0) {
				return result.getGeometries().iterator().next();
			}
		}
		
		if (ah.isSetBoundedBy()) {
			return parseBoundedBy(ah.getBoundedBy());
		}
		
		return null;
	}

	private static GeometryWithInterpolation parseBoundedBy(
			BoundingShapeType boundedBy) {
		Geometry result = PolygonFactory.createPolygon(boundedBy);
		return new GeometryWithInterpolation(result, null);
	}


}
