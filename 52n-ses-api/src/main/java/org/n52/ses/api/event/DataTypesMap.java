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
