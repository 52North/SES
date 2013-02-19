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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.opengis.gml.x32.AbstractCurveSegmentType;
import net.opengis.gml.x32.BoundingShapeType;

import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.conversion.gml32.geometry.GeometryWithInterpolation;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
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
		Point geom = GMLGeometryFactory.createPoint(point.getPos(), point.getSrsName());
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
		Geometry result = GMLGeometryFactory.createPolygon(boundedBy);
		return new GeometryWithInterpolation(result, null);
	}


}
