/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
