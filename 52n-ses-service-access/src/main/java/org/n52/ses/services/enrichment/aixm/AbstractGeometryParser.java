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
