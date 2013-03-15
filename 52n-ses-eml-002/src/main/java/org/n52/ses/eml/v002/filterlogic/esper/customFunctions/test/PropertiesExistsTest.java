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
package org.n52.ses.eml.v002.filterlogic.esper.customFunctions.test;

import java.util.HashMap;
import java.util.Map;

import org.n52.ses.api.event.MapEvent;
import org.n52.ses.eml.v002.filterlogic.esper.customFunctions.PropertyMethods;
import org.n52.ses.util.common.FaaSaaPilotConstants;

/**
 * Class to test the propertiesExsists method.
 * 
 * @author Thomas Everding
 *
 */
public class PropertiesExistsTest {
	
	/**
	 * Main to be executed.
	 * @param args arguments
	 */
	public static void main(String[] args) {
		//build Events
//		MapEvent e1 = new MapEvent(0, 1);
//		e1.put(MapEvent.IDENTIFIER_VALUE_KEY, "id");
		
		MapEvent e2 = new MapEvent(0, 2);
		Map<String, Object> map = new HashMap<String, Object>();
		
		Map<String, Object> iM = new HashMap<String, Object>();
		iM.put(FaaSaaPilotConstants.VALUE_NAME, "id");
		
		map.put(FaaSaaPilotConstants.IDENTIFIER_NAME, iM);
		e2.put(FaaSaaPilotConstants.AIRSPACE_NAME, map);
		
//		MapEvent e3 = new MapEvent(0, 4);
//		List<Object> list = new Vector<Object>();
//		list.add(map);
//		e3.put(FaaSaaPilotConstants.AIRSPACE_NAME, list);
		
		System.out.println("e2:\n" + e2.toString());
		
		//test events
		boolean e2short = PropertyMethods.propertyExists(e2, FaaSaaPilotConstants.AIRSPACE_NAME);
		boolean e2works = PropertyMethods.propertyExists(e2, FaaSaaPilotConstants.AIRSPACE_NAME + "." + FaaSaaPilotConstants.IDENTIFIER_NAME + "." + FaaSaaPilotConstants.VALUE_NAME);
//		boolean e3works = PropertyMethods.propertyExists(e3, FaaSaaPilotConstants.AIRSPACE_NAME + ".0." + FaaSaaPilotConstants.IDENTIFIER_NAME);
		
		//write results
		System.out.println("event.map short works: " + e2short);
		System.out.println("event.map.map.property works: " + e2works);
//		System.out.println("event.list.map.property works: " + e3works);
	}

}
