/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
import java.util.Map;

import net.opengis.gml.PointType;
import net.opengis.gml.TimeInstantDocument;
import net.opengis.gml.TimePeriodDocument;
import net.opengis.gml.TimePeriodType;
import net.opengis.gml.TimePositionType;
import net.opengis.sampling.x10.SamplingPointDocument;
import net.opengis.swe.x101.TimeDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.n52.oxf.conversion.unit.NumberWithUOM;
import org.n52.ses.api.IUnitConverter;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.GMLParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;


/**
 * 
 * @author Thomas Everding
 *
 */
public class ObjectPropertyValueParser {

	private static final Logger logger = LoggerFactory
			.getLogger(ObjectPropertyValueParser.class);
	
	private XmlObject xmlObj;
	
	//reserved keyword values
	private String value = null;
	private String geometry = null; 
	private long startTime = Long.MIN_VALUE;
	private long endTime = Long.MIN_VALUE;
	/*
	 * TODO private Vector<Object> causality = null;
	 */
	
	private String omResult = null;
	
	private DateTimeFormatter fmt = this.buildDTFormatter();
	
	private IUnitConverter unitConverter;

	
	/**
	 * 
	 * Constructor
	 *
	 * @param xmlObj the XML Object to parse
	 * @param logger logger to be used
	 */
	public ObjectPropertyValueParser(XmlObject xmlObj) {
		this.xmlObj = xmlObj;
	}
	
	
	/**
	 * Parses the XML object that was given via the constructor.
	 * 
	 * @param unitCon unit converter to be used.
	 * 
	 * @return a {@link List} of map events (just one representing the complete content)
	 */
	public List<MapEvent> parseXML (IUnitConverter unitCon) {
		this.unitConverter = unitCon;
		
		List<MapEvent> result = new ArrayList<MapEvent>();
		
		//parse XML
		Map<String, Object> parsed = this.parseRecursive(this.xmlObj.getDomNode());
		
		//check start and end time
		if (this.startTime == Long.MIN_VALUE) {
			//nothing found in XML input, set 'now'
			DateTime dt = new DateTime();
			this.startTime = dt.getMillis();
			this.endTime = this.startTime;
		}

		//create new map event
		MapEvent event = new MapEvent(this.startTime, this.endTime);
		
		//add all entries from 'parsed' into the map event
		event.putAll(parsed);
		
		//add all reserved keywords into the map event
		
		//add value
		//check if "value" is null
		if (this.value != null) {
			//add and check for doubleValue and stringValue
			event.put(MapEvent.VALUE_KEY, this.value);
			event.put(MapEvent.STRING_VALUE_KEY, this.value);
		}
		else {
			//use the result if available
			if (this.omResult != null) {
				event.put(MapEvent.VALUE_KEY, this.omResult);
				event.put(MapEvent.STRING_VALUE_KEY, this.omResult);
			}
		}
		
		//TODO: add causality
		
		//add geometry
		if (this.geometry != null) {
			event.put(MapEvent.GEOMETRY_KEY, this.geometry);
		}
		
		//add original message
		event.put(MapEvent.ORIGNIAL_MESSAGE_KEY, this.xmlObj.toString());
		
		//add the map event to the result list
		result.add(event);
		return result;
	}
	
	
	/**
	 * Parses a single XML object recursively.
	 * 
	 * @param node the DOM node to parse
	 * 
	 * @return a {@link Map} that contains the nodes content
	 */
	private Map<String, Object> parseRecursive(Node node) {
		Map<String, Object> result = new NoCollisionMap();
		
		//get all children
		NodeList children = node.getChildNodes();
		Node child;
		String localName;
		
		//parse all attributes
		NamedNodeMap attributes = node.getAttributes();
		result.putAll(this.parseAttributes(attributes));
		
		//parse all other children
		for (int i = 0; i < children.getLength(); i++) {
			child = children.item(i);
			
//			if (child instanceof Attr) {
//				//parse as attribute
//				attr = (Attr) child;
//				result.put(attr.getLocalName(), attr.getValue());
//				
//				//TODO: add interruption for UoM attributes
//					//TODO: first value with UoM is set on value key
//			}
//			else if (child instanceof Text) {
//				//parse as text
//				text = (Text) child;
//				result.put(text., text.getNodeValue());
//			}
//			else 
			if (child instanceof Element) {
				localName = child.getLocalName();
				
				if (child.getChildNodes().getLength() == 1 && child.getFirstChild() instanceof Text) {
					Map<String, Object> content = new NoCollisionMap();
					
					if (child.hasAttributes()) {
						content.putAll(this.parseAttributes(child.getAttributes()));
					}
					
					Node uomAttribute = child.getAttributes().getNamedItem("uom");
					String uom = null;
					if (uomAttribute != null) {
						//UoM found, save UoM and convert text content
						uom = uomAttribute.getNodeValue();
					}
					
					//parse the text content
					String propertyValue = child.getFirstChild().getNodeValue();
					if (uom == null) {
						//no UoM available
						content.put(MapEvent.CONTENT_KEY, propertyValue);
					}
					else {
						//UoM found, convert before storing
						try {
							//convert
							double val = Double.parseDouble(propertyValue);
							NumberWithUOM conv = this.unitConverter.convert(uom, val);
							
							//store
							propertyValue = Double.toString(conv.getValue());
							content.put(MapEvent.CONTENT_KEY, propertyValue);
							
							//enter correct UoM
							content.remove("uom");
							content.put("uom", conv.getUom());
							content.put("original-uom", uom);
						}
						catch (Throwable t) {
							//conversion error use original value
							content.put(MapEvent.CONTENT_KEY, propertyValue);
						}
						//store first found value with UoM as this.value
						if (this.value == null) {
							this.value = propertyValue;
						}
					}
					
					result.put(localName, content);
				}
				else {
					//parse time for shortcut access
					if (localName.contains("time") || localName.contains("Time")) {
						//this is most likely a time definition
						this.parseTime(child);
					}
					
					/*
					 * parse geometry shortcut
					 * 
					 * supported:
					 * 		om:featureOfInterest
					 */
					if (child.getLocalName().equals("featureOfInterest") && child.getNamespaceURI().equals("http://www.opengis.net/om/1.0")) {
						//parse geometry from feature of intereset
						this.parseFeatureOfinterest(child);
					}
					//TODO: add special method for causality -> how should this look like? (string content or parsed content?)
	
					//store result as class field, will be used as value if nothing else found
					if (child.getLocalName().equals("result") && child.getNamespaceURI().equals("http://www.opengis.net/om/1.0")) {
						this.omResult = child.toString();
					}
					
					//parse elements recursively
					Map<String, Object> parsedElement = this.parseRecursive(child);
					result.put(child.getLocalName(), parsedElement);
				}
			}
		}
		
		//return the parsing result
		return result;
	}


	/**
	 * parses attributes
	 * 
	 */
	private Map<String, Object> parseAttributes(NamedNodeMap attributes) {
		Node child;
		Map<String, Object> result = new NoCollisionMap();
		
		if (attributes != null) {
			for (int i = 0; i < attributes.getLength(); i++) {
				child = attributes.item(i);
				
				if (child instanceof Attr) {
					result.put(child.getLocalName(), child.getNodeValue());
				}
			}
		}
		
		return result;
	}


	/**
	 * parses the geometry shortcut from a feature of interest
	 * 
	 * @param foiNode the DOM node of the FOI
	 */
	private void parseFeatureOfinterest(Node foiNode) {
		Node child = foiNode.getFirstChild();
		
		//get first child that is not an element
		while ((child = child.getNextSibling()) != null) {
			if (child instanceof Element) {
				//found one element, just take this
				break;
			}
		}
		
		if (child == null) {
			//nothing useful found
			return;
		}
		
		/*
		 * parse the child element
		 * 
		 * supported:
		 * 		sa:SamplingPoint
		 */
		Geometry geom = null;
		
		if (child.getLocalName().equals("SamplingPoint") && child.getNamespaceURI().equals("http://www.opengis.net/sampling/1.0")) {
			//parse sampling point
			try {
				SamplingPointDocument spd = SamplingPointDocument.Factory.parse(child);
				PointType pt = spd.getSamplingPoint().getPosition().getPoint();
				geom = GML31Parser.parseGeometry(pt);
			}
			catch (XmlException e) {
				logger.warn(e.getMessage(), e);
			}
			catch (ParseException e) {
				logger.warn(e.getMessage(), e);
			}
			catch (GMLParseException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		//parse other geometry types here and assign 'geom'
		
		if (geom == null) {
			//no geometry found
			return;
		}
		
		this.geometry = geom.toText();
	}


	private void parseTime(Node child) {
		//only the first occurrence is used for the shortcut
		if (this.startTime != Long.MIN_VALUE) {
			//shortcut already set
			return;
		}
		
		String namespace = child.getNamespaceURI();
		String localName = child.getLocalName();
		
		if (namespace.equals("http://www.opengis.net/gml")) {
			/*
			 * parse supported GML 3.2 types
			 * 
			 * supported:
			 * 		gml:TimeInstant
			 * 		gml:TimePeriod
			 */
			if (localName.equals("TimeInstant")) {
				//parse gml:TimeInstant
				try {
					this.startTime = this.parseGMLTimeInstant(child);
					this.endTime = this.startTime;
				}
				catch (XmlException e) {
					logger.warn(e.getMessage(), e);
				}
			}
			else if (localName.equals("TimePeriod")) {
				//parse gml:TimePeriod
				try {
					TimePeriodDocument tpd = TimePeriodDocument.Factory.parse(child);
					TimePeriodType tp = tpd.getTimePeriod();
					
					if (tp.isSetBegin()) {
						//parse begin
						this.startTime = this.parseGMLTimeInstant(tp.getBegin().getDomNode());
					}
					else {
						//parse beginPosition
						this.startTime = this.parseGMLTimePosition(tp.getBeginPosition()).getMillis();
					}
					
					if (tp.isSetEnd()) {
						//parse begin
						this.endTime = this.parseGMLTimeInstant(tp.getEnd().getDomNode());
					}
					else {
						//parse beginPosition
						this.endTime = this.parseGMLTimePosition(tp.getEndPosition()).getMillis();
					}
				}
				catch (XmlException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
		else if (namespace.equals("http://www.opengis.net/swe/1.0.1")) {
			/*
			 * parse supported SWE Common types
			 * 
			 * implemented: 
			 * 		swe:Time
			 * 
			 * TODO:
			 * 		swe:TimeRange
			 * 
			 */
			
			if (localName.equals("Time")) {
				//parse swe:Time
				try {
					//set time as start and end time
					this.startTime = this.parseSWETime(child).getMillis();
					this.endTime = this.startTime;
				}
				catch (XmlException e) {
					logger.warn(e.getMessage(), e);
				}
			}
			else if (localName.equals("TimeRange")) {
				//TODO: parse swe:TimeRange
//				try {
//					TimeRangeDocument timeRange = TimeRangeDocument.Factory.parse(child);
//					List<Object> times = timeRange.getTimeRange().getValue();
//					
//					//TODO: extract time nodes from list and parse (private method available)
//				}
//				catch (XmlException e) {
//					e.printStackTrace();
//				}
			}
		}
		
		//if O&M sampling time led here just ignore it, the internal element will be parsed
	}


	/**
	 * parses a gml:TimeInstant
	 * 
	 * @param child the time instant as DOM node
	 * 
	 * @return the time as millisecond-string
	 * 
	 * @throws XmlException
	 */
	private long parseGMLTimeInstant(Node child) throws XmlException {
		TimeInstantDocument tid = TimeInstantDocument.Factory.parse(child);
		return this.parseGMLTimePosition(tid.getTimeInstant().getTimePosition()).getMillis();
	}
	
	
	/**
	 * parses gml:timePosition elements
	 * 
	 * @param pos the gml:timePosition element
	 * 
	 * @return the time as DateTime object
	 */
	private DateTime parseGMLTimePosition(TimePositionType pos) {
		String posString = pos.getStringValue();
		return this.fmt.parseDateTime(posString);
	}


	/**
	 * parses swe:Time elements
	 * 
	 * @param node the DOM node
	 * @return a DtaeTime object
	 * @throws XmlException if an parsing error occurs
	 */
	private DateTime parseSWETime(Node node) throws XmlException {
		//create document
		TimeDocument time = TimeDocument.Factory.parse(node);
		String timeValue = time.getTime().getValue().toString();
		
		//parse ISO string
		return this.fmt.parseDateTime(timeValue);
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
}
