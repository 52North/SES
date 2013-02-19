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
package org.n52.ses.filter.epl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.opengis.gml.AbstractFeatureType;
import net.opengis.sampling.x10.SamplingPointType;


import org.n52.ses.api.IUnitConverter;
import org.n52.ses.api.eml.IEML;
import org.n52.ses.api.eml.ILogicController;
import org.n52.ses.api.eml.IPatternSimple;
import org.n52.ses.api.event.DataTypesMap;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.filter.epl.EPLFilterImpl.EPLFilterInstance;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPStatementException;
import com.vividsolutions.jts.geom.Geometry;

public class EPLFilterController implements ILogicController {

	private static final Logger logger = LoggerFactory
			.getLogger(EPLFilterController.class);
	private EPServiceProvider epService;
	private Configuration config;
	private ISubscriptionManager subMgr;
	private Map<String, EPStatement> epStatements = new HashMap<String, EPStatement>();
	private Map<String, Object> eventProperties;
	private Map<String, Map<String, Object>> inputEventDataTypes = new HashMap<String, Map<String,Object>>();

	public EPLFilterController(ISubscriptionManager sub, EPLFilterImpl filter) throws Exception {
		this.config = new Configuration();
		this.subMgr = sub;

		String emlController = ConfigurationRegistry.getInstance().getPropertyForKey(ConfigurationRegistry.EML_CONTROLLER);
		if (emlController.equalsIgnoreCase(ConfigurationRegistry.EML_001_IMPL)) {
			this.config.addImport("org.n52.ses.eml.v001.filterlogic.esper.customFunctions.*");
		} else {
			this.config.addImport("org.n52.ses.eml.v002.filterlogic.esper.customFunctions.*");
		}

		//register default properties
		registerStandardPropertyNames();
		
		//register the external stream w/ properties at the engine
		registerEvent(filter.getExternalInputName(), this.eventProperties);

		//register newEventName streams at engine
		String newEventName = null;
		for (EPLFilterInstance eplStmt : filter.getEplFilters().keySet()) {
			newEventName = eplStmt.getNewEventName();
			if (newEventName != null && !newEventName.equals("")) {
				registerEvent(newEventName, this.eventProperties);
			}
		}
		
		//initialize esper
		this.epService = EPServiceProviderManager.getProvider("ses:id:"+ this.hashCode(), this.config);
		
		for (EPLFilterInstance eplStmt : filter.getEplFilters().keySet()) {
			String stream = filter.getEplFilters().get(eplStmt);
			addEPLStatement(eplStmt, stream);
		}
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
		
		HashMap<String, Object> dtmTypes = dtm.getTypes();
		for (String type : dtmTypes.keySet()) {
			this.eventProperties.put(type, dtmTypes.get(type));
		}
		
	}
	
	@Override
	public void initialize(IEML eml, IUnitConverter unitConverter)
			throws Exception {
	}

	@Override
	public void sendEvent(String name, MapEvent event) {
		StringBuilder sb = new StringBuilder();
		sb.append("posting new event (" + new Date().getTime() + "):");
		sb.append("\n\tname:  " + name);
		logger.debug(sb.toString());

		this.epService.getEPRuntime().sendEvent(event, name);
	}


	@Override
	public void registerEvent(String eventName,
			Map<String, Object> eventProperties) {
		this.config.addEventType(eventName, eventProperties);
		this.inputEventDataTypes.put(eventName, this.eventProperties);
	}

	@Override
	public Object getEventDatatype(String eventName) {
		return null;
	}

	@Override
	public Object getDatatype(String fullPropertyName) {
		return null;
	}

	@Override
	public String getNewEventName(String patternID, int selectFunctionNumber) {
		return null;
	}

	@Override
	public Map<String, IPatternSimple> getSimplePatterns() {
		return null;
	}

	@Override
	public ISubscriptionManager getSubMgr() {
		return this.subMgr;
	}

	@Override
	public void removeFromEngine() {
		this.epService.removeAllServiceStateListeners();
		this.epService.removeAllStatementStateListeners();
		this.epService.destroy();
		logger.info(getClass().getSimpleName()+" shutdown complete.");
	}

	private void addEPLStatement(EPLFilterInstance eplStmt, String inputName) throws Exception {
		EPStatement epStatement;

		/*
		 * register statements at engine.
		 * Try-Catch needed for better SoapFaults for users ->
		 * a statement can fail if the property was not registered
		 * in the DataTypesMap
		 */
		epStatement = null;
		try {
			logger.info("Register EPL Statement: "+ eplStmt.getStatement());
			epStatement = this.epService.getEPAdministrator().createEPL(eplStmt.getStatement());
		} catch (EPStatementException e) {
			logger.warn(e.getMessage());

			StringBuilder sb = new StringBuilder();
			for (StackTraceElement ste : e.getStackTrace()) {
				sb.append("\n" + ste.toString());
			}
			logger.warn(sb.toString());

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
			throw new Exception("Error in esper statement, possible EPL error: '" + e.getMessage() + "'", e);
		}

		//register listener at esper statement
		if (epStatement != null) {
			//store epStatements
			this.epStatements.put(eplStmt.getStatement(), epStatement);
			
			epStatement.addListener(new EPLStatementListener(eplStmt, this));
		}

	}
	
	
	public static void main(String[] args) {
		System.out.println(AbstractFeatureType.class.isAssignableFrom(SamplingPointType.class));
	}

}
