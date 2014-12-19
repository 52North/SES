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

package org.n52.ses.eml.v002.pattern;

import java.util.HashMap;

import org.n52.ses.eml.v002.Constants;
import org.n52.ses.eml.v002.filterlogic.EMLParser;
import org.n52.ses.api.eml.ILogicController;


/**
 * represents a repetitive pattern
 * 
 * @author Thomas Everding
 *
 */
public class PatternRepetitive extends APattern{ 
	
	private int repetitionCount;
	
	private String patternToRepeatID;
	
	private ILogicController controller;

	private int selectFunctionNumber;

	private String inputEventName;

	private EMLParser parser;
	
	/**
	 * @return the repetitionCount
	 */
	public int getRepetitionCount() {
		return this.repetitionCount;
	}

	/**
	 * @param repetitionCount the repetitionCount to set
	 */
	public void setRepetitionCount(int repetitionCount) {
		this.repetitionCount = repetitionCount;
	}

	/**
	 * @return the patternToRepeatID
	 */
	public String getPatternToRepeatID() {
		return this.patternToRepeatID;
	}

	/**
	 * @param patternToRepeatID the patternToRepeatID to set
	 */
	public void setPatternToRepeatID(String patternToRepeatID) {
		this.patternToRepeatID = patternToRepeatID;
	}

	@Override
	public Statement[] createEsperStatements() {
		//two statements needed per select function
		Statement[] result = new Statement[this.selectFunctions.size() +1];
		
		if (this.controller != null) {
			//get input event name
			this.inputEventName = this.controller.getNewEventName(this.patternToRepeatID, this.selectFunctionNumber);			
		}
		
		else if (this.parser != null) {
			this.inputEventName = getNewEventNameWithParser(/*this.patternToRepeatID, */this.selectFunctionNumber);
			
		}

		
		/*
		 * build statements
		 */
		String selectClause;
		String fromClause;
		SelFunction sel;
		Statement stat;
		
		//first statement: counting
		selectClause = Constants.EPL_SELECT
					   + " * ";
		fromClause = Constants.EPL_FROM
					 + " "
					 + this.inputEventName
					 + ".win:length_batch("
					 + this.repetitionCount
					 + ")";
		
		stat = new Statement();
		stat.setSelectFunction(null);
		stat.setStatement(selectClause + fromClause);
		
		//add first statement
		result[0] = stat;

		//further statements: selecting
		fromClause = Constants.EPL_FROM
		 + " "
		 + Constants.EPL_PATTERN
		 + " [every ("
		 + this.inputEventName
		 + Constants.REPETIVITE_COUNT_EVENT_SUFFIX
		 + " -> "
		 + this.inputEventName
		 + " = "
		 + this.inputEventName
		 + ")]";
		
		for (int i = 1; i < result.length; i ++) {
			sel = this.selectFunctions.get(i - 1);
			
			selectClause = Constants.EPL_SELECT
			   + " ";
			
//			if (sel.getFunctionName().equals(Constants.FUNC_SELECT_EVENT_NAME)) {
//				selectClause += inputEventName 
//								+ " as value ";
//			}
//			else {
			selectClause += sel.getSelectString(false);
//			}
			selectClause += " ";
			
			stat = new Statement();
			stat.setSelectFunction(sel);
			stat.setStatement(selectClause + fromClause);
			
			//add statement
			result[i] = stat;
		}
		
		return result;
	}
	
	
	/**
	 * Method for creating statements using just an EMLParser instead 
	 * of EsperController. (added by Matthes)
	 * 
	 * @param selFunctionNumber number of the select function
	 */
//	private String getNewEventNameWithParser(String secondPatternID2, int selectFunctionNumber) {
	private String getNewEventNameWithParser(int selFunctionNumber) {
		int sFN = selFunctionNumber;
		//get all patterns from parser
		HashMap<String, APattern> patterns = this.parser.getPatterns();
		
		//search for pattern
		if (!this.parser.getPatterns().containsKey(this.patternID)) {
			return null;
		}
		APattern pattern = patterns.get(this.patternID);
		
		//search for select function
		if (!(pattern.getSelectFunctions().size() > sFN)) {
			if (!(pattern.getSelectFunctions().size() >= 0)) {
				return null;
			}
			//set number to 0
			sFN = 0;
		}
		
		//return newEventName
		return pattern.getSelectFunctions().get(sFN).getNewEventName();
	}

	@Override
	public Statement[] createEsperStatements(EMLParser p) {
		this.parser = p;
		return createEsperStatements();
	}

	/**
	 * @param logicController the controller to set
	 */
	public void setController(ILogicController logicController) {
		this.controller = logicController;
	}

	
	/**
	 * sets the select function to use
	 * 
	 * @param selectFunctionNumber number of the select function
	 */
	public void setSelectFunctionToUse(int selectFunctionNumber) {
		this.selectFunctionNumber = selectFunctionNumber;
	}

	/**
	 * @return the inputEventName
	 */
	public String getInputEventName() {
		return this.inputEventName;
	}

	
	/**
	 * @return the selectFunctionNumber of the pattern to repeat
	 */
	public int getSelectFunctionNumber() {
		return this.selectFunctionNumber;
	}
}
