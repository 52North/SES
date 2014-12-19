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
import org.n52.oxf.conversion.gml32.xmlbeans.jts.PolygonFactory;
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
			geometries.add(PolygonFactory.createPolygon(airportHeliport.getBoundedBy()));
		}
		
		return geometries;
	}


}
