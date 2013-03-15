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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v001.filterlogic.esper.customFunctions;

import java.util.Vector;

import org.n52.ses.api.event.MapEvent;


/**
 * provides methods to perform causality test
 * 
 * @author Thomas Everding
 *
 */
public class CausalityMethods {
	
	/**
	 * Checks if an event is a causal ancestor of another event
	 * 
	 * @param event the possible ancestor
	 * @param causalVector the causal vector of the other event
	 * 
	 * @return <code>true</code> if the event is a causal ancestor
	 */
	@SuppressWarnings("unchecked")
	public static boolean isCausalAncestorOf(Object event, Object causalVector) {
		Vector<Object> vec = (Vector<Object>) causalVector;
		
		MapEvent eventMap = (MapEvent) event;
		String id = eventMap.get(MapEvent.SENSORID_KEY).toString() + eventMap.get(MapEvent.START_KEY).toString();
		
		String testID;
		MapEvent e;
		for (Object o : vec) {
			e = (MapEvent) o;
			testID = e.get(MapEvent.SENSORID_KEY).toString() + e.get(MapEvent.START_KEY).toString();
			
			if (testID.equals(id)) {
				return true;
			}
		}

		return false;
	}
	
	
	/**
	 * Checks if an event is a causal ancestor of another event
	 * 
	 * @param event the possible ancestor
	 * @param causalVector the causal vector of the other event
	 * 
	 * @return <code>true</code> if the event is not a causal ancestor
	 */
	public static boolean isNotCausalAncestorOf(Object event, Object causalVector) {
		return !isCausalAncestorOf(event, causalVector);
	}
}
