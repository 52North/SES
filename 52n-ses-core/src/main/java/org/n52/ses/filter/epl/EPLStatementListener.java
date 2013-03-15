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
