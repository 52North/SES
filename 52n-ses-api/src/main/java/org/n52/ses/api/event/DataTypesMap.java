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
package org.n52.ses.api.event;

import java.util.HashMap;

/**
 *
 * Class for holding the data types of any registered
 * phenomenon.
 *
 */
public class DataTypesMap {
	
	private static DataTypesMap instance;
	
	private HashMap<String, Object> types;
	
	private DataTypesMap() {
		this.types = new HashMap<String, Object>();
	}
	
	/**
	 * @return the single instance of this class
	 */
	public synchronized static DataTypesMap getInstance() {
		if (instance == null) {
			instance = new DataTypesMap();
		}
		return instance;
	}
	
	/**
	 * Registers a data type for the given phenomenon.
	 * Should be called for each RegisterPublisher (new sensor)
	 * request. 
	 * 
	 * @param phenomenon Phenomenon as String (e.g. a urn)
	 * @param type Data type (e.g. Double.class)
	 * @return true if phenomenon was not registered before. false else.
	 */
	public synchronized boolean registerNewDataType(String phenomenon, Object type) {
//		logger.info("adding new data type for phenomenon: " + phenomenon);
		
		if (this.types.containsKey(phenomenon)) {
//			logger.info("data type already registered, aborting");
			return false;
		}
		this.types.put(phenomenon, type);
//		logger.info("data type registered, type: " + type);
		return true;
	}
	
	
	/**
	 * 
	 * @param phenomenon the phenomenon
	 * @return Returns the data type for a phenomenon. Returns  'Object' if nothing registered.
	 */
	public synchronized Object getDataType(String phenomenon) {
//		logger.info("Data type requested for phenomenon: " + phenomenon);
		
		if (!this.types.containsKey(phenomenon)) {
//			logger.info("data type unknown, returning String");
			return String.class;
		}
//		logger.info("data type: " + types.get(phenomenon));
		return this.types.get(phenomenon);
	}

	/**
	 * @param string the phenomenon
	 * @return true if the type for the phenomenon is contained
	 */
	public boolean containsDataType(String string) {
		return this.types.containsKey(string);
	}

	
	/**
	 * This methods returns a copy of all registered data types.
	 * Hence, modification will only have local effects.
	 * 
	 * @return a copy of the registered data types
	 */
	public HashMap<String, Object> getTypes() {
		return new HashMap<String, Object>(this.types);
	}
	
	

}
