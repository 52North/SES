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
import net.opengis.gml.x32.AbstractSurfacePatchType;
import net.opengis.gml.x32.PolygonPatchType;

import org.apache.xmlbeans.XmlException;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
import org.n52.ses.api.exception.GMLParseException;

import aero.aixm.schema.x51.AirportHeliportTimeSlicePropertyType;
import aero.aixm.schema.x51.AirportHeliportTimeSliceType;
import aero.aixm.schema.x51.AirportHeliportType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;


/**
 * Parser of features of an Aixm Airport or Heliport. 
 * Creates the bounding box of all its geometries.
 * 
 * @author Klaus Drerup <klaus.drerup@uni-muenster.de>
 *
 */
public class AirportHeliportParser {
	
	public List<Geometry> parseGeometries(AirportHeliportType airportHeliport)
			throws XmlException, ParseException, GMLParseException {
		List<Geometry> geometries = new ArrayList<Geometry>();
		
		
		// get timeslices
		AirportHeliportTimeSlicePropertyType[] timeSliceArray 
			= airportHeliport.getTimeSliceArray();
		
		// iterate timeslices
		for (AirportHeliportTimeSlicePropertyType timeSlice: timeSliceArray){
			AirportHeliportTimeSliceType airportHeliportTimeSlice = timeSlice.getAirportHeliportTimeSlice();
			// is geometry defined?
			if (!airportHeliportTimeSlice.isSetAviationBoundary() 
					|| airportHeliportTimeSlice.getAviationBoundary().getElevatedSurface() == null
					|| airportHeliportTimeSlice.getAviationBoundary().getElevatedSurface().getPatches() == null
			)
				continue;
			AbstractSurfacePatchType[] abstractSurfacePatchArray 
				= airportHeliportTimeSlice.getAviationBoundary()
					.getElevatedSurface().getPatches().getAbstractSurfacePatchArray();
			String srs = airportHeliportTimeSlice.getAviationBoundary().getElevatedSurface().getSrsName();
			
			// iterate AbstractSurfacePatchArray
			for (AbstractSurfacePatchType patch : abstractSurfacePatchArray){
				AbstractRingType abstractRing = null;
				if ( patch instanceof PolygonPatchType){
					PolygonPatchType polyPatch = (PolygonPatchType) patch; 
					
					// parse abstract ring to geometry
					if (polyPatch.isSetExterior()){
						abstractRing = polyPatch.getExterior().getAbstractRing();
						
						// call the gml-parser:
						geometries.add(GMLGeometryFactory.createRing(abstractRing, srs));
					}
					
				}
				
			}
		}
		// parse bounded by
		if (geometries.size() == 0 && airportHeliport.isSetBoundedBy()){
			geometries.add(GMLGeometryFactory.createPolygon(airportHeliport.getBoundedBy()));
		}
		
		return geometries;
	}


}
