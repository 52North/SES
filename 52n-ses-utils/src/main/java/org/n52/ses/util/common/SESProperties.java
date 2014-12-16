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
package org.n52.ses.util.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.ses.api.common.GlobalConstants;
import org.n52.ses.util.concurrent.FIFOWorker;
import org.n52.ses.util.concurrent.PredictedTimeoutEstimation.IDWTimeoutEstimation;
import org.n52.ses.util.geometry.ICreateBuffer;
import org.n52.ses.util.postgres.PostGisBuffer;
import org.x52North.sensorweb.ses.config.EventServiceConfigurationDocument;
import org.x52North.sensorweb.ses.config.EventServiceConfigurationType;
import org.x52North.sensorweb.ses.config.EventServiceConfigurationType.Parameters;
import org.x52North.sensorweb.ses.config.EventServiceConfigurationType.Parameters.Parameter;
import org.x52North.sensorweb.ses.config.EventServiceConfigurationType.RegisteredParsers;
import org.x52North.sensorweb.ses.config.EventServiceConfigurationType.RegisteredParsers.Parser;


/**
 * Inherited properties class to define 
 * default values for the SES properties.
 * 
 * @author Thomas Everding
 *
 */
@SuppressWarnings("serial")
public class SESProperties extends Properties {


	private List<String> registeredParsers = new ArrayList<String>();
	private List<String> disabledRegisteredParsers = new ArrayList<String>();

	/**
	 * 
	 * Constructor
	 *
	 */
	public SESProperties() {
		this.initDefaultValues();
	}

	/**
	 * initializes the default values
	 * for the SES properties.
	 */
	private void initDefaultValues() {
		//build instance of the defaults field
		this.defaults = new Properties();
		
		//set default values
		this.defaults.setProperty(ConfigurationRegistry.POSTGRES_PORT_KEY, "default");
		this.defaults.setProperty(ConfigurationRegistry.POSTGRES_USER_KEY, "postgres");
		this.defaults.setProperty(ConfigurationRegistry.POSTGRES_DATABASE, "postgis");
		this.defaults.setProperty(ConfigurationRegistry.POSTGRES_HOST_KEY, "localhost");
		this.defaults.setProperty(ConfigurationRegistry.USED_FILTER_ENGINE, "org.n52.ses.filter.EsperClassProvider");
		this.defaults.setProperty(ConfigurationRegistry.SES_INSTANCE, "http://localhost:8080/ses-main-1.0-SNAPSHOT/services/" + GlobalConstants.NOTIFICATION_PRODUCER_CONTEXT_PATH);
		this.defaults.setProperty(ConfigurationRegistry.RESUBSCRIBE_ON_STARTUP, "true");
		this.defaults.setProperty(ConfigurationRegistry.USE_FOR_GENESIS, "false");
		this.defaults.setProperty(ConfigurationRegistry.USE_FOR_BAW, "false");
		this.defaults.setProperty(ConfigurationRegistry.TIME_TO_WAKEUP, "1000");
		this.defaults.setProperty(ConfigurationRegistry.PARSER, "basic");
		this.defaults.setProperty(ConfigurationRegistry.PRESERVE_GEOMETRY, "false");
		this.defaults.setProperty(ConfigurationRegistry.GENESIS_NAMESPACE, "http://genesis-fp7.eu/5000/demonstration");
		this.defaults.setProperty(ConfigurationRegistry.GENESIS_TOPIC, "gen:GENESIS/Villerest");
		this.defaults.setProperty(ConfigurationRegistry.USE_ENRICHMENT, "true");
		this.defaults.setProperty(ConfigurationRegistry.USE_REQUEST_LOGGER, "false");
		this.defaults.setProperty(ConfigurationRegistry.REQUEST_LOGGER_URL, "");
		this.defaults.setProperty(ConfigurationRegistry.EML_CONTROLLER, ConfigurationRegistry.EML_002_IMPL);
		this.defaults.setProperty(ConfigurationRegistry.MAX_THREADS, "5");
		this.defaults.setProperty(ConfigurationRegistry.CONCURRENT_WORKER, FIFOWorker.class.getName());
		this.defaults.setProperty(ConfigurationRegistry.TIMEOUT_ESTIMATION, IDWTimeoutEstimation.class.getName());
		this.defaults.setProperty(ConfigurationRegistry.CONCURRENT_MAXIMUM_TIMEOUT, "5000");
		this.defaults.setProperty(ConfigurationRegistry.CONCURRENT_MINIMUM_TIMEOUT, "500");
		this.defaults.setProperty(ConfigurationRegistry.USE_CONCURRENT_ORDERED_HANDLING, "false");
		this.defaults.setProperty(ConfigurationRegistry.CONCURRENT_INTELLIGENT_TIMEOUT, "true");
		this.defaults.setProperty(ConfigurationRegistry.VALIDATE_XML, "true");
		this.defaults.setProperty(ConfigurationRegistry.NOTIFY_TIMEOUT, "5000");
		this.defaults.setProperty(ConfigurationRegistry.USE_GZIP, "true");
		this.defaults.setProperty(ConfigurationRegistry.MINIMUM_GZIP_SIZE, "50000");
		this.defaults.setProperty(ConfigurationRegistry.BASIC_AUTH_USER, "");
		this.defaults.setProperty(ConfigurationRegistry.BASIC_AUTH_PASSWORD, "");
		this.defaults.setProperty(ConfigurationRegistry.MAX_PERSISTED_EVENTS, "10");
		this.defaults.setProperty(ICreateBuffer.class.getName(), PostGisBuffer.class.getName());
	}

	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		EventServiceConfigurationDocument doc = null;
		
		try {
			doc = EventServiceConfigurationDocument.Factory.parse(inStream);
			XmlOptions opts = new XmlOptions();
			ArrayList<XmlError> a = new ArrayList<XmlError>();
			opts.setErrorListener(a);
			if (!doc.validate(opts)) {
				throw new IOException("Configuration is not valid: "+a);
			}
		} catch (XmlException e) {
			throw new IOException(e);
		}

		for (Parameter param : doc.getEventServiceConfiguration().getParameters().getParameterArray()) {
			this.setProperty(param.getName(), param.getValue());
		}
		
		for (Parser parser : doc.getEventServiceConfiguration().getRegisteredParsers().getParserArray()) {
			if (parser.getEnabled()) {
				this.registeredParsers.add(parser.getJavaClass());
			} else {
				this.disabledRegisteredParsers.add(parser.getJavaClass());
			}
		}
		
	}
	
	

	@Override
	public void store(Writer writer, String comments) throws IOException {
		EventServiceConfigurationDocument doc = EventServiceConfigurationDocument.Factory.newInstance();
		
		EventServiceConfigurationType config = doc.addNewEventServiceConfiguration();
		Parameters parameters = config.addNewParameters();
		
		for (String p : this.stringPropertyNames()) {
			if (this.defaults.containsKey(p) && this.defaults.getProperty(p).equals(this.getProperty(p))) {
				continue;
			}
			
			Parameter param = parameters.addNewParameter();
			param.setName(p);
			param.setValue(getProperty(p));
		}
		
		RegisteredParsers parsers = config.addNewRegisteredParsers();
		
		for (String p : this.registeredParsers) {
			Parser parser = parsers.addNewParser();
			parser.setEnabled(true);
			parser.setJavaClass(p);
		}
		
		for (String p : this.disabledRegisteredParsers) {
			Parser parser = parsers.addNewParser();
			parser.setEnabled(false);
			parser.setJavaClass(p);
		}
		
		XmlOptions opts = new XmlOptions();
		opts.setSavePrettyPrint();
		
		doc.save(writer, opts);
		writer.flush();
		writer.close();
	}

	public List<String> getRegisteredParsers() {
		return this.registeredParsers;
	}
	
	
}
