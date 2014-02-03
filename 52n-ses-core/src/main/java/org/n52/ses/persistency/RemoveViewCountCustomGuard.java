/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.ses.persistency;

import java.io.IOException;

import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.xmlbeans.XmlException;
import org.n52.epos.pattern.CustomStatementEvent;
import org.n52.epos.rules.Rule;
import org.n52.ses.api.ISESFilePersistence;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RemoveViewCountCustomGuard implements CustomStatementEvent {
	
	private static final String EML_NAMESPACE = "http://www.opengis.net/eml/0.0.1";

	private static final String FES_NAMESPACE = "http://www.opengis.net/fes/2.0";

	private static final String REMOVE_GUARD_XPATH = "declare namespace eml='" +
			EML_NAMESPACE + "'; declare namespace fes='" +
			FES_NAMESPACE + "'; .//eml:SimplePattern/eml:Guard/fes:Filter/fes:PropertyIsEqualTo/fes:ValueReference[text()='VIEW_COUNT']";
	
	private static final Logger logger = LoggerFactory.getLogger(RemoveViewCountCustomGuard.class);

	@Override
	public void eventFired(Object[] newEvents, Rule subMgr) {
		logger.info("Attempting to remove VIEW_COUNT pattern form persistent subscription '{}'.",
				subMgr.getRuleListener().getEndpointReference().toString());
		
		ISESFilePersistence fp = ConfigurationRegistry.getInstance().getFilePersistence();
		
		if (fp != null) {
			try {
				fp.removePattern((EndpointReference) subMgr.getRuleListener().getEndpointReference(),
						REMOVE_GUARD_XPATH);
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
