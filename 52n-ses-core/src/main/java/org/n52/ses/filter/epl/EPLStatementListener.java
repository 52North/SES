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
package org.n52.ses.filter.epl;

import java.util.Map;

import org.n52.ses.api.event.MapEvent;
import org.n52.ses.filter.epl.EPLFilterImpl.EPLFilterInstance;

import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.map.MapEventBean;

public class EPLStatementListener implements UpdateListener {

	private boolean doOutput;
	private String newEventName;
	private EPLFilterController controller;

	public EPLStatementListener(EPLFilterInstance eplStmt, EPLFilterController controller) {
		this.controller = controller;
		this.doOutput = eplStmt.isDoOutput();
		this.newEventName = eplStmt.getNewEventName();
	}

	@Override
	public void update(EventBean[] arg0, EventBean[] arg1) {
		for (EventBean eventBean : arg0) {
			
			if (eventBean instanceof MapEventBean) {
				MapEvent event = null;
				if (eventBean.getUnderlying() instanceof Map<?, ?>) {
					Map<?, ?> alert = (Map<?, ?>) eventBean.getUnderlying();
					
					Object start = alert.get(MapEvent.START_KEY);
					Object end = alert.get(MapEvent.END_KEY);
					
					if (start instanceof Long && end instanceof Long) {
						event = new MapEvent((Long) start, (Long) end);	
					}
					else continue;
					
					Object message = alert.get(MapEvent.ORIGNIAL_MESSAGE_KEY);
					if (message != null) {
						event.put(MapEvent.ORIGNIAL_MESSAGE_KEY, message);
						
						if (this.doOutput) {
							this.controller.getSubMgr().publish(event.getOriginalMessage());
						}
					}
					
					if (this.newEventName != null && !this.newEventName.equals("")) {
						for (Object key : alert.keySet()) {
							if (!key.equals(MapEvent.ORIGNIAL_MESSAGE_KEY)
									|| !key.equals(MapEvent.THIS_KEY)) {
								event.put(key.toString(), alert.get(key));
							}
						}
						
						event.put(MapEvent.THIS_KEY, event);
						
						this.controller.sendEvent(this.newEventName, event);
					}
				}
				
			}
			
		}
		
	}

}
