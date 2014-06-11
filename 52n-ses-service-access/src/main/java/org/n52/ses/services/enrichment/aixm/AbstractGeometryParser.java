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
/**
 * 
 */
package org.n52.ses.services.enrichment.aixm;

import java.util.ArrayList;
import java.util.List;

import net.opengis.gml.x32.AbstractRingType;
import net.opengis.gml.x32.BoundingShapeType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.RingType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.api.exception.GMLParseException;
import org.n52.ses.io.parser.GML32Parser;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;

/**
 * Abstract class that offers basic methods to get the geometry of any AIXM feature.
 * Method parseGeometries must be implemented in order to retrieve the 
 * geometries of a feature.
 * 
 * @author Klaus Drerup <klaus.drerup@uni-muenster.de>
 *
 */
public abstract class AbstractGeometryParser {

	/**
	 * The resulting members from the WFS-request.
	 */
	protected XmlObject[] membersFromWFS;
	
	/**
	 * Parses the geometry from a gml:boundedBy element.
	 * 
	 * @param boundedBy
	 * 		the GML element
	 * @return
	 * 		the bounding box
	 * @throws GMLParseException 
	 * @throws ParseException 
	 */
	public Geometry parseBoundedBy(BoundingShapeType boundedBy) throws ParseException, GMLParseException{
		EnvelopeType envelope = boundedBy.getEnvelope();
		return envelopeToGeometry(envelope);
	}
	
	/**
	 * Calculates the bounding box of this feature. 
	 * 
	 * @return Geometry
	 * 		the bounding box
	 * @throws XmlException 
	 * @throws GMLParseException 
	 * @throws ParseException 
	 */
	public Geometry getBoundingBox() throws XmlException, ParseException, GMLParseException{
		List<Geometry> geometries = new ArrayList<Geometry>();
		for (XmlObject xmlO : this.getMembersFromWFS()){
			geometries.addAll(parseGeometries(xmlO));
		}
		return mergeGeometries(geometries);
	}
	
	/**
	 * Parses the geometries of this feature.
	 * 
	 * @param xmlO
	 * 		the feature
	 * @return
	 * 		the geometry 
	 * @throws XmlException
	 * @throws ParseException
	 * @throws GMLParseException
	 */
	protected abstract List<Geometry> parseGeometries(XmlObject xmlO) throws XmlException, ParseException, GMLParseException;
	
	/**
	 * Merges geometries and calculates their aggregated bounding boxes. 
	 * 
	 * @param geometries
	 * 		the geometries
	 * @return
	 * 		Envelope - the bounding box
	 */
	protected Geometry mergeGeometries(List<Geometry> geometries){
		Geometry aggregatedBB = null;
		
		Geometry[] geometryArray = geometries.toArray(new Geometry[0]);
		GeometryFactory geoFac = new GeometryFactory();
		GeometryCollection geoColl = new GeometryCollection(geometryArray, geoFac);
		aggregatedBB = geoColl.getEnvelope();
		
		return aggregatedBB;
	}
	
	/**
	 * Parses an AbstractRing (gml 3.2) to a geometry. 
	 * 
	 * @param abstractRing
	 * 		the abstract ring
	 * @return {@link Geometry} 
	 * 		the geometry
	 * @throws ParseException
	 * @throws GMLParseException
	 * @throws XmlException 
	 */
	protected Geometry ringToGeometry(AbstractRingType abstractRing) throws ParseException, GMLParseException, XmlException{
		// linear Ring
		if (abstractRing instanceof LinearRingType){
			LinearRingType linearRing = (LinearRingType) abstractRing;
			return GML32Parser.parseGeometry(linearRing);
		}
		
		// ring, can include curves
		else if (abstractRing instanceof RingType){
			return null;
		}
		return null;
	}
	
	

	/**
	 * @param membersFromWFS the membersFromWFS to set
	 */
	public void setMembersFromWFS(XmlObject[] membersFromWFS) {
		this.membersFromWFS = membersFromWFS;
	}

	/**
	 * @return the membersFromWFS
	 */
	public XmlObject[] getMembersFromWFS() {
		return membersFromWFS;
	}

	/**
	 * Parses an envelope to a geometry
	 * 
	 * @param envelope
	 * 		the envelope
	 * @return {@link Geometry} 
	 * 		the geometry
	 * @throws ParseException
	 * @throws GMLParseException
	 */
	protected Geometry envelopeToGeometry(EnvelopeType envelope) throws ParseException, GMLParseException {
		return GML32Parser.parseGeometry(envelope);
	}

	
}
