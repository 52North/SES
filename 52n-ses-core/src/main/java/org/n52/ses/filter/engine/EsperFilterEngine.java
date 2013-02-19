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
package org.n52.ses.filter.engine;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.namespace.QName;

import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.SubscriptionManager;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.IEnrichment;
import org.n52.ses.api.IFilterEngine;
import org.n52.ses.api.IUnitConverter;
import org.n52.ses.api.eml.ILogicController;
import org.n52.ses.api.eml.IPatternSimple;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.ws.IConstraintFilter;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.api.ws.SESConstraintFilter;
import org.n52.ses.api.ws.SESFilterCollection;
import org.n52.ses.services.enrichment.AIXMEnrichment;
import org.n52.ses.filter.epl.EPLFilterController;
import org.n52.ses.filter.epl.EPLFilterImpl;
import org.n52.ses.io.parser.ObjectPropertyValueParser;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.util.concurrent.FIFOWorker;
import org.n52.ses.util.concurrent.IConcurrentNotificationHandler;
import org.n52.ses.util.concurrent.IConcurrentNotificationHandler.IPollListener;
import org.n52.ses.util.concurrent.NamedThreadFactory;
import org.n52.ses.util.concurrent.QueuedMapEventCollection;
import org.n52.ses.wsn.NotificationMessageImpl;
import org.n52.ses.wsn.SESSubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class EsperFilterEngine implements IFilterEngine, IPollListener {

	private Map<ILogicController, String> esperControllers;
	private IUnitConverter unitConverter;
	private static final Logger logger = LoggerFactory.getLogger(EsperFilterEngine.class);
	private List<AbstractParser> parsers;
	private IEnrichment enricher = null;
	private Class<?> controllerClass;
	private boolean insertionSuspended;
	private Random random;
	private IConcurrentNotificationHandler queueWorker;
	private ExecutorService messageProcessingPool;
	private boolean controlledConcurrentUse;
	private boolean performanceTesting = false;
	private boolean testingSimulateLatency = true;
	private boolean testingThrowRandomExceptions = false;

	/**
	 * 
	 * Constructor
	 *
	 * @param converter unit converter
	 * @param logger logger
	 */
	public EsperFilterEngine(IUnitConverter converter) {
		if (logger.isInfoEnabled())
			logger.info("Init EsperFilterEngine...");
		this.unitConverter = converter;
		
		ConfigurationRegistry conf = ConfigurationRegistry.getInstance();
		if (logger.isInfoEnabled())
			logger.info("Using Configuration {}...", conf);
		
		this.messageProcessingPool = Executors.newFixedThreadPool(Integer.parseInt(
				conf.getPropertyForKey(ConfigurationRegistry.MAX_THREADS)),
				new NamedThreadFactory("FilterEngineProcessingPool"));

		//reflect the EsperController class, depending on the EML version
		try {
			String controllerString = conf.getPropertyForKey(ConfigurationRegistry.EML_CONTROLLER);
			if (logger.isInfoEnabled())
				logger.info("Init EsperController {}...", controllerString);
			this.controllerClass = Class.forName(controllerString, false, getClass().getClassLoader());
			if (!ILogicController.class.isAssignableFrom(this.controllerClass)) {
				throw new IllegalStateException("Could not instantiate the EML Controller. Check your configuration.");
			}
		} catch (ClassNotFoundException e) {
			logger.warn(e.getMessage(), e);
		}

		//multiple runtime objects needed for pattern management
		this.esperControllers = new ConcurrentHashMap<ILogicController, String>();

		/*
		 * create the parser instances
		 */
		if (logger.isInfoEnabled())
			logger.info("Init Parsers...");
		initializeParsers();

		/*
		 * check if we have enrichment activated
		 */
		String enrich = conf.getPropertyForKey(ConfigurationRegistry.USE_ENRICHMENT).toString();
		if (Boolean.parseBoolean(enrich.trim())) {
			//DUMMY - USE REFLECTIONS HERE in future
			this.enricher = new AIXMEnrichment();
		}

		/*
		 * concurrent fifo worker implementation.
		 * first, check if we use concurrency monitoring
		 */
		this.controlledConcurrentUse = Boolean.parseBoolean(conf.getPropertyForKey(
				ConfigurationRegistry.USE_CONCURRENT_ORDERED_HANDLING).trim());

		if (logger.isInfoEnabled())
			logger.info("Concurrent Message Processing? {}", this.controlledConcurrentUse);
		
		if (this.controlledConcurrentUse) {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(conf.getPropertyForKey(ConfigurationRegistry.CONCURRENT_WORKER));
			} catch (ClassNotFoundException e) {
				logger.warn(e.getMessage(), e);
			}

			if (clazz != null && IConcurrentNotificationHandler.class.isAssignableFrom(clazz)) {
				try {
					this.queueWorker = (IConcurrentNotificationHandler) clazz.newInstance();
					logger.info("Using {} as Notification Queue Worker.", this.queueWorker);
				} catch (InstantiationException e) {
					logger.warn(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					logger.warn(e.getMessage(), e);
				}
			}
			if (this.queueWorker == null) {
				try {
					this.queueWorker = new FIFOWorker();
					logger.info("Using {} as Notification Queue Worker.", this.queueWorker);
				} catch (ClassNotFoundException e) {
					logger.warn(e.getMessage(), e);
				} catch (InstantiationException e) {
					logger.warn(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					logger.warn(e.getMessage(), e);
				}
			}

			this.queueWorker.setPollListener(this);
			this.queueWorker.setTimeout(Integer.parseInt(conf.getPropertyForKey(
					ConfigurationRegistry.CONCURRENT_MAXIMUM_TIMEOUT)));
			this.queueWorker.setUseIntelligentTimeout(Boolean.parseBoolean(conf.getPropertyForKey(
					ConfigurationRegistry.CONCURRENT_INTELLIGENT_TIMEOUT)));

			this.queueWorker.startWorking();			
		}

		if (this.performanceTesting) {
			this.random = new Random();
		}

	}


	private void initializeParsers() {
		List<String> parsersClasses = ConfigurationRegistry.getInstance().getRegisteredParserClasses();
		this.parsers = new ArrayList<AbstractParser>();

		Class<?> clazz;
		AbstractParser parser;
		try {
			for (String c : parsersClasses) {
				clazz = Class.forName(c, false, getClass().getClassLoader());
				if (AbstractParser.class.isAssignableFrom(clazz)) {
					parser = (AbstractParser) clazz.newInstance();
					parser.setUnitConverter(this.unitConverter);
					this.parsers.add(parser);
				}
			}
		} catch (ClassNotFoundException e) {
			logger.warn(e.getMessage(), e);
		} catch (InstantiationException e) {
			logger.warn(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			logger.warn(e.getMessage(), e);
		}

//				this.parsers.add(new OMParser(this.unitConverter));
//				//deactivated to encourage OWS8Parser usage.
////						this.parsers.add(new FaaSaaPilotParser(this.unitConverter)); 
////						this.parsers.add(new AIXMParser(this.unitConverter));
//				this.parsers.add(new AircraftPositionParser());
//				this.parsers.add(new OWS8Parser(this.unitConverter));
//				this.parsers.add(new WXXMParser());
//				this.parsers.add(new GeossWFSParser());
//				this.parsers.add(new SASParser());
	}



	@Override
	public void filter(NotificationMessage message) {
		/*
		 * WE MUST ASSUME THE FOLLOWING:
		 * method calls are _ALL_ made from the same thread,
		 * in the best case it is SESMessageListener and it never
		 * got concurrent after reception at the HttpServlet.
		 * 
		 * This way, we can ensure the correct order of entrance into the concurrent container.
		 */
		if (this.controlledConcurrentUse) {
			if (insertionSuspended) return;

			/*
			 * outsource the processing of message into the thread pool.
			 */
			QueuedMapEventCollection coll = new QueuedMapEventCollection();
			coll.setFuture(this.messageProcessingPool.submit(new NotificationMessageProcessor(coll, message)));

			/*
			 * claim a dummy collection
			 */
			this.queueWorker.insertPendingEventCollection(coll);

		}
		else {
			/*
			 * treat not concurrent
			 * outsource the processing of message into the thread pool.
			 */
			this.messageProcessingPool.submit(new NotificationMessageProcessor(null,
					message));
		}

	}

	/**
	 * Parses O&M document and returns instances of MapEvent.
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private List<MapEvent> parseMessage(NotificationMessage message) throws Exception {
		EsperFilterEngine.logger.debug("parsing message");

		String parserProp = ConfigurationRegistry.getInstance().getPropertyForKey(ConfigurationRegistry.PARSER).toString();
		if (parserProp.equals("generic")) {
			/*
			 * use the generic parser
			 */
			EsperFilterEngine.logger.info("... using the generic parser");
			Collection<QName> messageContentNames = message.getMessageContentNames();

			ObjectPropertyValueParser opvParser;
			List<MapEvent> result = new ArrayList<MapEvent>();

			//parse each content element
			for (QName contentName: messageContentNames) {
				Element element = message.getMessageContent(contentName);

				if (element != null) {
					//parse element and add to result
					XmlObject xmlObj = XMLBeansParser.parse(element, false);
					opvParser = new ObjectPropertyValueParser(xmlObj);
					result.addAll(opvParser.parseXML(this.unitConverter));
				}
			}

			return result;
		}
		else if (parserProp.equals("basic")) {

			/*
			 * AbstractParser instances
			 */
			for (AbstractParser parser : this.parsers) {
				if (parser.accept(message)) {
					return parser.parse(message);
				}
			}

			EsperFilterEngine.logger.warn("Non of the registered Parsers could parse the NotificationMessage. The current" +
					" registered Parsers are: "+this.parsers);

		}
		return null;
	}


	@Override
	public void registerFilter(SubscriptionManager subMgr) throws Exception {

		SESSubscriptionManager sesSub;

		if (subMgr instanceof SESSubscriptionManager) {
			sesSub = (SESSubscriptionManager) subMgr;
		} else {
			throw new Exception("SESSubscriptionManager needed for registering a Filter.");
		}

		if (!sesSub.isHasConstraintFilter()) {
			throw new Exception("FilterEngine needs a IConstraintFilter.");
		}

		Filter originalFilter = subMgr.getFilter();
		IConstraintFilter filter;

		if (originalFilter instanceof SESConstraintFilter) {
			filter = (SESConstraintFilter) originalFilter;
		} else if (originalFilter instanceof SESFilterCollection) {
			SESFilterCollection filterColl = (SESFilterCollection) originalFilter;
			filter = filterColl.getConstraintFilter();
		} else {
			throw new Exception("FilterEngine needs IConstraintFilter.");
		}


		//check if we have an ISubscriptionManager, otherwise no EML parsing is needed
		ISubscriptionManager ism = null;
		if (subMgr instanceof ISubscriptionManager) {
			ism = (ISubscriptionManager) subMgr;
		}
		else return;

		ILogicController controller = null;
		String streamName = "";
		if (filter instanceof SESConstraintFilter) {
			/*
			 * parse the EML and get the patterns
			 * Also UNITCONVERSION is done here now as a first step
			 */
			Constructor<?> con = this.controllerClass.getConstructor(ISubscriptionManager.class);
			controller = (ILogicController) con.newInstance(ism);

			SESConstraintFilter emlFilter = (SESConstraintFilter) filter;
			controller.initialize(emlFilter.getEml(), this.unitConverter);

			Map<String, IPatternSimple> simplePatterns = controller.getSimplePatterns();

			//get streamName from EML
			for (String key : simplePatterns.keySet()) {
				IPatternSimple val = (IPatternSimple) simplePatterns.get(key);

				//check if we already have an external input. if, check if it differs
				if (!streamName.equals("") && !streamName.equals(val.getInputName())) {
					logger.warn("Multiple external input streams for one EML document! Currently only one external input stream per subscription supported. This could lead to dismissing of incoming data.");
				}
				streamName = val.getInputName();
			}
		}

		else if (filter instanceof EPLFilterImpl) {
			EPLFilterImpl epl = (EPLFilterImpl) filter;

			controller = new EPLFilterController(ism, epl);
			streamName = epl.getExternalInputName();
		}


		logger.info("Registering EML Controller for external input stream '"+ streamName +"'");
		this.esperControllers.put(controller, streamName);

	}



	/* (non-Javadoc)
	 * @see org.n52.ses.filter.engine.IFilterEngine#unregisterFilter(org.apache.muse.ws.notification.SubscriptionManager)
	 */
	@Override
	public void unregisterFilter(SubscriptionManager subMgr) throws Exception {
		if (this.performanceTesting) {
			/*
			 * for testing purposes: wait until we have processed all messages
			 */
			long start = System.currentTimeMillis();		
			this.queueWorker.joinUntilEmpty();
			long wait = System.currentTimeMillis() - start;
			Thread.sleep(2000);
			logger.info("Wait time til completion in sec: "+ (wait*1.0f/1000));
			logger.info("notProcessed Failures: "+this.queueWorker.getNotProcessedFailureCount());
			this.queueWorker.resetFailures();
		}

		for (ILogicController espc : this.esperControllers.keySet()) {
			if (espc.getSubMgr() == subMgr) {
				espc.removeFromEngine();

				this.esperControllers.remove(espc);
			}
		}
	}


	@Override
	public void onElementPolled(MapEvent alert) {
		if (this.esperControllers == null) return;

		for (ILogicController espc : this.esperControllers.keySet()) {
			espc.sendEvent(this.esperControllers.get(espc), alert);
		}
	}

	public void shutdown() {
		logger.info("Shutting down EsperFilterEngine...");

		if (this.controlledConcurrentUse) {
			this.queueWorker.stopWorking();
			this.queueWorker.notifyOnDataAvailability(null);
			this.messageProcessingPool.shutdownNow();	
		}

		for (ILogicController espc : this.esperControllers.keySet()) {
			espc.removeFromEngine();
		}		
	}

	private class NotificationMessageProcessor implements Runnable {

		private NotificationMessage message;
		private QueuedMapEventCollection coll;

		public NotificationMessageProcessor(QueuedMapEventCollection coll, NotificationMessage message) {
			this.coll = coll;
			this.message = message;
		}

		@Override
		public void run() {
			if (controlledConcurrentUse) {
				this.coll.updateStartTime();
			}

			List<MapEvent> alerts = new ArrayList<MapEvent>();
			try {
				alerts = parseMessage(message);
			} catch (XmlException e) {
				logger.warn(e.getMessage(), e);
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}

			if (performanceTesting) {
				/*
				 * simulate latency and heavy processing
				 */
				if (testingSimulateLatency) {
					int delta = (int) (2000 + random.nextDouble() * 1000);
					try {
						Thread.sleep(delta);
					} catch (InterruptedException e) {
						logger.warn(e.getMessage(), e);
					}					
				}


				/*
				 * simulate processing failure
				 */
				if (testingThrowRandomExceptions) {
					if (random.nextInt(10) == 0) {
						logger.warn("damn. random exception thrown!!!");
						return;
					}	
				}
			}

			/*
			 * add the original Message and enrich
			 */
			if (alerts != null) {
				for (MapEvent mapEvent : alerts) {
					mapEvent.put(MapEvent.ORIGNIAL_MESSAGE_KEY, new NotificationMessageImpl(message));

					//ENRICHMENT
					if (EsperFilterEngine.this.enricher != null) {
						EsperFilterEngine.this.enricher.enrichEvent(mapEvent);
					}
				}
			}


			if (insertionSuspended) return;

			if (controlledConcurrentUse) {
				coll.setCollection(alerts);
				EsperFilterEngine.this.queueWorker.notifyOnDataAvailability(coll);
			} else {
				if (alerts != null) {
					for (MapEvent mapEvent : alerts) {
						if (logger.isInfoEnabled()) {
							logger.info(mapEvent.toString());
						}
						onElementPolled(mapEvent);
					}	
				}
			}
		}

	}

}
