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
package org.n52.ses.util.xml;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.opengis.eml.x001.EventAttributeType;
import net.opengis.eml.x001.EventCharacteristicsType;
import net.opengis.eml.x001.EventContentPropertyType;
import net.opengis.eml.x001.EventTimePropertyType;
import net.opengis.eml.x001.EventType;
import net.opengis.eml.x001.EventCharacteristicsType.Attributes;
import net.opengis.eml.x001.EventCharacteristicsType.CausalVector;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.ses.x00.SESEventDocument;
import net.opengis.ses.x00.SESEventType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.parser.XMLHandlingException;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.event.MapEventFactory;
import org.n52.ses.api.ws.INotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for generating a SESEvent document from a MapEvent.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESEventGenerator {
	
	private MapEvent eventMap;
	
	private static final Logger logger = LoggerFactory.getLogger(SESEventGenerator.class);

	/**
	 * Do never access this variable directly!
	 *  
	 * Always use the getter method. The getter
	 * cares for synchronizing and increasing.
	 * 
	 * @see getNextID
	 */
	private static Integer ID_COUNTER = 0;

	/**
	 * Default constructor.
	 * @param event the MapEvent for generating the SESEvent.
	 */
	public SESEventGenerator(MapEvent event) {
		this.eventMap = event;
	}
	
	/**
	 * Get the {@link SESEventDocument}.
	 * 
	 * @return the {@link SESEventDocument}
	 */
	@SuppressWarnings("unchecked")
	public SESEventDocument generateEventDocument() {
		SESEventDocument result = SESEventDocument.Factory.newInstance();
		
		SESEventType sesEvent = result.addNewSESEvent();
		EventType emlEvent = sesEvent.addNewEvent();
		
		/*
		 * If the value is of type MapEvent
		 * then the selected event shall be 
		 * exported.
		 */
		if (this.eventMap.get(MapEvent.VALUE_KEY) instanceof Map<?, ?>) {
			boolean createCausality = false;
			Map<?, ?> innerMap = (Map<?, ?>) this.eventMap.get(MapEvent.VALUE_KEY);
			
			try {
				Object causal = innerMap.get(MapEvent.CAUSALITY_KEY);
				if (causal != null) {
					createCausality = !((List<?>)causal).isEmpty();
				}
			}
			catch (Throwable t) {
				//do nothing, keep createCausality = false
			}
			
			this.eventMap = MapEventFactory.parseFromMap((Map<String, Object>) innerMap, createCausality);
		}
		
		this.generateFromEventType(this.eventMap, emlEvent);
		
//		logger.info("generated ses:Event:\n" + result.xmlText());
//		try {
//			logger.info("DOM node: " + result.getDomNode().getTextContent());
//		}
//		catch (Throwable t){
//			logger.warning("unable to get DOM node from result");
//			logger.warning(t.getMessage());
//			
//			StackTraceElement[] stes = t.getStackTrace();
//			StringBuilder sb = new StringBuilder();
//			for (StackTraceElement ste : stes) {
//				sb.append("\n\t" + ste.toString());
//			}
//			logger.warning(sb.toString());
//			
//			logger.warning("cause: " + t.getCause().toString());
//		}
		return result;
	}
	
	/**
	 * A recursive method to generate the EventType. Recursion is needed because
	 * the causality is managed as a tree.
	 * 
	 * @param recursiveEvent the MapEvent for generating the child.
	 * @param predecEvent the parent EventType.
	 * @return the child EventType.
	 */
	private EventType generateFromEventType(MapEvent recursiveEvent, EventType predecEvent) {
//		//log all keys
//		StringBuilder sb = new StringBuilder();
//		sb.append("available keys in recursive event: ");
//		for (String key : recursiveEvent.keySet()) {
//			sb.append("\n\t" + key);
//		}
//		this.logger.info(sb.toString());
		
		EventContentPropertyType cont = predecEvent.addNewContent();
		
		//if original message is contained, add a Leaf instead of EventCharacteristics
		if (recursiveEvent.keySet().contains(MapEvent.ORIGNIAL_MESSAGE_KEY)) {
			INotificationMessage notify = (INotificationMessage) 
					recursiveEvent.get(MapEvent.ORIGNIAL_MESSAGE_KEY);
			try {
				/*
				 * TODO works with XMLBeansParser?!
				 */
				cont.addNewLeaf().set(XMLBeansParser.parse(notify.xmlToString(), true));
			} catch (XMLHandlingException e) {
				logger.warn(e.getMessage(), e);
				try {
					cont.addNewLeaf().set(XmlObject.Factory.parse(notify.xmlToString()));
				} catch (XmlException e2) {
					logger.warn(e2.getMessage(), e2);
				}
			}
		}
		
		else {
			//add data and do recursion
			EventCharacteristicsType eventChar = cont.addNewEventCharacteristics();
			
			Attributes attr = eventChar.addNewAttributes();
			
			for (String key : recursiveEvent.keySet()) {
				if (key.equals(MapEvent.CAUSALITY_KEY) || key.equals(MapEvent.THIS_KEY) ||
						key.equals(MapEvent.END_KEY) || key.equals(MapEvent.DOUBLE_VALUE_KEY) ||
						key.equals(MapEvent.STRING_VALUE_KEY)) {
					/*
					 * ignored values:
					 * 
					 *  - causality (handled later)
					 *  - this (recursion)
					 *  - end time (handles with start time)
					 *  - string and double value (duplicated information)
					 */
					continue;
				}
				
				//time
				else if (key.equals(MapEvent.START_KEY)) {
					EventTimePropertyType time = eventChar.addNewEventTime();
					
					if (recursiveEvent.get(MapEvent.END_KEY).equals(recursiveEvent.get(key))) {
						//start and end are the same -> TimeInstance
						TimeInstantType tit = time.addNewTimeInstant();
						
						//set gml:id
						tit.setId("TimeInstant_" + SESEventGenerator.getNextID());
						
						tit.addNewTimePosition().setStringValue(
								new DateTime(recursiveEvent.get(key)).toString());
					}
					else {
						//time period
						TimePeriodType period = time.addNewTimePeriod();
						
						//set gml:id
						period.setId("TimePeriod_" + SESEventGenerator.getNextID());
						
						period.addNewBeginPosition().setStringValue(
								new DateTime(recursiveEvent.get(key)).toString());
						period.addNewEndPosition().setStringValue(
								new DateTime(recursiveEvent.get(MapEvent.END_KEY)).toString());
					}
				}
				
				else {
					//other values
					Object value = recursiveEvent.get(key);
					
					EventAttributeType ea = attr.addNewEventAttribute();
					ea.setName(key);
					ea.addNewValue().newCursor().setTextValue(value.toString());					
				}
			}

			//get the cause. should be available cause no Original Message was set.
			Object cause = recursiveEvent.get(MapEvent.CAUSALITY_KEY);
			
			if (cause != null && cause instanceof Vector<?>) {
				Vector<?> causalities = (Vector<?>) cause;
				
				if (causalities.size() > 0) {
					//causal vector is present and not empty
					CausalVector cv = eventChar.addNewCausalVector();
					
					for (Object object : causalities) {
						if (object instanceof MapEvent) {
							
							//recursive
							cv.addNewEvent().set(
									generateFromEventType((MapEvent) object, EventType.Factory.newInstance()));
						}
					}
				}
			}
		}
		
		return predecEvent;
	}

	
	/**
	 * this method is multithread save
	 * 
	 * @return the ID_COUNTER increased by 1
	 */
	public static int getNextID() {
		synchronized (ID_COUNTER) {
			return ++ID_COUNTER;
		}
	}
}
