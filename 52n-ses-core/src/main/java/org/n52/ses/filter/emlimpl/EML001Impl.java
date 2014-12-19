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
package org.n52.ses.filter.emlimpl;

import java.math.BigInteger;

import net.opengis.eml.x001.EMLDocument;
import net.opengis.eml.x001.GuardType;
import net.opengis.eml.x001.SelectFunctionType;
import net.opengis.eml.x001.SimplePatternType;
import net.opengis.eml.x001.ViewType;
import net.opengis.eml.x001.AbstractPatternType.SelectFunctions;
import net.opengis.eml.x001.EMLDocument.EML;
import net.opengis.eml.x001.EMLDocument.EML.SimplePatterns;
import net.opengis.eml.x001.SelectFunctionType.SelectEvent;
import net.opengis.eml.x001.ViewType.LengthView;
import net.opengis.fes.x20.FilterType;

import org.apache.xmlbeans.XmlObject;
import org.n52.ses.api.eml.IEML;

public class EML001Impl implements IEML {

	
	private EMLDocument eml;

	public EML001Impl(XmlObject eml) {
		if (eml instanceof EMLDocument) {
			this.eml = (EMLDocument) eml;
		}
		else {
			throw new IllegalArgumentException("Only EML Version 0.0.1 allowed.");
		}
	}
	
	@Override
	public XmlObject getEMLDocumentInstance() {
		return this.eml;
	}
	
	@Override
	public XmlObject getEMLInstance() {
		return this.eml.getEML();
	}
	
	@Override
	public String toString() {
		return this.eml.toString();
	}
	
	public static EML001Impl generateStaticDocument(FilterType ogcFilter) {
		EMLDocument emlDoc = EMLDocument.Factory.newInstance();
		
		EML eml2 = emlDoc.addNewEML();
		eml2.addNewComplexPatterns();
		eml2.addNewRepetitivePatterns();
		eml2.addNewTimerPatterns();
		
		SimplePatterns simple = eml2.addNewSimplePatterns();
		
		SimplePatternType pattern = simple.addNewSimplePattern();
		
		pattern.setPatternID("defaultSimplePattern");
		pattern.setInputName("sensorStream");
		
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
		
		return new EML001Impl(emlDoc);
	}

}
