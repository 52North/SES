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
