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
package org.n52.ses.io.parser;

import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractRingPropertyType;
import net.opengis.gml.AbstractRingType;
import net.opengis.gml.CoordinatesType;
import net.opengis.gml.DirectPositionListType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.LineStringType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.MultiPolygonDocument;
import net.opengis.gml.MultiPolygonType;
import net.opengis.gml.PointType;
import net.opengis.gml.PolygonDocument;
import net.opengis.gml.PolygonPropertyType;
import net.opengis.gml.PolygonType;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.api.exception.GMLParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Class for GML 3.1.1 parsing.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 * @author Thomas Everding
 */
public class GML31Parser {

	/**
	 * Literal element of FES 2.0
	 */
	public static final QName FES_2_0_LITERAL_NAME = new QName(
			"http://www.opengis.net/fes/2.0", "Literal");

	/**
	 * global known namespace uri for GLM 3.1.1
	 */
	public static final String GML_3_1_1_NAME = "http://www.opengis.net/gml";

	/**
	 * creates a Geometry Object from an EnvelopeType.
	 * @param et The xml beans {@link EnvelopeType}
	 * @return java topolgy suite {@link Geometry}
	 * @throws ParseException if the geometry could not be parsed.
	 */
	private static Geometry getGeometryFromEnvelope(EnvelopeType et) throws ParseException {
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

		String wktString = "POLYGON(("+ lc[0] +" "+ lc[1] +", "+ lc[0] +" "+ uc[1] +", "+ uc[0] +" "+ uc[1] +
		", "+ uc[0] +" "+ lc[1] +", "+ lc[0] +" "+ lc[1]+ "))";

		WKTReader wktReader = new WKTReader();
		return wktReader.read(wktString);
	}


	/**
	 * @param pt xbeans {@link PointType}
	 * @return java topology suite {@link Geometry}
	 */
	private static final Geometry getGeometryFromPoint(PointType pt) {
		GeometryFactory gf = new GeometryFactory();

		Node child = pt.getDomNode().getFirstChild();

		while (child != null) {
			if (child.getLocalName()!= null && child.getLocalName().equals("pos")) {
				break;
			}
			child = child.getNextSibling();
		}

		String posString = "";
		if (child != null) {
			//child is the pos element
			posString = child.getFirstChild().getNodeValue();
		}

		//		Element elem = (Element) pt.getDomNode();
		//		posString = XmlUtils.toString(elem.getFirstChild()).trim();

		String[] cs = posString.split(" ");

		if (cs != null && cs.length > 1) {
			Coordinate c = new Coordinate(Double.parseDouble(cs[0]), Double.parseDouble(cs[1]));	
			return gf.createPoint(c);
		}

		return null;
	}

	/**
	 * @param lst xbeans {@link LineStringType}
	 * @return java topology suite {@link Geometry}
	 */
	private static final Geometry getGeometryFromLineString(LineStringType lst) {
		GeometryFactory gf = new GeometryFactory();

		CoordinatesType coords = lst.getCoordinates();
		String coordSep = coords.getCs();
		String decSep = coords.getDecimal();
		String tokSep = coords.getTs();

		String coordinates = coords.getStringValue().replaceAll("\\s+", " ");
		String[] coordArray = coordinates.split(tokSep);

		Coordinate[] coordinateSequence = new Coordinate[coordArray.length];

		int i = 0;
		for (String c : coordArray) {
			String[] values = c.split(coordSep);
			if (values.length != 2) {
				//something wrong with the list
				return null;
			}

			//do the right decimal seperator
			double value0 = 0, value1 = 0;

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

			coordinateSequence[i] = new Coordinate(value0,
					value1);
			i++;
		}

		return gf.createLineString(coordinateSequence);
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
			return getGeometryFromEnvelope((EnvelopeType) geomElement);
		}
		else if (geomElement instanceof PointType) {
			return getGeometryFromPoint((PointType) geomElement);
		}
		else if (geomElement instanceof LineStringType) {
			return getGeometryFromLineString((LineStringType) geomElement);
		}
		else if (geomElement instanceof LinearRingType) {
			return getGeometryFromLinearRing((LinearRingType) geomElement);
		}
		else if (geomElement instanceof PolygonType) {
			return getGeometryFromPolygon((PolygonType) geomElement);
		}
		else if (geomElement instanceof PolygonDocument) {
			return getGeometryFromPolygon(((PolygonDocument) geomElement).getPolygon());
		}
		else if (geomElement instanceof MultiPolygonType) {
			return getGeometryFromMultiPolygon((MultiPolygonType) geomElement);
		}
		else if (geomElement instanceof MultiPolygonDocument) {
			return getGeometryFromMultiPolygon(((MultiPolygonDocument) geomElement).getMultiPolygon());
		}

		return null;
	}


	private static Geometry getGeometryFromMultiPolygon(
			MultiPolygonType mpol) throws GMLParseException {

		Geometry result = null;

		for (PolygonPropertyType pol : mpol.getPolygonMemberArray()) {
			if (result == null) {
				result = getGeometryFromPolygon(pol.getPolygon());
			}
			else {
				result = result.union(getGeometryFromPolygon(pol.getPolygon()));
			}
		}

		return result;
	}


	private static Geometry getGeometryFromPolygon(PolygonType pt) throws GMLParseException {
		GeometryFactory gf = new GeometryFactory();

		AbstractRingPropertyType[] inter = pt.getInteriorArray();
		LinearRing[] holes = null;
		if (inter.length > 0) {
			holes = new LinearRing[inter.length];
			
			int count = 0;
			Geometry ringGeom;
			AbstractRingType ring;
			for (AbstractRingPropertyType inRing : inter) {
				ring = inRing.getRing();
				if (ring instanceof LinearRingType) {
					ringGeom = getGeometryFromLinearRing((LinearRingType) ring);
					holes[count] = (LinearRing) ringGeom;
				}
				else {
					/*
					 * could not handle interiors
					 */
					holes = null;
					break;
				}
				count++;
			}
		}

		/*
		 * parse exterior ring
		 */
		if (pt.isSetExterior()) {
			AbstractRingPropertyType exter = pt.getExterior();
			AbstractRingType ring = exter.getRing();

			if (ring instanceof LinearRingType) {
				Geometry ringGeom = getGeometryFromLinearRing((LinearRingType) ring);
				return gf.createPolygon((LinearRing) ringGeom, holes);
			}
			throw new GMLParseException("Only LinearRing supported " +
			"at the current developement state.");
		}

		return null;
	}


	@SuppressWarnings("rawtypes")
	private static Geometry getGeometryFromLinearRing(LinearRingType lrt) throws GMLParseException {
		GeometryFactory gf = new GeometryFactory();

		if (lrt.isSetPosList()) {
			DirectPositionListType posList = lrt.getPosList();

			List dataList = posList.getListValue();

			if ((dataList.size() % 2) != 0) {
				throw new GMLParseException("Odd element count. Either wrong number of doubles" +
				" inside the ring or coords are in 3D which is not supported.");
			}

			Coordinate[] coords = new Coordinate[(dataList.size() /2) + 1];

			for (int i = 0; i < dataList.size(); i=i+2) {
				coords[i/2] = new Coordinate((Double) dataList.get(i+1), (Double) dataList.get(i));
			}
			coords[coords.length - 1] = coords[0];

			return gf.createLinearRing(coords);
		}
		else if (lrt.isSetCoordinates()) {
			String[] dataList = lrt.getCoordinates().getStringValue().trim().split(" ");

			Coordinate[] coords = new Coordinate[dataList.length];

			String[] tmp;
			for (int i = 0; i < dataList.length; i++) {
				tmp = dataList[i].split(",");
				coords[i] = new Coordinate(Double.parseDouble(tmp[1]), Double.parseDouble(tmp[0]));
			}
			coords[coords.length - 1] = coords[0];

			return gf.createLinearRing(coords);
		}
		throw new GMLParseException("Only posList and coordinates currently supported.");
	}


}
