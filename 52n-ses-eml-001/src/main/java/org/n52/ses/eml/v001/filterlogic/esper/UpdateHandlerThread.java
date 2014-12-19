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

import java.util.Date;
import java.util.Map;

import org.n52.ses.eml.v001.Constants;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.event.MapEventFactory;
import org.n52.ses.eml.v001.pattern.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.event.map.MapEventBean;



/**
 * Handles updates from a {@link StatementListener}.
 * 
 * @author Thomas Everding
 * 
 */
public class UpdateHandlerThread implements Runnable {
	
	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(UpdateHandlerThread.class);
	
	private EsperController controller;
	
	private Statement statement;
	
	private EventBean bean;
	
	private boolean doOutput;
	
	private StatementListener listener;
	
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param listener listener that received the update
	 * @param bean the received update
	 */
	public UpdateHandlerThread(StatementListener listener, EventBean bean) {
		this.doOutput = listener.isDoOutput();
		this.controller = listener.getController();
		this.statement = listener.getStatement();
		this.bean = bean;
		this.listener = listener;
	}
	

	@Override
	public void run() {
		if (logger.isDebugEnabled()) {
			logger.debug("Update received for statement: " + this.statement.getStatement());
		}
		
//		logger.info("bean type: " + bean.getClass().getName());
//		logger.info("undelying type: " + bean.getUnderlying().getClass().getName());
		
		//build new event
		MapEvent event = null;
		
		if (this.bean instanceof MapEventBean) {
			MapEventBean selected = (MapEventBean) this.bean;
			String[] propertyNames = selected.getEventType().getPropertyNames();
			
//			StringBuilder log = new StringBuilder();
//			log.append("selected event properties.");
//			for (String key : propertyNames) {
//				log.append("\n\t" + key);
//			}
//			logger.info(log.toString());
			
			if (propertyNames.length > 1) {
				event = createEventFromComplexSelect(selected);
			}
			else if (propertyNames.length == 1){
				event = createEventFromSimpleSelect(event, propertyNames[0]);
			}
			else {
//				logger.info("will create event from simple select");
				event = createEventFromSimpleSelect(event);
			}
			
		}
		else {
//			logger.info("will create event from complex select with null event");
			event = createEventFromSimpleSelect(event);
		}
		
		if (event == null) {
			//still nothing selected...
			UpdateHandlerThread.logger.warn("no result generated from pattern update");
			return;
		}
		/*
		 * add original message
		 */
		if (this.bean.getUnderlying() instanceof Map<?, ?>) {
//			logger.info("adding original message from Map<?, ?>");
			Map<?, ?> alert = (Map<?, ?>) this.bean.getUnderlying();
			Object message = alert.get(MapEvent.ORIGNIAL_MESSAGE_KEY);
			if (message != null) {
				event.put(MapEvent.ORIGNIAL_MESSAGE_KEY, message);
			}
		}
		
//		//create causality if wanted
//		if (this.statement.getSelectFunction().getCreateCausality()) {
//			logger.info("creating causality");
//			logger.info("underl: " + bean.getUnderlying().getClass().getName());
//			
//			StringBuilder log = new StringBuilder();
//			log.append("if map then content:");
//			if (bean.getUnderlying() instanceof HashMap) {
//				HashMap map = (HashMap) bean.getUnderlying();
//				for (Object key : map.keySet()) {
//					log.append("\n\t" + key.toString() + ": " + map.get(key));
//				}
//			}
//			logger.info(log.toString());
//			
//			log = new StringBuilder();
//			log.append("result event:");
//			for (String key : event.keySet()) {
//				log.append("\n\t" + key + ": " + event.get(key));
//			}
//			logger.info(log.toString());
//			
//			//TODO: identify what is done next, dies somewhere here...
//			
//			if (bean.getUnderlying() instanceof MapEvent) {
//				logger.info("undelying is a map event!");
//				//select event performed?
//				MapEvent underlying = (MapEvent) bean.getUnderlying();
//				
//				logger.info("underlying map event: " + underlying);
//				
//				Vector<MapEvent> underlyingCausality = (Vector<MapEvent>) underlying.get(MapEvent.CAUSALITY_KEY);
//				
//				//add causality of underlying event
//				logger.info("undelying events: " + underlyingCausality.size());
//				for (MapEvent e : underlyingCausality) {
//					event.addCausalAncestor(e);
//				}
//				
//				//add underlying event to causality
//				event.addCausalAncestor(underlying);
//			}
//			else if (bean.getUnderlying() instanceof HashMap) {
//				logger.info("undelying is a hash map");
//				HashMap<?, ?> map = (HashMap<?, ?>) bean.getUnderlying();
//				
//				log = new StringBuilder();
//				log.append("undelying values:");
//				for (Object key : map.keySet()) {
//					log.append("\n\t" + key.toString() + ": " + map.get(key).toString() + " (" + map.get(key).getClass().getName() + ")");
//				}
//				logger.info(log.toString());
//				
//				//notify on select performed?
//				EPStatement epStatement = this.controller.getEPStatement(this.statement.getStatement());
//				if (epStatement != null) {
//					logger.info("ep statement available");
//					Object obj = epStatement.getUserObject();
//					if (obj != null) {
//						logger.info("user object: " + obj.getClass().getName());
//					}
//					else {
//						logger.info("user object is null");
//					}
//				}
//				
//				EPRuntime epRuntime = this.controller.getEpService().getEPRuntime();
//				Map<String, Object> values = epRuntime.getVariableValueAll();
//				
//				log = new StringBuilder();
//				log.append("runtime variable values:");
//				
//				for (String key : values.keySet()) {
//					log.append("\n\t" + key + ": " + values.get(key).toString() + " (" + values.get(key).getClass().getName() + ")");
//				}
//				logger.info(log.toString());
//				
//				Context context = this.controller.getEpService().getContext();
//				try {
//					logger.info("context results for name 'arrival': " + context.lookup("arrival"));
//				}
//				catch (NamingException e) {
//					e.printStackTrace();
//				}
//			}
//		}
		
		try {
			//send event to esper engine for further processing
			if (!this.statement.getSelectFunction().getNewEventName().equals("")) {
				this.controller.sendEvent(this.statement.getSelectFunction().getNewEventName(), event);
			}
			
			//if output=true send event to output
			if (this.doOutput) {
				if (logger.isDebugEnabled())
					logger.debug("performing output for this match");
				this.listener.doOutput(event);
			}
		}
		catch (Throwable e) {
			//log exception
			UpdateHandlerThread.logger.warn(e.getMessage());
			
			StringBuilder log = new StringBuilder();
			
			for (StackTraceElement ste : e.getStackTrace()) {
				log.append("\n" + ste.toString());
			}
			
			UpdateHandlerThread.logger.warn(log.toString());
			
			//forward exception
			throw new RuntimeException(e);
		}
	}


	/**
	 * generates a new {@link MapEvent} from a selection with multiple values
	 * @param meBean the selected {@link MapEventBean}
	 * @return the new map event
	 */
	private MapEvent createEventFromComplexSelect(MapEventBean meBean) {
		//event selected
		Map<String, Object> properties = meBean.getProperties();
		return parseEventFromMap(properties);
	}


	private MapEvent parseEventFromMap(Map<String, Object> properties) {
		return MapEventFactory.parseFromMap(properties, this.statement.getSelectFunction().getCreateCausality());
//		StringBuilder log = new StringBuilder();
//		log.append("parsing MapEvent from Map");
//		log.append("\n\tproperties:");
//		for (String key : properties.keySet()) {
//			log.append("\n\t" + key);
//		}
//		this.logger.info(log.toString());
		
//		long start;
//		long end;
//		if (properties.containsKey(MapEvent.START_KEY)) {
//			start = Long.parseLong(properties.get(MapEvent.START_KEY).toString());
//		}
//		else {
//			start = new Date().getTime();
//		}
//		
//		if (properties.containsKey(MapEvent.END_KEY)) {
//			end = Long.parseLong(properties.get(MapEvent.END_KEY).toString());
//			if (start > end) {
//				end = start;
//			}
//		}
//		else {
//			end = start;
//		}
//		
//		MapEvent event = new MapEvent(start, end);
//		
//		//copy content
//		for (String key : properties.keySet()) {
//			if (key.equals(MapEvent.START_KEY) || key.equals(MapEvent.END_KEY)) {
//				//already copied
//			}
//			else if (key.equals(MapEvent.THIS_KEY)) {
//				//ignore to prevent recursions
//			}
//			else if (key.equals(MapEvent.CAUSALITY_KEY)) {
//				if (this.statement.getSelectFunction().getCreateCausality()) {
//					//copy causality
//					Vector<MapEvent> causality = (Vector<MapEvent>) properties.get(key);
//					
//					for (MapEvent ancestor : causality) {
//						event.addCausalAncestor(ancestor);
//					}
//				}
//			}
//			else if (key.equals(MapEvent.CAUSAL_ANCESTOR_1_KEY) || key.equals(MapEvent.CAUSAL_ANCESTOR_2_KEY)) {
//				if (this.statement.getSelectFunction().getCreateCausality()) {
//					//add causal ancestors
////					logger.info("causal ancestor from pattern select found. type: " + properties.get(key).getClass().getName());
//					
//					if (properties.get(key) instanceof HashMap<?, ?>) {
//						MapEvent ancestorEvent = this.parseEventFromMap((Map<String, Object>) properties.get(key));
//						event.addCausalAncestor(ancestorEvent);
//					}
//					else if (properties.get(key) instanceof MapEvent) {
//						event.addCausalAncestor((MapEvent) properties.get(key));
//					}
//					else if (properties.get(key) instanceof MapEventBean) {
//						MapEventBean ancestorBean = (MapEventBean) properties.get(key);
//						event.addCausalAncestor(this.parseEventFromMap(ancestorBean.getProperties()));
//					}
//				}
//			}
//			else {
//				if (key.equals(MapEvent.VALUE_KEY) && (properties.get(key) instanceof MapEventBean)) {
//					/*
//					 * select event with causality should end up here
//					 * -> recursive call
//					 */
//					MapEventBean valueBean = (MapEventBean) properties.get(key);
//					event.put(key, this.parseEventFromMap(valueBean.getProperties()));
//				}
//				else {
//					//fallback / usual: just put it into the result
//					event.put(key, properties.get(key));
//				}
//			}
//		}
////		logger.info("Event parsed from map:\n" + event);
//		return event;
	}


	/**
	 * generates a new MapEvent from the selection of a single value
	 * @param event the selected event bean
	 * @return the new map event
	 */
	private MapEvent createEventFromSimpleSelect(MapEvent event) {
		return this.createEventFromSimpleSelect(event, MapEvent.VALUE_KEY);
	}


	/**
	 * generates a new MapEvent from the selection of a single value
	 * @param event the selected event bean
	 * @param propertyName the name of the only property
	 * @return the new map event
	 */
	private MapEvent createEventFromSimpleSelect(MapEvent event, String propertyName) {
		Date now = new Date();
		
		MapEvent result = null;
		try {
			// no event selected, use only the property 'value'
			Object obj = this.bean.get(propertyName);
			
			if (obj == null) {
				UpdateHandlerThread.logger.info("returning null, no value for property '" + propertyName + "'");
				return null;
			}
			
			//check for timer events
			if (obj.equals(Constants.TIMER_EVENT_VALUE)) {
				//timer event caught
				result = new MapEvent(now.getTime(), now.getTime());
				result.put(MapEvent.VALUE_KEY, now.getTime());
			}
			else {
				//handle object as usual
				result = new MapEvent(now.getTime(), now.getTime());
				
				result.put(MapEvent.VALUE_KEY, obj);
			}
		}
		catch (PropertyAccessException ex) {
			//no event property name value found
			UpdateHandlerThread.logger.warn(ex.getMessage());
		}
		catch (Throwable t) {
			UpdateHandlerThread.logger.warn(t.getMessage());
		}
		return result;
	}
}
