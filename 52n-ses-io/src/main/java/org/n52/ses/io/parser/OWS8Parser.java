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
package org.n52.ses.io.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.AbstractTimePrimitiveType;
import net.opengis.gml.x32.BoundingShapeType;
import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;
import net.opengis.gml.x32.TimePrimitivePropertyType;

import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.n52.oxf.conversion.gml32.geometry.GeometryWithInterpolation;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.GMLParseException;
import org.n52.ses.io.parser.aixm.jts.AIXMGeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import aero.aixm.schema.x51.AbstractAIXMFeatureType;
import aero.aixm.schema.x51.AirportHeliportAvailabilityType;
import aero.aixm.schema.x51.AirportHeliportTimeSliceType;
import aero.aixm.schema.x51.AirportHeliportType;
import aero.aixm.schema.x51.AirspaceActivationPropertyType;
import aero.aixm.schema.x51.AirspaceActivationType;
import aero.aixm.schema.x51.AirspaceLayerType;
import aero.aixm.schema.x51.AirspaceTimeSliceType;
import aero.aixm.schema.x51.AirspaceType;
import aero.aixm.schema.x51.ApronType;
import aero.aixm.schema.x51.CodeAirportHeliportDesignatorType;
import aero.aixm.schema.x51.CodeAirspaceDesignatorType;
import aero.aixm.schema.x51.ElevatedPointDocument;
import aero.aixm.schema.x51.ElevatedPointPropertyType;
import aero.aixm.schema.x51.ElevatedPointType;
import aero.aixm.schema.x51.NavaidTimeSliceType;
import aero.aixm.schema.x51.NavaidType;
import aero.aixm.schema.x51.RunwayTimeSliceType;
import aero.aixm.schema.x51.RunwayType;
import aero.aixm.schema.x51.TaxiwayType;
import aero.aixm.schema.x51.event.EventType;
import aero.aixm.schema.x51.message.AIXMBasicMessageDocument;
import aero.aixm.schema.x51.message.BasicMessageMemberAIXMPropertyType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;


/**
 * A parser for AIXM updates as provided in the 
 * OGC FAA SAA Dissemination Pilot
 * 
 * @author Thomas Everding, Matthes Rieke
 *
 */
public class OWS8Parser extends AbstractParser {

	private static final Logger logger = LoggerFactory
	.getLogger(OWS8Parser.class);

	/**
	 * AIXM message namespace
	 */
	public static final String AIXM_MESSAGE_NAMESPACE = "http://www.aixm.aero/schema/5.1/message";

	//AIXM Basic Message QName
	private static final QName MESSAGE_ROOT_QNAME = new QName(AIXM_MESSAGE_NAMESPACE, "AIXMBasicMessage");

	/**
	 * AIXM namespace
	 */
	public static final String AIXM_NAMESPACE = "http://www.aixm.aero/schema/5.1";

	private static final String AIXM_INTERPRETATION = "declare namespace aixm='"+ AIXM_NAMESPACE +"'; .//aixm:interpretation";

	/**
	 * reserved key for airspace identifier
	 */
	public static final String AIXM_AIRSPACE_KEY = "aixm:Airspace";


	/**
	 * reserved key for navaid identifier
	 */
	public static final String AIXM_NAVAID_KEY = "aixm:Navaid";

	public static final String AIXM_TAXIWAY_KEY = "aixm:Taxiway";

	public static final String AIXM_APRON_KEY = "aixm:Apron";

	/**
	 * reserved key  for airportheliport identifier
	 */
	public static final String AIXM_AIRPORT_HELIPORT_KEY = "aixm:AirportHeliport";

	/**
	 * reserved key for runway identifier
	 */
	public static final String AIXM_RUNWAY_KEY = "aixm:Runway";

	//UCUM code for foot
	private static final String UCUM_FEET = "[ft_i]";

	private DateTimeFormatter fmt = this.buildDTFormatter();



	@Override
	public boolean accept(NotificationMessage message) {
		Element elem = message.getMessageContent(MESSAGE_ROOT_QNAME);
		if (elem != null) {
			return true;
		}
		return false;
	}


	@Override
	public List<MapEvent> parse(NotificationMessage message) throws Exception {
		List<MapEvent> result = new ArrayList<MapEvent>();

		//get the notification content and create XMLBeans representation
		Element elem = message.getMessageContent(MESSAGE_ROOT_QNAME);
		AIXMBasicMessageDocument aixmMessage = AIXMBasicMessageDocument.Factory.parse(elem, null);

		result.add(this.parseAIXMBasicMessage(aixmMessage));

		return result;
	}


	/**
	 * parses an AIXM basic message
	 * 
	 * @param aixmMessage the message
	 * @return the message parse in a {@link MapEvent}
	 */
	public MapEvent parseAIXMBasicMessage(AIXMBasicMessageDocument aixmMessage) {
		MapEvent mapEvent = new MapEvent(0, 0);

		//get hasMember elements
		BasicMessageMemberAIXMPropertyType[] members = aixmMessage.getAIXMBasicMessage().getHasMemberArray();

		for (BasicMessageMemberAIXMPropertyType member : members) {
			this.parseMember(member, mapEvent);
		}
		
		if (!mapEvent.containsKey(MapEvent.GEOMETRY_KEY)) {
			tryLastCallGeometryResolve(mapEvent, aixmMessage);
		}

		return mapEvent;
	}


	/**
	 * Parses a member of the AIXM Basic Message
	 * 
	 * @param member the member
	 * @param mapEvent the event to populate
	 */
	/**
	 * @param member
	 * @param mapEvent
	 */
	private void parseMember(BasicMessageMemberAIXMPropertyType member, MapEvent mapEvent) {
		AbstractAIXMFeatureType abstractAIXMFeature = member.getAbstractAIXMFeature();


		if (!(abstractAIXMFeature instanceof EventType)) {
			if (abstractAIXMFeature.isSetIdentifier()) {
				mapEvent.put(MapEvent.IDENTIFIER_VALUE_KEY, abstractAIXMFeature.getIdentifier().getStringValue().trim());
			}
			
			String elementName = abstractAIXMFeature.getDomNode().getLocalName();
			if (elementName != null) {
				mapEvent.put(MapEvent.FEATURE_TYPE_KEY, "aixm:" + elementName.trim());
			}	
		}

		if (abstractAIXMFeature instanceof EventType) {
			//parse the Event part
			EventType event = (EventType) abstractAIXMFeature;
			this.parseEvent(event, mapEvent);
		}
		else if (abstractAIXMFeature instanceof AirspaceType) {
			//parse the Airspace part
			AirspaceType airspace = (AirspaceType) abstractAIXMFeature;
			this.parseAirspace(airspace, mapEvent);
		}
		else if (abstractAIXMFeature instanceof AirportHeliportType) {
			AirportHeliportType airport = (AirportHeliportType) abstractAIXMFeature;
			this.parseAirportHeliport(airport, mapEvent);
		}
		else if (abstractAIXMFeature instanceof RunwayType) {
			// parse the Runway part
			RunwayType runway = (RunwayType) abstractAIXMFeature;
			this.parseRunway(runway, mapEvent);
		}
		else if (abstractAIXMFeature instanceof NavaidType) {
			NavaidType navaid = (NavaidType) abstractAIXMFeature;
			this.parseNavaid(navaid, mapEvent);
		}
		else if (abstractAIXMFeature instanceof TaxiwayType) {
			this.parseTaxiway((TaxiwayType) abstractAIXMFeature, mapEvent);
		}
		else if (abstractAIXMFeature instanceof ApronType) {
			this.parseApron((ApronType) abstractAIXMFeature, mapEvent);
		}
		
	}


	private void tryLastCallGeometryResolve(MapEvent mapEvent,
			XmlObject aixmMessage) {
		Set<QName> candidates = new HashSet<QName>();
		candidates.add(new QName(AIXM_NAMESPACE, "location"));
		candidates.add(new QName(AIXM_NAMESPACE, "position"));
		for (QName qn : candidates) {
			XmlObject[] locations = XmlUtil.selectPath("declare namespace aixm='"+ qn.getNamespaceURI() +"'; .//aixm:"+qn.getLocalPart(), aixmMessage);
			for (XmlObject location : locations) {
				XmlCursor cur = location.newCursor();
				if (cur.toChild(ElevatedPointDocument.type.getDocumentElementName())) {
					GeometryWithInterpolation geom = AIXMGeometryFactory.createElevatedPoint((ElevatedPointType) cur.getObject());
					if (geom != null && geom.getGeometry() != null) {
						mapEvent.put(MapEvent.GEOMETRY_KEY, geom.getGeometry());
						return;
					}
				}
			}	
		}
		
	}


	private void parseApron(ApronType apron, MapEvent mapEvent) {
		// TODO Auto-generated method stub

	}


	private void parseTaxiway(TaxiwayType taxiway, MapEvent mapEvent) {
		// TODO Auto-generated method stub

	}


	/**
	 * Parses a Navaid.
	 * 
	 * @param navaid the navaid type
	 * @param mapEvent the event to be populated
	 */
	private void parseNavaid(NavaidType navaid, MapEvent mapEvent) {
		if (navaid.isSetIdentifier()) {
			CodeWithAuthorityType identifier = navaid.getIdentifier();
			this.parseGMLIdentifier(identifier, mapEvent);
		}

		//parse the timeslice
		if (navaid.getTimeSliceArray().length > 0) {
			NavaidTimeSliceType airspaceTimeSlice = navaid.getTimeSliceArray(0).getNavaidTimeSlice();
			this.parseTimeSlice(airspaceTimeSlice, mapEvent);
		}

		mapEvent.put(MapEvent.FEATURE_TYPE_KEY, AIXM_NAVAID_KEY);
	}


	/**
	 * Parses an airspace
	 * 
	 * @param airspace the airspace type
	 * @param mapEvent the event to be populated
	 */
	private void parseAirspace(AirspaceType airspace, MapEvent mapEvent) {
		//parse the identifier
		if (airspace.isSetIdentifier()) {
			CodeWithAuthorityType identifier = airspace.getIdentifier();
			this.parseGMLIdentifier(identifier, mapEvent);
		}

		//parse the time slice
		if (airspace.getTimeSliceArray().length > 0) {
			AirspaceTimeSliceType airspaceTimeSlice = airspace.getTimeSliceArray(0).getAirspaceTimeSlice();
			this.parseTimeSlice(airspaceTimeSlice, mapEvent);
		}

		if (airspace.isSetBoundedBy()) {
			//parse bounding box
			this.parseBoundingBox(airspace.getBoundedBy(), mapEvent);
		}

		mapEvent.put(MapEvent.FEATURE_TYPE_KEY, AIXM_AIRSPACE_KEY);
	}


	/**
	 * Parsed the AirportHeliportsUsage element.
	 * @throws ParseException 
	 */
	private void parseAirportHeliport(AirportHeliportType airport, MapEvent mapEvent) {
		//parse the identifier
		if (airport.isSetIdentifier()) {
			CodeWithAuthorityType identifier = airport.getIdentifier();
			this.parseGMLIdentifier(identifier, mapEvent);
		}

		//parse the time slice
		if (airport.getTimeSliceArray().length > 0) {
			AirportHeliportTimeSliceType airspaceTimeSlice = airport.getTimeSliceArray(0).getAirportHeliportTimeSlice();
			this.parseTimeSlice(airspaceTimeSlice, mapEvent);
		}
		
		GeometryWithInterpolation geom = AIXMGeometryFactory.resolveAirportHeliportGeometry(airport, new Date());
		if (geom != null && geom.getGeometry() != null) {
			mapEvent.put(MapEvent.GEOMETRY_KEY, geom.getGeometry());
		}

		mapEvent.put(MapEvent.FEATURE_TYPE_KEY, AIXM_AIRPORT_HELIPORT_KEY);
	}

	/**
	 * Parses the runway element.
	 * 
	 * @param runway
	 * 		the runway
	 * @param mapEvent
	 * 		the mapevent
	 */
	private void parseRunway(RunwayType runway, MapEvent mapEvent){
		// parse identifier
		if (runway.isSetIdentifier()){
			CodeWithAuthorityType identifier = runway.getIdentifier();
			this.parseGMLIdentifier(identifier, mapEvent);
		}

		//parse the time slice
		if (runway.sizeOfTimeSliceArray()>0){
			RunwayTimeSliceType runwayTimeSlice = runway.getTimeSliceArray(0).getRunwayTimeSlice();
			this.parseTimeSlice(runwayTimeSlice, mapEvent);
		}

		mapEvent.put(MapEvent.FEATURE_TYPE_KEY, AIXM_RUNWAY_KEY);
	}

	/**
	 * parses an airspace time slice
	 * 
	 * @param airspaceTimeSlice the time slice node of an airspace
	 * @param mapEvent the map event to populate
	 */
	private void parseTimeSlice(XmlObject timeSlice, MapEvent mapEvent) {
		/*
		 * general parsing stuff
		 */
		XmlObject[] interpret = XmlUtil.selectPath(AIXM_INTERPRETATION, timeSlice);
		if (interpret.length > 0) {
			mapEvent.put("interpretation", XmlUtil.stripText(interpret[0]));
		}

		/*
		 * specific parsing
		 */
		if (timeSlice instanceof AirspaceTimeSliceType) {
			AirspaceTimeSliceType airspaceTimeSlice = (AirspaceTimeSliceType) timeSlice;

			//parse valid time
			this.parseValidTime(airspaceTimeSlice.getValidTime(), mapEvent);

			//parse activation
			if (airspaceTimeSlice.getActivationArray().length > 0) {
				AirspaceActivationPropertyType activation = airspaceTimeSlice.getActivationArray(0);
				this.parseActivation(activation.getAirspaceActivation(), mapEvent);
			}

			//parse designator
			if (airspaceTimeSlice.isSetDesignator()){
				this.parseDesignator(airspaceTimeSlice.getDesignator(), mapEvent);
			}

			mapEvent.put("aixm:interpretation", airspaceTimeSlice.getInterpretation());
		}
		else if (timeSlice instanceof AirportHeliportTimeSliceType) {
			AirportHeliportTimeSliceType airportTimeSlice = (AirportHeliportTimeSliceType) timeSlice;

			//parse valid time
			this.parseValidTime(airportTimeSlice.getValidTime(), mapEvent);

			//parse designator
			if (airportTimeSlice.isSetDesignator()){
				this.parseDesignator(airportTimeSlice.getDesignator(), mapEvent);
			}
			
			if (airportTimeSlice.getAvailabilityArray().length > 0) {
				AirportHeliportAvailabilityType avail = airportTimeSlice.getAvailabilityArray(0).getAirportHeliportAvailability();
				if (avail.isSetOperationalStatus()) {
					mapEvent.put("aixm:operationalStatus", avail.getOperationalStatus().getStringValue());	
				}
			}

			mapEvent.put("interpretation", airportTimeSlice.getInterpretation());
		}
		else if (timeSlice instanceof RunwayTimeSliceType){
			RunwayTimeSliceType runwayTimeSlice = (RunwayTimeSliceType) timeSlice;

			// parse valid time
			this.parseValidTime(runwayTimeSlice.getValidTime(), mapEvent);

			// parse designator
			if (runwayTimeSlice.isSetDesignator()){
				this.parseDesignator(runwayTimeSlice.getDesignator(), mapEvent);
			}

			mapEvent.put("interpretation", runwayTimeSlice.getInterpretation());
		}
		else if (timeSlice instanceof NavaidTimeSliceType) {
			NavaidTimeSliceType navaidTimeSlice = (NavaidTimeSliceType) timeSlice;

			// parse valid time
			this.parseValidTime(navaidTimeSlice.getValidTime(), mapEvent);

			// parse designator
			if (navaidTimeSlice.isSetDesignator()){
				this.parseDesignator(navaidTimeSlice.getDesignator(), mapEvent);
			}

			if (navaidTimeSlice.isSetLocation()) {
				this.parseLocation(navaidTimeSlice.getLocation(), mapEvent);
			}

			mapEvent.put("interpretation", navaidTimeSlice.getInterpretation());
		}

	}



	private void parseLocation(ElevatedPointPropertyType location,
			MapEvent mapEvent) {

		Geometry geom = null;
		try {
			geom = GML32Parser.parseGeometry(location.getElevatedPoint());
		} catch (ParseException e) {
			logger.warn(e.getMessage(), e);
		} catch (GMLParseException e) {
			logger.warn(e.getMessage(), e);
		}

		if (geom != null) {
			mapEvent.put(MapEvent.GEOMETRY_KEY, geom);
		}
	}


	/**
	 * Parses the designator
	 * 
	 * @param designator the designator of an airspacetiemslice
	 * @param mapEvent the map event to populate
	 */
	private void parseDesignator(XmlObject designator,
			MapEvent mapEvent) {
		String designatorValue = null;


		if (designator instanceof CodeAirspaceDesignatorType) {
			// parse the designator value
			designatorValue = ((CodeAirspaceDesignatorType) designator).getStringValue();
		}
		else if (designator instanceof CodeAirportHeliportDesignatorType) {
			// parse the designator value
			designatorValue = ((CodeAirportHeliportDesignatorType) designator).getStringValue();
		}

		// parse the designator value
		mapEvent.put(MapEvent.AIXM_DESIGNATOR_KEY, designatorValue);
	}


	/**
	 * Parses the activation of the time slice
	 * 
	 * @param activation the aixm:activation element
	 * @param mapEvent the map event to populate
	 */
	private void parseActivation(AirspaceActivationType activation, MapEvent mapEvent) {
		//parse status
		if (activation.isSetStatus()) {
			String status = activation.getStatus().getStringValue();
			mapEvent.put(MapEvent.STAUS_KEY, status);
		}

		//parse levels
		if (activation.getLevelsArray().length > 0) {
			AirspaceLayerType levels = activation.getLevelsArray(0).getAirspaceLayer();
			this.parseLevels(levels, mapEvent);
		}

		//		//parse extension
		//		if (activation.getExtensionArray().length > 0) {
		//			Extension extension = activation.getExtensionArray(0);
		//			this.parseActivationExtension(extension, mapEvent);
		//		}
	}


	//	/**
	//	 * parses an aixm:extension element of the activation
	//	 * 
	//	 * @param extension the extension element
	//	 * @param mapEvent the event to populate
	//	 */
	//	private void parseActivationExtension(Extension extension, MapEvent mapEvent) {
	//		//get the reservation phase element
	//		AbstractExtensionType ext = extension.getAbstractAirspaceActivationExtension();
	//		if (ext instanceof AirspaceActivationExtensionType) {
	//			AirspaceActivationExtensionType aaEx = (AirspaceActivationExtensionType) ext;
	//			
	//			if (aaEx.isSetReservationPhase()) {
	//				String phase = aaEx.getReservationPhase().toString();
	//				mapEvent.put(MapEvent.RESERVATION_PHASE_KEY, phase);
	//			}
	//		}
	//	}


	/**
	 * Parses the levels of an activation
	 * 
	 * @param layer the aixm:levels element
	 * @param mapEvent the event to populate
	 */
	private void parseLevels(AirspaceLayerType layer, MapEvent mapEvent) {
		if (!layer.isSetLowerLimit() || !layer.isSetUpperLimit()) {
			return;
		}

		/*
		 * do we have layer elements?
		 */
		if (!(layer.isSetLowerLimit() && layer.isSetUpperLimit()))
			return;

		//get values and convert
		double u = Double.parseDouble(layer.getUpperLimit().getStringValue());
		double l = Double.parseDouble(layer.getLowerLimit().getStringValue());

		//convert lower to SI unit
		String uom = "";
		if (layer.getLowerLimit().isSetUom()) {
			uom = layer.getLowerLimit().getUom();

			//adjust non-UCUM codes
			if (uom.equals("FT") || uom.equals("ft")) {
				uom = UCUM_FEET;
			}
		}
		l = this.unitConverter.convert(uom, l).getValue();

		//convert upper to SI unit
		uom = "";
		if (layer.getUpperLimit().isSetUom()) {
			uom = layer.getUpperLimit().getUom();

			//adjust non-UCUM codes
			if (uom.equals("FT") || uom.equals("ft")) {
				uom = UCUM_FEET;
			}
		}
		u = this.unitConverter.convert(uom, u).getValue();

		//store level information in event
		mapEvent.put(MapEvent.LOWER_LIMIT_KEY, l);
		mapEvent.put(MapEvent.UPPER_LIMIT_KEY, u);
	}


	/**
	 * Parses the valid time of the time slice
	 * 
	 * @param timePrimitivePropertyType the gml:validTime element
	 * @param mapEvent the map event to populate
	 */
	private void parseValidTime(TimePrimitivePropertyType timePrimitivePropertyType, MapEvent mapEvent) {
		long begin = 0;
		long end = 0;

		//parse the valid time element
		AbstractTimePrimitiveType abstractTime = timePrimitivePropertyType.getAbstractTimePrimitive();
		if (abstractTime instanceof TimePeriodType) {
			//parse gml:TimePeriod type
			TimePeriodType timePeriod = (TimePeriodType) abstractTime;
			if (timePeriod.isSetBeginPosition()) {
				begin = this.parseTimePosition(timePeriod.getBeginPosition());
			}
			if (timePeriod.isSetEndPosition()) {
				end = this.parseTimePosition(timePeriod.getEndPosition());
			}
		}
		//parse other time types here

		mapEvent.put(MapEvent.START_KEY, begin);
		mapEvent.put(MapEvent.END_KEY, end);
	}


	/**
	 * Parses a gml:identifier element
	 * 
	 * @param identifier the gml:identifier
	 * @param mapEvent the map event to populate
	 */
	private void parseGMLIdentifier(CodeWithAuthorityType identifier, MapEvent mapEvent) {
		//get value
		String value = identifier.getStringValue();

		//get code space
		String codeSpace = identifier.getCodeSpace();

		//set properties
		mapEvent.put(MapEvent.IDENTIFIER_CODESPACE_KEY, codeSpace);
		mapEvent.put(MapEvent.IDENTIFIER_VALUE_KEY, value);
	}


	/**
	 * Parses an event:Event into a {@link MapEvent}
	 * 
	 * @param event the AIXM Event to parse
	 * 
	 * @param mapEvent the map event for the results
	 */
	private void parseEvent(EventType event, MapEvent mapEvent) {
		if (event.isSetBoundedBy()) {
			//parse the bounding box of the event
			this.parseBoundingBox(event.getBoundedBy(), mapEvent);
		}
	}


	/**
	 * parses a bounding box
	 * 
	 * @param boundingBox the gml Bounding box
	 * @param mapEvent the event to populate
	 */
	private void parseBoundingBox(BoundingShapeType boundingBox, MapEvent mapEvent) {
		//check if a bounding box was already parsed (multiple instances possible)
		if (mapEvent.containsKey(MapEvent.GEOMETRY_KEY)) {
			//geometry already parsed and set
			return;
		}

		//geometry not yet parsed, do so
		EnvelopeType envelope = boundingBox.getEnvelope();
		try {
			Geometry geom = GML32Parser.parseGeometry(envelope);
			mapEvent.put(MapEvent.GEOMETRY_KEY, geom);
		}
		catch (ParseException e) {
			logger.warn(e.getMessage(), e);
		}
		catch (GMLParseException e) {
			logger.warn(e.getMessage(), e);
		}
	}


	/**
	 * Parses a time position
	 * 
	 * @param timePositionType
	 * @return
	 */
	private long parseTimePosition(TimePositionType timePositionType) {
		String timeString = timePositionType.getStringValue().trim();

		if (timeString.isEmpty()) {
			return new DateTime("9999-01-01T00:00:00.000+00:00").getMillis();
		}

		//replace Z by +00:00
		if (timeString.endsWith("Z")) {
			timeString = timeString.substring(0, timeString.length() - 1) + "+00:00";
		}

		DateTime position = this.fmt.parseDateTime(timeString);
		return position.getMillis();
	}


	/**
	 * 
	 * @return a formatter for common ISO strings
	 */
	private DateTimeFormatter buildDTFormatter() {
		//build a parser for time stamps
		return new DateTimeFormatterBuilder()
		.appendYear(4, 4)		//4 digit year (YYYY)
		.appendLiteral("-")
		.appendMonthOfYear(2)	//2 digit month (MM)
		.appendLiteral("-")
		.appendDayOfMonth(2)	//2 digit day (DD)
		.appendLiteral("T")
		.appendHourOfDay(2)		//2 digit hour (hh)
		.appendLiteral(":")
		.appendMinuteOfHour(2)	//2 digit minute (mm)
		.appendLiteral(":")
		.appendSecondOfMinute(2)//2 digit second (ss)
		//optional 3 digit milliseconds of second
		.appendOptional(new DateTimeFormatterBuilder()
		.appendLiteral(".")
		.appendMillisOfSecond(3)
		.toParser())
		//optional time zone offset as (+|-)hh:mm
		.appendOptional(new DateTimeFormatterBuilder()
		.appendTimeZoneOffset("", true, 2, 2)
		.toParser())
		.toFormatter();
	}


	@Override
	protected String getName() {
		return "OWS-8 DNOTAM parser";
	}


	/**
	 * test main
	 * @param args s
	 */
	public static void main(String[] args) {
		//		System.out.println("initialize()");
		//		
		//		Logger logger = Logger.getLogger(OWS8Parser.class.getName());
		//		
		//		System.out.println("logger built");
		//		
		//		IUnitConverter uConv = new SESUnitConverter(logger);
		//		
		//		System.out.println("unit conv built");
		//		
		//		String testFilePath = "./ses-main/src/main\\resources\\wsdl\\test_files\\EVENT_2328231_PENDING.xml";
		//		File testFile = new File(testFilePath);
		//		
		//		System.out.println("file found: " + testFile.exists());
		//		
		//		try {
		//			ConfigurationRegistry.init(new FileInputStream(testFile), logger, null, uConv);
		//		}
		//		catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//		}
		//		
		//		OWS8Parser parser = new OWS8Parser(uConv);
		//		
		//		System.out.println("parser built");
		//		
		//		AIXMBasicMessageDocument message = null;
		//		try {
		//			message = AIXMBasicMessageDocument.Factory.parse(testFile);
		//		}
		//		catch (XmlException e) {
		//			e.printStackTrace();
		//		}
		//		catch (IOException e) {
		//			e.printStackTrace();
		//		}
		//		
		//		if (message != null) {
		//			System.out.println("test input parsed to XML bean");
		//			System.out.println(message.toString());
		//			
		//			MapEvent event = parser.parseAIXMBasicMessage(message);
		//			
		//			System.out.println(event.toString());
		//		}
	}
}
