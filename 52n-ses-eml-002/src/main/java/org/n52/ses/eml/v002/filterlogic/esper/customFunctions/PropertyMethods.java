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
package org.n52.ses.eml.v002.filterlogic.esper.customFunctions;

import java.util.List;
import java.util.Map;

/**
 * Provides methods to check properties
 */
public class PropertyMethods {
	
	/**
	 * check if a property exists
	 * 
	 * @param event the event that should contain the property
	 * @param propertyName the property name
	 * 
	 * @return true, if the event contains the property
	 */
	@SuppressWarnings("rawtypes")
	public static boolean propertyExists(Object event, Object propertyName) {
		//prepare types
		String propName = propertyName.toString();
		
		//first object is always a MapEvent
		Map eventMap = (Map) event;
		
		//check for nested properties
		if (propName.contains(".")) {
			//nested properties
			return containsNestedProperty(propName, eventMap);
		}
		//not nested
		return containsProperty(propName, eventMap);
	}
	
	@SuppressWarnings("rawtypes")
	private static boolean containsNestedProperty(String propName, Map map) {
		//check if first part is contained in map
		int dotIndex = propName.indexOf(".");
		String firstPart = propName.substring(0, dotIndex);
		if (!map.containsKey(firstPart)) {
			//if not, return false
			return false;
		}
		
		//if yes, do...
		//...check if value for the first part is not null 
		Object prop = map.get(firstPart);
		if (prop == null) {
			//return false if it is null
			return false;
		}
		
		//...check if the value is a map or list type, 
		if (!(prop instanceof Map) && !(prop instanceof List)) {
			//if not, return false as the first part of the nested property must point to a map or list
			return false;
		}
		
		//...check if the remaining part is nested
		String lastPart = propName.substring(dotIndex + 1);
		if (lastPart.contains(".")) {
			//if yes do a recursive call of containsNestedProperty either with a map or a list, 
			//return result or recursive call
			if (prop instanceof Map) {
				return containsNestedProperty(lastPart, (Map) prop);
			}
			//prop must be instance of List here, otherwise we would have a return false before
			return containsNestedProperty(lastPart, (List) prop);
		}
		
		//if not, return result of containsProperty
		if (prop instanceof Map) {
			return containsProperty(lastPart, (Map) prop);
		}
		//prop must be instance of List here, otherwise we would have a return false before
		return containsProperty(lastPart, (List) prop);
	}
	
	
	@SuppressWarnings("rawtypes")
	private static boolean containsNestedProperty(String propName, List list) {
		//get first part of the nested property name
		int dotIndex = propName.indexOf(".");
		String firstPart = propName.substring(0, dotIndex);
		
		//check if first part is an int
		int index = -1;
		try {
			index = Integer.parseInt(firstPart);
		}
		catch (NumberFormatException e) {
			//not an int
			return false;
		}
		
		//check if the index is contained in list
		if (index < 0 || index >= list.size()) {
			//if not, return false
			return false;
		}
		
		//if yes, do...
		//...check if value for the index is not null 
		Object prop = list.get(index);
		if (prop == null) {
			//return false if it is null
			return false;
		}
		
		//...check if the value is a map or list type, 
		if (!(prop instanceof Map) && !(prop instanceof List)) {
			//if not, return false as the first part of the nested property must point to a map or list
			return false;
		}
		
		//...check if the remaining part is nested
		String lastPart = propName.substring(dotIndex + 1);
		if (lastPart.contains(".")) {
			//if yes do a recursive call of containsNestedProperty either with a map or a list, 
			//return result or recursive call
			if (prop instanceof Map) {
				return containsNestedProperty(lastPart, (Map) prop);
			}
			//prop must be instance of List here, otherwise we would have a return false before
			return containsNestedProperty(lastPart, (List) prop);
		}
		
		//if not, return result of containsProperty
		if (prop instanceof Map) {
			return containsProperty(lastPart, (Map) prop);
		}
		//prop must be instance of List here, otherwise we would have a return false before
		return containsProperty(lastPart, (List) prop);
	}
	
	
	@SuppressWarnings("rawtypes")
	private static boolean containsProperty(String propName, List list) {
		//propName has to be an int
		int index = -1;
		try {
			index = Integer.parseInt(propName);
		}
		catch (NumberFormatException e) {
			//not an int
			return false;
		}
		
		//check if the list contains the property
		if (index >= 0 && index < list.size()){
			//number available, check if value != null
			if (list.get(index) == null) {
				//no value
				return false;
			}
		}
		else {
			//index not found or too large
			return false;
		}
		//all tests passed
		return true;
	}

	@SuppressWarnings("rawtypes")
	private static boolean containsProperty(String propName, Map map) {
		if (map.containsKey(propName)) {
			if (map.get(propName) == null) {
				//no value available
				return false;
			}
		}
		else {
			//property not in map
			return false;
		}
		//all tests passed
		return true;
	}
}
