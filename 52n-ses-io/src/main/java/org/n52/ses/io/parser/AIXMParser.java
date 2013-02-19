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
package org.n52.ses.io.parser;


import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.GMLParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;


/**
 * Parser for notifications encoded in AIXM.
 *
 */
public class AIXMParser extends AbstractParser {
	
	/**
	 * namespace used for parsing dNOTAMs
	 */
	public static final String DNOTAM_NAMESPACE = "http://www.aixm.aero/schema/5.1/dnotam";

	public static final String AIXM_NAMESPACE = "http://www.aixm.aero/schema/5.1";
	public static final String AIXM_MESSAGE_NAMESPACE = "http://www.aixm.aero/schema/5.1/message";
	private static final String GML_NAMESPACE = "http://www.opengis.net/gml/3.2";
//	private static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";

	/*
	 * Event Qnames
	 */
	private static final QName EVENT = new QName(DNOTAM_NAMESPACE, "Event");
	private static final QName EVENT_NAME = new QName(DNOTAM_NAMESPACE, "name");
//	private static final QName EVENT_DESCRIPTION = new QName(EVENT_NAMESPACE, "description");
	private static final QName EVENT_TYPE = new QName(DNOTAM_NAMESPACE, "type");
	private static final QName EVENT_HAS_MEMBER = new QName(DNOTAM_NAMESPACE, "hasMember");

	/*
	 * AIXM Qnames
	 */
	private static final QName AIXM_NAVAID = new QName(AIXM_NAMESPACE, "Navaid");
	private static final QName AIXM_TIME_SLICE = new QName(AIXM_NAMESPACE, "timeSlice");
	private static final QName AIXM_NAVAID_TIME_SLICE = new QName(AIXM_NAMESPACE, "NavaidTimeSlice");
	private static final QName AIXM_AIRPORT_HELIPORT_TIME_SLICE = new QName(AIXM_NAMESPACE, "AirportHeliportTimeSlice");
	private static final QName AIXM_INTERPRETATION = new QName(AIXM_NAMESPACE, "interpretation");
//	private static final QName AIXM_SEQUENCE_NUMBER = new QName(AIXM_NAMESPACE, "sequenceNumber");
//	private static final QName AIXM_CORRECTION_NUMBER = new QName(AIXM_NAMESPACE, "correctionNumber");
//	private static final QName AIXM_TYPE = new QName(AIXM_NAMESPACE, "type");
	private static final QName AIXM_DESIGNATOR = new QName(AIXM_NAMESPACE, "designator");
//	private static final QName AIXM_OPERATIONAL_STATUS = new QName(AIXM_NAMESPACE, "operationalStatus");
	private static final QName AIXM_AIRPORT_HELIPORT = new QName(AIXM_NAMESPACE, "AirportHeliport");
//	private static final QName AIXM_AFFECTED_RUNWAY_DIRECTION = new QName(AIXM_NAMESPACE, "affectedRunwayDirection");
//	private static final QName AIXM_LIMITATION = new QName(AIXM_NAMESPACE, "limitation");
//	private static final QName AIXM_AIRPORT_HELIPORT_USAGE_LIMITATION = new QName(AIXM_NAMESPACE, "AirportHeliportUsageLimitation");
//	private static final QName AIXM_CONDITION = new QName(AIXM_NAMESPACE, "condition");
//	private static final QName AIXM_AIRPORT_HELIPORT_USAGE_CONDITION = new QName(AIXM_NAMESPACE, "AirportHeliportUsageCondition");
//	private static final QName AIXM_FLIGHT = new QName(AIXM_NAMESPACE, "flight");
//	private static final QName AIXM_FLIGHT_CHARACTERISTICS = new QName(AIXM_NAMESPACE, "FlightCharacteristic");
//	private static final QName AIXM_RULE = new QName(AIXM_NAMESPACE, "rule");
//	private static final QName AIXM_OPERATION = new QName(AIXM_NAMESPACE, "operation");
//	private static final QName AIXM_AIRPORT_HELIPORT_OPERATION = new QName(AIXM_NAMESPACE, "AirportHeliportOperation");

	/*
	 * XPath expressions
	 */
	private static final String SELECT_OP_STATUS_XPATH = "declare namespace aixm='" +
	AIXM_NAMESPACE + "'; .//aixm:availability//aixm:operationalStatus";
	
	/*
	 * GML Qnames
	 */
	private static final QName GML_BOUNDED_BY = new QName(GML_NAMESPACE, "boundedBy");
//	private static final QName GML_ENVELOPE = new QName(GML_NAMESPACE, "Envelope");
//	private static final QName GML_LOWER_CORNER = new QName(GML_NAMESPACE, "lowerCorner");
//	private static final QName GML_UPPER_CORNER = new QName(GML_NAMESPACE, "upperCorner");
	private static final QName GML_IDENTIFIER = new QName(GML_NAMESPACE, "identifier");
	private static final QName GML_VALID_TIME = new QName(GML_NAMESPACE, "validTime");
	private static final QName GML_TIME_PERIOD = new QName(GML_NAMESPACE, "TimePeriod");
	private static final QName GML_TIME_INSTANT = new QName(GML_NAMESPACE, "TimeInstant");
	private static final QName GML_TIME_POSITION = new QName(GML_NAMESPACE, "timePosition");
	private static final QName GML_BEGIN_POSITION = new QName(GML_NAMESPACE, "beginPosition");
	private static final QName GML_END_POSITION = new QName(GML_NAMESPACE, "endPosition");

	/**
	 * globally used key for the aeronautical feature of a MapEvent
	 */
	public static final String AERO_FEATURE_KEY = "aeronauticalFeature";

	private static final String DNOTAM_TYPE_KEY = "aixmType";
//	private static final QName GML_POS = new QName(GML_NAMESPACE, "pos");
	

//	/*
//	 * XLink Qnames
//	 */
//	private static final QName XLINK_HREF = new QName(XLINK_NAMESPACE, "href");
//	private static final QName XLINK_TITLE = new QName(XLINK_NAMESPACE, "title");


	private static final Logger logger = LoggerFactory
			.getLogger(AIXMParser.class);

	@Override
	public boolean accept(NotificationMessage message) {
		Element content = message.getMessageContent(EVENT);

		if (content != null) {
			return true;
		}
		
		return false;
	}

	@Override
	public List<MapEvent> parse(NotificationMessage message) throws Exception {
		Element content = message.getMessageContent(EVENT);

		if (content != null) {
			XmlObject obj = XMLBeansParser.parse(content, false);
			return parseAIXM(obj);
		}
		
		return null;
	}

	/**
	 * Parses the AIXM documents and creates events from it.
	 * 
	 * @param doc the document to parse
	 * @return a List of MapEvent containing the resulting events.
	 */
	public List<MapEvent> parseAIXM(XmlObject doc) {
		List<MapEvent> resultingEvents = new ArrayList<MapEvent>();

		/*
		 * get all Events
		 */
		XmlObject[] events = doc.selectChildren(EVENT);
		if (events != null && events.length > 0) {
			for (XmlObject event : events) {

				String eventName = XmlUtil.stripText(event.selectChildren(EVENT_NAME));
//				String eventDesc = XmlUtil.stripText(event.selectChildren(EVENT_DESCRIPTION));
				String eventType = XmlUtil.stripText(event.selectChildren(EVENT_TYPE));

				XmlObject[] hasMember = event.selectChildren(EVENT_HAS_MEMBER);
				/*
				 * get the hasMember
				 */
				if (hasMember != null && hasMember.length > 0){
					try {
						resultingEvents = parseHasMember(hasMember);
					} catch (ParseException e) {
						logger.warn(e.getMessage(), e);
					}
				}

				/*
				 * add MapEvent here
				 */
				if (resultingEvents != null) {
					for (MapEvent newEvent : resultingEvents) {
						newEvent.put("event_name", eventName);
						newEvent.put("event_type", eventType);
					}
				}
			}

		}
		return resultingEvents;

	}

	/**
	 * Parses the hasMember section of the AIXM document
	 */
	private List<MapEvent> parseHasMember(XmlObject[] hasMember) throws ParseException {
		for (XmlObject members : hasMember) {

			/*
			 * get the Navaid
			 */
			XmlObject[] navaIds = members.selectChildren(AIXM_NAVAID);

			if (navaIds != null && navaIds.length > 0) {
				/*
				 * only one Navaid allowed
				 */
				XmlObject navaId = navaIds[0];
				return parseNavaid(navaId);

			}
			//else: no Navaid -> AirportHeliport?
			XmlObject[] airportHeliports = members.selectChildren(AIXM_AIRPORT_HELIPORT);

			if (airportHeliports != null && airportHeliports.length > 0) {
				/*
				 * only one AirportHeliport allowed
				 */
				return parseAirportHeliports(airportHeliports[0]);
			}
		}
		return null;
	}

	/**
	 * Parsed the AirportHeliportsUsage element.
	 * @throws ParseException 
	 */
	private List<MapEvent> parseAirportHeliports(XmlObject airportUsage) throws ParseException {
		List<MapEvent> newEvents = new ArrayList<MapEvent>();

		/*
		 * get the gml
		 */
		XmlObject[] boundedBys = airportUsage.selectChildren(GML_BOUNDED_BY);
		Geometry geom = null;
		if (boundedBys != null && boundedBys.length > 0) {
			try {
				geom = GML32Parser.parseGeometry(boundedBys[0], true);
			} catch (GMLParseException e) {
				throw new ParseException(e);
			}
		}

		XmlObject[] gmlident = airportUsage.selectChildren(GML_IDENTIFIER);
		String identifier = null;
		String identifierCodeSpace = null;
		if (gmlident != null && gmlident.length > 0) {
			identifier = XmlUtil.stripText(gmlident[0]);
			identifierCodeSpace = XmlUtil.stripText(gmlident[0].selectAttribute(new QName("", "codeSpace")));
		}
		

		/*
		 * get the timeSlice
		 */
		XmlObject[] timeSlices = airportUsage.selectChildren(AIXM_TIME_SLICE);

		if (timeSlices != null && timeSlices.length > 0) {
			MapEvent newEvent = null;
			for (XmlObject slice : timeSlices) {
				XmlObject heliportTimeSlice = slice.selectChildren(AIXM_AIRPORT_HELIPORT_TIME_SLICE)[0];

				/*
				 * parse the validTime
				 */
				XmlObject[] validTime = heliportTimeSlice.selectChildren(GML_VALID_TIME);
				newEvent = parseTime(validTime);

				/*
				 * parse other elements
				 */
				String interpretation = XmlUtil.stripText(heliportTimeSlice.selectChildren(AIXM_INTERPRETATION));
//				String sequenceNumber = XmlUtil.stripText(heliportTimeSlice.selectChildren(AIXM_SEQUENCE_NUMBER));
//				String correctionNumber = XmlUtil.stripText(heliportTimeSlice.selectChildren(AIXM_CORRECTION_NUMBER));
				
				String designator = XmlUtil.stripText(heliportTimeSlice.selectChildren(AIXM_DESIGNATOR));

				/*
				 * parse operational status
				 */
				XmlObject[] opStats = XmlUtil.selectPath(SELECT_OP_STATUS_XPATH, heliportTimeSlice);
				String opStatus = null;
				if (opStats.length > 0) {
					opStatus = XmlUtil.stripText(opStats);
				}
				
				/*
				 * (Metadata parsing left out)
				 */

				/*
				 * do not parse the featureLifetime. time already set
				 */

//				/*
//				 * parse the limitation
//				 */
//				XmlObject[] limitations = heliportTimeSlice.selectChildren(AIXM_LIMITATION);
//				if (limitations != null && limitations.length > 0) {
//					parseLimitations(limitations, newEvent);
//				}

				/*
				 * parse the runways
				 */
//				XmlObject[] runwayObjects = heliportTimeSlice.selectChildren(AIXM_AFFECTED_RUNWAY_DIRECTION);

				
				if (newEvent != null) {
					newEvent.put(MapEvent.GEOMETRY_KEY, geom);

					//only PERMDELTA or TEMPDELTA. if not, do not add it
					if (interpretation.equals("TEMPDELTA") || interpretation.equals("PERMDELTA")) {
						newEvent.put("aixm_interpretation", interpretation);
						newEvent.put("identifierCodeSpace", identifierCodeSpace);
						newEvent.put("identifier", identifier);
						newEvent.put("operationalStatus", opStatus);
						
						newEvent.put(DNOTAM_TYPE_KEY, "dnotam:Event");

						if (designator != null) {
							newEvent.put(AERO_FEATURE_KEY, designator);
						}
						else {
							newEvent.put(AERO_FEATURE_KEY, identifier);
						}
						
						newEvents.add(newEvent);
					}
				}
			}
		}
		
		return newEvents;
	}

//	/**
//	 * Parses the aixm:limitation element.
//	 * @param newEvent 
//	 */
//	private void parseLimitations(XmlObject[] limitations, MapEvent newEvent) {
//		for (XmlObject limit : limitations) {
//			/*
//			 * parse AirportHeliportUsageLimitation (only one allowed)
//			 */
//			XmlObject ahul = limit.selectChildren(AIXM_AIRPORT_HELIPORT_USAGE_LIMITATION)[0];
//			String type = XmlUtil.stripText(ahul.selectChildren(AIXM_TYPE));

//			/*
//			 * parse conditions
//			 */
//			XmlObject[] conditions = ahul.selectChildren(AIXM_CONDITION);
//			if (conditions != null && conditions.length > 0) {
//				for (XmlObject cond : conditions) {
//					/*
//					 * only one allowed
//					 */
////					XmlObject ahuc = cond.selectChildren(AIXM_AIRPORT_HELIPORT_USAGE_CONDITION)[0];
//
////					String flightRule = XmlUtil.stripText(ahuc.selectChildren(AIXM_FLIGHT)[0].
////							selectChildren(AIXM_FLIGHT_CHARACTERISTICS)[0].
////							selectChildren(AIXM_RULE)[0]);
//
////					String operationType = XmlUtil.stripText(ahuc.selectChildren(AIXM_OPERATION)[0].
////							selectChildren(AIXM_AIRPORT_HELIPORT_OPERATION)[0].
////							selectChildren(AIXM_TYPE)[0]);
//
//				}
//			}

//		}
//	}

	/**
	 * Parses the Navaid element.
	 * @param newEvent 
	 */
	private List<MapEvent> parseNavaid(XmlObject navaId) throws ParseException {
		List<MapEvent> newEvents = new ArrayList<MapEvent>();

		/*
		 * get the gml
		 */
		XmlObject[] boundedBys = navaId.selectChildren(GML_BOUNDED_BY);
		Geometry geom = null;
		if (boundedBys != null && boundedBys.length > 0) {
			try {
				geom = GML32Parser.parseGeometry(boundedBys[0]);
			} catch (GMLParseException e) {
				throw new ParseException(e);
			}
		}

		XmlObject[] gmlident = navaId.selectChildren(GML_IDENTIFIER);
		String identifier = null;
		String identifierCodeSpace = null;
		if (gmlident != null && gmlident.length > 0) {
			identifier = XmlUtil.stripText(gmlident[0]);
			identifierCodeSpace = XmlUtil.stripText(gmlident[0].selectAttribute(new QName("", "codeSpace")));
		}

		/*
		 * get the timeSlice
		 */
		XmlObject[] timeSlices = navaId.selectChildren(AIXM_TIME_SLICE);

		if (timeSlices != null && timeSlices.length > 0) {
			MapEvent newEvent = null;
			for (XmlObject slice : timeSlices) {
				XmlObject navaidTimeSlice = slice.selectChildren(AIXM_NAVAID_TIME_SLICE)[0];

				/*
				 * parse the validTime
				 */
				XmlObject[] validTime = navaidTimeSlice.selectChildren(GML_VALID_TIME);
				newEvent = parseTime(validTime);

				/*
				 * parse other elements
				 */
//				String interpretation = XmlUtil.stripText(navaidTimeSlice.selectChildren(AIXM_INTERPRETATION));
//				String sequenceNumber = XmlUtil.stripText(navaidTimeSlice.selectChildren(AIXM_SEQUENCE_NUMBER));
//				String correctionNumber = XmlUtil.stripText(navaidTimeSlice.selectChildren(AIXM_CORRECTION_NUMBER));
//				String type = XmlUtil.stripText(navaidTimeSlice.selectChildren(AIXM_TYPE));
				String designator = XmlUtil.stripText(navaidTimeSlice.selectChildren(AIXM_DESIGNATOR));
//				String operationalStatus = XmlUtil.stripText(navaidTimeSlice.selectChildren(AIXM_OPERATIONAL_STATUS));
				
				if (newEvent != null) {
					newEvent.put(MapEvent.GEOMETRY_KEY, geom);
					newEvent.put("identifierCodeSpace", identifierCodeSpace);
					newEvent.put("identifier", identifier);
					
					if (designator != null) {
						newEvent.put(AERO_FEATURE_KEY, designator);
					}
					else {
						newEvent.put(AERO_FEATURE_KEY, identifier);
					}
					
					newEvents.add(newEvent);
				}
			}

		}

		return newEvents;
	}

	/**
	 * parses the time of a gml:validTime
	 */
	private MapEvent parseTime(XmlObject[] time) {
		if (time != null && time.length > 0) {
			XmlObject[] period = time[0].selectChildren(GML_TIME_PERIOD);
			if (period != null && period.length > 0) {

				String beginPos = "";
				XmlObject hasIndeterminate = period[0].selectChildren(GML_BEGIN_POSITION)[0].selectAttribute(new QName("", "indeterminatePosition"));
				if (hasIndeterminate == null) {
					beginPos = XmlUtil.stripText(period[0].selectChildren(GML_BEGIN_POSITION));
				}
				else {
					beginPos = XmlUtil.stripText(hasIndeterminate);
				}

				String endPos = "";
				hasIndeterminate = period[0].selectChildren(GML_END_POSITION)[0].selectAttribute(new QName("", "indeterminatePosition"));
				if (hasIndeterminate == null) {
					endPos = XmlUtil.stripText(period[0].selectChildren(GML_END_POSITION));
				}
				else {
					endPos = XmlUtil.stripText(hasIndeterminate);
				}

				/*
				 * construct time in millis. for "unknown" set the MAX_VALUE
				 */
				long beginPosMs, endPosMs = 0L;
				if (beginPos.equals("unknown")) {
					beginPosMs = Long.MAX_VALUE;
				} else {
					beginPosMs = new DateTime(beginPos).getMillis();
				}

				if (endPos.equals("unknown")) {
					endPosMs = Long.MAX_VALUE;
				} else {
					endPosMs = new DateTime(endPos).getMillis();
				}
				return new MapEvent(beginPosMs, endPosMs);

			}
			XmlObject[] instant = time[0].selectChildren(GML_TIME_INSTANT);
			if (instant != null && instant.length > 0) {
				String timepos = XmlUtil.stripText(instant[0].selectChildren(GML_TIME_POSITION));

				long beginPosMs = new DateTime(timepos).getMillis();
				return new MapEvent(beginPosMs, beginPosMs);
			}
		}
		return null;
	}


	@Override
	protected String getName() {
		return "AIXMParser [dNOTAM Event]";
	}


//	/**
//	 * Constructs a Geometry from the given gml:boundedBy
//	 */
//	private Geometry parseBoundedBy(XmlObject[] boundedBys) throws ParseException {
//		if (boundedBys != null && boundedBys.length > 0) {
//			/*
//			 * get the first - only one allowed
//			 */
//			XmlObject boundedBy = boundedBys[0];
//
//			XmlObject[] env = boundedBy.selectChildren(GML_ENVELOPE);
//			if (env != null && env.length > 0) {
//				XmlObject[] lower = env[0].selectChildren(GML_LOWER_CORNER);
//				XmlObject[] upper = env[0].selectChildren(GML_UPPER_CORNER);
//
//				/*
//				 * get text of lowerCorner and split at " "
//				 */
//				String cString = XmlUtil.stripText(lower);
//				String[] lc = cString.split(" ");
//
//				int i = 0;
//				for (String string : lc) {
//					string = string.trim();
//					lc[i] = string;
//					i++;
//				}
//
//				/*
//				 * get text of upperCorner and split at " "
//				 */
//				cString = XmlUtil.stripText(upper);
//				String[] uc = cString.split(" ");
//
//				i = 0;
//				for (String string : uc) {
//					string = string.trim();
//					uc[i] = string;
//					i++;
//				}
//
//				/*
//				 * construct Geometry using JTS
//				 */
//				String wktString = "POLYGON(("+ lc[0] +" "+ lc[1] +", "+ lc[0] +" "+ uc[1] +", "+ uc[0] +" "+ uc[1] +
//				", "+ uc[0] +" "+ lc[1] +", "+ lc[0] +" "+ lc[1]+ "))";
//
//				WKTReader wktReader = new WKTReader();
//				return wktReader.read(wktString);
//			}
//			
//			XmlObject[] pos = boundedBy.selectChildren(GML_POS);
//			
//			if (pos != null && pos.length == 2) {
//				/*
//				 * get text of first pos and split at " "
//				 */
//				String cString = XmlUtil.stripText(pos[0]);
//				String[] lc = cString.split(" ");
//
//				int i = 0;
//				for (String string : lc) {
//					string = string.trim();
//					lc[i] = string;
//					i++;
//				}
//
//				/*
//				 * get text of second pos and split at " "
//				 */
//				cString = XmlUtil.stripText(pos[1]);
//				String[] uc = cString.split(" ");
//
//				i = 0;
//				for (String string : uc) {
//					string = string.trim();
//					uc[i] = string;
//					i++;
//				}
//
//				/*
//				 * construct Geometry using JTS
//				 */
//				String wktString = "POLYGON(("+ lc[0] +" "+ lc[1] +", "+ lc[0] +" "+ uc[1] +", "+ uc[0] +" "+ uc[1] +
//				", "+ uc[0] +" "+ lc[1] +", "+ lc[0] +" "+ lc[1]+ "))";
//
//				WKTReader wktReader = new WKTReader();
//				return wktReader.read(wktString);
//			}
//		}
//		return null;
//	}





}
