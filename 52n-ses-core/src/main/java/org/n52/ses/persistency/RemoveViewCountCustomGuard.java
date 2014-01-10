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
package org.n52.ses.persistency;

import org.n52.ses.api.ISESFilePersistence;
import org.n52.ses.api.common.CustomStatementEvent;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;

public class RemoveViewCountCustomGuard implements CustomStatementEvent {
	
	private static final Logger logger = LoggerFactory.getLogger(RemoveViewCountCustomGuard.class);

	@Override
	public void eventFired(EventBean[] newEvents, ISubscriptionManager subMgr) {
		logger.info("Attempting to remove VIEW_COUNT pattern form persistent subscription '{}'.", subMgr.getEndpointReference().toString());
		
		ISESFilePersistence fp = ConfigurationRegistry.getInstance().getFilePersistence();
		
		if (fp != null) {
			fp.removePattern(subMgr.getEndpointReference(), 
					"//eml:SimplePattern/eml:Guard/fes:Filter/fes:PropertyIsEqualTo/fes:ValueReference[@text='VIEW_COUNT']");
		}
	}

	@Override
	public boolean bindsToEvent(String eventIdentifier) {
		return eventIdentifier.equals(REMOVE_VIEW_COUNT_EVENT);
	}

}
