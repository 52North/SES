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
package org.n52.ses.io.parser;

import java.util.List;

import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.AbstractRingType;
import net.opengis.gml.x32.CoordinatesType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.EnvelopeDocument;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.GeodesicStringDocument;
import net.opengis.gml.x32.GeodesicStringType;
import net.opengis.gml.x32.LineStringDocument;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.PointDocument;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.PolygonDocument;
import net.opengis.gml.x32.PolygonType;
import net.opengis.gml.x32.PosDocument;
import net.opengis.gml.x32.TimeInstantDocument;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodDocument;
import net.opengis.gml.x32.TimePeriodType;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.oxf.conversion.gml32.geometry.GeometryWithInterpolation;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
import org.n52.ses.api.exception.GMLParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Class for GML 3.2 parsing.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class GML32Parser {

	/**
	 * namespace for GML 3.2
	 */
	public static final String GML32_NAMESPACE = "http://www.opengis.net/gml/3.2";

	private static final Logger logger = LoggerFactory
			.getLogger(GML32Parser.class);

	/**
	 * parses a Geometry Object from an EnvelopeType.
	 * 
	 * @param et The xml beans {@link EnvelopeType}
	 * @param crs the reference system if known, "" else
	 * @return java topology suite {@link Geometry}
	 * @throws ParseException if the geometry could not be parsed.
	 */
	private static Geometry getGeometryFromEnvelope(EnvelopeType et, String crs) throws ParseException {
		//get reference system if not yet defined
		String system = crs;
		if (et.isSetSrsName() && !system.equals("")) {
			system = et.getSrsName();
		}

		//get upper corner and split at " "
		Element elem = (Element) et.getUpperCorner().getDomNode();
		String ucString = XmlUtils.toString(elem.getFirstChild()).trim();
		String[] uc = ucString.split(" ");

		int i = 0;
		for (String string : uc) {
			string = string.trim();
			uc[i] = string;
			i++;
		}

		Coordinate upper = build2DCoordinate(uc[0], uc[1], system);

		//get lower corner and split at " "
		elem = (Element) et.getLowerCorner().getDomNode();
		String lcString = XmlUtils.toString(elem.getFirstChild()).trim();
		String[] lc = lcString.split(" ");

		i = 0;
		for (String string : lc) {
			string = string.trim();
			lc[i] = string;
			i++;
		}

		Coordinate lower = build2DCoordinate(lc[0], lc[1], system);

		String wktString = "POLYGON((" + lower.x + " "+ lower.y + ", "+ lower.x +" "+ upper.y +", "+ upper.x +" "+ upper.y +
		", "+ upper.x +" "+ lower.y +", "+ lower.x +" "+ lower.y + "))";

		WKTReader wktReader = new WKTReader();
		return wktReader.read(wktString);
	}


	/**
	 * parses a geometry from a point
	 * 
	 * @param pt xbeans {@link PointType}
	 * @param crs the reference system if known, "" else
	 * @return java topology suite {@link Geometry}
	 */
	private static final Geometry getGeometryFromPoint(PointType pt, String crs) {
		GeometryFactory gf = new GeometryFactory();

		Element elem = (Element) pt.getDomNode();
		String posString = XmlUtils.toString(elem.getFirstChild()).trim();

		if (posString.length() == 0) {
			posString = pt.getPos().getStringValue().trim();
		}

		String[] cs = posString.split(" ");

		//get reference system if not yet defined
		String system = crs;
		if (pt.isSetSrsName() && !system.equals("")) {
			system = pt.getSrsName();
		}


		if (cs != null && cs.length > 1) {
			Coordinate c = build2DCoordinate(cs[0], cs[1], system);
			return gf.createPoint(c);
		}

		return null;
	}


	/**
	 * Builds a 2D coordinate from two parts of a position string 
	 * and a reference system identifier.
	 * 
	 * @param first the first part of the position string
	 * @param second the second part of the position string
	 * @param crs the reference system identifier ("" if unknown)
	 * 
	 * @return a {@link Coordinate} at the given position
	 */
	private static Coordinate build2DCoordinate (String first, String second, String crs) {
		return new Coordinate(Double.parseDouble(first), Double.parseDouble(second));
	}

	/**
	 * Builds a 2D coordinate from two parts of a position string 
	 * and a reference system identifier.
	 * 
	 * @param first the first part of the position string
	 * @param second the second part of the position string
	 * @param crs the reference system identifier ("" if unknown)
	 * 
	 * @return a {@link Coordinate} at the given position
	 */
	private static Coordinate build2DCoordinate (double first, double second, String crs) {
		double x;
		double y;

		if (crs.equals("")) {
			/*
			 * not set 
			 * assume x, y
			 */
			x = first;
			y = second;
		}
		else if (crs.endsWith("4979")) {
			/*
			 * EPSG 4979 (WGS 83 3D)
			 * Axis order: lat, lon, h
			 */
			x = second;
			y = first;
			//H ignored
		}
		else if (crs.endsWith("4326")) {
			/*
			 * EPSG 4326 (WGS 84 2D)
			 * Axis order: lat, lon
			 */
			x = second;
			y = first;
		}
		else {
			/*
			 * unknown 
			 * assume x, y
			 */
			x = first;
			y = second;
		}

		return new Coordinate(x, y);
	}

	/**
	 * parses a geometry from a line string
	 * 
	 * @param lst xbeans {@link LineStringType}
	 * @param crs the reference system if known, "" else
	 * @return java topology suite {@link Geometry}
	 * @throws GMLParseException if parsing is not supported
	 */
	private static final Geometry getGeometryFromLineString(LineStringType lst, String crs) throws GMLParseException {
		//get reference system if not yet defined
		String system = crs.trim();
		if (lst.isSetSrsName() && system.isEmpty()) {
			system = lst.getSrsName();
		}

		GeometryFactory gf = new GeometryFactory();

		/*
		 * try posList
		 */
		if (lst.isSetPosList()) {
			return createLineStringFromPosList(lst.getPosList(), system, gf);
		}

		/*
		 * try coordinates
		 */
		if (lst.isSetCoordinates()) {
			CoordinatesType coords = lst.getCoordinates();
			String coordSep = coords.getCs();
			String decSep = coords.getDecimal();
			String tokSep = coords.getTs();

			String coordinates = coords.getStringValue().replaceAll("\\s+", " ");
			String[] coordArray = coordinates.split(tokSep);

			Coordinate[] coordinateSequence = new Coordinate[coordArray.length];

			int i = 0;
			for (String c : coordArray) {
				c = c.trim();
				String[] values = c.split(coordSep);
				if (values.length != 2) {
					//something wrong with the list
					return null;
				}

				//do the right decimal separator
				double value0 = 0;
				double value1 = 0;

				if (!decSep.equals(".")) {
					String[] valArray = values[0].split(decSep);

					if (valArray.length == 2) {
						value0 = Double.parseDouble(valArray[0] +"."+ valArray[1]);
					} else if (valArray.length == 1) {
						value0 = Double.parseDouble(valArray[0]);
					}

					valArray = values[1].split(decSep);

					if (valArray.length == 2) {
						value1 = Double.parseDouble(valArray[0] +"."+ valArray[1]);
					} else if (valArray.length == 1) {
						value1 = Double.parseDouble(valArray[0]);
					}	
				}
				else {
					value0 = Double.parseDouble(values[0]);
					value1 = Double.parseDouble(values[1]);
				}

				coordinateSequence[i] = build2DCoordinate(value0, value1, system);
				i++;
			}

			return gf.createLineString(coordinateSequence);
		}
		
		return null;
	}


	private static Geometry createLineStringFromPosList(DirectPositionListType posList,
			String system, GeometryFactory gf) throws GMLParseException {
		List<?> dataList = posList.getListValue();

		if ((dataList.size() % 2) != 0) {
			throw new GMLParseException("Odd element count. Either wrong number of doubles" +
			" inside the ring or coords are in 3D which is not supported.");
		}

		Coordinate[] coords = new Coordinate[dataList.size() / 2];

		for (int i = 0; i < dataList.size(); i=i+2) {
			coords[i/2] = build2DCoordinate((Double) dataList.get(i), (Double) dataList.get(i+1), system);
		}

		return gf.createLineString(coords);
	}


	/**
	 * Main method for parsing a geometry from an xml-fragment.
	 * This method delegates to the private concrete parsing methods.
	 * 
	 * @param geomElement the geometry xml object
	 * @return a {@link Geometry} as a JTS representation.
	 * @throws ParseException if the geometry could not be parsed.
	 * @throws GMLParseException if something could not be parsed or is not supported.
	 */
	public static Geometry parseGeometry(XmlObject geomElement) throws ParseException, GMLParseException {
		if (geomElement instanceof EnvelopeType) {
			return getGeometryFromEnvelope((EnvelopeType) geomElement, "");
		}
		else if (geomElement instanceof PointType) {
			return getGeometryFromPoint((PointType) geomElement, "");
		}
		else if (geomElement instanceof DirectPositionType) {
			DirectPositionType dirPos = (DirectPositionType) geomElement;
			String srs;
			if (dirPos.isSetSrsName()) {
				srs = dirPos.getSrsName();
			} else {
				srs = "";
			}
			return getGeoemtryFromPos(dirPos, srs);
		}
		else if (geomElement instanceof LineStringType) {
			return getGeometryFromLineString((LineStringType) geomElement, "");
		}
		else if (geomElement instanceof LinearRingType) {
			return getGeometryFromLinearRing((LinearRingType) geomElement, "");
		}
		else if (geomElement instanceof GeodesicStringDocument) {
			return getGeometryFromGeodesicString(((GeodesicStringDocument) geomElement).getGeodesicString());
		}
		else if (geomElement instanceof GeodesicStringType) {
			return getGeometryFromGeodesicString((GeodesicStringType) geomElement);
		}
		else if (geomElement instanceof PolygonType) {
			return getGeometryFromPolygon((PolygonType) geomElement, "");
		}
		else if (geomElement instanceof EnvelopeDocument) {
			return getGeometryFromEnvelope(((EnvelopeDocument) geomElement).getEnvelope(), "");
		}
		else if (geomElement instanceof PosDocument) {
			return getGeoemtryFromPos(((PosDocument) geomElement).getPos(), "");
		}
		else if (geomElement instanceof PolygonDocument) {
			return parseGeometry(((PolygonDocument) geomElement).getPolygon());
		}
		else if (geomElement instanceof PointDocument) {
			return parseGeometry(((PointDocument) geomElement).getPoint());
		}
		else if (geomElement instanceof LineStringDocument) {
			return parseGeometry(((LineStringDocument) geomElement).getLineString());
		}
		logger.info("could not parse gml type, returning null");
		return null;
	}

	private static Geometry getGeometryFromGeodesicString(GeodesicStringType geodesicString) {
		if (geodesicString.isSetPosList()) {
			GeometryWithInterpolation geom = GMLGeometryFactory.createGreatCirlce(geodesicString, null);
			GMLGeometryFactory.checkAndApplyInterpolation(geom);
			return geom.getGeometry();
		}
		throw new UnsupportedOperationException("Only posList is currently supported for GeodesicString.");
	}


	private static Geometry getGeoemtryFromPos(DirectPositionType pos, String crs) {
		//get reference system if not yet defined
		String system = crs;
		if (pos.isSetSrsName() && !system.equals("")) {
			system = pos.getSrsName();
		}

		List<?> list = pos.getListValue();
		if (list.size() < 2) {
			//not enough coordinates
			return null;
		}
		GeometryFactory gf = new GeometryFactory();
		return gf.createPoint(build2DCoordinate(list.get(0).toString(), list.get(1).toString(), crs));
	}


	/**
	 * Main method for parsing a geometry from an xml-fragment.
	 * If XmlAnyType object this tries to parse it to a geometry
	 * type.
	 * This method delegates to the private concrete parsing methods.
	 * 
	 * @param geomElement the geometry xml object
	 * @param anyType if this is an XmlAnyType object - tries parsing it.
	 * @return a {@link Geometry} as a JTS representation.
	 * @throws ParseException if the geometry could not be parsed.
	 * @throws GMLParseException if something could not be parsed or is not supported.
	 */
	public static Geometry parseGeometry(XmlObject geomElement, boolean anyType) throws ParseException, GMLParseException {
		if (anyType) {
			XmlObject xobj = null;
			try {
				xobj = XmlObject.Factory.parse(geomElement.toString());
			} catch (XmlException e) {
				// nothing, go on
			}

			if (xobj != null) {
				return parseGeometry(xobj);
			}
			throw new ParseException("Could not parse geometry - unkown type.");
		}

		return parseGeometry(geomElement);
	}


	/**
	 * parses a geometry from a polygon
	 * 
	 * @param pt the xbeans polygon type
	 * @param crs the reference system if known, "" else
	 * @return the {@link Geometry}
	 * @throws GMLParseException
	 */
	private static Geometry getGeometryFromPolygon(PolygonType pt, String crs) throws GMLParseException {
		//get reference system if not yet defined
		String system = crs;
		if (pt.isSetSrsName() && !system.equals("")) {
			system = pt.getSrsName();
		}

		GeometryFactory gf = new GeometryFactory();

		AbstractRingPropertyType[] inter = pt.getInteriorArray();
		if (inter.length > 0) {
			//			throw new GMLParseException("Currently interior rings in polygons" +
			//					" are not supported.");
			logger.info("interior ring is not parsed!");
		}

		/*
		 * parse exterior ring
		 */
		if (pt.isSetExterior()) {
			AbstractRingPropertyType exter = pt.getExterior();
			AbstractRingType ring = exter.getAbstractRing();

			if (ring instanceof LinearRingType) {
				Geometry ringGeom = getGeometryFromLinearRing((LinearRingType) ring, system);
				return gf.createPolygon((LinearRing) ringGeom, null);
			}
			throw new GMLParseException("Only LinearRing supported " +
			"at the current developement state.");
		}

		return null;
	}


	/**
	 * parses a geometry from a linear ring
	 * 
	 * @param lrt xbeans linear ring type
	 * @param crs the reference system if known, "" else
	 * @return a {@link Geometry}
	 * @throws GMLParseException
	 */
	private static Geometry getGeometryFromLinearRing(LinearRingType lrt, String crs) throws GMLParseException {
		if (lrt.isSetPosList()) {
			//parse posList
			GeometryFactory gf = new GeometryFactory();
			DirectPositionListType posList = lrt.getPosList();

			//get reference system if not yet defined
			String system = crs;
			if (posList.isSetSrsName() && !system.equals("")) {
				system = posList.getSrsName();
			}

			List<?> dataList = posList.getListValue();

			if ((dataList.size() % 2) != 0) {
				throw new GMLParseException("Odd element count. Either wrong number of doubles" +
				" inside the ring or coords are in 3D which is not supported.");
			}

			Coordinate[] coords = new Coordinate[(dataList.size() /2) + 1];

			for (int i = 0; i < dataList.size(); i=i+2) {
				coords[i/2] = build2DCoordinate((Double) dataList.get(i), (Double) dataList.get(i+1), system);
			}
			coords[coords.length - 1] = coords[0];

			return gf.createLinearRing(coords);
		}
		else if (lrt.isSetCoordinates()) {
			//parse coordinates
			return getGeometryFromCoordinates(lrt.getCoordinates(), crs);
		}
		else if (lrt.sizeOfPosArray() > 0){
			//parse PosArray
			GeometryFactory gf = new GeometryFactory();
			
			DirectPositionType[] posArray = lrt.getPosArray();
			Coordinate[] coords = new Coordinate[posArray.length];
			
			for (int i = 0; i < posArray.length; i++){
				List<?> xy = posArray[i].getListValue();
				if (xy.size() < 2) // coordinates missing
					return null;
				coords[i] = build2DCoordinate(xy.get(0).toString(), xy.get(1).toString(), crs);
			}
			
			return gf.createLinearRing(coords);
			
		}
		throw new GMLParseException("Only posList and posArray currently supported.");
	}


	/**
	 * parses a geometry from a coordinates set
	 * 
	 * @param ct the coordinates set as xbeans coordinates type
	 * @param crs the reference system if known, "" else
	 * @return a {@link Geometry}
	 * @throws GMLParseException
	 */
	private static Geometry getGeometryFromCoordinates(CoordinatesType ct, String crs) throws GMLParseException {
		GeometryFactory gf = new GeometryFactory();

		//read values
		String charSeparator = ct.getCs();
		String tupleSeparator = ct.getTs();
		String content = ct.getStringValue();

		//separate tuples
		String[] coords = content.split(tupleSeparator);
		Coordinate[] coordinates = new Coordinate[coords.length + 1];

		//build coordinates
		String[] coord;
		for (int i = 0; i < coords.length; i++) {
			coord = coords[i].split(charSeparator);
			coordinates[i] = build2DCoordinate(coord[0], coord[1], crs);
		}

		//close circle
		coordinates[coordinates.length - 1] = coordinates[0];

		//build linear ring (polygon)
		try {
			return gf.createLinearRing(coordinates);
		}
		catch (Throwable t) {
			throw new GMLParseException(t.getMessage());
		}

	}


	/**
	 * Parses the time from a GML32 time object.
	 * 
	 * @param time the xmlobject which holds the time
	 * @return {@link DateTime} array, null if nothing could be found
	 */
	public static DateTime[] parseTime(XmlObject time) {
		DateTime begin = null;
		DateTime end = null;

		/*
		 * wrapped in a document
		 */
		if (time instanceof TimePeriodDocument) {
			return parseTime(((TimePeriodDocument) time).getTimePeriod());
		}
		else if (time instanceof TimeInstantDocument) {
			return parseTime(((TimeInstantDocument) time).getTimeInstant());
		}

		/*
		 * just the types
		 */
		else if (time instanceof TimePeriodType) {
			/*
			 * TimePeriod
			 */
			TimePeriodType tpt = (TimePeriodType) time;

			if (tpt.isSetBeginPosition()) {
				begin = new DateTime(tpt.getBeginPosition().getStringValue());
			}
			else if (tpt.isSetBegin()) {
				begin = new DateTime(tpt.getBegin().getTimeInstant().getTimePosition().getStringValue());
			}

			if (tpt.isSetEndPosition()) {
				end = new DateTime(tpt.getEndPosition().getStringValue());
			}
			else if (tpt.isSetEnd()) {
				begin = new DateTime(tpt.getEnd().getTimeInstant().getTimePosition().getStringValue());
			}
		}
		else if (time instanceof TimeInstantType) {
			/*
			 * TimeInstanct
			 */
			TimeInstantType tit = (TimeInstantType) time;

			begin = new DateTime(tit.getTimePosition().getStringValue());

		}

		/*
		 * check what should be returned.
		 * 1-elem array if just begin is found
		 */
		if (begin != null) {
			if (end == null) {
				return new DateTime[] {begin};
			}
			return new DateTime[] {begin, end};
		}

		return null;
	}


}
