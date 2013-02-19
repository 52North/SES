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

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTestConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(IntegrationTestConfig.class);
	private static IntegrationTestConfig instance;
	private Properties config;
	private int consumerPort;
	private int servicePort;
	private String serviceUrl;
	
	private IntegrationTestConfig() {
		readConfig();
	}

	private void readConfig() {
		this.config = new Properties();
		try {
			this.config.load(getClass().getResourceAsStream("integration-test-ports.properties"));
			this.consumerPort = Integer.parseInt(this.config.getProperty("wsn.consumer.port"));
			this.servicePort = Integer.parseInt(this.config.getProperty("ses.instance.port"));
			this.serviceUrl = this.config.getProperty("ses.instance").trim();
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	public static synchronized IntegrationTestConfig getInstance() {
		if (instance == null) {
			instance = new IntegrationTestConfig();
		}
		return instance;
	}

	public int getConsumerPort() {
		return consumerPort;
	}

	public int getServicePort() {
		return servicePort;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}
	
	

}
