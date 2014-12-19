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
package org.n52.ses.eml.v002.pattern;

import org.n52.ses.api.eml.ILogicController;


/**
 * Representation of a reference to a pattern output.
 * 
 * @author Thomas Everding
 *
 */
public class PatternOutputReference {
	
	private int selectFunctionNumber;
	
	private String patternID;
	
	private ILogicController controller;
	
	private String newEventName = "";
	
	
	/**
	 * 
	 * Constructor
	 *
	 * @param selectFuncgtionNumber the select function number of the output
	 * @param patternID the ID of the pattern
	 * @param controller the logic controller to resolve the reference
	 */
	public PatternOutputReference(int selectFuncgtionNumber, String patternID, ILogicController controller) {
		this.selectFunctionNumber = selectFuncgtionNumber;
		this.patternID = patternID;
		this.controller = controller;
	}


	
	/**
	 * @return the selectFunctionNumber
	 */
	public int getSelectFunctionNumber() {
		return this.selectFunctionNumber;
	}


	
	/**
	 * @return the patternID
	 */
	public String getPatternID() {
		return this.patternID;
	}


	
	/**
	 * @return the newEventName as resolved by the controller. may return an empty string if the reference cannot (yet) be resolved
	 */
	public String getNewEventName() {
		if (this.newEventName.equals("") || this.newEventName.equals("null")) {
			//resolve reference
			this.newEventName = this.controller.getNewEventName(this.patternID, this.selectFunctionNumber);
			
			//check result
			if (this.newEventName.equals("null")) {
				return "";
			}
		}
		
		return this.newEventName;
	}

}
