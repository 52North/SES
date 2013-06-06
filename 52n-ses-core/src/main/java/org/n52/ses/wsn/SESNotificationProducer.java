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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.namespace.QName;

import org.apache.muse.core.routing.MessageHandler;
import org.apache.muse.util.ReflectUtils;
import org.apache.muse.util.xml.XmlSerializable;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.WsaConstants;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationConsumer;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.Policy;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.notification.faults.SubscribeCreationFailedFault;
import org.apache.muse.ws.notification.faults.TopicNotSupportedFault;
import org.apache.muse.ws.notification.faults.UnacceptableInitialTerminationTimeFault;
import org.apache.muse.ws.notification.impl.FilterCollection;
import org.apache.muse.ws.notification.impl.SimpleNotificationProducer;
import org.apache.muse.ws.notification.topics.Topic;
import org.apache.muse.ws.resource.WsResource;
import org.n52.ses.api.IFilterEngine;
import org.n52.ses.api.common.FreeResourceListener;
import org.n52.ses.api.common.GlobalConstants;
import org.n52.ses.api.ws.EngineCoveredFilter;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.api.ws.SESFilterCollection;
import org.n52.ses.requestlogger.RequestLoggerWrapper;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.util.concurrent.NamedThreadFactory;
import org.n52.ses.wsbr.SesTopicFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 * 
 */
public class SESNotificationProducer extends SimpleNotificationProducer
		implements FreeResourceListener {
	public static final String CONTEXT_PATH = GlobalConstants.NOTIFICATION_PRODUCER_CONTEXT_PATH;
	private static final Logger logger = LoggerFactory
			.getLogger(SESNotificationProducer.class);
	private static Object FIRST_RUN_MUTEX = new Object();

	private ExecutorService notfiyPool;

	private static boolean FIRST_RUN = true;

	@Override
	public void freeResources() {
		if (this.notfiyPool != null)
			this.notfiyPool.shutdownNow();
	}

	@Override
	public void publish(QName topicName, XmlSerializable content)
			throws SoapFault {
		publish(topicName, new XmlSerializable[] { content });
	}

	@Override
	public void publish(QName topicName, XmlSerializable[] content)
			throws SoapFault {
		Element[] contentXML = new Element[content.length];

		for (int n = 0; n < content.length; ++n)
			contentXML[n] = content[n].toXML();

		publish(topicName, contentXML);
	}

	@Override
	protected MessageHandler createSubscribeHandler() {
		MessageHandler handler = new SubscribeWithPolicyHandler();

		Method method = ReflectUtils.getFirstMethod(getClass(), "subscribe");
		handler.setMethod(method);

		return handler;
	}

	/**
	 * @throws SoapFault
	 *             if an error occurred on publishing
	 */
	@Override
	public void publish(QName topicName, Element[] content) throws SoapFault {
		//
		// construct the message/payload
		//
		NotificationMessage message = createNotificationMessage();

		for (int n = 0; n < content.length; ++n)
			message.addMessageContent(content[n]);

		message.setTopic(topicName);

		/*
		 * 
		 * check if SubscriptionManager without SESConstraintFilter exists and
		 * forward message to them.
		 */

		Iterator<?> i = getSubscriptions().iterator();

		while (i.hasNext()) {
			SESSubscriptionManager sub = (SESSubscriptionManager) i.next();
			if (!sub.isHasConstraintFilter()) {
				sub.publish(message);
			}
		}

		/*
		 * 
		 * Entrancepoint for the Esper engine
		 * 
		 * (normal SubscriptionManager call is not needed any longer)
		 * 
		 * NOT NEEDED ANY LONGER
		 */

		// this._filterEngine.filter(message);

		/*
		 * 
		 * End of entrancepoint for the Esper engine
		 */

		//
		// if a topic was used, record the message as the 'current' message
		// for the topic (will be returned by getCurrentMessage())
		//
		if (topicName != null) {
			Topic topic = getTopic(topicName);
			if (topic != null)
				topic.setCurrentMessage(message);
		}
	}

	@Override
	public WsResource subscribe(EndpointReference er, Filter filter, Date date,
			Policy policy) throws TopicNotSupportedFault,
			UnacceptableInitialTerminationTimeFault,
			SubscribeCreationFailedFault {

		if (logger.isDebugEnabled())
			logger.debug("subscribing... " + er.getAddress());

		if (ConfigurationRegistry.getInstance() != null
				&& er.getAddress()
						.toString()
						.equals(ConfigurationRegistry.getInstance()
								.getSesPortTypeEPR().getAddress().toString())) {
			/*
			 * this would create an infinit loop!!
			 */
			SESNotificationProducer.logger
					.warn("There was an attempt to create an infinite loop. The EndpointReference"
							+ " linked to this service's PortType. Subscription rejected.");
			throw new SubscribeCreationFailedFault(
					"There was an attempt to create an infinite loop. The EndpointReference"
							+ " linked to this service's PortType. Subscription rejected.");
		}

		// Filter f = getSESFilterCollectionFromCollection(filter);

		FilterCollection amusedFilters = findAmusedFilters(filter,
				new FilterCollection());

		/*
		 * create SESSubscriptionManager
		 */
		WsResource result = super.subscribe(er, amusedFilters, date, policy);

		// add metadata element to endpoint
		// TODO: get from wsdl?
		result.getEndpointReference().setMetadata(
				new QName("http://www.opengis.net/ses/0.0",
						"SubscriptionManager", "sesinst"),
				new QName("http://www.opengis.net/ses/0.0",
						"SubscriptionManagerService", "sesinst"),
				"SubscriptionManagerPort",
				ConfigurationRegistry.getInstance().getSubMgrWsdl());

		SESSubscriptionManager subMgr = (SESSubscriptionManager) result
				.getCapability(WsnConstants.SUBSCRIPTION_MGR_URI);

		// check for SESConstraintFilter and registers at IFilterEngine
		try {
			IFilterEngine filterEngine = ConfigurationRegistry.getInstance()
					.getFilterEngine();

			if (filterEngine == null) {
				throw new SubscribeCreationFailedFault(
						"Could not access an instanceof IFilterEngine! Subscriptions may not match!");
			} else {
				if (filterEngine.registerFilter(subMgr)) {
					subMgr.setHasEngineCoveredFilter(true);
				}
			}
		} catch (Exception e) {
			try {
				/*
				 * remove (delete router entry) the resource because creation
				 * has failed
				 */
				result.shutdown();
			} catch (SoapFault e1) {
				SESNotificationProducer.logger
						.warn("Could not remove false resource '"
								+ XmlUtils
										.toString(result
												.getEndpointReference()
												.getParameter(
														WsaConstants.DEFAULT_RESOURCE_ID_QNAME))
								+ "'.\n"
								+ "Please remove manually from the router-entries\\"
								+ SESSubscriptionManager.CONTEXT_PATH
								+ " folder or it "
								+ "will throw an exception at restart.");
			}
			throw new SubscribeCreationFailedFault(e);
		}

		return result;
	}

	private FilterCollection findAmusedFilters(Filter filter,
			FilterCollection candidates) {
		if (filter instanceof FilterCollection) {
			for (Object f : ((FilterCollection) filter).getFilters()) {
				findAmusedFilters((Filter) f, candidates);
			}
		} else if (!(filter instanceof EngineCoveredFilter)) {
			candidates.addFilter(filter);
		}

		return candidates;
	}

	@Override
	public void shutdown() throws SoapFault {
		super.shutdown();

		this.notfiyPool.shutdownNow();
	}

	@Override
	public void initialize() throws SoapFault {
		logger.info("initialising SESNotificationProducer..");

		super.initialize();

		synchronized (FIRST_RUN_MUTEX) {
			if (FIRST_RUN) {
				/*
				 * filter engine and unit converter
				 */
				ConfigurationRegistry conf = ConfigurationRegistry
						.getInstance();
				conf.registerFreeResourceListener(this);

				/*
				 * request logger
				 */
				RequestLoggerWrapper.init(conf);

				/* add default topics */
				SesTopicFactory.addDefaultTopics(this);

				/*
				 * init the threadpool
				 */
				this.notfiyPool = Executors
						.newFixedThreadPool(
								Integer.parseInt(conf
										.getPropertyForKey(ConfigurationRegistry.MAX_THREADS)),
								new NamedThreadFactory("NotifyHandlerPool"));
				/*
				 * create a new thread which waits for the persistent Publishers
				 * to be reloaded. New thread needed, because the current thread
				 * would be blocked forever as it is purposed to reload
				 * Publishers as well.
				 */
				new Thread(new Runnable() {
					@Override
					public void run() {
						/*
						 * we need to wait for all persistent publisher to be
						 * registered. before, some statement creations could
						 * fail due to the unavailability of data type
						 * definitions.
						 */
						ConfigurationRegistry.getInstance()
								.waitForAllPersistentPublishers();

						/* re-register submgrs */
						List<ISubscriptionManager> res = ConfigurationRegistry
								.getInstance().getReresubs();

						for (ISubscriptionManager sm : res) {

							if (sm instanceof SESSubscriptionManager) {
								SESSubscriptionManager sessm = (SESSubscriptionManager) sm;

								addSubscription(sessm.getWsResource());

								/* check if registering at esper is need */
								if (sessm.getFilter() instanceof SESFilterCollection) {
									SESFilterCollection sfc = (SESFilterCollection) sessm
											.getFilter();

									if (sfc.getConstraintFilter() != null) {
										try {
											ConfigurationRegistry.getInstance()
													.getFilterEngine()
													.registerFilter(sessm);
										} catch (Exception e) {
											logger.warn(e.getMessage(), e);
										}
									}
								}
							}
						}
					}
				}).start();

				FIRST_RUN = false;
			}
		}

	}

	@Override
	public void initializeCompleted() throws SoapFault {
		super.initializeCompleted();

		NotificationConsumer consumer = (NotificationConsumer) getResource()
				.getCapability(WsnConstants.CONSUMER_URI);

		/*
		 * add Listener to Consumer (needed for notify messages)
		 */
		consumer.addMessageListener(new SESMessageListener(this));
	}

	public void publishCompleteMessage(final NotificationMessage message)
			throws SoapFault {
		/*
		 * 
		 * entrance point for the Esper engine
		 */
		IFilterEngine filterEngine = ConfigurationRegistry.getInstance()
				.getFilterEngine();

		if (filterEngine == null) {
			logger.warn("Could not access an instanceof IFilterEngine! Subscriptions may not match!");
		} else {
			filterEngine.filter(message);
		}

		/*
		 * call other wsnt:Filters
		 */
		if (this.notfiyPool == null)
			return;

		this.notfiyPool.submit(new Runnable() {

			@Override
			public void run() {
				/*
				 * other
				 */
				Collection<?> contentNames = message.getMessageContentNames();

				for (Iterator<?> iterator = contentNames.iterator(); iterator
						.hasNext();) {
					QName qn = (QName) iterator.next();
					Element content = message.getMessageContent(qn);

					if (content != null) {
						try {
							publish(message.getTopic(), content);
						} catch (SoapFault e) {
							logger.warn(e.getMessage(), e);
						}
					}
				}
			}
		});

	}

}
