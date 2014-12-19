/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
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

import java.math.BigDecimal;
import java.util.HashMap;

import org.apache.xmlbeans.GDuration;
import org.n52.ses.eml.v001.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Represents a single view
 * 
 * @author Thomas Everding
 * 
 */
public class DataView {
	
	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(DataView.class);
	
	private String viewName;
	
	private String esperString = "";
	
	private boolean batch = false;
	
	private HashMap<String, Object> parameters;
	
	
	/**
	 * 
	 * Constructor
	 * 
	 */
	public DataView() {
		this.parameters = new HashMap<String, Object>();
	}
	

	/**
	 * @return the viewName
	 */
	public String getViewName() {
		return this.viewName;
	}
	

	/**
	 * @param viewName the viewName to set
	 */
	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	

	/**
	 * @return the parameters
	 */
	public HashMap<String, Object> getParameters() {
		return this.parameters;
	}
	

	/**
	 * adds a parameter for the view
	 * 
	 * @param pName name of the parameter
	 * @param pValue value of the parameter
	 */
	public void addParameter(String pName, Object pValue) {
		this.parameters.put(pName, pValue);
	}
	

	/**
	 * @return the batch
	 */
	public boolean isBatch() {
		return this.batch;
	}
	

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(boolean batch) {
		this.batch = batch;
	}
	

	/**
	 * 
	 * @return the view as esper statement part, creates it if necessary
	 */
	public String getViewString() {
		if (this.esperString.equals("")) {
			//create esper string
			
			/*
			 * AllView
			 */
			if (this.viewName.equals(Constants.VIEW_ALL_NAME)) {
				this.esperString = ".win:keepall()";
			}
			
			/*
			 * LengthView
			 */
			else if (this.viewName.equals(Constants.VIEW_LENGTH_NAME)) {
				this.esperString = ".win:length";
				if (this.batch) {
					this.esperString += "_batch";
				}
				this.esperString += "(" 
								  + this.parameters.get(Constants.VIEW_PARAM_EVENT_COUNT_NAME) 
								  + ")";
			}
			
			/*
			 * TimeView
			 */
			else if (this.viewName.equals(Constants.VIEW_TIME_NAME)) {
				this.esperString = ".win:time";
				if (this.batch) {
					this.esperString += "_batch";
				}
				this.esperString += "(" 
								  + this.parameters.get(Constants.VIEW_PARAM_DURATION_NAME)
								  + " msec)";
			}
			
			/*
			 * TimeLengthView
			 */
			else if (this.viewName.equals(Constants.VIEW_TIME_LENGTH_NAME)) {
				if (!this.batch) {
					logger.warn("sliding TimeLengthView is not supported, using batch mode");
				}
				this.esperString = ".win:time_length_batch("
								 + this.parameters.get(Constants.VIEW_PARAM_DURATION_NAME)
								 + " msec, "
								 + this.parameters.get(Constants.VIEW_PARAM_EVENT_COUNT_NAME)
								 + ")";
			}
			
			/*
			 * create statements for additional views here
			 */
			/*
			 * TimeWithParameter (user defined)
			 * is a user defined view with three parameters
			 * - START_EAGER set to true or false (default false)
			 * - FORCE_UPDATES set to true or false (default false)
			 * - Duration
			 * See http://esper.codehaus.org/esper-2.3.0/doc/reference/en/html/epl-views.html#view-win-time-batch
			 */
			else if (this.viewName.equals(Constants.VIEW_TIME_WITH_PARAMETER)) {
				logger.info("####### creating esper string for user defined view \"TimeBatchWithParameter\"");
				// get parameter
				boolean isForceUpdates = false;
				boolean isStartEager = false;
				boolean first = true;
				String parameter = null;
				long duration = 60000l;
				Object obj = null;
				GDuration gDuration = null;
				//
				// isForceUpdates
				obj = this.parameters.get(Constants.VIEW_PARAM_USD_FORCE_UPDATES);
				if (obj != null) {
					isForceUpdates = Boolean.parseBoolean(obj.toString());
				}
				//
				// isStartEager
				obj = this.parameters.get(Constants.VIEW_PARAM_USD_START_EAGER);
				if (obj != null) {
					isStartEager = Boolean.parseBoolean(obj.toString());
				}
				//
				// duration
				obj = this.parameters.get(Constants.VIEW_PARAM_DURATION_NAME);
				// we have to parse the duration here
				try{
					gDuration = new GDuration(obj.toString());
					duration = this.parseTimerDuration(gDuration);
				} catch (Exception e) {
					logger.warn("duration in EML documet is not parseable, using default 60 sec");
				}
				logger.debug("####### " +
						"parameter:\nisForceUpdates: " + 
						isForceUpdates + 
						"\nisStartEager: " +
						isStartEager +
						"\nDuration: " +
						duration +
						"");
				//
				// create esperString
				// esper keywords
				if (isForceUpdates) {
					parameter = "\"FORCE_UPDATE";
					first = false;
				}
				if (isStartEager) {
					if (first) {
						parameter = "\"";
					} else{
						parameter += ", ";
					}
					parameter += "START_EAGER";
				}
				if(isForceUpdates || isStartEager){
					parameter += "\"";
				}
				//
				this.esperString = ".win:time_batch(" 
					  + duration +" msec"
					  + (parameter!=null?", " + parameter : "") // add parameter only if given
					  + ")";
				logger.debug("####### created esper string: \"" + this.esperString + "\"");
			}
			/*
			 * User Define View "SelectLast"
			 * 
			 * combines two views: {Time/Batch|Length/Batch} && std:lastevent
			 * 
			 * Three parameters:
			 *    - type: allowed values "time"|"length"
			 *    - Duration: XMLDuration | integer <-- event count
			 *    - isBatch: true | false
			 * 
			 * See http://esper.codehaus.org/esper-2.3.0/doc/reference/en/html/epl-views.html#view-std-last
			 */
			else if (this.viewName.equals(Constants.VIEW_SELECT_LAST)) {
				logger.info("####### creating esper string for user defined view \"SelectLast\"");
				//
				boolean isTime = false;  // default is length view
				boolean isBatch = false;
				String durationOrLength = null;
				long duration = 60000l;
				Object obj = null;
				GDuration gDuration = null;
				//
				// Time or Length
				obj = this.parameters.get(Constants.VIEW_PARAM_USD_TYPE);
				if (obj != null) {
					String type = (String) obj;
					if (type.equals(Constants.VIEW_PARAM_USD_TYPE_TIME)) {
						isTime = true;
					} else if (!type.equals(Constants.VIEW_PARAM_USD_TYPE_LENGTH)) {
						logger.warn("####### wrong input parameter for user " +
								"defined view \"SelectLast\": given value: " + 
								type + 
								"; allowed values: [" + 
								Constants.VIEW_PARAM_USD_TYPE_TIME +
								" | " +
								Constants.VIEW_PARAM_USD_TYPE_LENGTH +
								"] using default: length");
					}
				}
				//
				// parse duration -> durationOrLength
				obj = this.parameters.get(Constants.VIEW_PARAM_DURATION_NAME);
				if (isTime) {
					// we have to parse the duration here
					try{
						gDuration = new GDuration(obj.toString());
						duration = this.parseTimerDuration(gDuration);
						durationOrLength = duration + "";
					} catch (Exception e) {
						logger.warn("duration in EML documet is not parseable, using default 60 sec");
					}
				} else{
					// we have to parse to int
					Integer length = 1;
					try{
						length = Integer.parseInt(((String) obj));
					} catch (NumberFormatException nfe) {
						logger.warn("length in EML documet is not parseable, using default \"1\"");
					}
					durationOrLength = length + "";
				}
				//
				// parse isBatch
				obj = this.parameters.get(Constants.VIEW_PARAM_USD_IS_BATCH);
				if (obj != null) {
					isBatch = Boolean.parseBoolean(obj.toString());
				}
				//
				//
				logger.debug("####### " +
						"parameter:\nisTime: " + 
						isTime + 
						"\nisBatch: " +
						isBatch +
						"\nDuration: " +
						duration +
						"\ndurationOrLength: " +
						durationOrLength);
				//
				//
				// build esper statement
				if (isTime) {
					this.esperString = ".win:time";
					if (isBatch) {
						this.esperString += "_batch";
					}
					this.esperString += "(" 
									  + durationOrLength
									  + " msec)";
				} else {
					this.esperString = ".win:length";
					if (isBatch) {
						this.esperString += "_batch";
					}
					this.esperString += "(" 
									  + durationOrLength
									  + ")";
				}
				// this.esperString += ".std:lastevent()";
//				this.esperString += ".ext:sort(startTime,false,1)";
				
				/*
				 * selection of lastevent is implemented in the EsperController
				 * method buildListeners. workaround because esper is not
				 * capable of this functionality
				 */
				
				logger.debug("####### created esper string: \"" + this.esperString + "\"");
			}
		}
		return this.esperString;
	}
	
	/**
	 * parses a xs:duration to long(ms)
	 * 
	 * used for TimeBatchWithParameter (user defined view)
	 * 
	 * @param timerInterval xs:duration
	 * @return long(ms)
	 */
	private long parseTimerDuration(GDuration timerInterval) {
		long result = 0;
		//years
		result += timerInterval.getYear() * 365 * 24 * 60 * 60 * 1000;
		
		//months
		result += timerInterval.getMonth() * 30 * 24 * 60 * 60 * 1000;
		
		//days
		result += timerInterval.getDay() * 24 * 60 * 60 * 1000;
		
		//hours
		result += timerInterval.getHour() * 60 * 60 * 1000;
		
		//minutes
		result += timerInterval.getMinute() * 60 * 1000;
		
		//seconds
		result += timerInterval.getSecond() * 1000;
		
		//fraction of second
		result += timerInterval.getFraction().multiply(new BigDecimal(1000)).longValue();
		
		return result;
	}
	
	
}
