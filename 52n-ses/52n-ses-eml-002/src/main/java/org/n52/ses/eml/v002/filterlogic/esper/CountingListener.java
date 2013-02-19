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

package org.n52.ses.eml.v002.filterlogic.esper;

import java.util.HashMap;
import java.util.Vector;

import org.n52.ses.api.event.MapEvent;
import org.n52.ses.eml.v002.Constants;
import org.n52.ses.api.eml.ILogicController;

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
