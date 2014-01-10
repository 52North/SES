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
package org.n52.ses.wsn;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.namespace.QName;

import org.apache.muse.core.ResourceManager;
import org.apache.muse.core.SimpleResourceManager;
import org.apache.muse.core.descriptor.ResourceDefinition;
import org.apache.muse.core.routing.ResourceIdFactory;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.WsaConstants;
import org.apache.muse.ws.addressing.soap.SimpleSoapClient;
import org.apache.muse.ws.addressing.soap.SoapClient;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.Policy;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.notification.impl.FilterCollection;
import org.apache.muse.ws.notification.impl.FilterFactory;
import org.apache.muse.ws.notification.impl.MessagePatternFilterHandler;
import org.apache.muse.ws.notification.impl.SimpleSubscriptionManager;
import org.apache.muse.ws.resource.lifetime.ScheduledTermination;
import org.apache.muse.ws.resource.lifetime.WsrlConstants;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.oxf.xmlbeans.parser.XMLHandlingException;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.IClassProvider;
import org.n52.ses.api.IFilterEngine;
import org.n52.ses.api.common.GlobalConstants;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.ws.INotificationMessage;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.api.ws.SESFilterCollection;
import org.n52.ses.common.SESResourceIdFactory;
import org.n52.ses.common.environment.SESSoapClient;
import org.n52.ses.common.https.AcceptAllSocketFactory;
import org.n52.ses.common.https.HTTPSConnectionHandler;
import org.n52.ses.filter.SESConstraintFilterHandler;
import org.n52.ses.filter.dialects.SelectiveMetadataFilter;
import org.n52.ses.filter.epl.EPLFilterHandler;
import org.n52.ses.persistency.SESFilePersistence;
import org.n52.ses.storedfilters.StoredFilterHandler;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.util.unitconversion.SESUnitConverter;
import org.n52.ses.util.xml.SESEventGenerator;
import org.n52.ses.util.xml.XMLHelper;
import org.n52.ses.wsn.contentfilter.MessageContentFiler;
import org.n52.ses.wsn.contentfilter.PropertyExclusionContentFilter;
import org.n52.ses.wsn.dissemination.AbstractDisseminationMethod;
import org.n52.ses.wsn.dissemination.DefaultDisseminationMethod;
import org.n52.ses.wsn.dissemination.DisseminationMethodFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESSubscriptionManager extends SimpleSubscriptionManager implements ISubscriptionManager {


	private static final Logger logger = LoggerFactory.getLogger(SESSubscriptionManager.class);

	/**
	 *  constant needed by the PublisherEndpoint to retrieve the instance 
	 */
	public static final String CONTEXT_PATH = GlobalConstants.SUBSCRIPTION_MANAGER_CONTEXT_PATH;


	private static boolean FIRST_RUN = true;
	private static Object FIRST_RUN_MUTEX = new Object();


	/**
	 * Flag is true if the SESSubscriptionManager has a
	 * SESConstraintFilter. 
	 */
	private boolean hasConstraintFilter = false;

	private AbstractDisseminationMethod disseminationMethod;

	private Policy policy;

	private List<MessageContentFiler> messageContentFilters = new ArrayList<MessageContentFiler>();


	/**
	 * Flag is true if the SESSubscriptionManager has a
	 * SESConstraintFilter. 
	 * @return <code>true</code> if there is a constraint filter available
	 */
	public boolean isHasConstraintFilter() {
		return this.hasConstraintFilter;
	}



	@Override
	public void initialize() throws SoapFault {
		if (logger.isInfoEnabled())
			logger.info("initialising SESSubscriptionManager..");

		synchronized (FIRST_RUN_MUTEX) {
			if (FIRST_RUN) {
				initializeSESResources();
				FIRST_RUN = false;
			}	
		}

		if (getSubscriptionPolicy() != null) {
			this.disseminationMethod = DisseminationMethodFactory.createDisseminationMethodFromPolicy(getSubscriptionPolicy(),
					getConsumerClient(), getWsResource().getEndpointReference());
		}
		if (this.disseminationMethod == null) {
			this.disseminationMethod = new DefaultDisseminationMethod();
		}
		this.disseminationMethod.setNumberOfTries(getNumberOfTries());

		/*
		 * increment the id counter. needed if some resources
		 * are deleted manually or by unsubscribing.
		 */
		String ids = "";
		Element child = getResource().getEndpointReference().getParameter(
				WsaConstants.DEFAULT_RESOURCE_ID_QNAME);
		if (child != null && child.getFirstChild() != null) {
			ids = XmlUtils.toString(child.getFirstChild());

			ResourceManager manager = this.getResource().getResourceManager();
			if (manager instanceof SimpleResourceManager) {
				ResourceDefinition def = ((SimpleResourceManager) manager).getResourceDefinition(
						CONTEXT_PATH);
				ResourceIdFactory idFactory = def.getResourceIdFactory();
				if (idFactory instanceof SESResourceIdFactory) {
					SESResourceIdFactory sesFac = (SESResourceIdFactory) idFactory;
					sesFac.setIdentifierCount(
							Integer.parseInt(ids.substring(ids.indexOf(sesFac.getPrefix())
									+ sesFac.getPrefix().length(),
									ids.length())) + 1);
				}
			}
		}


		/* 
		 * resubscribe
		 */
		if (ConfigurationRegistry.getInstance().persistencyEnabled()) {
			initializeResubscription(ids);
		}

		super.initialize();

	}


	/**
	 * Method used
	 * 
	 * @param ids the id of the resource
	 * @throws SoapFault
	 */
	private void initializeResubscription(String ids) throws SoapFault {
		Element persSub = getResource().getEndpointReference().getParameter(
				SESFilePersistence.SES_SUBSCRIBE_PERS_NAME);

		/* if no PersistentSubscribe than its an actual new subscribe */
		if (persSub == null) return;

		if (logger.isDebugEnabled())
			logger.debug("resubscribing from router-entries: " +ids);

		/* first get filters */
		QName filQN = WsnConstants.FILTER_QNAME;
		NodeList filter = persSub.getElementsByTagNameNS(filQN.getNamespaceURI(),
				filQN.getLocalPart());

		if (filter.getLength() == 1) {
			Filter fil = FilterFactory.getInstance().newInstance((Element) filter.item(0));

			fil = SESNotificationProducer.getSESFilterCollectionFromCollection(fil);

			if (fil != null) {
				this.setFilter(fil);
			}

		}


		/* set Consumer */
		QName consQN = WsnConstants.CONSUMER_QNAME;
		NodeList consumer = persSub.getElementsByTagNameNS(consQN.getNamespaceURI(),
				consQN.getLocalPart());

		if (consumer.getLength() == 1) {
			this.setConsumerReference(new EndpointReference((Element) consumer.item(0)));
		}


		/* set producer */				
		QName prodQN = WsnConstants.PRODUCER_QNAME;
		NodeList producer = persSub.getElementsByTagNameNS(prodQN.getNamespaceURI(),
				prodQN.getLocalPart());

		if (producer.getLength() == 1) {
			this.setProducerReference(new EndpointReference((Element) producer.item(0)));
		}

		/* subscription policy */
		QName polQN = WsnConstants.POLICY_QNAME;
		NodeList policies = persSub.getElementsByTagNameNS(polQN.getNamespaceURI(),
				polQN.getLocalPart());

		if (policies.getLength() == 1) {
			//TODO this.setSubscriptionPolicy(policy);
		}

		/* termination time */
		QName timeQN = WsnConstants.INIT_TERMINATION_TIME_QNAME;
		NodeList time = persSub.getElementsByTagNameNS(timeQN.getNamespaceURI(),
				timeQN.getLocalPart());
		if (time.getLength() == 1) {
			try {
				if (!time.item(0).getTextContent().equals("")) {
					this.setInitialTerminationTime(new DateTime(time.item(0).getTextContent()).toDate());
				}
			}
			catch (Exception e) {
				logger.warn(e.getMessage(), e);
				return;
			}
		}

		if (this.getResource().hasCapability(WsrlConstants.SCHEDULED_TERMINATION_URI)) {
			ScheduledTermination wsrl = (ScheduledTermination)this.getResource()
			.getCapability(WsrlConstants.SCHEDULED_TERMINATION_URI);

			wsrl.setTerminationTime(this.getInitialTerminationTime());
		}

		/* finally register at registry
		 * initialization cannot be perfmored now, because esper statements need to registered
		 * as the final step, due to availability of metadata from possible
		 * Publishers.
		 */
		ConfigurationRegistry reg = ConfigurationRegistry.getInstance();
		reg.addReregisteredSubMgr(this);
	}



	private void initializeSESResources() throws SoapFault {
		//add the SESConstraintFilter and EPLFilter
		if (logger.isInfoEnabled())
			logger.info("initializing SES Resources...");
		FilterFactory.getInstance().addHandler(new SESConstraintFilterHandler());
		FilterFactory.getInstance().addHandler(new EPLFilterHandler());
		FilterFactory.getInstance().addHandler(new StoredFilterHandler());
		FilterFactory.getInstance().addHandler(new MessagePatternFilterHandler());
		

		if (logger.isInfoEnabled())
			logger.info("initializing config from file {}...", getResource().getEnvironment().getDataResource(ConfigurationRegistry.CONFIG_FILE));
		InputStream config = getResource().getEnvironment().getDataResourceStream(ConfigurationRegistry.CONFIG_FILE);
		SESUnitConverter unitConverter = new SESUnitConverter();

		/*
		 * 
		 * init the registry
		 * 
		 */
		ConfigurationRegistry.init(config, getEnvironment(), unitConverter);
		ConfigurationRegistry.getInstance().setFilePersistence(new SESFilePersistence());

		SoapClient soapClient = getEnvironment().getSoapClient();
		if (soapClient instanceof SESSoapClient) {
			((SESSoapClient) soapClient).initialize();
		}


		ConfigurationRegistry confReg = ConfigurationRegistry.getInstance();

		// get the filter engine with reflections
		String feString = confReg.getPropertyForKey(ConfigurationRegistry.USED_FILTER_ENGINE);
		if (logger.isInfoEnabled())
			logger.info("Init Filter Engine: {}", feString);
		
		IFilterEngine filterEngine = null;
		if (feString != null) {
			Class<?> c;
			IClassProvider prov = null;
			try {
				c = Class.forName(feString);
				prov = (IClassProvider) c.newInstance();
			} catch (ClassNotFoundException e) {
				throw new SoapFault(e);
			} catch (InstantiationException e) {
				throw new SoapFault(e);
			} catch (IllegalAccessException e) {
				throw new SoapFault(e);
			}

			filterEngine = prov.getFilterEngine(unitConverter);
		}

		if (filterEngine == null) {
			throw new SoapFault("could not initialize FilterEngine (used IClassProvider: "
					+ feString);
		}

		confReg.setFilterEngine(filterEngine);

		/* let the filter listen for new sensor registrations */
		//TODO: check if needed for resource management!
		//		getResource().getResourceManager().addListener(
		//				new FilterListener<IPublisherEndpoint>(IPublisherEndpoint.class, filterEngine));

		/*
		 * add a HTTPSConnectionHandler
		 */
		SoapClient client = getResource().getEnvironment().getSoapClient();
		if (client instanceof SimpleSoapClient) {
			((SimpleSoapClient) client).setConnectionHandler(
					new HTTPSConnectionHandler());
		}
		try {
			HostnameVerifier hv = new HostnameVerifier() {
				@Override
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}

			};
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			HttpsURLConnection.setDefaultSSLSocketFactory(AcceptAllSocketFactory.getSocketFactory());
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}		
	}



	@Override
	public EndpointReference getConsumerReference() {
		if (super.getConsumerReference() == null) {
			try {
				return new EndpointReference(new URI("http://52north.org"));
			} catch (URISyntaxException e) {
				logger.warn(e.getMessage(), e);
			}
			return null;
		}
		return super.getConsumerReference();
	}



	@Override
	public void setFilter(Filter filter) {
		if (filter instanceof SESFilterCollection) {
			SESFilterCollection filterColl = (SESFilterCollection) filter;
			if (filterColl.getConstraintFilter() != null || filterColl.hasEPLFilter()) {
				this.hasConstraintFilter = true;
			}

			
		}
		
		lookupMessageContentFilters(filter);
		
		super.setFilter(filter);
	}


	@SuppressWarnings("unchecked")
	private void lookupMessageContentFilters(Filter filter) {
		Collection<Filter> collection;
		if (filter instanceof FilterCollection) {
			collection = ((FilterCollection) filter).getFilters();
		}
		else if (filter instanceof SESFilterCollection) {
			collection = ((SESFilterCollection) filter).getFilters();
		}
		else {
			return;
		}
		
		Iterator<Filter> it = collection.iterator();
		Filter next;
		while (it.hasNext()) {
			next = it.next();
			if (next instanceof SelectiveMetadataFilter) {
				//TODO hacked. fix it
				this.messageContentFilters.add(new PropertyExclusionContentFilter(((SelectiveMetadataFilter) next).getExcludedQNames()));
			}
		}
	}



	/**
	 * Implementation of the Unsubscribe method of the SubscriptionManager.
	 * 
	 * @throws SoapFault if an error occurred on unsubscribing
	 */
	public void unsubscribe() throws SoapFault {

		try {
			//get the ID for logging
			Document doc = XmlUtils.createDocument(this.getWsResource().toString());
			NodeList id = doc.getDocumentElement().getElementsByTagNameNS(
					WsaConstants.DEFAULT_RESOURCE_ID_QNAME.getNamespaceURI(),
					WsaConstants.DEFAULT_RESOURCE_ID_QNAME.getLocalPart());
			
			if (id != null && id.item(0) != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("...unsubscribing... "+ id.item(0).getTextContent());
				}
			}
			else {
				throw new RuntimeException("Detected an attemp to remove the base Endpoint resource! Rejected.");
			}
			

		} catch (IOException e) {
			throw new SoapFault(e);
		} catch (SAXException e) {
			throw new SoapFault(e);
		}

		//stop the resource
		this.getWsResource().shutdown();
	}



	/**
	 * Sends the given message to the subscription's consumer resource.
	 * This method is not called the normal way, if this SubscriptionManager has
	 * a constraintFilter (Level 2 or Level 3). It is only called after the Level 2/3
	 * filter applied, if not it is not called.
	 * 
	 * @param message the message
	 *
	 */
	@Override
	public void publish(NotificationMessage message) {
		//just check if paused
		if (isPaused()) {
			logger.debug("Message not sent - SubscriptionManager is paused.");
			return;
		}
		//check the filters except SESConstraintFilter
		if (!getFilter().accepts(message)) return;

		applyMessageContentFilters(message);

		if (!this.disseminationMethod.newMessage(message, getConsumerClient(),
				getResource().getEndpointReference(), getProducerReference(),
				getConsumerReference()) && isDestroyedOnFailure()) {
			try {
				getResource().shutdown();
			}
			catch (SoapFault error)	 {
				logger.warn(error.getMessage(), error);
			}
		}
	}


	private void applyMessageContentFilters(NotificationMessage message) {
		for (MessageContentFiler filter : this.messageContentFilters ) {
			filter.filterMessage(message);
		}
	}



	@Override
	public void reRegister() {

	}

	
	@Override
	public Policy getSubscriptionPolicy() {
		return this.policy;
	}

	@Override
	public void setSubscriptionPolicy(Policy policy) {
		this.policy = policy;
	}

	@Override
	public void prepareShutdown() throws SoapFault {

		if (logger.isDebugEnabled())
			logger.debug("shutting down. unregister statements and delete from DB.");

		/*
		 * try to unregister at filter engine
		 */
		try {
			ConfigurationRegistry.getInstance().getFilterEngine().unregisterFilter(this);
		} catch (Exception e) {
			throw new SoapFault(e);
		}
		
		this.disseminationMethod.shutdown();

		super.prepareShutdown();
	}



	@Override
	public void publish(INotificationMessage origMessage) {
		if (origMessage.getNotificationMessage() != null) {
			publish((NotificationMessage) origMessage.getNotificationMessage());
		}
	}


	@Override
	public XmlObject generateSESEvent(MapEvent resultEvent) {
		SESEventGenerator gen = new SESEventGenerator(resultEvent);
		return gen.generateEventDocument();
	}



	@Override
	public boolean sendSESNotificationMessge(XmlObject eventDoc) {
		SESNotificationMessage message = new SESNotificationMessage();

		//remove duplicate gml:ids
		eventDoc = XMLHelper.removeIDDublications(eventDoc);

		//get DOM Node from Event
		Node node = null;
		try {
			node = XmlUtil.getDomNode(eventDoc);
		} catch (XMLHandlingException e) {
			logger.warn(e.getMessage(), e);
		}

		boolean sent = false;
		if (node != null) {
			//node successfully created
			message.addMessageContent((Element) node);

			if (logger.isDebugEnabled()) {
				logger.debug("message to send: " + message.toString());
			}
			publish(message);
			sent = true;
		}	    
		return sent;
	}



	@Override
	public EndpointReference getEndpointReference() {
		return this.getWsResource() == null ? null : this.getWsResource().getEndpointReference();
	}




}