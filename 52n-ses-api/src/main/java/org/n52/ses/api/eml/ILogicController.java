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
package org.n52.ses.api.eml;


import java.util.Map;

import org.n52.ses.api.IUnitConverter;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.ws.ISubscriptionManager;



/**
 * interface for EML logic controllers (e.g. esper controller)
 *
 */
public interface ILogicController {

	
	/**
	 * Initializes the controller
	 * 
	 * @param eml the EML to execute
	 * @param unitConverter the unit converter
	 * @throws Exception 
	 */
	void initialize(IEML eml, IUnitConverter unitConverter) throws Exception;

	/**
	 * send a new event to the engine
	 * 
	 * @param inputName the name of the event type
	 * @param event the new event
	 */
	void sendEvent(String inputName, MapEvent event);

	/**
	 * registers a new event type
	 * 
	 * @param eventName name of the new event type
	 * @param eventProperties map containing the names and the types of the event properties
	 */
	void registerEvent(String eventName, Map<String, Object> eventProperties);

	/**
	 * get a map containing all data types of an event
	 * 
	 * @param eventName name of the event (only the event name)
	 * 
	 * @return a map containing all data types of an event 
	 * or the class of the data type if the event is an input event
	 */
	Object getEventDatatype(String eventName);

	/**
	 * Searches for the data type of a property.
	 * 
	 * @param fullPropertyName the full EML name of the property
	 * 
	 * @return a java.lang.Class or a Map containing Classes and/or further Maps
	 */
	Object getDatatype(String fullPropertyName);

	/**
	 * Returns the newEventName of a given pattern
	 * 
	 * @param patternID id of the pattern
	 * @param selectFunctionNumber number of the select function which results are counted
	 * 
	 * @return the newEventName of the pattern
	 */
	String getNewEventName(String patternID, int selectFunctionNumber);

	
	/**
	 * @return the simple patterns
	 */
	Map<String, IPatternSimple> getSimplePatterns();

	ISubscriptionManager getSubMgr();

	void removeFromEngine();
}
