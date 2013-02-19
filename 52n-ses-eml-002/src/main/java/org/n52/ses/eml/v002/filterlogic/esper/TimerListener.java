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
package org.n52.ses.eml.v002.filterlogic.esper;
///**
// * Part of the diploma thesis of Thomas Everding.
// * @author Thomas Everding
// */
//
//package de.ifgi.lehre.thesisEverding.eml.esper;
//
//import java.util.HashMap;
//import java.util.Vector;
//
//import com.espertech.esper.client.UpdateListener;
//import com.espertech.esper.client.EventBean;
//
//import de.ifgi.lehre.thesisEverding.eml.Constants;
//import de.ifgi.lehre.thesisEverding.eml.event.MapEvent;
//import de.ifgi.lehre.thesisEverding.eml.pattern.SelFunction;
//import de.ifgi.lehre.thesisEverding.eml.pattern.Statement;
//
//
///**
// * Special listener for timer events.
// * 
// * Timer patterns look like: select * from pattern [every ((e=event) and (-timer...-))]
// * 
// * Needed to send a new event (e) to restart the pattern. Timer patterns without [...](e=event) and[...] do not
// * generate an output in esper.
// * 
// * @author Thomas Everding
// * 
// */
//public class TimerListener implements UpdateListener {
//	
//	private Statement statement;
//	
//	private EsperController controller;
//	
////	private boolean doOutput;
//	
//	private String internalEventName;
//	
////	private boolean getOutDescriptionPerformed = false;
//	
////	private OutputDescription outDescription;
//	
//	
//	/**
//	 * 
//	 * Constructor
//	 * 
//	 * @param statement one {@link Statement}, used to configure this listener
//	 * @param controller the esper controller with the esper engine
//	 */
//	public TimerListener(Statement statement, EsperController controller) {
//		this.statement = statement;
//		this.controller = controller;
//		
//		this.initialize();
//	}
//	
//
//	/**
//	 * initializes the listener
//	 */
//	private void initialize() {
////		//check for output
////		if (this.statement.getSelectFunction().getOutputName().equals("")) {
////			this.doOutput = false;
////		}
////		else {
////			this.doOutput = true;
////		}
//		
//		//register special timer events at esper engine
//		SelFunction sel = this.statement.getSelectFunction();
//		internalEventName = Constants.TIMER_INTERNAL_EVENT_PREFIX + sel.getNewEventName();
//		//common attributes
//		HashMap<String, Object> eventProperties = new HashMap<String, Object>();
//		eventProperties.put(MapEvent.START_KEY, Long.class);
//		eventProperties.put(MapEvent.END_KEY, Long.class);
//		eventProperties.put(MapEvent.CAUSALITY_KEY, Vector.class);
//		
//		//save time also as value
//		eventProperties.put(MapEvent.VALUE_KEY, Long.class);
//		
//		//register internal event
//		this.controller.registerEvent(internalEventName, eventProperties);
//	}
//	
//
//	@Override
//	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
//		//handle every new event
//		if (newEvents != null) {
//			for (EventBean bean : newEvents) {
//				this.handleEvent(bean);
//			}
//		}
//	}
//	
//
//	/**
//	 * handles a single new event
//	 * 
//	 * @param bean the new event
//	 */
//	@SuppressWarnings("unchecked")
//	private void handleEvent(EventBean bean) {
//		TimerUpdateHandlerThread handler = new TimerUpdateHandlerThread(this.controller, this.statement, this.internalEventName, bean);
//		
//		//handle match in its own thread
//		Thread t = new Thread(handler);
//		t.start();
////		//event received, publish new event for next timer pattern match
////		Date now = new Date();
////		MapEvent event = new MapEvent(now.getTime(), now.getTime());
////		event.put(MapEvent.VALUE_KEY, now.getTime());
////		
////		//create causality if wanted
////		if (this.statement.getSelectFunction().getCreateCausality()) {
////			MapEvent underlying = (MapEvent) bean.getUnderlying();
////			
////			Vector<MapEvent> underlyingCausality = (Vector<MapEvent>) underlying.get(MapEvent.CAUSALITY_KEY);
////			
////			//add causality of underlying event
////			for (MapEvent e : underlyingCausality) {
////				event.addCausalAncestor(e);
////			}
////			
////			//add underlying event to causality
////			event.addCausalAncestor(underlying);
////		}
////		
////		//publish to make next match possible
////		logger.debug("");
////		logger.debug("pushing event");
////		this.controller.sendEvent(this.internalEventName, event);
////		
////		if (doOutput) {
////			this.doOutput(event);
////		}
//	}
//	
//
////	/**
////	 * performs the output
////	 * 
////	 * @param event event with output data
////	 */
////	public synchronized void doOutput(MapEvent event) {
////		String outputName = this.statement.getSelectFunction().getOutputName();
////		
////		//load output description
////		if (this.outDescription == null) {
////			if (this.getOutDescriptionPerformed) {
////				//output description not found
////				return;
////			}
////			
////			//try to find output description
////			this.outDescription = this.controller.getOutputDescription(outputName);
////			this.getOutDescriptionPerformed = true;
////			
////			if (this.outDescription == null) {
////				//not found
////				return;
////			}
////		}
////		
////		//send output (the whole event or only the value)
////		if (this.outDescription.getDataType().equals(SupportedDataTypes.EVENT)) {
////			//send event
////			this.controller.doOutput(outputName, event);
////		}
////		else {
////			//send only value
////			this.controller.doOutput(outputName, event.get(MapEvent.VALUE_KEY));
////		}
////	}
//	
//
//	/**
//	 * @return the internalEventName
//	 */
//	public String getInternalEventName() {
//		return this.internalEventName;
//	}
//	
//}
