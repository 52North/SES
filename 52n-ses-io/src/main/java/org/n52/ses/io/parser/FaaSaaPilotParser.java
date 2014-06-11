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
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.AbstractTimePrimitiveType;
import net.opengis.gml.x32.BoundingShapeType;
import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;
import net.opengis.gml.x32.TimePrimitivePropertyType;

import org.apache.muse.ws.notification.NotificationMessage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.GMLParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import aero.aixm.schema.x51.AbstractAIXMFeatureType;
import aero.aixm.schema.x51.AirspaceActivationPropertyType;
import aero.aixm.schema.x51.AirspaceActivationType;
import aero.aixm.schema.x51.AirspaceLayerType;
import aero.aixm.schema.x51.AirspaceTimeSliceType;
import aero.aixm.schema.x51.AirspaceType;
import aero.aixm.schema.x51.CodeAirspaceDesignatorType;
import aero.aixm.schema.x51.event.EventType;
import aero.aixm.schema.x51.message.AIXMBasicMessageDocument;
import aero.aixm.schema.x51.message.BasicMessageMemberAIXMPropertyType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;


/**
 * A parser for AIXM updates as provided in the 
 * OGC FAA SAA Dissemination Pilot
 * 
 * @author Thomas Everding
 *
 */
public class FaaSaaPilotParser extends AbstractParser {
	
	private static final Logger logger = LoggerFactory
			.getLogger(FaaSaaPilotParser.class);
	
	//AIXM namespace
	private static final String AIXM_MESSAGE_NAMESPACE = "http://www.aixm.aero/schema/5.1/message";
	
	//AIXM Basic Message QName
	private static final QName MESSAGE_ROOT_QNAME = new QName(AIXM_MESSAGE_NAMESPACE, "AIXMBasicMessage");
	
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
	private MapEvent parseAIXMBasicMessage(AIXMBasicMessageDocument aixmMessage) {
		MapEvent mapEvent = new MapEvent(0, 0);
		
		//get hasMember elements
		BasicMessageMemberAIXMPropertyType[] members = aixmMessage.getAIXMBasicMessage().getHasMemberArray();
		
		for (BasicMessageMemberAIXMPropertyType member : members) {
			this.parseMember(member, mapEvent);
		}
		
		return mapEvent;
	}


	/**
	 * Parses a member of the AIXM Basic Message
	 * 
	 * @param member the member
	 * @param mapEvent the event to populate
	 */
	private void parseMember(BasicMessageMemberAIXMPropertyType member, MapEvent mapEvent) {
		AbstractAIXMFeatureType abtractAIXMFeature = member.getAbstractAIXMFeature();
		
		if (abtractAIXMFeature instanceof EventType) {
			//parse the Event part
			EventType event = (EventType) abtractAIXMFeature;
			this.parseEvent(event, mapEvent);
		}
		else if (abtractAIXMFeature instanceof AirspaceType) {
			//parse the Airspace part
			AirspaceType airspace = (AirspaceType) abtractAIXMFeature;
			this.parseAirspace(airspace, mapEvent);
		}
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
	}


	/**
	 * parses an airspace time slice
	 * 
	 * @param airspaceTimeSlice the time slice node of an airspace
	 * @param mapEvent the map event to populate
	 */
	private void parseTimeSlice(AirspaceTimeSliceType airspaceTimeSlice, MapEvent mapEvent) {
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
		
	}



	/**
	 * Parses the designator
	 * 
	 * @param designator the designator of an airspacetiemslice
	 * @param mapEvent the map event to populate
	 */
	private void parseDesignator(CodeAirspaceDesignatorType designator,
			MapEvent mapEvent) {
		// parse the designator value
		String designatorValue = designator.getStringValue();
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
		String timeString = timePositionType.getStringValue();
		
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
		return "FAA SAA Pilot parser";
	}
	
	
	/**
	 * test main
	 * @param args s
	 */
	public static void main(String[] args) {
//		System.out.println("initialize()");
//		
//		Logger logger = Logger.getLogger(FaaSaaPilotParser.class.getName());
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
//		FaaSaaPilotParser parser = new FaaSaaPilotParser(uConv);
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
