/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v001.pattern;

import java.util.HashMap;
import java.util.Vector;

import org.n52.ses.api.eml.ILogicController;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.eml.v001.Constants;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * represents a single select function
 * 
 * @author Thomas Everding
 * 
 */
public class SelFunction {
	
	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(SelFunction.class);
	
	private String outputName = "";
	
	private boolean createCausality = false;
	
	private String selectString = "";
	
	private String newEventName = "";
	
	private String functionName;
	
	private HashMap<String, Object> functionParameters;
	
	private HashMap<String, String> fullPropertyNames;
	
//	private String inputName = "";
	
	private Vector<PatternOutputReference> inputReferences;
	
	private String statement;
	
	private boolean singleValueOutput;
	
	private boolean allowOriginalMessageAsResult = false;


	private ILogicController controller;
	
	/**
	 * contains the data types of the resulting event or the inner (nested) types, if the resulting event contains
	 * an event.
	 */
	private HashMap<String, Object> dataTypes;
	
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param logicController controller of this process
	 * 
	 */
	public SelFunction(ILogicController logicController) {
		this.functionParameters = new HashMap<String, Object>();
		this.fullPropertyNames = new HashMap<String, String>();
		this.dataTypes = new HashMap<String, Object>();
		this.inputReferences = new Vector<PatternOutputReference>();
		
		this.controller = logicController;
	}
	

	/**
	 * @return the outputName
	 */
	public String getOutputName() {
		return this.outputName;
	}
	

	/**
	 * @param outputName the outputName to set
	 */
	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}
	

	/**
	 * @return the createCausality
	 */
	public boolean getCreateCausality() {
		return this.createCausality;
	}
	

	/**
	 * @param createCausality the createCausality to set
	 */
	public void setCreateCausality(boolean createCausality) {
		this.createCausality = createCausality;
	}
	

	/**
	 * @return the newEventName
	 */
	public String getNewEventName() {
		return this.newEventName;
	}
	

	/**
	 * @param newEventName the newEventName to set
	 */
	public void setNewEventName(String newEventName) {
		this.newEventName = newEventName;
	}
	

	/**
	 * @return the functionName
	 */
	public String getFunctionName() {
		return this.functionName;
	}
	

	/**
	 * @param functionName the functionName to set
	 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	

	/**
	 * @return the functionParameters
	 */
	public HashMap<String, Object> getFunctionParameters() {
		return this.functionParameters;
	}
	

	/**
	 * adds a single parameter to the function parameter map
	 * 
	 * @param pName name of the parameter
	 * @param pValue value of the parameter
	 */
	public void addFunctionParameter(String pName, Object pValue) {
		Object pV = pValue;
		if (pName.equals(Constants.SELECT_PARAM_PROPERTY_NAME)) {
			//parse property name
			pV = this.parsePropertyName(pValue.toString());
		}
		this.functionParameters.put(pName, pV);
	}
	

	/**
	 * parses a property name for use in esper
	 * 
	 * @param name the property name in EML
	 */
	private String parsePropertyName(String name) {
		String result = name;
		//		int i = name.indexOf("/");
		//		
		//		if (i >= 0) {
		//			//cut off the event name
		//			result = name.substring(i + 1);
		//		}
		//		else {
		//			result = name;
		//		}
		
		//replace "/" by "."
		result = result.replaceAll("/", ".");
		
		//save property name
		this.fullPropertyNames.put(result, name);
		
		return result;
	}
	

	/**
	 * 
	 * @param forSimplePattern set to <code>true</code> if called for a simple pattern
	 * @return the select string, creates it if necessary
	 */
	public String getSelectString(boolean forSimplePattern) {
		//boolean that indicates if additional selections are possible
		boolean preservingPossible = false;
		
		if (this.selectString.equals("")) {
			//create select string
			
			/*
			 * SelectEvent
			 */
			if (this.functionName.equals(Constants.FUNC_SELECT_EVENT_NAME)) {
				this.singleValueOutput = false;
				this.allowOriginalMessageAsResult = true;
				//preserving not necessary
				
				if (this.functionParameters.containsKey(Constants.SELECT_PARAM_EVENT_NAME)) {
					//with EventName given
					String eventName = "" + this.functionParameters.get(Constants.SELECT_PARAM_EVENT_NAME);
					if (forSimplePattern) {
						//named not possible in simple patterns
						this.selectString = "*";
					}
					else {
						//select specified event
						this.selectString = "" + eventName + " as value";
					}
					
					//set data type
					this.dataTypes.put(MapEvent.VALUE_KEY, this.controller.getEventDatatype(eventName));
				}
				else {
					SelFunction.logger.warn("No event name given.");
					//					//no EventName given
					//					this.selectString = "*";
					//					
					//					//set data type
					//					this.dataTypes.put(MapEvent.VALUE_KEY, MapEvent.class);
				}
			}
			
			/*
			 * SelectProperty
			 */
			else if (this.functionName.equals(Constants.FUNC_SELECT_PROPERTY_NAME)) {
				this.singleValueOutput = true;
				preservingPossible = true;
				
				//load property data type
				String fullPropertyName = this.fullPropertyNames.get(this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME));
				this.dataTypes.put(MapEvent.VALUE_KEY, this.controller.getDatatype(fullPropertyName));
				
				//adjust property name for simple patterns
				String pName = this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME).toString();
				if (forSimplePattern) {
					if (pName.contains(".")) {
						//exclude event name
						pName = pName.substring(pName.indexOf(".") + 1);
					}
				}
				//pName.replaceAll(":", "__");
				
				//build select string
				this.selectString = "" + pName + " as value";
			}
			
			/*
			 * SelectSum
			 */
			else if (this.functionName.equals(Constants.FUNC_SELECT_SUM_NAME)) {
				this.singleValueOutput = true;
				this.dataTypes.put(MapEvent.VALUE_KEY, Number.class);
				//preserving not possible
				
				//adjust property name for simple patterns
				String pName = this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME).toString();
				if (forSimplePattern) {
					if (pName.contains(".")) {
						//exclude event name
						pName = pName.substring(pName.indexOf(".") + 1);
					}
				}
				//pName.replaceAll(":", "__");
				
				this.selectString = "sum(" + pName + ")" + " as value";
			}
			
			/*
			 * SelectAvg
			 */
			else if (this.functionName.equals(Constants.FUNC_SELECT_AVG_NAME)) {
				this.singleValueOutput = true;
				this.dataTypes.put(MapEvent.VALUE_KEY, Number.class);
				//preserving not possible
				
				//adjust property name for simple patterns
				String pName = this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME).toString();
				if (forSimplePattern) {
					if (pName.contains(".")) {
						//exclude event name
						pName = pName.substring(pName.indexOf(".") + 1);
					}
				}
				//pName.replaceAll(":", "__");
				
				this.selectString = "avg(" + pName + ")" + " as value";
			}
			
			/*
			 * SelectMax
			 */
			else if (this.functionName.equals(Constants.FUNC_SELECT_MAX_NAME)) {
				this.singleValueOutput = true;
				this.dataTypes.put(MapEvent.VALUE_KEY, Number.class);
				preservingPossible = true;
				
				//adjust property name for simple patterns
				String pName = this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME).toString();
				if (forSimplePattern) {
					if (pName.contains(".")) {
						//exclude event name
						pName = pName.substring(pName.indexOf(".") + 1);
					}
				}
				//pName.replaceAll(":", "__");
				
				this.selectString = "max(" + pName + ")" + " as value";
			}
			
			/*
			 * SelectMin
			 */
			else if (this.functionName.equals(Constants.FUNC_SELECT_MIN_NAME)) {
				this.singleValueOutput = true;
				this.dataTypes.put(MapEvent.VALUE_KEY, Number.class);
				preservingPossible = true;
				
				//adjust property name for simple patterns
				String pName = this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME).toString();
				if (forSimplePattern) {
					if (pName.contains(".")) {
						//exclude event name
						pName = pName.substring(pName.indexOf(".") + 1);
					}
				}
				//pName.replaceAll(":", "__");
				
				this.selectString = "min(" + pName + ")" + " as value";
			}
			
			/*
			 * SelectCount
			 */
			else if (this.functionName.equals(Constants.FUNC_SELECT_COUNT_NAME)) {
				this.singleValueOutput = true;
				this.dataTypes.put(MapEvent.VALUE_KEY, Integer.class);
				this.selectString = "count(*) as value";
				
				//preserving not possible
			}
			
			/*
			 * NotifyOnSelect
			 */
			else if (this.functionName.equals(Constants.FUNC_NOTIFY_ON_SELECT_NAME)) {
				this.singleValueOutput = true;
				this.dataTypes.put(MapEvent.VALUE_KEY, String.class);
				this.selectString = "\"" + this.functionParameters.get(Constants.SELECT_PARAM_MESSAGE_NAME)
						+ "\" as value";
				//preserving not possible
			}
			
			/*
			 * SelectStdDev (user defined)
			 */
			else if (this.functionName.equals(Constants.FUNC_SELECT_STDDEV_NAME)) {
				this.singleValueOutput = true;
				this.dataTypes.put(MapEvent.VALUE_KEY, Number.class);
				//preserving not possible
				
				//adjust property name for simple patterns
				String pName = this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME).toString();
				if (forSimplePattern) {
					if (pName.contains(".")) {
						//exclude event name
						pName = pName.substring(pName.indexOf(".") + 1);
					}
				}
				//pName.replaceAll(":", "__");
				
				this.selectString = "stddev(" + pName + ")" + " as value";
			}
			
			/*
			 * GetAdditiveInverseValue (user defined)
			 */
			else if (this.functionName.equals(Constants.FUNC_GET_ADD_INV_VALUE_NAME)) {
				int logcount = 0;
				SelFunction.logger.info("###### creating select string for 'get additive inverse value'");
				
				this.singleValueOutput = true;
				preservingPossible = true;
				
				SelFunction.logger.info("####### " + logcount++);//0
				
				//load property data type
				String fullPropertyName = this.fullPropertyNames.get(this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME));
				SelFunction.logger.info("####### " + logcount++);//1
				Object dataType = this.controller.getDatatype(fullPropertyName);
				SelFunction.logger.info("####### " + logcount++);//2
				this.dataTypes.put(MapEvent.VALUE_KEY, dataType);
				
				SelFunction.logger.info("####### property data type loaded: " + dataType.toString());
				
				//adjust property name for simple patterns
				String pName = this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME).toString();
				if (forSimplePattern) {
					if (pName.contains(".")) {
						//exclude event name
						pName = pName.substring(pName.indexOf(".") + 1);
					}
				}
				//pName.replaceAll(":", "__");
				SelFunction.logger.info("####### property name for simple patterns adjusted");
				
				//get stream name from propertyName
				String streamName = fullPropertyName.substring(0, fullPropertyName.indexOf("/"));
				SelFunction.logger.info("####### stream name: " + streamName);
				
				//build select string
				if (dataType.equals(Number.class)) {
					this.selectString = "("+ streamName +"."+ MapEvent.DOUBLE_VALUE_KEY +" * -1) as value";
//					this.selectString = "(input.observedProperty * -1) as value";
				} else {
					this.selectString = "" + pName + " as value";
				}
				SelFunction.logger.debug("####### the select string is: " + this.selectString);
			}
			
			/*
			 * SelectDifference (user defined)
			 */
			else if (this.functionName.equals(Constants.FUNC_GET_DIFFERENCE)) {
				SelFunction.logger.info("##++## building select function for select difference");
				
				//set singleValueOutput
				this.singleValueOutput = true;
				
				preservingPossible = true;
				
				//get references
				String firstReference = "" + this.functionParameters.get(Constants.SELECT_PARAM_1_NAME);
				String secondReference = "" + this.functionParameters.get(Constants.SELECT_PARAM_2_NAME);
				
				if (firstReference.equals("") || secondReference.equals("")) {
					//references not found
					SelFunction.logger.warn("references for user defined select function 'SelectDifference' not found");
					return "";
				}
				
				if (forSimplePattern) {
					//adjust references for simple pattern
					firstReference = firstReference.substring(firstReference.indexOf("/"));
					secondReference = secondReference.substring(secondReference.indexOf("/"));
				}
				//replace / by .
				firstReference = firstReference.replaceAll("/", ".");
				secondReference = secondReference.replaceAll("/", ".");
				
				this.selectString = "(" + firstReference + " - " + secondReference + ") as value";
				
				SelFunction.logger.info("##++## select string: " + this.selectString);
			}
			
			/*
			 * SelectFirst (user defined)
			 * select prev(count(*) -1, value) as value from aEvent.win:time(60000 msec) 
			 */
			else if (this.functionName.equals(Constants.FUNC_SELECT_FIRST)){
				SelFunction.logger.info("##++## building select string for SelectFirst");
				
				this.singleValueOutput = true;
				
				String property = "" + this.functionParameters.get(Constants.SELECT_PARAM_PROPERTY_NAME);
				
				if (property.equals("")) {
					SelFunction.logger.warn("property for user defined select function 'SelectFirst' not found");
					return "";
				}
				
				if (forSimplePattern) {
					if (property.contains(".")) {
						//exclude event name
						property = property.substring(property.indexOf(".") + 1);
					}
				}
				
				this.selectString = " prev(count(*)-1, " + property + ") as value";
				
				SelFunction.logger.info("##++## select string: " + this.selectString);
			}
			/*
			 * parse additional select functions here
			 */
		}
		
		/*
		 * select also inputs for causality creation
		 */
		if (this.createCausality) {
			if (forSimplePattern) {
//			if (!this.inputName.equals("")) {
				//causality for simple pattern
				this.selectString += ", * " + " as " + MapEvent.CAUSAL_ANCESTOR_1_KEY;
			}
			else if (!(this.inputReferences.size() < 1)) {
				//causality for complex or repetitive pattern
				PatternOutputReference ref = this.inputReferences.get(0);
				String eventName = ref.getNewEventName();
				this.selectString += ", " + eventName + " as " + MapEvent.CAUSAL_ANCESTOR_1_KEY;
				
				if (this.inputReferences.size() > 1) {
					//also add the second
					ref = this.inputReferences.get(1);
					if (!eventName.equals(ref.getNewEventName())) {
						//ancestor 1 is not the same as ancestor 2
						eventName = ref.getNewEventName();
						this.selectString += ", " + eventName + " as " + MapEvent.CAUSAL_ANCESTOR_2_KEY;
					}
				}
			}
			/*
			 * TODO: add selection of all ancestors here (view, ...)
			 * SELECT * as MapEvent.CAUSAL_ANCESTORS should work
			 */
		}
		
		if (preservingPossible && forSimplePattern) {
			//more properties can be preserved
			boolean geometry = Boolean.parseBoolean(ConfigurationRegistry.getInstance().getPropertyForKey(ConfigurationRegistry.PRESERVE_GEOMETRY));
			if (geometry) {
				//add geometry also
				this.selectString += ", " + MapEvent.GEOMETRY_KEY + " as " + MapEvent.GEOMETRY_KEY;
			}
			
			/*
			 * add more here
			 * 
			 * 1. go to ConfiguratioRegistry and create new property key
			 * 2. go to SESProperties and enter as default value "false"
			 */
		}
		
		return this.selectString;
	}
	

	/**
	 * @return the statement
	 */
	public String getStatement() {
		return this.statement;
	}
	

	/**
	 * @param statement the statement to set
	 */
	public void setStatement(String statement) {
		this.statement = statement;
	}
	

	/**
	 * @return the singleValueOutput
	 */
	public boolean isSingleValueOutput() {
		return this.singleValueOutput;
	}
	

	/**
	 * @return the dataTypes
	 */
	public HashMap<String, Object> getDataTypes() {
		return this.dataTypes;
	}

	
//	/**
//	 * @param inputName the inputName of the pattern (for simple patterns with causality)
//	 */
//	public void setInputName(String inputName) {
//		this.inputName = inputName;
//	}


	
	/**
	 * @param inputReferences the inputReferences of a complex or repetitve pattern for the causality
	 */
	public void setInputReferences(Vector<PatternOutputReference> inputReferences) {
		this.inputReferences = inputReferences;
	}
	
	
	/**
	 * @return true if it is allowed to return the original message as result
	 */
	public boolean allowsOriginalMessageAsResult() {
		return this.allowOriginalMessageAsResult;
	}


	/**
	 * registers newEventName.doubleValue at the DataTypesMap if
	 * should be.
	 */
	public void registerOutputProperties() {
//		if (this.functionName.equals(Constants.FUNC_SELECT_COUNT_NAME) ||
//				this.functionName.equals(Constants.FUNC_SELECT_AVG_NAME) ||
//				this.functionName.equals(Constants.FUNC_SELECT_MAX_NAME) ||
//				this.functionName.equals(Constants.FUNC_SELECT_MIN_NAME) ||
//				this.functionName.equals(Constants.FUNC_SELECT_STDDEV_NAME) ||
//				this.functionName.equals(Constants.FUNC_SELECT_SUM_NAME)) {
//			
//			/*
//			 * TODO: is this ok? value is then recognized as Double in every
//			 * stream
//			 */
//			DataTypesMap.getInstance().registerNewDataType(this.newEventName+ "."+ MapEvent.DOUBLE_VALUE_KEY, Double.class);
//			//preserving not possible
//		}
	}
}
