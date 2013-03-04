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
package org.n52.ses.common;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import javax.xml.namespace.QName;


import net.opengis.ses.x00.CapabilitiesDocument;
import net.opengis.ses.x00.DescribeSensorDocument;
import net.opengis.ses.x00.ContentsDocument.Contents;
import net.opengis.ses.x00.ContentsDocument.Contents.RegisteredSensors;
import net.opengis.esSf.x00.DescribeStoredFilterDocument;
import net.opengis.esSf.x00.DescribeStoredFilterResponseDocument;
import net.opengis.esSf.x00.DescribeStoredFilterResponseDocument.DescribeStoredFilterResponse;
import net.opengis.esSf.x00.ListStoredFiltersResponseDocument;
import net.opengis.esSf.x00.ListStoredFiltersResponseDocument.ListStoredFiltersResponse;
import net.opengis.esSf.x00.StoredFilterDescriptionType;

import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.resource.impl.AbstractWsResourceCapability;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.parser.XMLHandlingException;
import org.n52.ses.api.common.WsbrConstants;
import org.n52.ses.api.ws.IPublisherEndpoint;
import org.n52.ses.storedfilters.StoredFilterInstance;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.wsbr.RegisterPublisher;
import org.n52.ses.wsn.SESSubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 * @author Matthes Rieke <m.rieke@uni-muenster.de
 * 
 * 
 * This capability informs about what this service provides. Available sensors
 * are listed and can be further described.
 */
public class Capabilites extends AbstractWsResourceCapability implements ICapabilites {
	
	private static final Logger logger = LoggerFactory.getLogger(Capabilites.class);
	
	public static final QName CAPABILITIES_QNAME = new QName("http://www.opengis.net/ses/0.0", "Capabilities");
	
	private static final String CAPABILITIES_FILE = "/sesconfig/capabilites_base.xml";
	private static final String CAPABILITIES_FILE_FILTER = "/sesconfig/capabilites_filter.xml-fragment";
	private static final String CAPABILITIES_FILE_EML = "/sesconfig/capabilites_eml.xml-fragment";
	private RegisterPublisher registerpublisher;
	private CapabilitiesDocument capabilities;


	@Override
	public void initialize() throws SoapFault {
		super.initialize();

		/*
		 * this is needed because we do not want the default (ReflectionMessageHandler) to take care
		 * of the XML stuff...
		 */
		setMessageHandler(new SimpleHandler("http://www.opengis.net/ses/GetCapabilitiesRequest",null,this,"getCapabilities"));
		setMessageHandler(new SimpleHandler("http://www.opengis.net/ses/DescribeSensorRequest",null,this,"describeSensor"));
		setMessageHandler(new SimpleHandler("http://www.opengis.net/es-sf/DescribeStoredFilterRequest",null,this,"describeStoredFilter"));
		setMessageHandler(new SimpleHandler("http://www.opengis.net/es-sf/ListStoredFiltersRequest",null,this,"listStoredFilters"));

		this.registerpublisher = getRegisterPublisher();
		ConfigurationRegistry.getInstance().setGlobalRegisterPublisher(this.registerpublisher);
		this.capabilities = getCapabilityTemplate();

	}


	/**
	 * Checks if the capabilities template exists
	 * 
	 * @return the template or a new instance of CapabilitesDocument
	 * @throws
	 */
	private CapabilitiesDocument getCapabilityTemplate() throws SoapFault {
		CapabilitiesDocument capabilites;

		String capString = null;
		try {
			capString = readCapabilityFiles();
		} catch (IOException e1) {
			logger.warn(e1.getMessage(), e1);
		}

		/* check if a template exists */
		if (capString == null) {
			capabilites = CapabilitiesDocument.Factory.newInstance();
			capabilites.addNewCapabilities();

			capabilites.getCapabilities().setVersion("0.0.0");
		} else {
			try {
				capabilites = (CapabilitiesDocument) XMLBeansParser.parse(capString);
			} catch (XMLHandlingException e) {
				throw new SoapFault("The" +
						" capabilites template is invalid! The error is:" + e.getMessage());
			} catch (ClassCastException e) {
				throw new SoapFault("The capabilites" +
						" template is invalid! It doesn't seem to be a valid" +
				" Capabilites document.");
			}
		}

		return capabilites;
	}


	private String readCapabilityFiles() throws IOException {
		/*
		 * read base file
		 */
		InputStream stream = getClass().getResourceAsStream(CAPABILITIES_FILE);

		String lineSep = System.getProperty("line.separator");
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		String nextLine = "";
		StringBuffer sb = new StringBuffer();
		while ((nextLine = br.readLine()) != null) {
			sb.append(nextLine);
			sb.append(lineSep);
		}

		String defaulturi = getEnvironment().getDefaultURI().substring(0,
				getEnvironment().getDefaultURI().lastIndexOf("/services"));
		String subMgrUrl = defaulturi + "/services/" + SESSubscriptionManager.CONTEXT_PATH;
		String prmUrl = defaulturi + "/services/" + RegisterPublisher.RESOURCE_TYPE;
		
		String baseString = sb.toString().replace("${broker}", getEnvironment().getDeploymentEPR().getAddress().toString());
		baseString = baseString.replace("${subMgr}", subMgrUrl);
		baseString = baseString.replace("${pubRegMgr}", prmUrl);
		
		/*
		 * read filter file
		 */
		stream = getClass().getResourceAsStream(CAPABILITIES_FILE_FILTER);

		lineSep = System.getProperty("line.separator");
		br = new BufferedReader(new InputStreamReader(stream));
		nextLine = "";
		sb = new StringBuffer();
		while ((nextLine = br.readLine()) != null) {
			sb.append(nextLine);
			sb.append(lineSep);
		}
		
		baseString = baseString.replace("<!-- Filter_Capabilities here -->", sb.toString());
		
		/*
		 * read eml file
		 */
		stream = getClass().getResourceAsStream(CAPABILITIES_FILE_EML);

		lineSep = System.getProperty("line.separator");
		br = new BufferedReader(new InputStreamReader(stream));
		nextLine = "";
		sb = new StringBuffer();
		while ((nextLine = br.readLine()) != null) {
			sb.append(nextLine);
			sb.append(lineSep);
		}
		
		String emlStr = sb.toString();
		
		if (ConfigurationRegistry.getInstance().getPropertyForKey(ConfigurationRegistry.EML_CONTROLLER).equals(
				ConfigurationRegistry.EML_002_IMPL)) {
			emlStr = emlStr.replace("http://www.opengis.net/eml/0.0.1", "http://www.opengis.net/eml/0.0.2");
			emlStr = emlStr.replace("OGC-EML-0_0_1", "OGC-EML-0_0_2");
		}
		
		baseString = baseString.replace("<!-- EML_Capabilities here -->", emlStr);
		
		return baseString;
	}


	/**
	 * looks up the <em>first</em> {@link RegisterPublisher} instance
	 * 
	 * @return its reference
	 * @throws SoapFault the {@link RegisterPublisher} endpoint instance
	 */
	protected RegisterPublisher getRegisterPublisher() throws SoapFault {
		try {
			final RegisterPublisher wsn = (RegisterPublisher) getResource().getCapability(WsbrConstants.NAMESPACE_URI);

			if (wsn == null) {
				String all = getResource().getCapabilityURIs().toString();
				throw new SoapFault("Cannot find the" +
						" RegisterPublisher instance. " +
						"Known instances are: " + all);
			}
			return wsn;
		} catch (ClassCastException e) {
			throw new SoapFault("Internal Server error." +
					" Expected " + WsbrConstants.NAMESPACE_URI + " to be handled by "
					+ RegisterPublisher.class.getCanonicalName());
		}
	}

	/**
	 * Reflections-method for describing a sensor.
	 * @param o xml-text possible.
	 * @return DescribeSensor response
	 * @throws Exception if Sensor was not found inside the service.
	 */
	public Element describeSensorReflect(Object[] o) throws Exception {
		return describeSensor((Element) o[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.ses.common.ICapabilites#describeSensor(org.w3c.dom.Element)
	 */
	@Override
	public Element describeSensor(Element sensor_element) throws Exception {

		XmlObject xmlobject = XMLBeansParser.parse(sensor_element, true);
		String sensorID;

		if(xmlobject instanceof DescribeSensorDocument) {
			sensorID=((DescribeSensorDocument)xmlobject).getDescribeSensor().getSensorID();
		} else {
			throw new Exception(
					sensor_element.toString() + " is not a valid sensor identifier");
		}


		Collection<IPublisherEndpoint> endpoints = this.registerpublisher.getPublisherEndpoints();

		/*
		 * iterate over all endpoints. Could be made faster with either caching
		 * or hashes.
		 */
		for (IPublisherEndpoint publisherEndpoint : endpoints) {
			if (publisherEndpoint.getSensorId().equals(sensorID)) {
				return publisherEndpoint.getSensorML();
			}
		}

		throw new Exception("Sensor " +
				sensorID + " does not exist. Please check the capabilites or report an error");
	}


	/* (non-Javadoc)
	 * @see org.n52.ses.common.ICapabilites#describeStoredFilter(org.w3c.dom.Element)
	 */
	public Element describeStoredFilter(Element describeStoredFilter)
			throws Exception {
		XmlObject xmlobject = XMLBeansParser.parse(describeStoredFilter, true);
		String id;

		if (xmlobject instanceof DescribeStoredFilterDocument) {
			id = ((DescribeStoredFilterDocument) xmlobject).getDescribeStoredFilter().getStoredFilterID();
		} else {
			throw new Exception(
					describeStoredFilter.toString() + " is not a valid stored filter identifier");
		}
		
		StoredFilterInstance instance = StoredFilterInstance.getByID(id);
		
		DescribeStoredFilterResponseDocument resultDoc = DescribeStoredFilterResponseDocument.Factory.newInstance();
		DescribeStoredFilterResponse resp = resultDoc.addNewDescribeStoredFilterResponse();
		resp.addNewStoredFilterDescription();
		resp.set(StoredFilterDescriptionType.Factory.parse(instance.getStoredFilterDescription()));
		
		return (Element) resp.getDomNode();
	}
	
	public Element listStoredFilters(Element listStoredFilters) throws Exception{
		ListStoredFiltersResponseDocument resp = ListStoredFiltersResponseDocument.Factory.newInstance();
		ListStoredFiltersResponse sfr = resp.addNewListStoredFiltersResponse();
		
		for (StoredFilterInstance inst : StoredFilterInstance.getAvailableInstances()) {
			sfr.addStoredFilterID(inst.getId());			
		}
		
		return (Element) sfr.getDomNode();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.ses.common.ICapabilites#getCapabilities()
	 */
	@Override
	public Element getCapabilities(Element xml) throws Exception {
		logger.info("creating capabilites");
		CapabilitiesDocument temp = (CapabilitiesDocument) this.capabilities.copy();

		/* add content section if missing */
		Contents content = temp.getCapabilities().getContents();
		if (content == null) {
			content = temp.getCapabilities().addNewContents();
		}

		/* read available sensors */
		Collection<IPublisherEndpoint> endpoints = this.registerpublisher.getPublisherEndpoints();
		if (endpoints.size() > 0) {
			RegisteredSensors sensors = content.addNewRegisteredSensors();

			for (IPublisherEndpoint publisherEndpoint : endpoints) {
				String sensor = publisherEndpoint.getSensorId();
				sensors.addSensorID(sensor);
			}
		}

		try {

			XMLBeansParser.strictValidate(temp);
			logger.info("capabilites are valid!");
		} catch (XMLHandlingException e) {
			throw new Exception("Internal" +
					" server error: capabilities are not valid: " + e.getMessage());
		}



		Node node = temp.getCapabilities().getDomNode();
		if(node instanceof Element) {
			logger.info("returning capabilites");
			return (Element) temp.getCapabilities().getDomNode();
		}
		throw new Exception("Expected "
				+ node.toString() + " to be an org.w3c.dom.Element, but it is " 
				+ node.getClass().getCanonicalName());
	}


}
