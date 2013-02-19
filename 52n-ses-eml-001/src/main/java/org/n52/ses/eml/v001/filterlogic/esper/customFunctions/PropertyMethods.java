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
package org.n52.ses.eml.v001.filterlogic.esper.customFunctions;

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
		Map eventMap = (Map) event;
		if (eventMap.containsKey(propertyName.toString())) {
			if (eventMap.get(propertyName.toString()) == null) {
				return false;
			}
		}
		else {
			return false;
		}
		return true;
	}
	

}
