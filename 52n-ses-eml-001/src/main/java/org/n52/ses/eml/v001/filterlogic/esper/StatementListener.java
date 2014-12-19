/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
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

import net.opengis.eml.x001.EMLDocument.EML;

import org.apache.xmlbeans.XmlObject;
import org.n52.ses.api.common.CustomStatementEvent;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.ws.INotificationMessage;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.api.ws.SESConstraintFilter;
import org.n52.ses.api.ws.SESFilterCollection;
import org.n52.ses.eml.v001.pattern.SelFunction;
import org.n52.ses.eml.v001.pattern.Statement;
import org.n52.ses.eml.v001.util.EventModelGenerator;
import org.n52.ses.eml.v001.util.ThreadPool;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.EventBean;


/**
 * listener for a single esper statement
 * 
 * @author Thomas Everding
 *
 */
public class StatementListener implements UpdateListener{
	
	private Statement statement;
	
	private EsperController controller;
	
	private boolean doOutput;
	
	private ISubscriptionManager subMgr;
	
	private static int instanceCount = 1;
	
	private int instanceNumber;

	private boolean paused = false;

	private static final Logger logger = LoggerFactory
			.getLogger(StatementListener.class);

	/**
	 * 
	 * Constructor
	 *
	 * @param statement one {@link Statement}, used to configure this listener
	 * @param controller the esper controller with the esper engine
	 */
	public StatementListener(Statement statement, EsperController controller) {
		this.statement = statement;
		this.controller = controller;
		
		this.initialize();
	}
	
	/**
	 * 
	 * Constructor
	 *
	 * @param statement statement one {@link Statement}, used to configure this listener
	 * @param controller the esper controller with the esper engine
	 * @param sub the subscription manager
	 */
	public StatementListener(Statement statement, EsperController controller, ISubscriptionManager sub) {
		this(statement, controller);
		this.subMgr = sub;
	}
	
	
	/**
	 * initializes the listener
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
		//set instance number
		this.instanceNumber = instanceCount;
		instanceCount++;
		
		//check for output
		if (this.statement.getSelectFunction().getOutputName().equals("")) {
			
			//TODO (hack for static EML) fix output for StaticEMLDocument
			if (this.statement.getSelectFunction().getNewEventName().equals("")) {
				this.doOutput = true;
			} else {
				this.doOutput = false;
			}
		}
		else {
			this.doOutput = true;
		}
		
		//register new event at esper engine
		SelFunction sel = this.statement.getSelectFunction();
		if (!sel.getNewEventName().equals("")) {
			String eventName = sel.getNewEventName();
			
			//common attributes
//			HashMap<String, Object> eventProperties = new HashMap<String, Object>();
//			eventProperties.put(MapEvent.START_KEY, Long.class);
//			eventProperties.put(MapEvent.END_KEY, Long.class);
//			eventProperties.put(MapEvent.CAUSALITY_KEY, Vector.class);
			
			HashMap<String, Object> eventProperties = this.controller.getEventProperties();
			
			
			//register every event attribute
			//TODO for string as result value maybe start debugging here
			if (sel.isSingleValueOutput()) {
				for (String key : sel.getDataTypes().keySet()) {
					if (!eventProperties.containsKey(key))
						eventProperties.put(key, sel.getDataTypes().get(key));
				}
			}
			else {
				//nested properties
				HashMap<String, Object> nestedMap = new HashMap<String, Object>();
				for (String key : sel.getDataTypes().keySet()) {
					nestedMap.put(key, sel.getDataTypes().get(key));
				}
				
				if (nestedMap.get(MapEvent.VALUE_KEY) instanceof HashMap) {
					//get inner map
					nestedMap = (HashMap<String, Object>) nestedMap.get(MapEvent.VALUE_KEY);
				}
				
				//add nested properties
				for (String key : nestedMap.keySet()) {
					if (key.equals(MapEvent.START_KEY) || key.equals(MapEvent.END_KEY) || key.equals(MapEvent.CAUSALITY_KEY)) {
						//do nothing
					}
					else {
						if (!eventProperties.containsKey(key))
							eventProperties.put(key, nestedMap.get(key));
					}
					
				}
			}
			
//			logger.info("registering event properties as outputs from statement: " + statement.getStatement());
//			
//			for (String key : eventProperties.keySet()) {
//				logger.info("key '" + key + "' has the type '" + eventProperties.get(key) + "'");
//			}
			this.controller.registerEvent(eventName, eventProperties);
		}
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if (paused) {
			return;
		}
		
		/*
		 * new matches for the pattern received
		 * 
		 * handle every match
		 */
		if (newEvents != null && newEvents.length > 0) {
			for (EventBean newEvent : newEvents) {
				this.handleMatch(newEvent);
			}
			
			if (this.statement.hasCustomStatementEvents()) {
				for (CustomStatementEvent cse : this.statement.getCustomStatementEvents()) {
					cse.eventFired(newEvents, this.subMgr);
				}
			}
		}
	}


	/**
	 * handles a single pattern match
	 * 
	 * @param newEvent the EventBean representing the match
	 */
	protected synchronized void handleMatch(EventBean newEvent) {
		
		logger.debug("Statement {} matched for Event '{}'",
				this.getStatement().getStatement(), newEvent.getUnderlying().toString());
		
		UpdateHandlerThread handler = new UpdateHandlerThread(this, newEvent);
		
		//handle match in its own thread using a ThreadPool
		ThreadPool tp = ThreadPool.getInstance();
		tp.execute(handler);
	}
	
	
//	/**
//	 * Sends the received result to the controller for output
//	 * 
//	 * @param resultEvent the result to send
//	 */
//	public synchronized void doOutput(MapEvent resultEvent) {
//		String outputName = this.statement.getSelectFunction().getOutputName();
//		
//		//load output description
//		if (this.outDescription == null) {
//			if (this.getOutDescriptionPerformed) {
//				//output description not found
//				return;
//			}
//			
//			//try to find output description
//			this.outDescription = this.controller.getOutputDescription(outputName);
//			this.getOutDescriptionPerformed = true;
//			
//			if (this.outDescription == null) {
//				//not found
//				return;
//			}
//		}
//		
//		//send output (the whole event or only the value)
//		if (this.outDescription.getDataType().equals(SupportedDataTypes.EVENT)) {
//			//send event
//			this.controller.doOutput(outputName, resultEvent);
//		}
//		else {
//			//send only value
//			this.controller.doOutput(outputName, resultEvent.get(MapEvent.VALUE_KEY));
//		}
//	}
	
	/**
	 * Sends the received result to the controller for output
	 * 
	 * @param resultEvent the result to send
	 */
	public synchronized void doOutput(MapEvent resultEvent) {
		if (logger.isDebugEnabled())
			logger.debug("performing output for statement:\n" + this.statement.getStatement());
		
	    boolean sent = false;
	    
	    /*
	     * For GENESIS the output has to be the old eml:Event
	     */
	    ConfigurationRegistry config = ConfigurationRegistry.getInstance();
	    boolean genesisMode = false;
	    Object gm = config.getPropertyForKey(ConfigurationRegistry.USE_FOR_GENESIS);
	    if (gm != null) {
	    	genesisMode = Boolean.parseBoolean(gm.toString());
	    }
	    
	    //check if it is allowed to use the original message.
	    //check also if it is used for GENESIS
	    if (this.statement.getSelectFunction().allowsOriginalMessageAsResult() && !genesisMode) {
	    	//get original message
	    	INotificationMessage origMessage = resultEvent.getOriginalMessage();
		    if (origMessage != null) {
		    	try {
		 	    	//get message and forward to SESSubscriptionManager
		    		StatementListener.logger.info("sending original message");
		 			this.subMgr.publish(origMessage);
		 			sent = true;
		 		}
		 		catch (Throwable t) {
		 			//any other exception occurred, sent is false -> do nothing
		 		}
		    }
	    }
	    
	    /* 
	     * If not yet sent (original message sending 
	     * failed or not allowed) build an ses:Event 
	     * and send it.
	     */
	    if (!sent){
	    	XmlObject eventDoc = null;
	    	
	    	/*
	    	 * for GENESIS we do ses:Event output.
	    	 * otherwise use OGC event model
	    	 */
	    	if (!genesisMode) {
	    		//generate Event model
		    	StatementListener.logger.info("generating OGC Event model output");
	    		EventModelGenerator eventGen = new EventModelGenerator(resultEvent);
	    		
	    		if (this.subMgr.getFilter() instanceof SESFilterCollection) {
	    			SESFilterCollection sesFilter = (SESFilterCollection) this.subMgr.getFilter();
	    			if (sesFilter.getConstraintFilter() != null) {
	    				
	    				//EML or EPL filter?
	    				if (sesFilter.getConstraintFilter() instanceof SESConstraintFilter) {
	    					SESConstraintFilter sesConstr = (SESConstraintFilter) sesFilter.getConstraintFilter();
	    					eventDoc = eventGen.generateEventDocument(
		    						(EML) sesConstr.getEml().getEMLInstance());
	    				}
	    				
	    				
	    			}
	    			else {
	    				eventDoc = eventGen.generateEventDocument();
	    			}
	    		}
	    		else {
	    			eventDoc = eventGen.generateEventDocument();
	    		}
	    	}
	    	else {
	    		//generate SESEvent
	    		
	    		eventDoc = this.subMgr.generateSESEvent(resultEvent);
	    	}
	    	
	    	sent = this.subMgr.sendSESNotificationMessge(eventDoc);	
	    }
		
		if (!sent) {
			StatementListener.logger.warn("An error occured while sending a NotificationMessage" +
					" with the SubscriptionManager. It was not sent.");
		}
	}
	
	

	/**
	 * @return the statement
	 */
	public Statement getStatement() {
		return this.statement;
	}


	/**
	 * @return the controller
	 */
	public EsperController getController() {
		return this.controller;
	}


	/**
	 * @return the doOutput
	 */
	public boolean isDoOutput() {
		return this.doOutput;
	}
	
	
	/**
	 * @return the instanceNumber
	 */
	public int getInstanceNumber() {
		return this.instanceNumber;
	}

	public void pause() {
		this.paused = true;
	}

	public void resume() {
		this.paused = false;
	}
}