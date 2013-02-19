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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v001.filterlogic.esper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.opengis.eml.x001.EMLDocument.EML;
import net.opengis.eml.x001.EventAttributeType;
import net.opengis.eml.x001.SimplePatternType;
import net.opengis.eml.x001.SimplePatternType.PropertyRestrictions;
import net.opengis.fes.x20.FilterType;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.SubscriptionManager;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.eml.v001.Constants;
import org.n52.ses.api.IUnitConverter;
import org.n52.ses.api.eml.IEML;
import org.n52.ses.api.eml.ILogicController;
import org.n52.ses.api.eml.IPatternSimple;
import org.n52.ses.api.event.DataTypesMap;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.eml.v001.filterlogic.EMLParser;
import org.n52.ses.eml.v001.pattern.APattern;
import org.n52.ses.eml.v001.pattern.PatternComplex;
import org.n52.ses.eml.v001.pattern.PatternRepetitive;
import org.n52.ses.eml.v001.pattern.PatternSimple;
import org.n52.ses.eml.v001.pattern.PropRestriction;
import org.n52.ses.eml.v001.pattern.SelFunction;
import org.n52.ses.eml.v001.pattern.Statement;
import org.n52.ses.util.xml.EMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPStatementException;
import com.vividsolutions.jts.geom.Geometry;



/**
 * central class for executing a set of esper EPL statements for a single process
 * 
 * @author Thomas Everding
 * 
 */
public class EsperController implements ILogicController {
	
	private static final String	CUSTOM_ESPER_FUNCTIONS_NAMESPACE= "org.n52.ses.eml.v001.filterlogic.esper.customFunctions.*";

	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(EsperController.class);
	
	private Configuration config;
	
	private EPServiceProvider epService;
	
	private HashMap<String, StatementListener> listeners;
	
	private HashMap<String, CountingListener> countingListeners;
	
	private HashMap<String, EPStatement> epStatements;
	
	private EMLParser parser;

	private ISubscriptionManager subMgr;
	
//	private OutputDescription[] outputDescriptions;
	
	private HashMap<String, Object> inputEventDataTypes;
	
//	private EMLProcess process;

//	private InputDescription[] inputDescriptions;

	private HashMap<String, Object> eventProperties;
	
	
	/**
	 * 
	 * Constructor
	 * 
	 */
	public EsperController() {
		this.config = new Configuration();
		this.listeners = new HashMap<String, StatementListener>();
		this.countingListeners = new HashMap<String, CountingListener>();
		//		this.timerListeners = new HashMap<String, TimerListener>();
		this.epStatements = new HashMap<String, EPStatement>();
		this.inputEventDataTypes = new HashMap<String, Object>();
	}
	
	
	/**
	 * 
	 * Constructor
	 *
	 * @param sub subscription manager
	 */
	public EsperController(ISubscriptionManager sub) {
		this();
		this.subMgr = sub;
	}
	

	/**
	 * Registers an event with a name at the engine. Nestable maps are allowed.
	 * 
	 * @param eventName Name of the event used in the patterns (mostly the inputName).
	 * @param properties Properties of the event. Each event is send as an HashMap. These Properties must
	 * contain an entry for each key in the HashMap containing the data type of the HashMap value.
	 */
	@Override
	public synchronized void registerEvent(String eventName, Map<String, Object> properties) {
		
//		logger.info("registering an event with following properties:");
//		logger.info("\t event name: " + eventName);
//		
//		for (String key : properties.keySet()) {
//			logger.info("\t'" + key + "' of type '" + properties.get(key) + "'");
//		}
		
		if (!this.config.getEventTypesNestableMapEvents().containsKey(eventName)) {
			this.config.addEventType(eventName, properties);
		}
	}
	

	/**
	 * 
	 * @return the event properties map
	 */
	public HashMap<String, Object> getEventProperties() {
		return this.eventProperties;
	}


//	/**
//	 * Registers an event for the given {@link InputDescription}
//	 * 
//	 * @param descr description of an process input
//	 */
//	private void registerEvent(InputDescription descr) {
//		String eventName = descr.getName();
//		HashMap<String, Object> props = new HashMap<String, Object>();
//		
//		if (descr.getDataType() != SupportedDataTypes.EVENT) {
//			/*
//			 * check data type
//			 */
//
//			if (descr.getDataType().equals(SupportedDataTypes.BOOLEAN)) {
//				//boolean
//				props.put(MapEvent.VALUE_KEY, Boolean.class);
//			}
//			
//			else if (descr.getDataType().equals(SupportedDataTypes.CATEGORY)) {
//				//category
//				props.put(MapEvent.VALUE_KEY, String.class);
//			}
//			
//			else if (descr.getDataType().equals(SupportedDataTypes.COUNT)) {
//				//count
//				props.put(MapEvent.VALUE_KEY, Integer.class);
//			}
//			
//			else if (descr.getDataType().equals(SupportedDataTypes.QUANTITY)) {
//				//quantity
//				props.put(MapEvent.VALUE_KEY, Double.class);
//			}
//			
//			else if (descr.getDataType().equals(SupportedDataTypes.TEXT)) {
//				//text
//				props.put(MapEvent.VALUE_KEY, String.class);
//			}
//			
//			else if (descr.getDataType().equals(SupportedDataTypes.TIME)) {
//				//time
//				props.put(MapEvent.VALUE_KEY, Long.class);
//			}
//			else {
//				//unknown
//				props.put(MapEvent.VALUE_KEY, Object.class);
//			}
//			
//			//save input event data type
////			this.inputEventDataTypes.put(eventName, props.get(MapEvent.VALUE_KEY));
//		}
//		else {
//			/*
//			 * register swe:Event
//			 */
//			this.buildEventPropertyDataTypeMap(props, descr);
//			
//			//save input event data type
////			this.inputEventDataTypes.put(eventName, props);
//		}
//		
//		//register at esper engine
//		this.registerEvent(eventName, props);
//	}
	

//	/**
//	 * builds the properties map for events (swe:Event)
//	 * 
//	 * @param props data type map of the input
//	 * @param description description of the connection delivering an swe:Event
//	 */
//	private void buildEventPropertyDataTypeMap(HashMap<String, Object> props, InputDescription description) {
//		//parse swe:Event data types from description
//		HashMap<String, Object> dataTypes = EventDataTypeParser.parse(description);
//		
//		//add data types to properties
//		for (String key : dataTypes.keySet()) {
//			props.put(key, dataTypes.get(key));
//		}
//	}


	/**
	 * Initializes the controller with the EML patterns.
	 * 
	 * @param eml The event patterns in EML.
	 * @throws Exception exceptions thrown during the listener initialization
	 */
	@Override
	public void initialize(IEML eml, IUnitConverter conv) throws Exception{
		replacePhenomenonStringsAndConvertUnits(eml, conv);
		
		if (logger.isDebugEnabled())
			logger.debug("initializing esper controller");
		this.parser = new EMLParser(this);
		this.parser.parseEML((EML) eml.getEMLInstance());
		HashMap<String, APattern> patterns = this.parser.getPatterns();
		
		/*
		 * register standard property names
		 */
		this.registerStandardPropertyNames();
		
		/*
		 * Instantiate propertyNames for esper config
		 */
		
		Map<String, APattern> simplePatterns = new HashMap<String, APattern>();
		
		for (String key : patterns.keySet()) {
			APattern value = patterns.get(key);
			if (value instanceof PatternSimple) {
				simplePatterns.put(key, value);
			}
		}
		
		//register Map as event type with registered phenomenons/types
		this.eventProperties = new HashMap<String, Object>();
		this.eventProperties.put(MapEvent.START_KEY, Long.class);
		this.eventProperties.put(MapEvent.END_KEY, Long.class);
		this.eventProperties.put(MapEvent.STRING_VALUE_KEY, String.class);
		this.eventProperties.put(MapEvent.DOUBLE_VALUE_KEY, Double.class);
		this.eventProperties.put(MapEvent.CAUSALITY_KEY, Vector.class);
		this.eventProperties.put(MapEvent.GEOMETRY_KEY, Geometry.class);
		this.eventProperties.put(MapEvent.SENSORID_KEY, String.class);
		this.eventProperties.put(MapEvent.THIS_KEY, Map.class);

		/*
		 * Get data types for phenomenons.
		 */
		
		/*
		 * we need to register output values of all patterns
		 * at the DataTypesMap
		 */
		for (APattern patt : patterns.values()) {
			for (SelFunction func : patt.getSelectFunctions()) {
				if (!func.getNewEventName().equals("")) {
					func.registerOutputProperties();
				}	
			}
		}
		
		
		//TODO: check if string as a value does work (seems not...)
		DataTypesMap dtm = DataTypesMap.getInstance();
		
		/*
		 * the following loop is needed if a simple pattern 
		 * accesses an event property which is not a standard
		 * property known by the EML parser. If so this property 
		 * has to be added to the event that is registered with
		 * a data type. 
		 */
		for (String key : simplePatterns.keySet()) {
			APattern val = simplePatterns.get(key);
			for (Object key2 : val.getPropertyNames()) {
				this.eventProperties.put(key2.toString(), dtm.getDataType(key2.toString()));
			}
		}

		getPropertiesFromPatterns(this.eventProperties, patterns);
		
		for (String key : simplePatterns.keySet()) {
			PatternSimple val = (PatternSimple) simplePatterns.get(key);
//			logger.info("#### registering event for input " + val.getInputName());
			
//			logger.info("datatype of field '" + MapEvent.SENSORID_KEY + "' is '" + eventProperties.get(MapEvent.SENSORID_KEY) + "'");
			
			registerEvent(val.getInputName(), this.eventProperties);
			//add to list of inputs
			this.inputEventDataTypes.put(val.getInputName(), this.eventProperties);
		}
		
		
		/*
		 * end of (instantiate propertyNames for esper config)
		 */
		
		//register custom guards
		this.registerCustomGuards();
		
		//register custom functions
		this.registerCustomFunctions();
		
		//build listeners
		this.buildListeners(patterns);
		
		//log the statements
		this.logStatements();
		
		//initialize esper
		this.epService = EPServiceProviderManager.getProvider("ses:id:"+ this.hashCode(), this.config);
		
		//initialize repetitive count listeners
		this.initializeCountingListeners();
		
		//initialize listeners
		this.initializeListeners();
		
		//register debug listeners
//		this.buildDebugListeners();
		
		//		//initialize timer listeners
		//		this.initializeTimerListeners();
		//		
		//		//publish one internal timer event for every timer pattern (start these patterns)
		//		this.startTimerPatterns();
		
		//send initial counting event
		this.sendInitialCountingEvent();
		if (logger.isDebugEnabled())
			logger.debug("esper controller is ready");
	}
	

	/**
	 * Registers the standard property names at the
	 * data types map.
	 */
	private void registerStandardPropertyNames() {
		//get data types map
		DataTypesMap dtm = DataTypesMap.getInstance();
		
		//register types
		dtm.registerNewDataType(MapEvent.SENSORID_KEY, String.class);
		dtm.registerNewDataType(MapEvent.STRING_VALUE_KEY, String.class);
		dtm.registerNewDataType(MapEvent.DOUBLE_VALUE_KEY, Double.class);
		dtm.registerNewDataType(MapEvent.FOI_ID_KEY, String.class);
		dtm.registerNewDataType(MapEvent.START_KEY, Long.class);
		dtm.registerNewDataType(MapEvent.END_KEY, Long.class);
		dtm.registerNewDataType(MapEvent.OBSERVED_PROPERTY_KEY, String.class);
		dtm.registerNewDataType(MapEvent.RESERVATION_PHASE_KEY, String.class);
		dtm.registerNewDataType(MapEvent.STAUS_KEY, String.class);
		dtm.registerNewDataType(MapEvent.IDENTIFIER_VALUE_KEY, String.class);
		dtm.registerNewDataType(MapEvent.IDENTIFIER_CODESPACE_KEY, String.class);
		dtm.registerNewDataType(MapEvent.VALID_TIME_KEY, String.class);
		dtm.registerNewDataType(MapEvent.LOWER_LIMIT_KEY, Double.class);
		dtm.registerNewDataType(MapEvent.UPPER_LIMIT_KEY, Double.class);
	}


	private void getPropertiesFromPatterns(Map<String, Object> properties,
			Map<String, APattern> patternMap) {
		
		APattern pat;
		String curr;
		DataTypesMap dtm = DataTypesMap.getInstance();
		for (String key : patternMap.keySet()) {
			 pat = patternMap.get(key);
			for (Object s : pat.getPropertyNames()) {
				curr = s.toString();
				
				if (curr.contains("/")) {
					curr = curr.substring(curr.indexOf("/")+1, curr.length());	
				} else {
					curr = curr.substring(curr.indexOf(".")+1, curr.length());
				}
				
				properties.put(curr, dtm.getDataType(curr));
			}
		}
		
	}


	/**
	 * registers the namespace of the custom functions
	 */
	private void registerCustomFunctions() {
		//standard namespace
		this.config.addImport(CUSTOM_ESPER_FUNCTIONS_NAMESPACE);
	}


	/**
	 * logs all created statements
	 */
	private void logStatements() {
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Statements:");
			
			for (String key : this.listeners.keySet()) {
				sb.append("\n\t" + key);
			}
			
			for (String key : this.countingListeners.keySet()) {
				sb.append("\n\t" + key);
			}
			
			sb.append("\n--");
			logger.info(sb.toString());
		}
		
	}


	/**
	 * registers the custom guards
	 */
	private void registerCustomGuards() {
//		//greater than
//		this.config.addPlugInPatternGuard(Constants.GUARD_COMPARISON_NAMESPACE, 
//										  "greater", 
//										  "de.ifgi.lehre.thesisEverding.eml.complexGuard.comparison.GreaterThanGuardFactory");
	}


	/**
	 * registers debug listeners for every statement
	 */
	@SuppressWarnings("unused")
	private void buildDebugListeners() {
		//a debug listener for every statement
		EPStatement statement;
		
		for (String key : this.epStatements.keySet()) {
			statement = this.epStatements.get(key);
			
			statement.addListener(new DebugListener());
		}
	}
	

	/**
	 * sends an initial counting event to all counting listeners
	 */
	private void sendInitialCountingEvent() {
		CountingListener cListener;
		for (String key : this.countingListeners.keySet()) {
			cListener = this.countingListeners.get(key);
			
			//publish start event for counting
			Date now = new Date();
			MapEvent event = new MapEvent(now.getTime(), now.getTime());
			event.put(MapEvent.VALUE_KEY, 1);
			
			this.sendEvent(cListener.getInputEventName(), event);
		}
	}
	

	/**
	 * Sends an event to the esper runtime.
	 * 
	 * @param name The name of the event.
	 * @param event The event itself.
	 */
	@Override
	public synchronized void sendEvent(String name, MapEvent event) {
		
//		this.logger.info("event is " + ((event != null) ? "not " : "") + "null!");
		
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
//			sb.append("\n");
			sb.append("posting new event (" + System.currentTimeMillis() + "; hash="+event.hashCode()+"):");
			sb.append("\n\tname:  " + name);
//			sb.append("\n" + event.toString());
//			sb.append("\n\tevent: " + event.getClass().getName());
//			sb.append("current time: " + new Date().getTime());
//			for (String key : event.keySet()) {
//				sb.append("\n\t" + key + ": \t" + event.get(key));
//			}
//			sb.append("\n");
			EsperController.logger.debug(sb.toString());	
		}
		
		
		this.epService.getEPRuntime().sendEvent(event, name);
	}
	

	/**
	 * builds and registers the listeners for all patterns
	 * 
	 * @param patterns the patterns
	 */
	private void buildListeners(HashMap<String, APattern> patterns) {
		HashMap<String, Object> completedPatterns = new HashMap<String, Object>();
		Vector<APattern> uncompletedPatterns = new Vector<APattern>();
		APattern patt;
		
		//check every pattern
		for (String key : patterns.keySet()) {
			patt = patterns.get(key);
			
			//first run: only simple and timer patterns
			if (patt instanceof PatternComplex || patt instanceof PatternRepetitive) {
				//these patterns need other patterns to be registered first
				uncompletedPatterns.add(patt);
				continue;
			}
			
			this.buildListenersForPattern(patt);
			completedPatterns.put(patt.getPatternID(), patt);
		}
		
		//second run: complex and repetitive patterns
		int i = -1;
		int maxTests = uncompletedPatterns.size() * 3;
		while (uncompletedPatterns.size() > 0) {
			uncompletedPatterns = this.buildComplexListeners(completedPatterns, uncompletedPatterns);
			
			//check for loop
			i++;
			if (i > maxTests) {
				EsperController.logger.warn("One of the patterns can not be build or there is a loop in the patterns. This is not allowed.");
				break;
			}
		}
	}
	

	/**
	 * builds the listeners for patterns that use other patterns (complex and repetitive)
	 * 
	 * @param completedPatterns already completed patterns
	 * @param uncompletedPatterns patterns without listener
	 * @return the patterns witch could not be build
	 */
	private Vector<APattern> buildComplexListeners(HashMap<String, Object> completedPatterns,
			Vector<APattern> uncompletedPatterns) {
		
		Vector<APattern> stillUncomPatterns = new Vector<APattern>();
		PatternComplex cp;
		PatternRepetitive rp;
		for (APattern pat : uncompletedPatterns) {
			//check if all internal patterns are already completed
			if (pat instanceof PatternComplex) {
				//complex pattern
				cp = (PatternComplex) pat;
				
				if (completedPatterns.containsKey(cp.getFirstPatternID())
						&& completedPatterns.containsKey(cp.getSecondPatternID())) {
					//build pattern listeners
					this.buildListenersForPattern(pat);
					completedPatterns.put(pat.getPatternID(), pat);
				}
				else {
					//append to list, try later
					stillUncomPatterns.add(pat);
				}
			}
			else {
				//repetitive pattern
				rp = (PatternRepetitive) pat;
				
				if (completedPatterns.containsKey(rp.getPatternToRepeatID())) {
					//build pattern listeners
					this.buildListenersForPattern(pat);
					completedPatterns.put(pat.getPatternID(), pat);
				}
				else {
					//append to list, try later
					stillUncomPatterns.add(pat);
				}
			}
		}
		
		return stillUncomPatterns;
	}
	

	/**
	 * builds and registers the listeners for a single pattern
	 * 
	 * @param pattern the pattern
	 */
	private void buildListenersForPattern(APattern pattern) {
		if (EsperController.logger.isDebugEnabled())
			EsperController.logger.debug("building listener for pattern " + pattern.getPatternID());
		if (pattern instanceof PatternRepetitive) {
			/*
			 * repetitive pattern needs two statements per select function
			 * 
			 * first statement is the counting statement, the others are the selecting statements.
			 */
			Statement[] statements = pattern.createEsperStatements();
			
			//create new CountingListener for the first statement
			CountingListener cListener = new CountingListener(this,
					((PatternRepetitive) pattern).getInputEventName());
			this.countingListeners.put(statements[0].getStatement(), cListener);
			
			//create listeners for the selecting statements
			for (int i = 1; i < statements.length; i++) {
				this.buildListener(statements[i]);
			}
			
		}
		else {
			/*
			 * other pattern only needs one per statement
			 */
			for (Statement statement : pattern.createEsperStatements()) {
				this.buildListener(statement);
			}
		}
	}
	

	/**
	 * builds the listener for a single statement
	 * 
	 * @param statement the statement
	 */
	private void buildListener(Statement statement) {
		StatementListener listener;
		//create new listener
		
		/*
		 * here we need a workaround. esper is not capable
		 * of sending first/last events of a batch-view - it 
		 * instead sends all events in that view. check the EML
		 * if first or last event is selected and then register 
		 * LastEventStatementListener or FirstEventStatementListener
		 */
		if (statement.getView() != null && statement.getView().getViewName().equals(Constants.VIEW_SELECT_LAST)) {
			listener = new LastEventStatementListener(statement, this, this.subMgr);
		}
		else {
			listener = new StatementListener(statement, this, this.subMgr);
		}
		
		//add listener to map
		this.listeners.put(statement.getStatement(), listener);
	}
	

	/**
	 * initializes the listeners (registers them at the statements)
	 * @throws Exception 
	 */
	private synchronized void initializeListeners() throws Exception {
		//for every statement in the map
		EPStatement epStatement;
		for (String statement : this.listeners.keySet()) {
			
			/*
			 * register statements at engine.
			 * Try-Catch needed for better SoapFaults for users ->
			 * a statement can fail if the property was not registered
			 * in the DataTypesMap
			 */
			epStatement = null;
			try {
				epStatement = this.epService.getEPAdministrator().createEPL(statement);
			} catch (EPStatementException e) {
				EsperController.logger.warn(e.getMessage());
				
				StringBuilder sb = new StringBuilder();
				for (StackTraceElement ste : e.getStackTrace()) {
					sb.append("\n" + ste.toString());
				}
				EsperController.logger.warn(sb.toString());
				
				if (e.getMessage().contains("Implicit conversion")) {
					throw new Exception("Registration of statement failed. Looks like your observerd property was" +
					" not registered by any publisher.\r\n" +
					"If you used \"value\" in your Guard, please use \"doubleValue\" or \"stringValue\" instead.\r\n" +
					"Standard data types:\r\n" +
					"sensorID = String\r\n" +
					"stringValue = String\r\n" +
					"doubleValue = double\r\n" +
					"startTime = long\r\n" +
					"endTime = long\r\n" +
					"observedProperty = String\r\n" +
					"foiID = String");
				}
				//else throw initial exception
				throw new Exception("Error in esper statement, possible EML error: '" + e.getMessage() + "'", e);
			}
			
			//register listener at esper statement
			if (epStatement != null) {
				epStatement.addListener(this.listeners.get(statement));
				//store epStatements
				this.epStatements.put(statement, epStatement);
			}
			
//			StringBuilder sb = new StringBuilder();
//			sb.append("listener registred at engine");
//			sb.append("\n\tstatement: " + statement);
//			sb.append("\n\tlistener number: " + listeners.get(statement).getInstanceNumber());
//			sb.append("\n\tlistener state: " + epStatement.getState().toString());
//			logger.info(sb.toString());
			
			
		}
	}
	

	/**
	 * initializes the counting listeners
	 */
	private synchronized void initializeCountingListeners() {
		//for every statement in the map
		EPStatement epStatement;
		CountingListener cListener;
		for (String statement : this.countingListeners.keySet()) {
			cListener = this.countingListeners.get(statement);
			
			//register statement at engine
			epStatement = this.epService.getEPAdministrator().createEPL(statement);
			
			//register listener at esper statement
			epStatement.addListener(cListener);
			
			//store epStatements
			this.epStatements.put(statement, epStatement);
		}
	}
	

	/**
	 * Searches for the data type of a property.
	 * 
	 * @param fullPropertyName the full EML name of the property
	 * 
	 * @return a java.lang.Class or a Map containing Classes and/or further Maps
	 */
	@Override
	public Object getDatatype(String fullPropertyName) {
		//split into event and property name part
		String eventName;
		String propertyName;
		int lastSlash = fullPropertyName.lastIndexOf("/");
		
		propertyName = fullPropertyName.substring(lastSlash + 1);
		
		int lastButOneSlash = fullPropertyName.substring(0, lastSlash).lastIndexOf("/");
		
		if (lastButOneSlash <= 0) {
			//full name looks like "event/value"
			eventName = fullPropertyName.substring(0, lastSlash);
		}
		else {
			//full name looks like "event/nestedEvent/value", we need nestedEvent
			eventName = fullPropertyName.substring(lastButOneSlash + 1, lastSlash);
		}
		
		//check all inputs first
//		for (InputDescription descr : this.inputDescriptions) {
//			if (descr.getName().equals(eventName)) {
//				return DataTypeNameToClassConverter.convert(descr.getDataType());
//			}
//		}
		
		//then check property Restrictions
		for (APattern pat : this.parser.getPatterns().values()) {
			if (pat instanceof PatternSimple) {
				PatternSimple pats = (PatternSimple) pat;
				for (PropRestriction propRes : pats.getPropertyRestrictions()) {
					if (propRes.getName().equals(propertyName)) {
						if (propRes.getValue().equals("\"" + MapEvent.DOUBLE_VALUE_KEY + "\"")) {
							return Number.class;
						}
					}
				}
			}
		}
		
		//then check all patterns
		for (APattern pat : this.parser.getPatterns().values()) {
			for (SelFunction sel : pat.getSelectFunctions()) {
				if (sel.getNewEventName().equals(eventName)) {
					//this select function defines the data type
					return sel.getDataTypes().get(propertyName);
				}
			}
		}
		return null;
	}
	

	/**
	 * get a map containing all data types of an event
	 * 
	 * @param eventName name of the event (only the event name)
	 * 
	 * @return a map containing all data types of an event 
	 * or the class of the data type if the event is an input event
	 */
	@Override
	public Object getEventDatatype(String eventName) {
		//check input data types
		if (this.inputEventDataTypes.containsKey(eventName)) {
			//event is process input
			return this.inputEventDataTypes.get(eventName);
		}
		
		//check pattern outputs
//		logger.info("check pattern outputs (for event name '" + eventName + "'), no. of patterns: " + this.parser.getPatterns().size());
		for (APattern pat : this.parser.getPatterns().values()) {
//			logger.info("no. of select functions: " + pat.getSelectFunctions().size());
			for (SelFunction sel : pat.getSelectFunctions()) {
//				logger.info("new event name: " + sel.getNewEventName());
				if (sel.getNewEventName().equals(eventName)) {
					//event definition found
					return sel.getDataTypes();
				}
			}
		}
		
		//nothing found
		EsperController.logger.warn("No data type description found for event '" + eventName + "'.");
		return null;
	}
	

//	/**
//	 * 
//	 * @param name name of the output
//	 * @return the {@link OutputDescription} oft an output or <code>null</code> if there is no output for the
//	 * name
//	 */
//	public OutputDescription getOutputDescription(String name) {
////		for (OutputDescription descr : this.outputDescriptions) {
////			if (descr.getName().equals(name)) {
////				//output description found
////				return descr;
////			}
////		}
//		logger.severe("No description for output '" + name + "' found.");
//		return null;
//	}
	

	/**
	 * Performs the output of a value.
	 * 
	 * @param outputName the name of the output
	 * @param value the value to send
	 */
	public void doOutput(String outputName, Object value) {
//		this.process.doOutput(outputName, value);
	}
	

	//	/**
	//	 * @return the epService
	//	 */
	//	public EPServiceProvider getEpService() {
	//		return this.epService;
	//	}
	
	//	/**
	//	 * test main
	//	 * 
	//	 * @param args
	//	 */
	//	public static void main(String[] args) {
	//		/*
	//		 * Initialize the Logger (Config file is located in folder 'xml' with name 'log4j.xml')
	//		 */
	//		org.apache.log4j.xml.DOMConfigurator.configureAndWatch("xml" + java.io.File.separator + "log4j.xml",
	//				60 * 1000);
	//		
	//		EsperController c = new EsperController(null);
	//		c.test();
	//	}
	
	/**
	 * Returns the newEventName of a given pattern
	 * 
	 * @param patternID id of the pattern
	 * @param selectFunctionNumber number of the select function which results are counted
	 * 
	 * @return the newEventName of the pattern
	 */
	@SuppressWarnings("all")
	public String getNewEventName(String patternID, int selectFunctionNumber) {
		//get all patterns from parser
		HashMap<String, APattern> patterns = this.parser.getPatterns();
		
		//search for pattern
		if (!this.parser.getPatterns().containsKey(patternID)) {
			this.logger.warn("pattern ID (" + patternID + ") not found");
			return null;
		}
		APattern pattern = patterns.get(patternID);
		
		//search for select function
		if (!(pattern.getSelectFunctions().size() > selectFunctionNumber)) {
			if (!(pattern.getSelectFunctions().size() >= 0)) {
				this.logger.warn("No select function and therefore no 'newEventName' defined in pattern '" + patternID
						+ "'. Can not use this pattern in a repetitive pattern.");
				return null;
			}
			this.logger.warn("The pattern with the id '" + patternID + "does not define at least "
					+ selectFunctionNumber + " selectfunctions. Using first select function instead.");
			
			//set number to 0
			selectFunctionNumber = 0;
		}
		
		//return newEventName
		return pattern.getSelectFunctions().get(selectFunctionNumber).getNewEventName();
	}


	/**
	 * @return the EML parser
	 */
	public EMLParser getParser() {
		return this.parser;
	}


	/**
	 * 
	 * @param statement the esper statement
	 * 
	 * @return the registered statement object for the statement or null if not present
	 */
	public EPStatement getEPStatement(String statement) {
		if (this.epStatements.containsKey(statement)) {
			return this.epStatements.get(statement);
		}
		return null;
	}


	/**
	 * Retunrs the {@link SubscriptionManager} associated with
	 * this {@link EsperController}
	 * 
	 * @return the associated {@link SubscriptionManager}
	 */
	public ISubscriptionManager getSubMgr() {
		return this.subMgr;
	}


	/**
	 * Removes all statements and listeners from the esper engine,
	 */
	public void removeFromEngine() {
		for (EPStatement epst : this.epStatements.values()) {
			if (logger.isDebugEnabled())
				logger.debug("Removing statement: \n\t"+ epst.getText());
			epst.removeAllListeners();
			epst.destroy();
		}
		
		/* destroy this complete engine - its independent */
		this.epService.destroy();
	}


	@Override
	public Map<String, IPatternSimple> getSimplePatterns() {
		Map<String, APattern> patternMap = getParser().getPatterns();
		Map<String, IPatternSimple> simplePatterns = new HashMap<String, IPatternSimple>();

		for (String key : patternMap.keySet()) {
			APattern value = patternMap.get(key);
			if (value instanceof IPatternSimple) {
				simplePatterns.put(key, (IPatternSimple) value);
			}
		}
		
		return simplePatterns;
	}

	
	/**
	 * replaces phenomenon Strings containing ":" to "__" and
	 * converts any quantity units to its base units.
	 * @param converter the unit converter
	 * @throws Exception exceptions that occur
	 */
	public void replacePhenomenonStringsAndConvertUnits(IEML eml, IUnitConverter converter) throws Exception {
		if (eml.getEMLInstance() != null) {
			EML emlXml = (EML) eml.getEMLInstance();
			FilterType filter = null;
			SimplePatternType[] patterns = emlXml.getSimplePatterns().getSimplePatternArray();
			for (SimplePatternType spt : patterns) {
				if (spt.isSetGuard()) {
					filter = spt.getGuard().getFilter();
					
					EMLHelper.replaceForFilter(filter, converter);
				}
				
				PropertyRestrictions propRes = spt.getPropertyRestrictions();
				if (propRes != null) {
					EventAttributeType[] arr = propRes.getPropertyRestrictionArray();
					
					for (EventAttributeType eat : arr) {
						XmlObject obj = XmlObject.Factory.newInstance();

						Element elem = (Element) eat.getValue().getDomNode();
						String tempText = XmlUtils.toString(elem.getFirstChild()).trim();
						
//						tempText = tempText.replaceAll(":", "__").replaceAll("\\.", "_");
						
						//TODO unit conversion not performed.
						
						obj.newCursor().setTextValue(tempText);
						eat.setValue(obj);
					}
				}
			}

		}

	}
}
