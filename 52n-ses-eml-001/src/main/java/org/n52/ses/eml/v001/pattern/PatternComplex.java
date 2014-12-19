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

import org.n52.ses.api.eml.ILogicController;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.eml.v001.Constants;
import org.n52.ses.eml.v001.filterlogic.EMLParser;
import org.n52.ses.eml.v001.filterlogic.esper.customFunctions.MethodNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * representation of a complex pattern
 * 
 * @author Thomas Everding
 * 
 */
public class PatternComplex extends AGuardedViewPattern {

	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(PatternComplex.class);
	
	private PatternOperator operator;

	private String firstPatternID;

	private String secondPatternID;

	private int firstSelectFunctionNumber;

	private int secondSelectFunctionNumber;

	private ILogicController controller;

	private long maxListeningDuration = -1;

	private EMLParser parser;


	/**
	 * @return the operator
	 */
	public PatternOperator getOperator() {
		return this.operator;
	}


	/**
	 * sets the operator
	 * 
	 * @param operator the operator to set
	 */
	public void setOperator(PatternOperator operator) {
		this.operator = operator;
	}


	/**
	 * @return the first pattern ID
	 */
	public String getFirstPatternID() {
		return this.firstPatternID;
	}


	/**
	 * @param firstPatternID the first pattern ID to set
	 */
	public void setFirstPatternID(String firstPatternID) {
		this.firstPatternID = firstPatternID;
	}


	/**
	 * @return the second pattern ID
	 */
	public String getSecondPatternID() {
		return this.secondPatternID;
	}


	/**
	 * @param secondPatternID the second pattern ID to set
	 */
	public void setSecondPatternID(String secondPatternID) {
		this.secondPatternID = secondPatternID;
	}


	/**
	 * @return the firstSelectFunctionNumber
	 */
	public int getFirstSelectFunctionNumber() {
		return this.firstSelectFunctionNumber;
	}


	/**
	 * @param firstSelectFunctionNumber the firstSelectFunctionNumber to set
	 */
	public void setFirstSelectFunctionNumber(int firstSelectFunctionNumber) {
		this.firstSelectFunctionNumber = firstSelectFunctionNumber;
	}


	/**
	 * @return the secondSelectFunctionNumber
	 */
	public int getSecondSelectFunctionNumber() {
		return this.secondSelectFunctionNumber;
	}


	/**
	 * @param secondSelectFunctionNumber the secondSelectFunctionNumber to set
	 */
	public void setSecondSelectFunctionNumber(int secondSelectFunctionNumber) {
		this.secondSelectFunctionNumber = secondSelectFunctionNumber;
	}


	/**
	 * @param logicController the controller to set
	 */
	public void setController(ILogicController logicController) {
		this.controller = logicController;
	}


	/**
	 * @return the maxListeningDuration (ms)
	 */
	public long getMaxListeningDuration() {
		return this.maxListeningDuration;
	}


	/**
	 * @param maxListeningDuration the maxListeningDuration to set (ms)
	 */
	public void setMaxListeningDuration(long maxListeningDuration) {
		this.maxListeningDuration = maxListeningDuration;
	}


	@Override
	public Statement[] createEsperStatements() {
		Statement[] result = new Statement[this.selectFunctions.size()];

		String firstInputEventName = "";
		String secondInputEventName = "";
		if (this.controller != null ) {
			//get first input event name
			firstInputEventName = this.controller.getNewEventName(this.firstPatternID, this.firstSelectFunctionNumber);

			//get second input event name
			secondInputEventName = this.controller.getNewEventName(this.secondPatternID, this.secondSelectFunctionNumber);

		}

		else if (this.parser != null) {
			firstInputEventName = getNewEventNameWithParser(this.firstSelectFunctionNumber) ;

			secondInputEventName = getNewEventNameWithParser(this.secondSelectFunctionNumber);
		}

		else {
			//something went wrong
			return null;
		}
		
		/*
		 * build statements
		 */
		String selectClause;
		String fromClause;
		String whereClause = "";
		SelFunction sel;
		Statement stat;

		if (this.guard != null) { //TODO workaround
			//build where clause (use maxListeningDuration and the Guard!)
			
			String pName = null;
			try {
				//TODO: does only work with one select function...
				HashMap<String, Object> params = this.selectFunctions.get(0).getFunctionParameters();
				
				if (params.containsKey(Constants.SELECT_PARAM_PROPERTY_NAME)) {
					pName = params.get(Constants.SELECT_PARAM_PROPERTY_NAME).toString();
				}
			}
			catch (Throwable t) {
				PatternComplex.logger.warn("could not load parameters from select function");
			}
			whereClause = this.guard.createStatement(true);
			
			if (pName != null) pName = pName.trim();
			//TODO: wtf? this worked and seems not be changed, but now the (overloaded?) method is gone?!
//			whereClause = this.guard.createStatement(true, pName);
		}

		if (this.operator.equals(Constants.OPERATOR_CAUSE_NAME)
				|| this.operator.equals(Constants.OPERATOR_BEFORE_NAME)) { //TODO operator.name ? evtl parallel statt before
			/*
			 * CAUSE and PARALLEL have to work different (using guards with user functions)
			 */

			//build where clause
			if (whereClause.equals("")) {
				//no previous where clause, create one
				whereClause = Constants.EPL_WHERE
				+ " "
				+ this.buildCausalCheckString(firstInputEventName, secondInputEventName);
			}
			else {
				//append to existing where clause
				whereClause += "and"
					+ this.buildCausalCheckString(firstInputEventName, secondInputEventName);
			}

			//build from clause (always use and)
			fromClause = Constants.EPL_FROM
			+ " "
			+ Constants.EPL_PATTERN
			+ " [every ((("
			+ firstInputEventName
			+ "="
			+ firstInputEventName
			+ ") and ("
			+ secondInputEventName
			+ "="
			+ secondInputEventName
			+ ")) "
			+ this.buildPatternTimerClause()
			+ ")]";
		}
		else {
			//build from clause
			fromClause = this.buildFromClause(firstInputEventName, secondInputEventName);
		}
		
		for (int i = 0; i < result.length; i++) {
			sel = this.selectFunctions.get(i);

			//build select clause
			selectClause = Constants.EPL_SELECT
			+ " "
			+ sel.getSelectString(false)
			+ " ";
			
			//build statement
			stat = new Statement();
			stat.setSelectFunction(sel);
			stat.setStatement(selectClause + fromClause + whereClause);
			stat.setView(this.view);

			//add statement to result
			result[i] = stat;
		}

		return result;
	}


	/**
	 * Method for creating statements using just an EMLParser instead 
	 * of EsperController. (added by Matthes Rieke)
	 */
	private String getNewEventNameWithParser(int selectFunctionNumber) {
		int selFuncNumber = selectFunctionNumber;
		//get all patterns from parser
		HashMap<String, APattern> patterns = this.parser.getPatterns();

		//search for pattern
		if (!this.parser.getPatterns().containsKey(this.patternID)) {
			PatternComplex.logger.warn("pattern ID (" + this.patternID + ") not found");
			return null;
		}
		APattern pattern = patterns.get(this.patternID);

		//search for select function
		if (!(pattern.getSelectFunctions().size() > selFuncNumber)) {
			if (!(pattern.getSelectFunctions().size() >= 0)) {
				PatternComplex.logger.warn("No select function and therefore no 'newEventName' defined in pattern '" + this.patternID
						+ "'. Can not use this pattern in a repetitive pattern.");
				return null;
			}
			PatternComplex.logger.warn("The pattern with the id '" + this.patternID + "does not define at least "
					+ selFuncNumber + " selectfunctions. Using first select function instead.");

			//set number to 0
			selFuncNumber = 0;
		}

		//return newEventName
		return pattern.getSelectFunctions().get(selFuncNumber).getNewEventName();
	}


	@Override
	public Statement[] createEsperStatements(EMLParser emlParser) {
		this.parser = emlParser;
		return createEsperStatements();
	}

	/**
	 * build a part of the where clause for CAUSE and PARALLEL operators
	 * 
	 * @param secondInputEventName name of the possible causal ancestor
	 * @param firstInputEventName name of the event with the causal vector
	 * 
	 * @return the part of the where clause without 'where'
	 */
	private String buildCausalCheckString(String firstInputEventName, String secondInputEventName) {
		String result = "";

		//add operator
		if (this.operator.equals(Constants.OPERATOR_CAUSE_NAME)) {
			//CAUSE
			result += MethodNames.IS_CAUSAL_ANCESTOR_NAME
			+ "(";
		}
		else {
			//PARALLEL
			result += MethodNames.IS_NOT_CAUSAL_ANCESTOR_NAME
			+ "(";
		}

		//add parameters
		result += firstInputEventName
		+ ", "
		+ secondInputEventName
		+ "."
		+ MapEvent.CAUSALITY_KEY
		+ ")";

		return result;
	}


	/**
	 * builds a from clause
	 * 
	 * @param firstInputEventName name of the first input event
	 * @param secondInputEventName name of the second input event
	 * @return
	 */
	private String buildFromClause(String firstInputEventName, String secondInputEventName) {
		String clause = Constants.EPL_FROM
		+ " "
		+ Constants.EPL_PATTERN
		+ " [every ((("
		+ firstInputEventName
		+ "="
		+ firstInputEventName
		+ ") "
		+ this.getOperatorString()
		+ " ("
		+ secondInputEventName
		+ "="
		+ secondInputEventName
		+ ")) "
		+ this.buildPatternTimerClause()
		+ ")]"
		
		/*
		 * see esper-reference 2.3.0 chapter 5.2.2:
		 * "In addition, a data window view can be declared onto a pattern."
		 */
		+ this.view.getViewString()
		+" ";
		
		return clause;
	}


	/**
	 * build the timer:within clause
	 * 
	 * @return the pattern guard (timer:within)
	 */
	private String buildPatternTimerClause() {
		if(this.maxListeningDuration < 0) {
			//no within clause
			return "";
		}

		//build clause
		String result = Constants.EPL_WHERE
		+ " timer:within("
		+ this.maxListeningDuration
		+ " msec)";
		return result;
	}


	/**
	 * 
	 * @return the esper representation for the operator
	 */
	private String getOperatorString() {
		String op = "";

		String opName = this.operator.getName();

		//AND
		if (opName.equals(Constants.OPERATOR_AND_NAME)) {
			op = "and";
		}

		//AND NOT
		else if (opName.equals(Constants.OPERATOR_AND_NOT_NAME)) {
			op = ") and not (";
			/*
			 * The parentheses are necessary for correct execution.
			 * The 'every' operator must only be used for the first pattern.
			 */
		}

		//OR
		else if (opName.equals(Constants.OPERATOR_OR_NAME)) {
			op = "or";
		}

		//BEFORE
		else if (opName.equals(Constants.OPERATOR_BEFORE_NAME)) {
			op = "->";
		}

		//CAUSE
		else if (opName.equals(Constants.OPERATOR_CAUSE_NAME)) {
			op = "and";
			//functionality must be implemented as guard using 'in'
		}

		//PARALLEL
		else if (opName.equals(Constants.OPERATOR_PARALLEL_NAME)) {
			op = "and";
			//functionality must be implemented as guard using 'in'
		}

		//user defined operators here
		else {
			PatternComplex.logger.warn("the operator '" + opName + "' is not supported");
		}

		return op;
	}
}
