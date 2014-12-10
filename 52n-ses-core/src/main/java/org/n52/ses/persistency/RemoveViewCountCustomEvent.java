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

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.n52.ses.api.ISESFilePersistence;
import org.n52.ses.api.common.CustomStatementEvent;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;

public class RemoveViewCountCustomEvent implements CustomStatementEvent {
	
	private static final String EML_NAMESPACE = "http://www.opengis.net/eml/0.0.1";

	private static final String FES_NAMESPACE = "http://www.opengis.net/fes/2.0";

	private static final String REMOVE_GUARD_XPATH = "declare namespace eml='" +
			EML_NAMESPACE + "'; declare namespace fes='" +
			FES_NAMESPACE + "'; .//eml:SimplePattern/eml:Guard/fes:Filter/fes:PropertyIsEqualTo/fes:ValueReference[text()='VIEW_COUNT']";
	
	private static final Logger logger = LoggerFactory.getLogger(RemoveViewCountCustomEvent.class);

	@Override
	public void eventFired(EventBean[] newEvents, ISubscriptionManager subMgr) {
		logger.info("Attempting to remove VIEW_COUNT pattern form persistent subscription '{}'.", subMgr.getEndpointReference().toString());
		
		ISESFilePersistence fp = ConfigurationRegistry.getInstance().getFilePersistence();
		
		if (fp != null) {
			try {
				fp.removePattern(subMgr.getEndpointReference(), REMOVE_GUARD_XPATH);
			} catch (XmlException e) {
				logger.warn(e.getMessage(), e);
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			} catch (RuntimeException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean bindsToEvent(String eventIdentifier) {
		return eventIdentifier.equals(REMOVE_VIEW_COUNT_EVENT);
	}

}
