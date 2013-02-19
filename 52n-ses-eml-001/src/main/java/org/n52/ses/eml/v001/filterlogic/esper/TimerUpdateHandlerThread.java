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
