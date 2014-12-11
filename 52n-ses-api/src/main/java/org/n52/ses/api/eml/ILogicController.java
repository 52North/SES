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
	 * send a new event to the engine. an implementation shall
	 * delegate to {@link #sendEvent(String, MapEvent, boolean)}
	 * with persist=true.
	 * 
	 * @param inputName the name of the event type
	 * @param event the new event
	 */
	void sendEvent(String inputName, MapEvent event);

	/**
	 * send a new event to the engine
	 * 
	 * @param inputName the name of the event type
	 * @param event the new event
	 * @param persist if the event should be persisted (for re-insert on restart of the service)
	 */
	void sendEvent(String inputName, MapEvent event, boolean persist);
	
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

	void pauseAllStatements();

	void resumeAllStatements();
}
