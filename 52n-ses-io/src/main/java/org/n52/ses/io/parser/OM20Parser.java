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
package org.n52.ses.io.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.AbstractTimeObjectType;
import net.opengis.gml.x32.FeatureCollectionDocument;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;
import net.opengis.om.x20.OMObservationDocument;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.om.x20.TimeObjectPropertyType;

import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.joda.time.DateTime;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.event.MapEvent;
import org.w3c.dom.Element;

/**
 * O&M 2.0 Parser.
 * Parsing is closely related to the outputs of the 52°North SOS 2.0
 * implementation.
 * 
 * @author matthes rieke
 *
 */
public class OM20Parser extends AbstractParser {

	Set<QName> supportedQNames = new HashSet<QName>(Arrays.asList(
			new QName[] {OMObservationDocument.type.getDocumentElementName(),
					FeatureCollectionDocument.type.getDocumentElementName()}));
	
	@Override
	public boolean accept(NotificationMessage message) {
		return hasMessageContent(message, supportedQNames);
	}

	@Override
	public List<MapEvent> parse(NotificationMessage message) throws Exception {
		List<Element> contents = extractMessageContent(message, supportedQNames);
		
		List<MapEvent> result = new ArrayList<MapEvent>();
		XmlObject xo = null;
		for (Element elem : contents) {
			xo = XmlObject.Factory.parse(elem);
			if (xo instanceof OMObservationDocument) {
				result.add(parseObservation((OMObservationDocument) xo));
			}
			else if (xo instanceof FeatureCollectionDocument) {
				result.addAll(parseFeatureCollection((FeatureCollectionDocument) xo));
			}
		}
		
		return result;
	}

	private List<MapEvent> parseFeatureCollection(FeatureCollectionDocument xo) {
		// TODO Auto-generated method stub
		return null;
	}

	private MapEvent parseObservation(OMObservationDocument xo) {
		OMObservationType obs = xo.getOMObservation();
		
		MapEvent result = parsePhenomenonTime(obs.getPhenomenonTime());
		
		parseProcedure(obs.getProcedure(), result);
		
		parseObservedProperty(obs.getObservedProperty(), result);
		
		parseFeatureOfInterest(obs.getFeatureOfInterest(), result);
		
		parseResult(obs.getResult(), result);
		
		return result;
	}

	private void parseResult(XmlObject object, MapEvent result) {
		if (object instanceof XmlAnyTypeImpl) {
			String value = XmlUtil.stripText(object);
			Double asDouble = parseAsDouble(value);
			if (asDouble != null) {
				result.put(MapEvent.DOUBLE_VALUE_KEY, asDouble.doubleValue());
				if (result.get(MapEvent.OBSERVED_PROPERTY_KEY) != null) {
					result.put(result.get(MapEvent.OBSERVED_PROPERTY_KEY).toString(), asDouble.doubleValue());
				}
			}
			else {
				if (result.get(MapEvent.OBSERVED_PROPERTY_KEY) != null) {
					result.put(result.get(MapEvent.OBSERVED_PROPERTY_KEY).toString(), value);
				}
			}
			
			result.put(MapEvent.STRING_VALUE_KEY, value);
			
		}
	}

	private Double parseAsDouble(String value) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
		}
		return null;
	}

	private void parseFeatureOfInterest(FeaturePropertyType featureOfInterest,
			MapEvent result) {
		if (featureOfInterest.isSetHref()) {
			result.put(MapEvent.FEATURE_TYPE_KEY, featureOfInterest.getHref());
		}
	}

	private void parseProcedure(OMProcessPropertyType procedure, MapEvent result) {
		if (procedure.isSetHref()) {
			result.put(MapEvent.SENSORID_KEY, procedure.getHref());
			result.put("procedure", procedure.getHref());
		}
	}

	private void parseObservedProperty(ReferenceType observedProperty,
			MapEvent result) {
		if (observedProperty.isSetHref()) {
			result.put(MapEvent.OBSERVED_PROPERTY_KEY, observedProperty.getHref());
		}
	}

	private MapEvent parsePhenomenonTime(TimeObjectPropertyType phenomenonTime) {
		AbstractTimeObjectType timeObject = phenomenonTime.getAbstractTimeObject();
		
		if (timeObject instanceof TimeInstantType) {
			TimePositionType pos = ((TimeInstantType) timeObject).getTimePosition();
			DateTime dateTime = new DateTime(pos.getStringValue());
			return new MapEvent(dateTime.getMillis(), dateTime.getMillis());
		}
		else if (timeObject instanceof TimePeriodType) {
			TimePositionType begin = ((TimePeriodType) timeObject).getBeginPosition();
			TimePositionType end = ((TimePeriodType) timeObject).getEndPosition();
			DateTime beginDate = new DateTime(begin.getStringValue());
			DateTime endDate = new DateTime(end.getStringValue());
			return new MapEvent(beginDate.getMillis(), endDate.getMillis());
		}
		
		return null;
	}

	@Override
	protected String getName() {
		return "O&M 2.0 Parser for Observation and gml32:FeatureCollection";
	}

}
