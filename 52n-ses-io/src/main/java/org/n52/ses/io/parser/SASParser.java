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
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.event.MapEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Parser for notification messages encoded as SAS (RFC, 06-028r5) alerts.
 * 
 * As the alert structure is not known to the parser
 * it is currently build only for specific SAS alerts.
 * The alert data has to be formatted in the following way:
 * <code><AlertData>%TIME,%LAT,%LON,%VALUE</AlertData></code>
 * where %VALUE has to be in a base (SI) unit,
 * %LAT and %LON are in WGS 84 and define a point, 
 * all entries have to be separated by a comma (",").
 * 
 * @author Thomas Everding
 *
 */
public class SASParser extends AbstractParser {
	
	/**
	 * XML namespace of the SAS (RFC; 06-028r5)
	 */
	public static final String SAS_NAMESPACE = "http://www.opengis.net/sas/0.0";
	
	/**
	 * XML namespace of the SAS as used by some implementers (e.g. RSA in GENESIS Project)
	 */
	public static final String SAS_NAMESPACE_090 = "http://www.opengis.net/sas/0.9.0";

	private String value;
	private String geometry;
	private String sensorID;
	private String timestamp;
	private String alertdata;
	
	private static final Logger logger = LoggerFactory
			.getLogger(SASParser.class);
	
	
	
	/**
	 * Parses an SAS alert
	 * 
	 * @param xmlObj the SAS alert as DOM node
	 * 
	 * @return A list of {@link MapEvent}s containing the alert as first entity.
	 */
	public List<MapEvent> parseXML (XmlObject xmlObj) {
		//parse string based
		this.parseAlertDocumentText(xmlObj.toString());
		
		//parse time stamp text
		DateTimeFormatter dtf = this.buildDTFormatter();
		DateTime dt = dtf.parseDateTime(this.timestamp);
		long timeStamp = dt.getMillis();
		
		//parse alert data
		this.parseAlertData(this.alertdata);

		//build result list
		List<MapEvent> result = new ArrayList<MapEvent>();
		
		//build MapEvent
		MapEvent event = new MapEvent(timeStamp, timeStamp);
		
		//set value
		event.put(MapEvent.VALUE_KEY, this.value);
		
		//set geometry
		event.put(MapEvent.GEOMETRY_KEY, this.geometry);
		
		//set sensor ID
		event.put(MapEvent.SENSORID_KEY, this.sensorID);
		
		//set original message
		event.put(MapEvent.ORIGNIAL_MESSAGE_KEY, xmlObj.toString());
		
		//add event to result
		result.add(event);
		
		SASParser.logger.info("SAS alert parsed");
		return result;
	}
	
	/**
	 * extracts the content of a SAS alert
	 * 
	 * @param xmlText the alert text
	 */
	private void parseAlertDocumentText(String xmlText) {
		//split alert
		String[]entries = xmlText.split(">");
		
		/*
		 * content of "entries":
		 * 
		 * 0: <Alert...
		 * 1: <SensorID
		 * 2: urn:ogc...</SensorID
		 * 3: <Timestamp
		 * 4: 2010-04...</Timestamp
		 * 5: <AlertData
		 * 6: 2010-04..., 47....</AlertData
		 * 7: </Alert
		 */
		
		//remove closing tags
		this.sensorID = entries[2].substring(0, entries[2].indexOf("<"));
		this.timestamp = entries[4].substring(0, entries[4].indexOf("<"));
		this.alertdata = entries[6].substring(0, entries[6].indexOf("<"));
	}

	
	/**
	 * parses the alert data
	 * 
	 * @param data an AlertData element content
	 */
	private void parseAlertData(String data) {
		//split data string
		String[] parts = data.split(",");
		
		//store value
		this.value = parts[3];
		
		//store geometry as WKT
		this.geometry = "POINT (" + parts[1] + " " + parts[2] + ")";
		
		//time key is repeated at parts[0] but parsed from the time specific element
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
			//optional 'Z' at the end of the time string
			.appendOptional(new DateTimeFormatterBuilder()
								.appendLiteral("Z")
								.toParser())
			.toFormatter();
	}

	@Override
	public boolean accept(NotificationMessage message) {
		//namespace as defined in RFC Specification
		QName sasQName = new QName(SAS_NAMESPACE, "Alert");
		Element content = message.getMessageContent(sasQName);

		if (content != null) {
			return true;
		}
		
		//alternative namespace
		sasQName = new QName(SAS_NAMESPACE_090, "Alert");
		content = message.getMessageContent(sasQName);

		if (content != null) {
			return true;
		}
		
		return false;
	}

	@Override
	public List<MapEvent> parse(NotificationMessage message) throws Exception {
		//namespace as defined in RFC Specification
		QName sasQName = new QName(SAS_NAMESPACE, "Alert");
		Element content = message.getMessageContent(sasQName);

		if (content != null) {
			XmlObject obj = XMLBeansParser.parse(content, false);
			return this.parseXML(obj);
		}
		
		//alternative namespace
		sasQName = new QName(SASParser.SAS_NAMESPACE_090, "Alert");
		content = message.getMessageContent(sasQName);

		if (content != null) {
			XmlObject obj = XMLBeansParser.parse(content, false);
			return this.parseXML(obj);
		}
		
		return null;
	}

	@Override
	protected String getName() {
		return "SASParser";
	}
}
