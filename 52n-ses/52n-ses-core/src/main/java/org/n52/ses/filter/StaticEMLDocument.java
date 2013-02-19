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
package org.n52.ses.filter;

import java.math.BigInteger;

import net.opengis.eml.x002.*;
import net.opengis.eml.x002.AbstractPatternType.SelectFunctions;
import net.opengis.eml.x002.EMLDocument.EML;
import net.opengis.eml.x002.EMLDocument.EML.SimplePatterns;
import net.opengis.eml.x002.SelectFunctionType.SelectEvent;
import net.opengis.eml.x002.SimplePatternType.Input;
import net.opengis.eml.x002.ViewType.LengthView;
import net.opengis.fes.x20.FilterType;


/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class StaticEMLDocument {
	
	/**
	 * Generates a simple EML type holding one
	 * simple pattern with the given FilterType.
	 * 
	 * @param ogcFilter an OGC filter encoding 2.0 filter
	 * 
	 * @return an EML document with a simple pattern using the filter as guard
	 */
	public static EML getWrapperDocument(FilterType ogcFilter) {
		EML eml = EML.Factory.newInstance();
		
		eml.addNewComplexPatterns();
		eml.addNewRepetitivePatterns();
		eml.addNewTimerPatterns();
		
		SimplePatterns simple = eml.addNewSimplePatterns();
		
		SimplePatternType pattern = simple.addNewSimplePattern();
		
		pattern.setPatternID("defaultSimplePattern");
		Input input = Input.Factory.newInstance();
		input.setExternalInput("input");
		
		pattern.setInput(input);
		
		SelectFunctions funcs = pattern.addNewSelectFunctions();
		SelectFunctionType func = funcs.addNewSelectFunction();
		
		func.setNewEventName("");
		func.setOutputName("output");
		SelectEvent selEv = func.addNewSelectEvent();
		selEv.setEventName("sensorStream");
		
		ViewType view = pattern.addNewView();
		LengthView length = view.addNewLengthView();
		length.setEventCount(new BigInteger("1"));
		
		GuardType guard = pattern.addNewGuard();
		guard.setFilter(ogcFilter);
		
		pattern.addNewPropertyRestrictions();
		
		return eml;
	}

}
