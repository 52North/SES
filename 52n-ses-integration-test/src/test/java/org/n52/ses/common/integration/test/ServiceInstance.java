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
package org.n52.ses.common.integration.test;

import java.net.MalformedURLException;
import java.net.URL;

import org.n52.oxf.ses.adapter.SESServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceInstance {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceInstance.class);
	private static URL host;
	private static SESServiceInstance instance;
	protected boolean serviceTested = false;
	
	static {
		try {
			host = new URL(IntegrationTestConfig.getInstance().getServiceUrl());
			logger.info("using ses host at {}", host);
		} catch (MalformedURLException e) {
			logger.warn(e.getMessage(), e);
		}
		
		instance = new SESServiceInstance(host);
	}
	
	public static SESServiceInstance getInstance() {
		return instance;
	}


}
