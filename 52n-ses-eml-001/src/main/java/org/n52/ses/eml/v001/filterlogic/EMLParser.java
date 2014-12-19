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

package org.n52.ses.eml.v001.filterlogic;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import net.opengis.eml.x001.AbstractGuardedViewPatternType;
import net.opengis.eml.x001.AbstractPatternType;
import net.opengis.eml.x001.AbstractViewPatternType;
import net.opengis.eml.x001.EventAttributeType;
import net.opengis.eml.x001.GuardType;
import net.opengis.eml.x001.RepetitivePatternType;
import net.opengis.eml.x001.SelectFunctionType;
import net.opengis.eml.x001.SimplePatternType;
import net.opengis.eml.x001.TimerPatternType;
import net.opengis.eml.x001.UserDefinedOperatorType;
import net.opengis.eml.x001.UserParameterType;
import net.opengis.eml.x001.ViewType;
import net.opengis.eml.x001.ComplexPatternDocument.ComplexPattern;
import net.opengis.eml.x001.ComplexPatternType.Logicaloperator;
import net.opengis.eml.x001.ComplexPatternType.StructuralOperator;
import net.opengis.eml.x001.EMLDocument.EML;
import net.opengis.eml.x001.SelectFunctionType.NotifyOnSelect;
import net.opengis.eml.x001.SelectFunctionType.SelectAvg;
import net.opengis.eml.x001.SelectFunctionType.SelectEvent;
import net.opengis.eml.x001.SelectFunctionType.SelectMax;
import net.opengis.eml.x001.SelectFunctionType.SelectMin;
import net.opengis.eml.x001.SelectFunctionType.SelectProperty;
import net.opengis.eml.x001.SelectFunctionType.SelectSum;
import net.opengis.eml.x001.SelectFunctionType.UserDefinedSelectFunction;
import net.opengis.eml.x001.TimerPatternType.TimerAt;
import net.opengis.eml.x001.ViewType.LengthView;
import net.opengis.eml.x001.ViewType.TimeLengthView;
import net.opengis.eml.x001.ViewType.TimeView;
import net.opengis.eml.x001.ViewType.UserDefinedView;

import org.apache.xmlbeans.GDuration;
import org.n52.ses.api.eml.ILogicController;
import org.n52.ses.eml.v001.Constants;
import org.n52.ses.eml.v001.pattern.AGuardedViewPattern;
import org.n52.ses.eml.v001.pattern.APattern;
import org.n52.ses.eml.v001.pattern.AViewPattern;
import org.n52.ses.eml.v001.pattern.DataView;
import org.n52.ses.eml.v001.pattern.PatternComplex;
import org.n52.ses.eml.v001.pattern.PatternGuard;
import org.n52.ses.eml.v001.pattern.PatternOperator;
import org.n52.ses.eml.v001.pattern.PatternOutputReference;
import org.n52.ses.eml.v001.pattern.PatternRepetitive;
import org.n52.ses.eml.v001.pattern.PatternSimple;
import org.n52.ses.eml.v001.pattern.PatternTimer;
import org.n52.ses.eml.v001.pattern.PropRestriction;
import org.n52.ses.eml.v001.pattern.SelFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * parses an EML file and generates an esper controller
 * 
 * @author Thomas Everding
 * 
 */
public class EMLParser {
	
	private HashMap<String, APattern> patterns;
	
	private ILogicController controller;
	
	private static final Logger logger = LoggerFactory
			.getLogger(EMLParser.class);
	
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param controller controller of this process
	 * 
	 */
	public EMLParser(ILogicController controller) {
		this.patterns = new HashMap<String, APattern>();
		this.controller = controller;
	}
	

	/**
	 * parses an EML document and creates an esper controller
	 * 
	 * @param eml the EML document
	 */
	public void parseEML(EML eml) {
		EMLParser.logger.info("parsing EML document");
		/*
		 * parse simple patterns
		 */
		SimplePatternType[] sPatterns = eml.getSimplePatterns().getSimplePatternArray();
		for (SimplePatternType pattern : sPatterns) {
			//parse pattern
			this.parseSimplePattern(pattern);
		}
		
		/*
		 * parse complex patterns
		 */
		ComplexPattern[] cPatterns = eml.getComplexPatterns().getComplexPatternArray();
		for (ComplexPattern pattern : cPatterns) {
			//parse pattern
			this.parseComplexPattern(pattern);
		}
		
		/*
		 * parse timer patterns
		 */
		TimerPatternType[] tPatterns = eml.getTimerPatterns().getTimerPatternArray();
		for (TimerPatternType pattern : tPatterns) {
			//parse pattern
			this.parseTimerPattern(pattern);
		}
		
		/*
		 * parse repetitive patterns
		 */
		RepetitivePatternType[] rPatterns = eml.getRepetitivePatterns().getRepetitivePatternArray();
		for (RepetitivePatternType pattern : rPatterns) {
			//parse pattern
			this.parseRepetitivePattern(pattern);
		}
	}
	

	/**
	 * parses a simple patterns
	 * 
	 * @param pattern the pattern to parse
	 */
	private void parseSimplePattern(SimplePatternType pattern) {
		//create representation
		PatternSimple simplePattern = new PatternSimple(this.controller);
		
		/*
		 * parse SimplePattern content
		 */

		//set input name
		simplePattern.setInputName(pattern.getInputName().toString()
				.replaceAll(":", "__").replaceAll("\\.", "_"));
		
		//set property restrictions
		if (pattern.getPropertyRestrictions().getPropertyRestrictionArray().length > 0) {
			EventAttributeType[] restrictions = pattern.getPropertyRestrictions().getPropertyRestrictionArray();
			
			for (EventAttributeType restr : restrictions) {
				simplePattern.addPropertyRestriction(this.parsePropertyRestriction(restr, simplePattern.getPropertyNames()));
			}
		}
		
		/*
		 * parse derived content
		 */

		//parse AbstractPatternType content
		this.parsePattern(simplePattern, pattern);
		
		//parse AbstractViewPattern
		this.parseViewPattern(simplePattern, pattern);
		
		//parse AbstractGuardedViewPattern
		this.parseGuardedViewPattern(simplePattern, pattern);
		
		/*
		 * store simple pattern
		 */
		this.patterns.put(simplePattern.getPatternID(), simplePattern);
	}
	

	/**
	 * parses a complex pattern
	 * 
	 * @param pattern the pattern to parse
	 */
	private void parseComplexPattern(ComplexPattern pattern) {
		//create representation
		PatternComplex complexPattern = new PatternComplex();
		
		/*
		 * parse complex pattern content
		 */

		//set operator
		complexPattern.setOperator(this.parsePatternOperator(pattern));
		
		//set first pattern ID
		complexPattern.setFirstPatternID(pattern.getFirstPattern().getPatternReference().trim());
		
		//set select function to use
		complexPattern.setFirstSelectFunctionNumber(pattern.getFirstPattern().getSelectFunctionNumber());
		
		//set second pattern
		complexPattern.setSecondPatternID(pattern.getSecondPattern().getPatternReference().trim());
		
		//set select function to use
		complexPattern.setSecondSelectFunctionNumber(pattern.getSecondPattern().getSelectFunctionNumber());
		
		//set controller reference
		complexPattern.setController(this.controller);
		
		//set maximum listening duration
		if (pattern.isSetMaximumListeningDuration()) {
			complexPattern.setMaxListeningDuration(this.parseTimerDuration(pattern.getMaximumListeningDuration()));
		}
		
		/*
		 * parse derived content
		 */

		//parse AbstractPatternType content
		this.parsePattern(complexPattern, pattern);
		
		//parse AbstractViewPattern
		this.parseViewPattern(complexPattern, pattern);
		
		//parse AbstractGuardedViewPattern
		this.parseGuardedViewPattern(complexPattern, pattern);
		
		/*
		 * store complex pattern
		 */
		this.patterns.put(complexPattern.getPatternID(), complexPattern);
	}
	

	/**
	 * parses a timer pattern
	 * 
	 * @param pattern the pattern to parse
	 */
	private void parseTimerPattern(TimerPatternType pattern) {
		//create representation
		PatternTimer timerPattern = new PatternTimer();
		
		/*
		 * parse timer pattern content
		 */

		if (pattern.isSetTimerInterval()) {
			//interval timer
			timerPattern.setInterval(true);
			
			//set duration
			timerPattern.setDuration(this.parseTimerDuration(pattern.getTimerInterval()));
		}
		else {
			//at timer
			timerPattern.setInterval(false);
			
			//set second, 
			TimerAt t = pattern.getTimerAt();
			
			if (t.isSetSecond()) {
				timerPattern.setSecond(t.getSecond());
			}
			
			//minute, 
			if (t.isSetMinute()) {
				timerPattern.setMinute(t.getMinute());
			}
			
			//hour, 
			if (t.isSetHour()) {
				timerPattern.setHour(t.getHour());
			}
			
			//weekday, 
			if (t.isSetDayOfWeek()) {
				timerPattern.setDayOfWeek(t.getDayOfWeek());
			}
			
			//day of month, 
			if (t.isSetDayOfMonth()) {
				timerPattern.setDayOfMonth(t.getDayOfMonth());
			}
			
			//and month
			if (t.isSetMonth()) {
				timerPattern.setMonth(t.getMonth());
			}
		}
		
		/*
		 * parse derived content
		 */

		//parse AbstractPatternType content
		this.parsePattern(timerPattern, pattern);
		
		//parse AbstractViewPattern
		this.parseViewPattern(timerPattern, pattern);
		
		/*
		 * store timer pattern
		 */
		this.patterns.put(timerPattern.getPatternID(), timerPattern);
	}
	

	/**
	 * parses a repetivite pattern
	 * 
	 * @param pattern
	 */
	private void parseRepetitivePattern(RepetitivePatternType pattern) {
		//create representation
		PatternRepetitive repetitivePattern = new PatternRepetitive();
		
		/*
		 * parse repetitive pattern content
		 */

		//set event count
		repetitivePattern.setRepetitionCount(pattern.getEventCount().intValue());
		
		//set pattern to repeat
		repetitivePattern.setPatternToRepeatID(pattern.getPatternToRepeat().getPatternReference().trim());
		
		//set select function to use
		repetitivePattern.setSelectFunctionToUse(pattern.getPatternToRepeat().getSelectFunctionNumber());
		
		//set controller
		repetitivePattern.setController(this.controller);
		
		/*
		 * parse derived content
		 */

		//parse AbstractPatternType content
		this.parsePattern(repetitivePattern, pattern);
		
		/*
		 * store repetitive pattern
		 */
		this.patterns.put(repetitivePattern.getPatternID(), repetitivePattern);
	}
	

	/**
	 * parses the content of AbstractPatterns
	 * 
	 * @param representation internal representation of the pattern
	 * @param bean bean of the pattern
	 */
	private void parsePattern(APattern representation, AbstractPatternType bean) {
		/*
		 * set pattern ID
		 */
		representation.setPatternID(bean.getPatternID().trim());
		
		/*
		 * set description
		 */
		if (bean.isSetPatternDescription()) {
			representation.setDescription(bean.getPatternDescription());
		}
		
		/*
		 * set select functions
		 */
		
		//first get input names
		//TODO this only adds the last events from a set (view) as causal ancestor
		Object inputNames = null;
		if (representation instanceof PatternSimple) {
			//get input name defined in pattern as input names
			inputNames = ((PatternSimple)representation).getInputName();
		}
		else if (representation instanceof PatternComplex) {
			//get the new event names of the two source patterns as input names
			Vector<PatternOutputReference> vec = new Vector<PatternOutputReference>(2);
			PatternComplex cp = (PatternComplex) representation;
			vec.add(new PatternOutputReference(cp.getFirstSelectFunctionNumber(), cp.getFirstPatternID(), this.controller));
			vec.add(new PatternOutputReference(cp.getSecondSelectFunctionNumber(), cp.getSecondPatternID(), this.controller));
			
			inputNames = vec;
		}
		else if (representation instanceof PatternRepetitive) {
			//get the event to count as input name
			Vector<PatternOutputReference> vec = new Vector<PatternOutputReference>(1);
			PatternRepetitive rp = (PatternRepetitive) representation;
			vec.add(new PatternOutputReference(rp.getSelectFunctionNumber(), rp.getPatternToRepeatID(), this.controller));
			
			inputNames = vec;
		}
		//no input names for timer patterns, causality does not make sense there
		
		if (bean.getSelectFunctions().getSelectFunctionArray().length > 0) {
			SelectFunctionType[] selFunctions = bean.getSelectFunctions().getSelectFunctionArray();
			
			for (SelectFunctionType selectFunction : selFunctions) {
				HashSet<Object> pn = representation.getPropertyNames();
				SelFunction sf = this.parseSelectFunction(selectFunction, pn, inputNames);
				representation.addSelectFunction(sf);
			}
		}
	}
	

	/**
	 * parses the content of AbstractViewPatterns
	 * 
	 * @param representation internal representation of the pattern
	 * @param bean bean of the pattern
	 */
	private void parseViewPattern(AViewPattern representation, AbstractViewPatternType bean) {
		//set view
		if (bean.isSetView()) {
			representation.setView(this.parseView(bean.getView()));
		}
		else {
			//all view as standard
			DataView view = new DataView();
			view.setViewName(Constants.VIEW_ALL_NAME);
			representation.setView(view);
		}
	}
	

	/**
	 * parses the content of AbstractGuardedViewPatterns
	 * 
	 * @param representation internal representation of the pattern
	 * @param bean bean of the pattern
	 */
	private void parseGuardedViewPattern(AGuardedViewPattern representation, AbstractGuardedViewPatternType bean) {
		//set guard
		if (bean.isSetGuard()) {
			representation.setGuard(this.parseGuard(bean.getGuard(), representation.getPropertyNames()));
		}
	}
	

	/**
	 * parses a single select function
	 * 
	 * @return an internal representation for the select function
	 */
	@SuppressWarnings("unchecked")
	private SelFunction parseSelectFunction(SelectFunctionType selectFunction, HashSet<Object> propertyNames, Object inputNames) {
		SelFunction result = new SelFunction(this.controller);
		
		/*
		 * parse the select function standard properties
		 */

		//set the newEventName
		result.setNewEventName(selectFunction.getNewEventName()
				.replaceAll(":", "__").replaceAll("\\.", "_"));
		
		//set output name
		if (selectFunction.isSetOutputName()) {
			result.setOutputName(selectFunction.getOutputName().trim());
		}
		
		//set create causality
		if (selectFunction.isSetCreateCausality()) {
			result.setCreateCausality(selectFunction.getCreateCausality());
			
			//set inputs for causal ancestors
			if (inputNames != null) {
				if (inputNames instanceof Vector<?>) {
					result.setInputReferences((Vector<PatternOutputReference>) inputNames);
				}
//				else if (inputNames instanceof String) {
//					result.setInputName((String) inputNames);
//				}
			}
		}
		
		/*
		 * parse the function itself and its parameters
		 */

		//check SelectEvent
		if (selectFunction.isSetSelectEvent()) {
			result.setFunctionName(Constants.FUNC_SELECT_EVENT_NAME);
			
			SelectEvent se = selectFunction.getSelectEvent();
			
			result.addFunctionParameter(Constants.SELECT_PARAM_EVENT_NAME, se.getEventName());
		}
		
		//check SelectProperty
		else if (selectFunction.isSetSelectProperty()) {
			result.setFunctionName(Constants.FUNC_SELECT_PROPERTY_NAME);
			
			SelectProperty sp = selectFunction.getSelectProperty();
			
			result.addFunctionParameter(Constants.SELECT_PARAM_PROPERTY_NAME, sp.getPropertyName());
			
			if (!propertyNames.contains(sp.getPropertyName())) {
				propertyNames.add(sp.getPropertyName());
			}
		}
		
		//check SelectSum
		else if (selectFunction.isSetSelectSum()) {
			result.setFunctionName(Constants.FUNC_SELECT_SUM_NAME);
			
			SelectSum sum = selectFunction.getSelectSum();
			
			result.addFunctionParameter(Constants.SELECT_PARAM_PROPERTY_NAME, sum.getPropertyName());
			
			if (!propertyNames.contains(sum.getPropertyName())) {
				propertyNames.add(sum.getPropertyName());
			}
		}
		
		//check SelectAvg
		else if (selectFunction.isSetSelectAvg()) {
			result.setFunctionName(Constants.FUNC_SELECT_AVG_NAME);
			
			SelectAvg avg = selectFunction.getSelectAvg();
			
			result.addFunctionParameter(Constants.SELECT_PARAM_PROPERTY_NAME, avg.getPropertyName());
			
			if (!propertyNames.contains(avg.getPropertyName())) {
				propertyNames.add(avg.getPropertyName());
			}
		}
		
		//check SelectMax
		else if (selectFunction.isSetSelectMax()) {
			result.setFunctionName(Constants.FUNC_SELECT_MAX_NAME);
			
			SelectMax max = selectFunction.getSelectMax();
			
			result.addFunctionParameter(Constants.SELECT_PARAM_PROPERTY_NAME, max.getPropertyName());
			
			if (!propertyNames.contains(max.getPropertyName())) {
				propertyNames.add(max.getPropertyName());
			}
		}
		
		//check SelectMin
		else if (selectFunction.isSetSelectMin()) {
			result.setFunctionName(Constants.FUNC_SELECT_MIN_NAME);
			
			SelectMin min = selectFunction.getSelectMin();
			
			result.addFunctionParameter(Constants.SELECT_PARAM_PROPERTY_NAME, min.getPropertyName());
			
			if (!propertyNames.contains(min.getPropertyName())) {
				propertyNames.add(min.getPropertyName());
			}
		}
		
		//check SelectCount
		else if (selectFunction.isSetSelectCount()) {
			result.setFunctionName(Constants.FUNC_SELECT_COUNT_NAME);
		}
		
		//check NotifyOnSelect
		else if (selectFunction.isSetNotifyOnSelect()) {
			result.setFunctionName(Constants.FUNC_NOTIFY_ON_SELECT_NAME);
			
			NotifyOnSelect nos = selectFunction.getNotifyOnSelect();
			result.addFunctionParameter(Constants.SELECT_PARAM_MESSAGE_NAME, nos.getMessage());
		}
		
		//else user defined function
		else {
			EMLParser.logger.info("parsing user defined sel function");
			UserDefinedSelectFunction udsf = selectFunction.getUserDefinedSelectFunction();
			
			result.setFunctionName(udsf.getName());
			
			UserParameterType[] parameters = udsf.getFunctionParameters().getFunctionParameterArray();
			
			for (UserParameterType param : parameters) {
				result.addFunctionParameter(param.getUserParameterName(), param.getUserParameterValue());
			}
		}
		
		return result;
	}
	

	/**
	 * parses a view
	 * 
	 * @param view
	 * @return an internal representation for the view
	 */
	private DataView parseView(ViewType view) {
		DataView result = new DataView();
		
		/*
		 * parse view
		 */

		//check LengthView
		if (view.isSetLengthView()) {
			result.setViewName(Constants.VIEW_LENGTH_NAME);
			
			LengthView lv = view.getLengthView();
			
			if (lv.isSetIsBatch()) {
				result.setBatch(lv.getIsBatch());
			}
			result.addParameter(Constants.VIEW_PARAM_EVENT_COUNT_NAME, lv.getEventCount());
		}
		
		//check TimeView
		else if (view.isSetTimeView()) {
			result.setViewName(Constants.VIEW_TIME_NAME);
			
			TimeView tv = view.getTimeView();
			
			if (tv.isSetIsBatch()) {
				result.setBatch(tv.getIsBatch());
			}
			result.addParameter(Constants.VIEW_PARAM_DURATION_NAME, this.parseTimerDuration(tv.getDuration()));
		}
		
		//check TimeLengthView
		else if (view.isSetTimeLengthView()) {
			result.setViewName(Constants.VIEW_TIME_LENGTH_NAME);
			
			TimeLengthView tlv = view.getTimeLengthView();
			
			if (tlv.isSetIsBatch()) {
				result.setBatch(tlv.getIsBatch());
			}
			result.addParameter(Constants.VIEW_PARAM_EVENT_COUNT_NAME, tlv.getEventCount());
			result.addParameter(Constants.VIEW_PARAM_DURATION_NAME, this.parseTimerDuration(tlv.getDuration()));
		}
		
		//check AllView
		else if (view.isSetAllView()) {
			result.setViewName(Constants.VIEW_ALL_NAME);
		}
		
		//else user defined view
		else {
			UserDefinedView udv = view.getUserDefinedView();
			
			result.setViewName(udv.getName());
			
			UserParameterType[] parameters = udv.getViewParameters().getViewParameterArray();
			
			for (UserParameterType param : parameters) {
				result.addParameter(param.getUserParameterName(), param.getUserParameterValue());
			}
		}
		
		return result;
	}
	

	/**
	 * parses a guard
	 * 
	 * @param guard
	 * @return an internal representation for the guard
	 */
	private PatternGuard parseGuard(GuardType guard, HashSet<Object > propertyNames) {
		PatternGuard result = new PatternGuard();
		result.setFilter(guard.getFilter(), propertyNames);
		
		return result;
	}
	

	/**
	 * parses a property restriction
	 * 
	 * @param restriction
	 * @return an internal representation for the property restriction
	 */
	private PropRestriction parsePropertyRestriction(EventAttributeType restriction, HashSet<Object> propertyNames) {
		PropRestriction result = new PropRestriction();
		
		//property name
		String propName = restriction.getName().trim(); 
		
		/*
		 * parse 
		 * 	"<EventName>/<PropName>/<NestedPropName>"
		 * to
		 * 	"<PropName>.<NestedPropName>"
		 * 
		 * (nested names are optional, 
		 *  event name and at least one
		 *  property name are mandatory)
		 */
		propName = propName.substring(propName.indexOf("/") + 1);
		propName = propName.replaceAll("/", ".");
		
		result.setName(propName);
		
		if (!propertyNames.contains(propName)) {
			propertyNames.add(propName);
		}
		
		//property value
		String valueString = restriction.getValue().toString();
		
		//TODO: parse SWE Common
		
		try {
			//cut of start
			int index = valueString.indexOf(">");
			valueString = valueString.substring(index + 1);
			
			//cut of end
			index = valueString.lastIndexOf("<");
			valueString = valueString.substring(0, index);
			
			result.setValue("\"" + valueString + "\"");
		}
		catch (Exception ex) {
			result.setValue("");
		}
		
//		String[] segments = valueString.split(SWE_VALUE_TAG);
//		if (segments.length == 2) {
//			result.setValue(segments[1].split("</")[0]);
//		}
//		else {
//			result.setValue("");
//		}
		
		return result;
	}
	

	/**
	 * parses the complex pattern operator
	 * 
	 * @param pattern
	 * @return an internal representation for the operator
	 */
	private PatternOperator parsePatternOperator(ComplexPattern pattern) {
		PatternOperator result = new PatternOperator();
		
		/*
		 * parse structural operators
		 */
		if (pattern.isSetStructuralOperator()) {
			StructuralOperator so = pattern.getStructuralOperator();
			//CAUSE
			if (so.isSetCAUSE()) {
				result.setName(Constants.OPERATOR_CAUSE_NAME);
			}
			//PARALLEL
			else if (so.isSetPARALLEL()) {
				result.setName(Constants.OPERATOR_PARALLEL_NAME);
			}
			//BEFORE
			else {
				result.setName(Constants.OPERATOR_BEFORE_NAME);
			}
		}
		
		/*
		 * parse logical operators
		 */
		else if (pattern.isSetLogicaloperator()) {
			Logicaloperator lo = pattern.getLogicaloperator();
			//AND
			if (lo.isSetAND()) {
				result.setName(Constants.OPERATOR_AND_NAME);
			}
			//AND_NOT
			else if (lo.isSetANDNOT()) {
				result.setName(Constants.OPERATOR_AND_NOT_NAME);
			}
			//OR
			else {
				result.setName(Constants.OPERATOR_OR_NAME);
			}
		}
		
		/*
		 * parse user defined operators
		 */
		else {
			//user defined
			UserDefinedOperatorType uop = pattern.getUserDefindeBinaryOperator();
			result.setName(uop.getName());
		}
		return result;
	}
	

	/**
	 * parses a xs:duration to long(ms)
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
	

	/**
	 * @return the parsed patterns
	 */
	public HashMap<String, APattern> getPatterns() {
		return this.patterns;
	}
}