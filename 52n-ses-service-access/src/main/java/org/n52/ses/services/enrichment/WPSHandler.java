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
import java.util.Arrays;
import java.util.List;

import net.opengis.ows.x11.CodeType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.DataType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteDocument.Execute;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.conversion.gml32.geometry.GeometryWithInterpolation;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.io.parser.OWS8Parser;
import org.n52.ses.io.parser.aixm.jts.AIXMGeometryFactory;
import org.n52.ses.services.wps.WPSConnector;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class WPSHandler implements EnrichmentHandler {
	
	private List<WPSConnector> wpsInstances;
	private static final Logger logger = LoggerFactory.getLogger(WPSHandler.class);

	public WPSHandler() {
		this.wpsInstances = new ArrayList<WPSConnector>();
		initConnectors(ConfigurationRegistry.getInstance().getPropertyForKey("WPS_URL"));
	}

	private void initConnectors(String propertyForKey) {
		if (propertyForKey == null) return;
		
		String[] wpsKeys = propertyForKey.split(";");
		setServiceUrls(Arrays.asList(wpsKeys));
	}
	
	public void setServiceUrls(List<String> urls) {
		this.wpsInstances.clear();
		for (String s : urls) {
			this.wpsInstances.add(new WPSConnector(s));
		}
	}

	@Override
	public boolean enrichFeature(MapEvent mapEvent, String identifier,
			String featureType) {
		if (mapEvent.containsKey(MapEvent.GEOMETRY_KEY)) return false;
		Geometry geometry = resolveGeometryForFeature(identifier, featureType);
		if (geometry != null) {
			mapEvent.put(MapEvent.GEOMETRY_KEY, geometry);
			return true;
		}
		return false;
	}

	private Geometry resolveGeometryForFeature(String identifier, String featureType) {
		Geometry result = executeRequest(createByIdentifierExecute(identifier));
		
		if (result == null) {
			
		}
		return result;
	}

	private Geometry executeRequest(XmlObject request) {
		for (WPSConnector wps : this.wpsInstances) {
			try {
				ProcessOutputs result = wps.executeRequest(request);
				if (result != null) {
					return parseResult(result);
				}
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}
		
		return null;
	}

	private Geometry parseResult(ProcessOutputs result) {
		for (OutputDataType output : result.getOutputArray()) {
			DataType data = output.getData();
			if (data.isSetComplexData()) {
				List<GeometryWithInterpolation> geometryList = extractGeometry(data.getComplexData());
				return GMLGeometryFactory.createAggregatedGeometry(geometryList);
			}
		}
		return null;
	}

	private List<GeometryWithInterpolation> extractGeometry(ComplexDataType complexData) {
		XmlCursor cur = complexData.newCursor();
		cur.toFirstChild();
		return AIXMGeometryFactory.parseGeometry(cur.getObject());
	}

	private XmlObject createByIdentifierExecute(String identifier) {
		ExecuteDocument doc = ExecuteDocument.Factory.newInstance();
		Execute execute = doc.addNewExecute();
		execute.setService("WPS");
		execute.setVersion("1.0.0");
		CodeType processIdentifier = execute.addNewIdentifier();
		processIdentifier.setStringValue("ResolveAIXMFeatureGeometry");
		DataInputsType inputs = execute.addNewDataInputs();
		createFeatureInput(inputs.addNewInput(), identifier);
		return doc;
	}

	private void createFeatureInput(InputType input, String identifier) {
		input.addNewIdentifier().setStringValue("Feature");
		ComplexDataType complex = input.addNewData().addNewComplexData();
		complex.set(createFeatureIdentifierElement(identifier));
	}

	private XmlObject createFeatureIdentifierElement(String identifier) {
		StringBuilder sb = new StringBuilder();
		sb.append("<aixm-ext:FeatureIdentifier xmlns:aixm-ext=\"http://www.opengis.net/ows9/aviation/aixm/extension\">");
		sb.append("<aixm-ext:identifier codeSpace=\"urn:uuid:\">");
		sb.append(identifier);
        sb.append("</aixm-ext:identifier></aixm-ext:FeatureIdentifier>");
		try {
			return XmlObject.Factory.parse(sb.toString());
		} catch (XmlException e) {
			throw new IllegalStateException("Unexpected error.", e);
		}
	}

	@Override
	public boolean canHandle(String featureType) {
		if (featureType.equals(OWS8Parser.AIXM_TAXIWAY_KEY)) {
			return true;
		}
		else if (featureType.equals(OWS8Parser.AIXM_APRON_KEY)) {
			return true;
		}
		return false;
	}

}
