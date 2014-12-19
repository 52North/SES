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
package org.n52.ses.eml.v001.filter.spatial;


import net.opengis.fes.x20.DistanceBufferType;
import net.opengis.gml.x32.GeodesicStringDocument;
import net.opengis.gml.x32.LineStringDocument;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingDocument;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.PointDocument;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.PolygonDocument;
import net.opengis.gml.x32.PolygonType;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.GMLParseException;
import org.n52.ses.eml.v001.filterlogic.esper.customFunctions.MethodNames;
import org.n52.ses.io.parser.GML32Parser;
import org.n52.ses.util.geometry.SpatialAnalysisTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;


/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public abstract class ADistanceBufferFilter extends ASpatialFilter {
	
	private static final Logger logger = LoggerFactory
			.getLogger(ADistanceBufferFilter.class);
	
	
	private static final String GML_NAMESPACE = "http://www.opengis.net/gml/3.2";
	
	/**
	 * qualified name of GML Point
	 */
	protected static final String POINT_NAME = "Point";
	
	/**
	 * name of GML LinearRing
	 */
	protected static final String LINEAR_RING_NAME = "LinearRing";
	
	/**
	 * name of GML GeodesicString
	 */
	protected static final String GEODESIC_STRING_NAME = "GeodesicString";
	
	/**
	 * qualified name of GML LineString
	 */
	protected static final String LINE_STRING_NAME = "LineString";
	
	/**
	 * qualified name of GML Polygon
	 */
	protected static final String POLYGON_NAME = "Polygon";
	
	
	/**
	 * the type of this filter.
	 */
	protected DistanceBufferType distanceBufferType;

	private String crs = "";
	
	/**
	 * 
	 * Constructor
	 *
	 * @param dbOp distance buffer type
	 */
	public ADistanceBufferFilter(DistanceBufferType dbOp) {
		this.distanceBufferType = dbOp;
	}
	
	/**
	 * creates the esper sub-expression
	 * 
	 * @param methodName the java method name
	 * @return the sub-expression
	 */
	protected String createExpressionForDistanceFilter(String methodName) {
		boolean isNot = false;

		Element elem = (Element) this.distanceBufferType.getDistance().getDomNode();
		String val = XmlUtils.toString(elem.getFirstChild()).trim();
		
		double distance = Double.parseDouble(val);
		
		String uom = this.distanceBufferType.getDistance().getUom();
		
//		XmlObject elem2 = this.distanceBufferType.getDistance().selectAttribute(new QName("", "uom"));
//		if (elem2 != null) {
//			Node child = elem2.getDomNode().getFirstChild();
//			if (child != null) {
//				uom = XmlUtils.toString(child).trim();
//			}
//		}
		
		//hack for snowflakes "nautical miles"...........
		if (uom.equals("nautical mile")) {
			uom = "[nmi_i]";
		}
		
		
		//extract geometry
		Geometry geom = null;

		Node dbNode = this.distanceBufferType.getDomNode();
		NodeList nodes = dbNode.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			geom = this.parseGeometry(nodes.item(i));
			if (geom != null) {
				break;
			}
		}
		
		if (geom == null) {
			logger.info("geometry not found");
			return null;
		}
		
		//do the buffer
		geom = SpatialAnalysisTools.buffer(geom, distance, uom, this.crs);
		
		String mName;
		
		if (methodName.equals("beyond")) {
			//Buffer and NOT(intersect)
			isNot = true;
		}
		
		//we do an intersect to the buffer in the end
		mName = "intersects";
		
		StringBuilder sb = new StringBuilder();
		
		if (isNot) sb.append("not(");
		
		sb.append(MethodNames.SPATIAL_METHODS_PREFIX);
		sb.append(mName+ "(");
		sb.append(MethodNames.SPATIAL_METHODS_PREFIX);
		
		//create WKT from corners
		sb.append("fromWKT(\""+ geom.toText() +"\")");
		sb.append(", ");
		sb.append(MapEvent.GEOMETRY_KEY +")");
		
		if (isNot) sb.append(")");
		
		return sb.toString();
	}
	
	
	private Geometry parseGeometry (Node node) {
		if (node == null) {
			return null;
		}
		
		if (node.getNamespaceURI() == null) {
			return null;
		}
		
		if (!node.getNamespaceURI().equals(GML_NAMESPACE)) {
			//unsupported encoding
			return null;
		}
		Geometry result = null;
		String nodeName = node.getLocalName(); 
		if (LINEAR_RING_NAME.equals(nodeName)) {
			try {
				LinearRingDocument lrdoc = LinearRingDocument.Factory.parse(node);
				LinearRingType lrt = lrdoc.getLinearRing();
				result = GML32Parser.parseGeometry(lrt);
				
				/*
				 * get the srsName from posList or others
				 */
				if (lrt.isSetPosList()) {
					this.crs = lrt.getPosList().getSrsName();
				}
				else {
					if (lrt.getPointPropertyArray().length > 0) {
						if (lrt.getPointPropertyArray()[0].isSetPoint())
							this.crs = lrt.getPointPropertyArray()[0].getPoint().getSrsName();
					} else if (lrt.getPosArray().length > 0) {
						this.crs = lrt.getPosArray()[0].getSrsName();
					} else if (lrt.getPointRepArray().length > 0) {
						if (lrt.getPointRepArray()[0].isSetPoint())
							this.crs = lrt.getPointRepArray()[0].getPoint().getSrsName();
					}
				}
			} catch (ParseException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
			} catch (GMLParseException e) {
				throw new UnsupportedOperationException(e);
			}
			catch (XmlException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
			}
		}
		
		else if (GEODESIC_STRING_NAME.equals(nodeName)) {
			try {
				GeodesicStringDocument geodesic = GeodesicStringDocument.Factory.parse(node);
				if (geodesic.getGeodesicString().isSetPosList()) {
					this.crs = geodesic.getGeodesicString().getPosList().getSrsName();
				}
				return GML32Parser.parseGeometry(geodesic);
			} catch (XmlException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
			} catch (ParseException e) {
				throw new UnsupportedOperationException(e);
			} catch (GMLParseException e) {
				throw new UnsupportedOperationException(e);
			}
		}
		
		else if (POINT_NAME.equals(nodeName)) {
			try {
				PointDocument pdoc = PointDocument.Factory.parse(node);
				PointType pt = pdoc.getPoint();
				this.crs = pt.getSrsName();
				result = GML32Parser.parseGeometry(pt);
			} catch (ParseException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
			} catch (GMLParseException e) {
				throw new UnsupportedOperationException(e);
			}
			catch (XmlException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
			}
		}
		
		else if (LINE_STRING_NAME.equals(nodeName)) {
			logger.debug("extract line string");
			try {
				LineStringDocument lsdoc = LineStringDocument.Factory.parse(node);
				LineStringType lst = lsdoc.getLineString();
				this.crs = lst.getSrsName();
				result = GML32Parser.parseGeometry(lst);
			} catch (ParseException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
			} catch (GMLParseException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
				throw new UnsupportedOperationException(e);
			}
			catch (XmlException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
			}
		}
		
		else if (POLYGON_NAME.equals(nodeName)) {
			try {
				PolygonDocument pdoc = PolygonDocument.Factory.parse(node);
				PolygonType pt = pdoc.getPolygon();
				this.crs = pt.getSrsName();
				result = GML32Parser.parseGeometry(pt);
			}
			catch (ParseException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
			}
			catch (GMLParseException e) {
				throw new UnsupportedOperationException(e);
			}
			catch (XmlException e) {
				logger.warn("could not parse the geometry: " + e.getMessage());
			}
		}
		return result;
	}
}
