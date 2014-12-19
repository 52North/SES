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
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.api.exception.GMLParseException;

import aero.aixm.schema.x51.AirspaceDocument;
import aero.aixm.schema.x51.AirspaceGeometryComponentPropertyType;
import aero.aixm.schema.x51.AirspaceTimeSlicePropertyType;
import aero.aixm.schema.x51.AirspaceType;
import aero.aixm.schema.x51.AirspaceVolumePropertyType;
import aero.aixm.schema.x51.SurfacePropertyType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/**
 * Parser of features of an aixm Airspace. 
 * Creates the bounding box of all its geometries.
 * 
 * @author Klaus Drerup <klaus.drerup@uni-muenster.de>
 *
 */
public class AirspaceParser extends AbstractGeometryParser {

	/**
	 * Get geometry components
	 * 
	 * @param timeSlice
	 * 		the timeslice
	 * @return geometryComponentArray
	 * 		the geometries
	 */
	private AirspaceGeometryComponentPropertyType[] getAirspaceGeometries(AirspaceTimeSlicePropertyType timeSlice){
		AirspaceGeometryComponentPropertyType[] geometryComponentArray = 
			timeSlice.getAirspaceTimeSlice().getGeometryComponentArray();
		return geometryComponentArray;
	}
	
	/**
	 * Get abstract surface patches
	 * 
	 * @param airSpaceGeometry
	 * 		the Geometry of an Airspace
	 * @return AbstractSurfacePatchType[]
	 * 		the abstractsurfacePatches
	 */
	private AbstractSurfacePatchType[] getSurfacePatches( AirspaceGeometryComponentPropertyType airSpaceGeometry){
		
		if (airSpaceGeometry.getAirspaceGeometryComponent().isSetTheAirspaceVolume()){
			AirspaceVolumePropertyType theAirspaceVolume = 
				airSpaceGeometry.getAirspaceGeometryComponent().getTheAirspaceVolume();
		
			if (theAirspaceVolume.getAirspaceVolume().isSetHorizontalProjection()){
				SurfacePropertyType horizontalProjection = 
					theAirspaceVolume.getAirspaceVolume().getHorizontalProjection();
				
				AbstractSurfacePatchType[] abstractSurfacePatchArray = 
					horizontalProjection.getSurface().getPatches().getAbstractSurfacePatchArray();
				
				return abstractSurfacePatchArray;
			}
		
		}
		return null;
	}
	
	/**
	 * Get abstract rings
	 * 
	 * @param abSurPatches
	 * 		the surfacepatches of a airspace
	 * @return AbstractRingType
	 * 		the abstractRing of this airspace
	 */
	private AbstractRingType getAbstractRing(AbstractSurfacePatchType abSurPatches){
		AbstractRingType abRing = null;
		if (abSurPatches instanceof PolygonPatchType){
			abRing = ((PolygonPatchType) abSurPatches).getExterior().getAbstractRing();
		}

		return abRing;
	}

	/* (non-Javadoc)
	 * @see org.n52.ses.enrichment.parser.AIXMAbstractGeometryParser#parseGeometries(org.apache.xmlbeans.XmlObject)
	 */
	@Override
	protected List<Geometry> parseGeometries(XmlObject xmlO)
			throws XmlException, ParseException, GMLParseException {
		
		List<Geometry> geometries = new ArrayList<Geometry>();
		
		// parse the feature's xml-document
		AirspaceDocument airSpaceDoc = AirspaceDocument.Factory.parse(xmlO.xmlText()); 
		AirspaceType airspace = airSpaceDoc.getAirspace();
		
		// iterate timeslices
		AirspaceTimeSlicePropertyType[] timeSlices = airspace.getTimeSliceArray();
		if (timeSlices != null)
		for (AirspaceTimeSlicePropertyType timeSlice : timeSlices){
			
			// iterate geometry components
			AirspaceGeometryComponentPropertyType[] airspaceGeometries = getAirspaceGeometries(timeSlice);
			if (airspaceGeometries != null)
			for (AirspaceGeometryComponentPropertyType airSpaceGeometry : airspaceGeometries){
				
				// iterate surface patches
				AbstractSurfacePatchType[] surfacePatches = getSurfacePatches(airSpaceGeometry);
				if (surfacePatches != null)
					for (AbstractSurfacePatchType patches : surfacePatches){
						
						AbstractRingType abstractRing = getAbstractRing(patches);
						
						// call the gml-parser:
						geometries.add(super.ringToGeometry(abstractRing));
					}
			}
		}
		
		// parse bounded by
		if (geometries.size() == 0 && airspace.isSetBoundedBy()){
			geometries.add(super.parseBoundedBy(airspace.getBoundedBy()));
		}
		
		return geometries;
	}
	
}
