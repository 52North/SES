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
package org.n52.ses.eml.v001.filterlogic.esper;
/**
 * Copyright (C) 2009
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
///**
// * Copyright (C) 2009
// * by 52 North Initiative for Geospatial Open Source Software GmbH
// *
// * Contact: Andreas Wytzisk
// * 52 North Initiative for Geospatial Open Source Software GmbH
// * Martin-Luther-King-Weg 24
// * 48155 Muenster, Germany
// * info@52north.org
// *
// * This program is free software; you can redistribute and/or modify it under
// * the terms of the GNU General Public License version 2 as published by the
// * Free Software Foundation.
// *
// * This program is distributed WITHOUT ANY WARRANTY; even without the implied
// * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License along with
// * this program (see gnu-gpl v2.txt). If not, write to the Free Software
// * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
// * visit the Free Software Foundation web page, http://www.fsf.org.
// */
///**
// * Part of the diploma thesis of Thomas Everding.
// * @author Thomas Everding
// */
//
//package org.n52.swe.ses.emlmodule.eml.filterlogic.esper;
//
//import java.util.HashMap;
//import java.util.Vector;
//import java.util.logging.Logger;
//
//import org.n52.swe.ses.common.ConfigurationRegistry;
//import org.n52.swe.ses.emlmodule.eml.constants.SupportedDataTypes;
//import org.n52.swe.ses.emlmodule.eml.event.MapEvent;
//import org.n52.swe.ses.emlmodule.process.InputDescription;
//
//import net.opengis.swex.x001.EventType;
//import net.opengis.swex.x001.EventType.Properties.Property;
//import net.opengis.swex.x001.PropertyValueDocument.PropertyValue;
//
//
///**
// * parses the data types of a swe:Event
// * 
// * @author Thomas Everding
// * 
// */
//public class EventDataTypeParser {
//	
//	/*
//	 * Logger instance for this class
//	 */
//	private static Logger logger = ConfigurationRegistry.getInstance().getGlobalLogger();
//	
//	/**
//	 * parses the event data types for an input description
//	 * 
//	 * @param description the input description with data type {@link SupportedDataTypes}.EVENT
//	 * 
//	 * @return a hash map containing the name of each event property as key and its data type (as {@link Class})
//	 * as value
//	 */
//	public static synchronized HashMap<String, Object> parse(InputDescription description) {
//		if (!description.getDataType().equals(SupportedDataTypes.EVENT)) {
//			//no swe:Event described
//			logger.severe("the input is not of type swe:Event");
//			return null;
//		}
//		
//		HashMap<String, Object> result;
//		
//		if (description.getInputUnderlying() != null) {
//			//input description
//			EventType eventType = (EventType) description.getInputUnderlying().getAbstractDataRecord();
//			result = parseEventType(eventType);
//		}
//		else {
//			//parameter description
//			EventType eventType = (EventType) description.getParamUnderlying().getAbstractDataRecord();
//			result = parseEventType(eventType);
//		}
//		
//		return result;
//	}
//
//	/**
//	 * parses the data types of a single swe:Event
//	 * 
//	 * @param eventType the swe:Event
//	 * 
//	 * @return a map with the data types
//	 */
//	private static HashMap<String, Object> parseEventType(EventType eventType) {
//		HashMap<String, Object> result = new HashMap<String, Object>();
//		Property[] properties = eventType.getProperties().getPropertyArray();
//		
//		//get data type of every property
//		for (Property prop : properties) {
//			Object dataType = parseDataType(prop.getPropertyValue());
//			result.put(prop.getPropertyName(), dataType);
//		}
//		
//		//generic event attributes
//		result.put(MapEvent.CAUSALITY_KEY, Vector.class);
//		result.put(MapEvent.START_KEY, Long.class);
//		result.put(MapEvent.END_KEY, Long.class);
//		
//		return result;
//	}
//
//	
//	/**
//	 * returns the data type for a single property
//	 * 
//	 * @param propertyValue the description of a single property
//	 * 
//	 * @return the property's data type
//	 */
//	private static Object parseDataType(PropertyValue propertyValue) {
//		
//		if (propertyValue.isSetBoolean()) {
//			//boolean type
//			return Boolean.class;
//		}
//		
//		if (propertyValue.isSetCategory()) {
//			//category type
//			return String.class;
//		}
//		
//		if (propertyValue.isSetCount()) {
//			//count type
//			return Integer.class;
//		}
//		
//		if (propertyValue.isSetQuantity()) {
//			//quantity type
//			return Double.class;
//		}
//		
//		if (propertyValue.isSetText()) {
//			//text type
//			return String.class;
//		}
//		
//		if (propertyValue.isSetTime()) {
//			//time type
//			return Long.class;
//		}
//		
//		//data type not supported / unknown
//		logger.warning("datatype is not supported or unknown");
//		return Object.class;
//	}
//}
