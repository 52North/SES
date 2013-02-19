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
package org.n52.ses.services.enrichment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.opengis.wfs.x20.MemberPropertyType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.conversion.gml32.geometry.GeometryWithInterpolation;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.io.parser.OWS8Parser;
import org.n52.ses.io.parser.aixm.ElevatedSurfaceGeometry;
import org.n52.ses.io.parser.aixm.jts.AIXMGeometryFactory;
import org.n52.ses.services.wfs.WFSConnector;
import org.n52.ses.services.wfs.WFSQuery;
import org.n52.ses.services.wfs.queries.GetAssociatedFeatureByGMLIdentifier;
import org.n52.ses.services.wfs.queries.GetFeatureByAIXMDesignator;
import org.n52.ses.services.wfs.queries.GetFeatureByGMLIdentifier;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aero.aixm.schema.x51.AirportHeliportType;
import aero.aixm.schema.x51.AirspaceType;
import aero.aixm.schema.x51.ApronElementType;
import aero.aixm.schema.x51.RunwayElementType;
import aero.aixm.schema.x51.TaxiwayElementType;

import com.vividsolutions.jts.geom.Geometry;

public class WFSHandler implements EnrichmentHandler {

	private List<WFSConnector> wfsInstances;
	private static final Logger logger = LoggerFactory.getLogger(WFSHandler.class);


	public WFSHandler() {
		wfsInstances = new ArrayList<WFSConnector>();
		initWFSConnectors(ConfigurationRegistry.getInstance().getPropertyForKey("WFS_URL"));
	}

	/**
	 * Reads the configuration for WFS-settings and
	 * initializes for each WFS an WFS-Connector.
	 * 
	 * @param propertyForKey
	 * 		the property-string for the WFSs
	 */
	private void initWFSConnectors(String propertyForKey){
		// get properties of each WFS
		String[] wfsKeys = propertyForKey.split(";");

		for (String s : wfsKeys){
			WFSConnector wfsC = null;
			StringTokenizer st = new StringTokenizer(s,"@@");

			// url
			String url = st.nextToken();

			// soap
			String soapString = st.nextToken();
			Boolean usesSOAP = false;
			if (soapString.equals("true"))
				usesSOAP = true;

			// authentication
			String auth = "";
			if (st.hasMoreTokens())
				auth = st.nextToken().trim();

			// get user name and pw if available, create WFSConnector
			if (!auth.isEmpty()){
				String[] accessArray = auth.split(":");
				Map<String,String> userPW = new HashMap<String,String>();
				userPW.put(WFSConnector.USER_KEY, accessArray[0]);
				userPW.put(WFSConnector.PASSWORD_KEY, accessArray[1]);
				wfsC = new WFSConnector(url, usesSOAP, userPW);
			} else{
				wfsC = new WFSConnector(url, usesSOAP);
			}

			// add WFS to list
			if  (wfsC != null)
				wfsInstances.add(wfsC);	
		}
	}


	@Override
	public boolean enrichFeature(MapEvent mapEvent, String identifier,
			String featureType) {
		if (mapEvent.containsKey(MapEvent.GEOMETRY_KEY)) return false;
		// set parameter list for WFC connectors
		HashMap<String,String> featureOpts = new HashMap<String, String>();
		featureOpts.put(WFSConnector.FEATURE_TYPE_KEY, featureType);
		if (identifier != null)
			featureOpts.put(WFSConnector.GML_IDENTIFIER_KEY, identifier);


		if (identifier == null || featureType == null) {
			return false;
		}

		// enrich a specific feature type
		Geometry bbox = null;
		try {
			XmlObject[] wfsFeatures = executeQuery(createGetFeatureQuery(featureOpts));

			if (wfsFeatures != null) {
				bbox = resolveGeometry(wfsFeatures);
			}

		} catch (Exception e) {
			logger.info("Could not enrich event, identifier: " + identifier);
			logger.warn(e.getMessage(), e);
		}

		// enrichment: add information to mapEvent
		if (bbox != null) {
			logger.info("ENRICHMENT of event, adding geometry");
			mapEvent.put(MapEvent.GEOMETRY_KEY, bbox);
			return true;
		}

		return false;
	}


	private Geometry resolveGeometry(XmlObject[] wfsFeatures) throws Exception {
		for (XmlObject xmlObject : wfsFeatures) {
			MemberPropertyType member;
			if (xmlObject instanceof MemberPropertyType) {
				member = (MemberPropertyType) xmlObject;
				XmlCursor cur = member.newCursor();
				cur.toFirstChild();
				XmlObject inner = cur.getObject();

				// calculate a bounding box from WFS-response for the specific feature type
				if (inner instanceof AirportHeliportType){
					return enrichWithAirportHeliport((AirportHeliportType) inner);
				}
				else if (inner instanceof AirspaceType){
					return enrichWithAirspace((AirspaceType) inner);
				}
				else if (inner instanceof RunwayElementType){
					return enrichWithRunway((RunwayElementType) inner);
				}
				else if (inner instanceof TaxiwayElementType) {
					return enrichWithTaxiway((TaxiwayElementType) inner);
				}
				else if (inner instanceof ApronElementType) {
					return enrichWithApron((ApronElementType) inner);
				}
				
			}
		}

		return null;
	}

	private WFSQuery createGetFeatureQuery(HashMap<String, String> featureOpts) {
		if (!featureOpts.containsKey(WFSConnector.FEATURE_TYPE_KEY)) {
			throw new IllegalStateException("No FeatureType defined for the WFS Query.");
		}

		String featureType = featureOpts.get(WFSConnector.FEATURE_TYPE_KEY);

		if (featureType.equals(OWS8Parser.AIXM_RUNWAY_KEY) || featureType.equals(OWS8Parser.AIXM_TAXIWAY_KEY) ||
				featureType.equals(OWS8Parser.AIXM_APRON_KEY)){
			return new GetAssociatedFeatureByGMLIdentifier(featureOpts.get(WFSConnector.FEATURE_TYPE_KEY), 10,
					featureOpts.get(WFSConnector.GML_IDENTIFIER_KEY));
		}

		if (featureOpts.containsKey(WFSConnector.DESIGNATOR_KEY)) {
			return new GetFeatureByAIXMDesignator(featureOpts.get(WFSConnector.FEATURE_TYPE_KEY), 10,
					featureOpts.get(WFSConnector.DESIGNATOR_KEY));
		}
		else if (featureOpts.containsKey(WFSConnector.GML_IDENTIFIER_KEY)) {
			return new GetFeatureByGMLIdentifier(featureOpts.get(WFSConnector.FEATURE_TYPE_KEY), 10,
					featureOpts.get(WFSConnector.GML_IDENTIFIER_KEY));
		}

		return null;
	}


	/**
	 * Calculates the bounding box from the members in the WFS-response. 
	 * 
	 * @param inner
	 * 		features from the wfs
	 * @return {@link Geometry}
	 * 		 the bounding box
	 * @throws Exception
	 */
	private Geometry enrichWithTaxiway(TaxiwayElementType inner) throws Exception {
		ElevatedSurfaceGeometry geom = AIXMGeometryFactory.resolveTaxiwayElementGeometry(inner, new Date());
		
		if (geom == null) return null;
		
		GeometryWithInterpolation withInterpol = geom.getGeometries().iterator().next();
		GMLGeometryFactory.checkAndApplyInterpolation(withInterpol);
		return withInterpol.getGeometry();
	}

	/**
	 * Calculates the bounding box from the members in the WFS-response. 
	 * 
	 * @param inner
	 * 		features from the wfs
	 * @return {@link Geometry}
	 * 		 the bounding box
	 * @throws Exception
	 */
	private Geometry enrichWithRunway(RunwayElementType inner) throws Exception {
		ElevatedSurfaceGeometry geom = AIXMGeometryFactory.resolveRunwayElementGeometry(inner, new Date());
		
		if (geom == null) return null;
		
		GeometryWithInterpolation withInterpol = geom.getGeometries().iterator().next();
		GMLGeometryFactory.checkAndApplyInterpolation(withInterpol);
		return withInterpol.getGeometry();
	}

	private Geometry enrichWithApron(ApronElementType inner) throws Exception {
		ElevatedSurfaceGeometry geom = AIXMGeometryFactory.resolveApronElementGeometry(inner, new Date());
		
		if (geom == null) return null;
		
		GeometryWithInterpolation withInterpol = geom.getGeometries().iterator().next();
		GMLGeometryFactory.checkAndApplyInterpolation(withInterpol);
		return withInterpol.getGeometry();
	}

	/**
	 * Calculates the bounding box from the members in the WFS-response. 
	 * 
	 * @param aerodromes
	 * 		features from the wfs
	 * @return {@link Geometry}
	 * 		 the bounding box
	 * @throws Exception
	 */
	private Geometry enrichWithAirportHeliport(AirportHeliportType ah) throws Exception{
		if (ah != null){
			GeometryWithInterpolation result = AIXMGeometryFactory.resolveAirportHeliportGeometry(ah, new Date());
			return result.getGeometry(); 
		}

		return null;
	}

	/**
	 * Calculates the bounding box from the members in the WFS-response. 
	 * 
	 * @param inner
	 * 		features from the wfs
	 * @return {@link Geometry}
	 * 		 the bounding box
	 * @throws Exception
	 */
	private Geometry enrichWithAirspace(AirspaceType inner) throws Exception{
		Geometry boundingBox = null;


		return boundingBox;

	}


	public XmlObject[] executeQuery(WFSQuery query) {
		for (WFSConnector wfsC : wfsInstances){
			XmlObject[] wfsFeatures;
			try {
				wfsFeatures = wfsC.executeQuery(query);
			} catch (Exception e) {
				logger.warn(wfsC.getURL() + "; " +e.getMessage());
				continue;
			}
			if (wfsFeatures != null)
				return wfsFeatures;
		}

		return null;
	}

	@Override
	public boolean canHandle(String featureType) {
		if (featureType.equals(OWS8Parser.AIXM_AIRPORT_HELIPORT_KEY)){
			return true;
		}
		else if (featureType.equals(OWS8Parser.AIXM_AIRSPACE_KEY)){
			return true;
		}
		else if (featureType.equals(OWS8Parser.AIXM_RUNWAY_KEY)){
			return true;
		}
		else if (featureType.equals(OWS8Parser.AIXM_TAXIWAY_KEY)){
			return true;
		}
		else if (featureType.equals(OWS8Parser.AIXM_NAVAID_KEY)) {
			return true;
		}
		return false;
	}

}
