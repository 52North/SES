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
package org.n52.ses.common.integration.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

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
		this.config = new UnescapedProperties();
		try {
			this.config.load(getClass().getResourceAsStream("integration-test-ports.properties"));
			this.consumerPort = Integer.parseInt(this.config.getProperty("wsn.consumer.port"));
			this.servicePort = Integer.parseInt(this.config.getProperty("ses.instance.port"));
			this.serviceUrl = this.config.getProperty("ses.instance").trim();
			this.localWebappDirectory = this.config.getProperty("local.webapp.directory").trim();
			logger.info("Local webapp dir: {}", this.localWebappDirectory);
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
	
	private static class UnescapedProperties extends Properties {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void load(InputStream fis) throws IOException {
	        Scanner in = new Scanner(fis);
	        ByteArrayOutputStream out = new ByteArrayOutputStream();

	        String line;
	        while(in.hasNext()) {
	        	line = in.nextLine();
	            out.write(line.replace("\\","\\\\").getBytes());
	            out.write("\n".getBytes());
	        }
	        in.close();

	        InputStream is = new ByteArrayInputStream(out.toByteArray());
	        super.load(is);
	    }
	}
	

}
