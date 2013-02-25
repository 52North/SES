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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.n52.ses.util.common.SESProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTestConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(IntegrationTestConfig.class);
	private static IntegrationTestConfig instance;
	private Properties config;
	private int consumerPort;
	private int servicePort;
	private String serviceUrl;
	private String localWebappDirectory;
	private SESProperties properties;
	private int notificationTimeout;
	
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
			this.localWebappDirectory = this.config.getProperty("local.webapp.directory").trim();
			this.notificationTimeout = Integer.parseInt(this.config.getProperty("notification.timeout"));
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

	public String getLocalWebappDirectory() {
		return localWebappDirectory;
	}
	
	public String getConfigPropertyOfDeployedService(String key) throws IOException {
		SESProperties config = getConfiguration();
		return config.getProperty(key);
	}
	
	
	private synchronized SESProperties getConfiguration() throws IOException {
		if (this.properties == null) {
			loadProperties();
		}
		
		
		return this.properties;
	}

	private void loadProperties() throws IOException {
		this.properties = new SESProperties();
		this.properties.load(findDeployedConfigurationFile());		
	}

	
	private InputStream findDeployedConfigurationFile() throws FileNotFoundException {
		String webappDirectoryString = getLocalWebappDirectory();
		File webappDirectory = new File(webappDirectoryString);
		if (!webappDirectory.exists()) throw new IllegalStateException("Local webapp directory not accesible");
		
		File configFile = new File(webappDirectory, "WEB-INF/classes/sesconfig/ses_config.xml");
		
		if (!configFile.exists()) throw new IllegalStateException("Config not accesible");
		
		return new FileInputStream(configFile);
	}

	public int getNotificationTimeout() {
		return this.notificationTimeout;
	}
	

}
