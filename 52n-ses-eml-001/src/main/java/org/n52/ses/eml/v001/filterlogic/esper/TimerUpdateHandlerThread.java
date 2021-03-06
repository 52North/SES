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
package org.n52.ses.eml.v001.filterlogic.esper;
///**
// * Part of the diploma thesis of Thomas Everding.
// * @author Thomas Everding
// */
//
//package de.ifgi.lehre.thesisEverding.eml.esper;
//
//import java.util.Date;
//import java.util.Vector;
//
//import com.espertech.esper.client.EventBean;
//
//import de.ifgi.lehre.thesisEverding.eml.event.MapEvent;
//import de.ifgi.lehre.thesisEverding.eml.pattern.Statement;
//
//
///**
// * Handles updates from a {@link TimerListener}.
// * 
// * @author Thomas Everding
// * 
// */
//public class TimerUpdateHandlerThread implements Runnable {
//	
//	private Statement statement;
//	
//	private EventBean bean;
//	
//	private EsperController controller;
//	
//	private String internalEventName;
//	
////	private boolean doOutput;
//	
////	private TimerListener listener;
//	
//	
//	/**
//	 * 
//	 * Constructor
//	 *
//	 * @param controller {@link EsperController} 
//	 * @param statement {@link Statement} of the listener
//	 * @param internalEventName internal name of the event
//	 * @param bean the event update
//	 */
//	public TimerUpdateHandlerThread(EsperController controller, Statement statement, String internalEventName, EventBean bean) {
//		this.bean = bean;
//		this.controller = controller;
//		this.statement = statement;
//		this.internalEventName = internalEventName;
//	}
//	
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public void run() {
//		//event received, publish new event for next timer pattern match
//		Date now = new Date();
//		MapEvent event = new MapEvent(now.getTime(), now.getTime());
//		event.put(MapEvent.VALUE_KEY, now.getTime());
//		
//		//create causality if wanted
//		if (this.statement.getSelectFunction().getCreateCausality()) {
//			MapEvent underlying = (MapEvent) bean.getUnderlying();
//			
//			Vector<MapEvent> underlyingCausality = (Vector<MapEvent>) underlying.get(MapEvent.CAUSALITY_KEY);
//			
//			//add causality of underlying event
//			for (MapEvent e : underlyingCausality) {
//				event.addCausalAncestor(e);
//			}
//			
//			//add underlying event to causality
//			event.addCausalAncestor(underlying);
//		}
//		
//		//publish to make next match possible
//		this.controller.sendEvent(this.internalEventName, event);
//		
////		if (doOutput) {
////			listener.doOutput(event);
////		}
//	}
//	
//}
