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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.muse.core.AbstractFilePersistence;
import org.apache.muse.core.Resource;
import org.apache.muse.core.ResourceManager;
import org.apache.muse.core.routing.RouterFilePersistence;
import org.apache.muse.core.routing.RouterPersistence;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.WsaConstants;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.SubscriptionManager;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.resource.impl.SimpleWsResource;
import org.apache.muse.ws.resource.properties.ResourcePropertyCollection;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.ISESFilePersistence;
import org.n52.ses.api.common.SensorMLConstants;
import org.n52.ses.common.SensorML;
import org.n52.ses.wsbr.RegisterPublisher;
import org.n52.ses.wsn.SESSubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * This class mainly contains code from {@link RouterFilePersistence}
 * and is extended to maintain re-subscriptions on startup of the service.
 * 
 * @author matthes rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESFilePersistence extends AbstractFilePersistence implements RouterPersistence, ISESFilePersistence {


	private static final Logger logger = LoggerFactory
			.getLogger(SESFilePersistence.class);



	private static boolean FIRST_RUN = true;

	/**
	 * QName for persistent stored filters
	 */
	public static final QName SES_STORED_FILTER_NAME = new QName("http://www.opengis.net/es-sf/0.0", "StoredFilter");
	
	/**
	 * QName for the persistent subscribe
	 */
	public static QName SES_SUBSCRIBE_PERS_NAME = new QName("http://www.opengis.net/ses/0.0", "PersistentSubscribe");

	/**
	 * QName for the persistent register publisher
	 */
	public static QName SES_REGPUB_PERS_NAME = new QName("http://www.opengis.net/ses/0.0", "PersistentRegisterPublisher");

	/**
	 * the count of persistent RegisterPublisher instances
	 */
	public static int persRegPubCount = -1;

	/**
	 * the count of persistent Subscribe instances
	 */
	public static int persSubscribeCount = -1;
	
	/**
	 * 
	 * Constructor
	 *
	 */
	public SESFilePersistence() {
		super();

	}


	/**
	 * 
	 * {@inheritDoc}
	 * <br><br>
	 * This implementation serializes the EPR to XML, then writes the 
	 * wsa:ReferenceParameters element to the file. If the EPR had no 
	 * reference parameters, an empty element is written.
	 * 
	 */
	@Override
	protected void createResourceFile(EndpointReference epr, Resource resource, File resourceFile)
	throws SoapFault {
		SubscriptionManager subMgr = (SubscriptionManager)
		resource.getCapability(WsnConstants.SUBSCRIPTION_MGR_URI);

		SensorML sensorML = (SensorML)
		resource.getCapability(SensorMLConstants.NAMESPACE);

		SimpleWsResource simpleResource = null;
		if (resource instanceof SimpleWsResource) {
			simpleResource = (SimpleWsResource) resource;
		}

		Document root = epr.toXML().getOwnerDocument();
		
		try
		{
			//
			// extract the wsa:ReferenceParameters XML out of the EPR and 
			// save that to a file - if the EPR had no params, just make 
			// an empty element (because we need a valid XML doc)
			//
			Element eprXML = epr.toXML();
			Element params = XmlUtils.getElement(eprXML, WsaConstants.PARAMETERS_QNAME);

			if (params == null)
				params = XmlUtils.createElement(root, WsaConstants.PARAMETERS_QNAME, null);

			/* add subscribe request to instance file */
			Element elem = null;
			if (subMgr != null) {
				elem = XmlUtils.createElement(root, SES_SUBSCRIBE_PERS_NAME, null);
				
				Element subMgrElem = subMgr.toXML();

				//import the node. otherwise, it cannot be appended.
				elem.appendChild(root.importNode(subMgrElem, true));
				params.appendChild(elem);
			}
			/* this is a publishers */
			else if (sensorML != null) {
				elem = XmlUtils.createElement(root, SES_REGPUB_PERS_NAME, null);
				
				elem.appendChild(elem.getOwnerDocument().importNode(sensorML.getSensorML(), true));

				if (simpleResource != null) {
					ResourcePropertyCollection props = simpleResource.getPropertyCollection();

					/* LifeTime */
					QName qn = new QName("http://docs.oasis-open.org/wsrf/rl-2", "TerminationTime");
					for (Element el : props.getResourceProperty(qn)) {
						Element el2 = XmlUtils.createElement(root, qn, XmlUtils.toString(el.getFirstChild(), false));
						elem.appendChild(el2);
					}

					/* demand? */
					qn = new QName("http://docs.oasis-open.org/wsn/br-2", "Demand");
					for (Element el : props.getResourceProperty(qn)) {
						Element el2 = XmlUtils.createElement(root, qn, XmlUtils.toString(el.getFirstChild(), false));
						elem.appendChild(el2);
					}

					/* topic */
					qn = new QName("http://docs.oasis-open.org/wsn/br-2", "Topic");
					for (Element el : props.getResourceProperty(qn)) {
						Element el2 = XmlUtils.createElement(root, qn, XmlUtils.toString(el.getFirstChild(), false));
						elem.appendChild(el2);
					}
					
					params.appendChild(elem);
				}
			}

			XmlUtils.toFile(params, resourceFile);

			/* again remove from EPR for easier resource management */
			if (subMgr != null) {
				params.removeChild(elem);
			}
			else if (sensorML != null) {
				params.removeChild(elem);
			}
		}

		catch (IOException error)
		{
			throw new SoapFault(error);
		}
	}

	/**
	 * 
	 * @return The string 'resource-instance-'.
	 * 
	 */
	@Override
	protected String getFilePrefix() {
		return "resource-instance-";
	}

	/**
	 * 
	 * {@inheritDoc}
	 * <br><br>
	 * This implementation treats the XML fragment as a wsa:ReferenceParameters 
	 * element. It creates an EPR for the given resource type (context path) 
	 * and then adds the reference parameters to it. Finally, it creates an 
	 * instance of the resource type and (re-)sets the EPR that it has constructed.
	 * 
	 */
	@Override
	protected Resource reloadResource(String contextPath, Element resourceXML)
	throws SoapFault {
		
		/*
		 * needed as FIRST_RUN flag because here we have all
		 * properties initialized (this is not the case in the
		 * constructor).
		 */
		if (FIRST_RUN) {
			/*
			 * count the files which have persistent subscriptions in it.
			 */
			if (persRegPubCount == -1) {
				File tmp = getResourceTypeDirectory(RegisterPublisher.RESOURCE_TYPE);
				persRegPubCount  = tmp.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						try {
							XmlObject obj = XmlObject.Factory.parse(pathname);
							if (XmlUtil.selectPath("declare namespace pre='"+ SES_REGPUB_PERS_NAME.getNamespaceURI() +"'; " +
							"//pre:"+ SES_REGPUB_PERS_NAME.getLocalPart(), obj).length > 0) {
								return true;
							}
						} catch (XmlException e) {
							logger.warn(e.getMessage(), e);
						} catch (IOException e) {
							logger.warn(e.getMessage(), e);
						}
						return false;
					}
				}).length;
			}
			
			/*
			 * count the files which have persistent publishers in it.
			 */
			if (persSubscribeCount == -1) {
				File tmp = getResourceTypeDirectory(SESSubscriptionManager.CONTEXT_PATH);
				persSubscribeCount  = tmp.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						try {
							XmlObject obj = XmlObject.Factory.parse(pathname);
							if (XmlUtil.selectPath("declare namespace pre='"+ SES_SUBSCRIBE_PERS_NAME.getNamespaceURI() +"'; " +
									"//pre:"+ SES_SUBSCRIBE_PERS_NAME.getLocalPart(), obj).length > 0) {
								return true;
							}
						} catch (XmlException e) {
							logger.warn(e.getMessage(), e);
						} catch (IOException e) {
							logger.warn(e.getMessage(), e);
						}
						return false;
					}
				}).length;
			}
			
			FIRST_RUN = false;
		}
		
		ResourceManager manager = getResourceManager();

		//
		// the XML from the file is the reference parameters of an EPR, so 
		// we're going to construct the rest of the EPR XML around it and 
		// then turn it into an EPR object
		//

		//
		// wrap parameter XML in a wsa:EndpointReference element
		//        
		Document doc = resourceXML.getOwnerDocument();
		Element eprXML = XmlUtils.createElement(doc, WsaConstants.EPR_QNAME);

		//
		// get the right address URI for the wsa:Address element - this is 
		// the default URI (has the proper host/port/app) with the context 
		// path for the resource type at the end
		//
		String address = manager.getEnvironment().getDefaultURI();
		int lastSlash = address.lastIndexOf('/');
		address = address.substring(0, lastSlash + 1) + contextPath;

//		boolean alreadyHasAddress = false;
//		if (resourceXML != null) {
//			NodeList addressElement = resourceXML.getElementsByTagNameNS(WsaConstants.ADDRESS_QNAME.getNamespaceURI(),
//					WsaConstants.ADDRESS_QNAME.getLocalPart());
//			if (addressElement.getLength() > 0) {
//				alreadyHasAddress = true;
//			}
//		}
//
//		if (!alreadyHasAddress)
			XmlUtils.setElement(eprXML, WsaConstants.ADDRESS_QNAME, address);

		//ReferenceParameters are to placed AFTER wsa:Address
		eprXML.appendChild(resourceXML);
		
		//
		// create EPR object from XML and set it on the newly-created resource
		//
		EndpointReference epr = new EndpointReference(eprXML);

		Resource resource = manager.createResource(contextPath);
		resource.setEndpointReference(epr);

		//
		// continue initialization/registration
		//
		resource.initialize();

		/* remove all added resource parameteres (e.g., PersistentSubscribe). 
		 * needed for Destroy of a resource. */
		if ((epr.getParameter(SESFilePersistence.SES_SUBSCRIBE_PERS_NAME) != null)) {
			epr.removeParameter(SESFilePersistence.SES_SUBSCRIBE_PERS_NAME);
		}
		if ((epr.getParameter(SESFilePersistence.SES_REGPUB_PERS_NAME) != null)) {
			epr.removeParameter(SESFilePersistence.SES_REGPUB_PERS_NAME);
		}
		if (epr.getParameter(SESFilePersistence.SES_STORED_FILTER_NAME) != null) {
			epr.removeParameter(SESFilePersistence.SES_STORED_FILTER_NAME);
		}

		manager.addResource(epr, resource);

		return resource;
	}

	/**
	 * 
	 * This implementation checks to see if the resource type is one that 
	 * is being persisted, and if so, creates a file for the instance.
	 * 
	 */
	@Override
	public void resourceAdded(EndpointReference epr, Resource resource) 
	throws SoapFault {
		String contextPath = resource.getContextPath();

		if (getResourceManager().isUsingPersistence(contextPath))
			createResourceFile(epr, resource);
	}

	/**
	 * 
	 * This implementation checks to see if the resource type is one that 
	 * is being persisted, and if so, tries to delete the resource's file.
	 * 
	 */
	@Override
	public void resourceRemoved(EndpointReference epr)
	throws SoapFault {
		String contextPath = getContextPath(epr);

		if (getResourceManager().isUsingPersistence(contextPath))
			destroyResourceFile(epr);
	}


	/**
	 * @return the count of persistent publishers
	 */
	public int getPersistentPublisherCount() {
		return persRegPubCount;
	}


	/**
	 * @return the count of persistent subscriptions
	 */
	public int getPersistentSubscriberCount() {
		return persSubscribeCount;
	}

	

}
