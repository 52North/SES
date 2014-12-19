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

package org.n52.ses.eml.v001.filterlogic.esper;

import java.util.HashMap;
import java.util.Vector;

import org.n52.ses.eml.v001.Constants;
import org.n52.ses.api.eml.ILogicController;
import org.n52.ses.api.event.MapEvent;

import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.EventBean;


/**
 * Listener for the counting statement of repetitive patterns.
 * 
 * @author Thomas Everding
 *
 */
public class CountingListener implements UpdateListener{
	
	private ILogicController controller;
	
	private String inputEventName;

	private String eventName;
	
	
	/**
	 * 
	 * Constructor
	 *
	 * @param controller the esper controller
	 * @param inputEventName name of the event which is counted
	 */
	public CountingListener(ILogicController controller, String inputEventName) {
		this.controller = controller;
		this.inputEventName = inputEventName;
		
		this.initialize();
	}

	
	/**
	 * initializes this listener
	 */
	private void initialize() {
		//register counting event at esper engine
		HashMap<String, Object> eventProperties = new HashMap<String, Object>();
		eventProperties.put(MapEvent.START_KEY, Long.class);
		eventProperties.put(MapEvent.END_KEY, Long.class);
		eventProperties.put(MapEvent.CAUSALITY_KEY, Vector.class);
		
		this.eventName = this.inputEventName + Constants.REPETIVITE_COUNT_EVENT_SUFFIX;
		this.controller.registerEvent(this.eventName, eventProperties);
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if (newEvents == null) {
			//no new events
			return;
		}
		
		//handle all events
		for (EventBean bean : newEvents) {
			this.handleEvent(bean);
		}
	}
	
	
	/**
	 * handles a single new event
	 * 
	 * @param bean the new event
	 */
	private synchronized void handleEvent(EventBean bean) {
		//create new event, property values are regardless
		MapEvent event = new MapEvent(1, 1);
		
		//send event
		this.controller.sendEvent(this.eventName, event);
	}


	/**
	 * @return the inputEventName
	 */
	public String getInputEventName() {
		return this.inputEventName;
	}
	
}
