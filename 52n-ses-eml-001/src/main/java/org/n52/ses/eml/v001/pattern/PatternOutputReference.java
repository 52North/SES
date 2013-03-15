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
package org.n52.ses.eml.v001.pattern;

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
