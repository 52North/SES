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
package org.n52.ses.util.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.muse.core.Environment;
import org.apache.muse.ws.addressing.EndpointReference;
import org.n52.oxf.xmlbeans.parser.GMLAbstractFeatureCase;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.ses.api.IFilterEngine;
import org.n52.ses.api.ISESFilePersistence;
import org.n52.ses.api.common.FreeResourceListener;
import org.n52.ses.api.common.GlobalConstants;
import org.n52.ses.api.ws.IPublisherEndpoint;
import org.n52.ses.api.ws.IRegisterPublisher;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * Use this class to get global functions and parameters.
 * 
 * Please use a public static final String for each key.
 * 
 * If there is a default value for a key add it to the SESProperties class.
 * 
 * If you want that your key to be easily visible, also add it in the
 * ses_config.properties file (src/main/resources/sesconfig/).
 *
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class ConfigurationRegistry {
	
	/**
	 * Key for the postgres port
	 */
	public static final String POSTGRES_PORT_KEY = "POSTGRES_PORT";
	
	/**
	 * Key for the postgres user
	 */
	public static final String POSTGRES_USER_KEY = "POSTGRES_USER";
	
	/**
	 * Key for the postgres password
	 */
	public static final String POSTGRES_PWD_KEY = "POSTGRES_PWD";
	
	/**
	 * the host of the database
	 */
	public static final String POSTGRES_HOST_KEY = "POSTGRES_HOST";
	
	/**
	 * Key for the postgres database
	 */
	public static final String POSTGRES_DATABASE = "POSTGRES_DATABASE";
	
	/**
	 * Key for the filter engine in use
	 */
	public static final String USED_FILTER_ENGINE = "USED_FILTER_ENGINE";

	
	/**
	 * Key for the re-subscribe setting
	 * 
	 * Allowed values are 'yes' and 'no'
	 */
	public static final String RESUBSCRIBE_ON_STARTUP = "RESUBSCRIBE_ON_STARTUP";
	
	/**
	 * Key for the path to the SES instance
	 */
	public static final String SES_INSTANCE = "SES_INSTANCE";
	
	/**
	 * Key for the indication if the instance is used for GENESIS.
	 * 
	 * This has implications on the encoding of the output.
	 * 
	 * Allowed values are <code>true</code> and <code>false</code>
	 */
	public static final String USE_FOR_GENESIS = "GENESIS";
	
	/**
	 * The namespace used for Genesis Topics
	 */
	public static final String GENESIS_NAMESPACE = "GENESIS_NAMESPACE";
	
	/**
	 * the concrete topic expression used for Genesis topics
	 * (must include the prefix)
	 */
	public static final String GENESIS_TOPIC = "GENESIS_TOPIC";
	
	/**
	 * milliseconds to wait before performing the startup procedures
	 */
	public static final String TIME_TO_WAKEUP = "TIME_TO_WAKEUP";
	
	/**
	 * Indicates which parser to use for incoming notifications
	 * 
	 * Allowed values are "basic"(default) and "generic"
	 */
	public static final String PARSER = "PARSER";
	
	/**
	 * Instructs the SES to preserve the geometry information
	 * when selecting only a property and not a complete
	 * event. This will only be done for select functions
	 * that act only on one event (e.g. SelectProperty but 
	 * not SelectSum). Will also only be done on simple patterns.
	 * 
	 * ATTENTION: this may cause errors as some events might 
	 * not contain a geometry!
	 * 
	 * Allowed values are <code>false</code> (default) and <code>true</code>.
	 */
	public static final String PRESERVE_GEOMETRY = "PRESERVE_GEOMETRY";

	/**
	 * Key for the indication if the instance is used for BAW.
	 * 
	 * This has implications on the encoding of some select functions.
	 * 
	 * Allowed values are <code>true</code> and <code>false</code>
	 */
	public static final String USE_FOR_BAW = "BAW";

	/**
	 * the property for the enrichment flag.
	 */
	public static final String USE_ENRICHMENT = "USE_ENRICHMENT";

	/**
	 * property for using the external request logging webapp
	 */
	public static final String USE_REQUEST_LOGGER = "USE_REQUEST_LOGGER";

	/**
	 * the url for the logging webapp
	 */
	public static final String REQUEST_LOGGER_URL = "REQUEST_LOGGER_URL";

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationRegistry.class);

	
	/**
	 * the key for the EsperController fqcn
	 */
	public static final String EML_CONTROLLER = "EML_CONTROLLER";

	public static final String EML_001_IMPL = "org.n52.ses.eml.v001.filterlogic.esper.EsperController";
	public static final String EML_002_IMPL = "org.n52.ses.eml.v002.filterlogic.esper.EsperController";
	
	/**
	 * the key for maximum number of threads per threadpool
	 */
	public static final String MAX_THREADS = "MAX_THREADS";

	
	/**
	 * th key for the {@link IConcurrentNotificationHandler} impl
	 */
	public static final String CONCURRENT_WORKER = "CONCURRENT_WORKER";

	
	/**
	 * the initial timeout at setup time used in the {@link IConcurrentNotificationHandler}
	 * for waiting for processing of elements has been finished.
	 */
	public static final String CONCURRENT_MAXIMUM_TIMEOUT = "CONCURRENT_MAXIMUM_TIMEOUT";

	/**
	 * the timeout which will never be undercutted in the {@link IConcurrentNotificationHandler}
	 * for waiting for processing of elements has been finished.
	 */
	public static final String CONCURRENT_MINIMUM_TIMEOUT = "CONCURRENT_MINIMUM_TIMEOUT";

	/**
	 * the key for enabling the concurrent fifo worker queue. this should be activated
	 * in environments where order of messages is a requirment for filter matching (e.g. EML
	 * causel patterns)
	 */
	public static final String USE_CONCURRENT_ORDERED_HANDLING = "USE_CONCURRENT_ORDERED_HANDLING";
	
	/**
	 * key for enabling the use of intelligent timeout estimation
	 */
	public static final String CONCURRENT_INTELLIGENT_TIMEOUT = "CONCURRENT_INTELLIGENT_TIMEOUT";

	/**
	 * the key for the {@link ITimeoutEstimation} implementation to be used.
	 */
	public static final String TIMEOUT_ESTIMATION = "TIMEOUT_ESTIMATION";
	
	/**
	 * key for enalbing xml validation
	 */
	public static final String VALIDATE_XML = "VALIDATE_XML";

	/**
	 * key for the timeout when sending notification to consumers
	 */
	public static final String NOTIFY_TIMEOUT = "NOTIFY_TIMEOUT";
	
	/**
	 * the location of the config file (relative to WEB-INF/classes)
	 */
	public static final String CONFIG_FILE = "sesconfig/ses_config.xml";

	/**
	 * use gzip compression for outgoing requests?
	 */
	public static final String USE_GZIP = "USE_GZIP";

	
	/**
	 * minimum size in bytes to enable gzip
	 */
	public static final String MINIMUM_GZIP_SIZE = "MINIMUM_GZIP_SIZE";

	/**
	 * used if the service instance is secured with HTTP basic authentication
	 */
	public static final String BASIC_AUTH_USER = "BASIC_AUTH_USER";
	public static final String BASIC_AUTH_PASSWORD = "BASIC_AUTH_PASSWORD";

	
	private static ConfigurationRegistry _instance;
	private SESProperties parameters;
	private EndpointReference sesPortTypeEPR;
	private List<ISubscriptionManager> reresubs;
	private List<IPublisherEndpoint> rerepubs;

	private ISESFilePersistence filePersistence;
	private IFilterEngine filterEngine;

	private IRegisterPublisher registerPublisher;

	private Environment environment;

	private String subMgrWsdl;

	private boolean persistencyEnabled;

	private List<FreeResourceListener> freeResourceListeners = new ArrayList<FreeResourceListener>();

	private File configFile;
	
	/**
	 * @param config InputStream of config file.
	 * @param defaultURI the default URI of the service
	 * @param unitConverter 
	 */
	private ConfigurationRegistry(InputStream config, String defaultURI) {
		try {
			this.sesPortTypeEPR = new EndpointReference(new URI("http://localhost/URIfailure"));
			
			if (defaultURI != null) {
				this.sesPortTypeEPR = new EndpointReference(new URI(defaultURI));
			}
		} catch (URISyntaxException e1) {
			logger.warn(e1.getMessage(), e1);
		}
		
		/*
		 * init parameters
		 */
		this.parameters = new SESProperties();
		try {
			this.parameters.load(config);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
		
		/*
		 * init filter engine
		 */
		IFilterEngine filterEngine = null;
		ServiceLoader<IFilterEngine> loader = ServiceLoader.load(IFilterEngine.class);
		for (IFilterEngine iFilterEngine : loader) {
			filterEngine = iFilterEngine;
			break;
		}
		this.filterEngine = filterEngine;

		/*
		 * register lax validation cases 
		 */
		XMLBeansParser.registerLaxValidationCase(GMLAbstractFeatureCase.getInstance());
		
		/*
		 * Check if we have globally deactivated validation.
		 * This could be useful in predictable environments where
		 * every request is sort of the same.
		 */
		XMLBeansParser.setValidationGloballyEnabled(Boolean.parseBoolean(
				this.parameters.getProperty(VALIDATE_XML)));
		
		this.reresubs = new ArrayList<ISubscriptionManager>();
		this.rerepubs = new ArrayList<IPublisherEndpoint>();
		this.persistencyEnabled = Boolean.parseBoolean(this.parameters.getProperty(RESUBSCRIBE_ON_STARTUP));
	}
	
	/**
	 * @return The singleton instance of the {@link ConfigurationRegistry}
	 */
	public static synchronized ConfigurationRegistry getInstance() {
		while (_instance == null) {
			try {
				logger.info("Thread {} is waiting for the instance...", Thread.currentThread().getName());
				ConfigurationRegistry.class.wait(5000);
			} catch (InterruptedException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		
		return _instance;
	}
	
	/**
	 * Initialize the ConfigurationRegistry (with properties file, Logging
	 * and default environment-URI (http://xyz:123/ses-webapp/Broker)
	 * 
	 * @param config InputStream for properties file
	 * @param env the muse environment
	 * @param defaultURI Default muse-environment URI
	 * @param unitConverter converter for units of measurement
	 */
	public static synchronized void init(Environment env) {
		InputStream config = env.getDataResourceStream(ConfigurationRegistry.CONFIG_FILE);
		init(config, env);
	}
	
	public static synchronized void init(InputStream config, Environment env) {
		if (_instance == null) {
			if (logger.isInfoEnabled()) {
				logger.info("initializing config from file {}...",
						env.getDataResource(ConfigurationRegistry.CONFIG_FILE));
			}
			
			_instance = new ConfigurationRegistry(config, env == null ? "" : env.getDefaultURI());
			_instance.setEnvironment(env);
			ConfigurationRegistry.class.notifyAll();
		}
	}
	

	/**
	 * @param env the muse environment
	 */
	private void setEnvironment(Environment env) {
		this.environment = env;
	}

	/**
	 * @return the muse environment
	 */
	public Environment getEnvironment() {
		return this.environment;
	}

	/**
	 * Returns a configuration value.
	 * 
	 * @param key the parameter
	 * @return the corresponding value
	 */
	public String getPropertyForKey(String key) {
		if (this.parameters != null) {
			return this.parameters.getProperty(key);
		}
		
		return null;
	}
	

	/**
	 * @return the default service endpoint.
	 */
	public EndpointReference getSesPortTypeEPR() {
		return this.sesPortTypeEPR;
	}


	/**
	 * adds a registered subscription manager
	 * @param sesSubscriptionManager the subscription manager
	 */
	public synchronized void addReregisteredSubMgr(ISubscriptionManager sesSubscriptionManager) {
		synchronized (this.reresubs) {
			this.reresubs.add(sesSubscriptionManager);
			this.reresubs.notifyAll();
		}
		
	}
	
	/**
	 * adds a registered publisher endpoint
	 * @param repub the publishers
	 */
	public void addReregisteredPublisher(IPublisherEndpoint repub) {
		synchronized (this.rerepubs) {
			this.rerepubs.add(repub);
			this.rerepubs.notifyAll();
		}
	}

	/**
	 * 
	 * @return the registered subscription managers
	 */
	public synchronized List<ISubscriptionManager> getReresubs() {
		return this.reresubs;
	}
	
	
	
	/**
	 * @return the registered publisher endpoints
	 */
	public List<IPublisherEndpoint> getRerepubs() {
		return rerepubs;
	}

	/**
	 * This methods blocks the calling thread until
	 * all persistent Publisher instances are active.
	 */
	public void waitForAllPersistentPublishers() {
		synchronized (this.rerepubs) {
			while (this.filePersistence == null || this.rerepubs.size() != this.filePersistence.getPersistentPublisherCount()) {
				try {
					this.rerepubs.wait();
				} catch (InterruptedException e) {
					logger.warn(e.getMessage(), e);
				}
			}	
		}
			
	}
	
	public void setFilePersistence(ISESFilePersistence fp) {
		this.filePersistence = fp;
		synchronized (this.rerepubs) {
			this.rerepubs.notifyAll();
		}
	}

	public ISESFilePersistence getFilePersistence() {
		return filePersistence;
	}

	/**
	 * Registers the {@link IFilterEngine}
	 * 
	 * @param filterEngine instance of implementing class
	 */
	public void setFilterEngine(IFilterEngine filterEngine) {
		this.filterEngine = filterEngine;
	}
	
	/**
	 * @return the instance of the {@link IFilterEngine}
	 */
	public IFilterEngine getFilterEngine() {
		return this.filterEngine;
	}

	/**
	 * @param registerpublisher the global one
	 */
	public void setGlobalRegisterPublisher(IRegisterPublisher registerpublisher) {
		this.registerPublisher = registerpublisher;
	}

	/**
	 * @return the global one
	 */
	public IRegisterPublisher getGlobalRegisterPublisher() {
		return this.registerPublisher;
	}

	/**
	 * Method for initiating graceful shutdown of the SES
	 */
	public void shutdown() {
		this.filterEngine.shutdown();
		for (FreeResourceListener frl : this.freeResourceListeners) {
			frl.freeResources();
		}
	}

	/**
	 * @param string the global URL for the submanager wsdl
	 */
	public void setSubscriptionManagerWsdl(String string) {
		this.subMgrWsdl = string;
	}

	/**
	 * @return the global URL for the submanager wsdl
	 */
	public String getSubMgrWsdl() {
		if (this.subMgrWsdl == null) {
			String tmp = getEnvironment().getDefaultURI().substring(0,
					getEnvironment().getDefaultURI().lastIndexOf("/services"));
			String subMgrUrl = tmp + "/services/" + GlobalConstants.SUBSCRIPTION_MANAGER_CONTEXT_PATH + "?wsdl";
			return subMgrUrl;
		}
		return this.subMgrWsdl;
	}

	public boolean persistencyEnabled() {
		return this.persistencyEnabled;
	}

	/**
	 * @param frl the listener to add. freeResource() gets called during shutdown.
	 */
	public void registerFreeResourceListener(
			FreeResourceListener frl) {
		this.freeResourceListeners.add(frl);
	}

	public SESProperties getProperties() {
		return this.parameters;
	}

	public void setPropertyForKey(String key, String value) {
		this.parameters.put(key, value);
	}

	public void saveConfiguration() throws IOException {
		FileWriter fw = new FileWriter(this.configFile);
		this.parameters.store(fw, null);
	}

	public List<String> getRegisteredParserClasses() {
		return this.parameters.getRegisteredParsers();
	}


}
