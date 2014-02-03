/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.ses.services.wfs;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import net.opengis.fes.x20.FilterDocument;
import net.opengis.wfs.x20.GetFeatureDocument;
import net.opengis.wfs.x20.GetFeatureType;
import net.opengis.wfs.x20.QueryDocument;
import net.opengis.wfs.x20.QueryType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

public abstract class WFSQuery {

	public static final String GML_PREFIX = "gml";
	public static final String AIXM_PREFIX = "aixm";
	public static final String GML_IDENTIFIER = GML_PREFIX+":identifier";

	public abstract XmlObject createQuery() throws IOException, XmlException;
	
	protected XmlObject buildGetFeatureRequest(String featureType, FilterDocument filter, int featureCount){
		
		// getFeature
		GetFeatureDocument getFeatureDoc = GetFeatureDocument.Factory.newInstance();
		GetFeatureType getFeature = getFeatureDoc.addNewGetFeature();
		
		// Query
		QueryDocument queryDoc = QueryDocument.Factory.newInstance();
		QueryType query = queryDoc.addNewQuery();
		
		// filter
		if (filter != null){
			query.set(filter);
		}
		
		ArrayList<String> typeList = new ArrayList<String>();
		typeList.add(featureType);
		query.setTypeNames(typeList );
		query.setHandle("Q01");
		
		// set request parameters
		getFeature.set(queryDoc);
		getFeature.setService("WFS");
		getFeature.setVersion("2.0.0");
		getFeature.setOutputFormat("application/gml+xml; version=3.2");
		if (featureCount != 0){
			getFeature.setCount(BigInteger.valueOf(featureCount));
		}
		
		// add namespaces for attribute 
		XmlCursor c = getFeature.newCursor();
		c.toNextToken();

		c.insertNamespace(AIXM_PREFIX, "http://www.aixm.aero/schema/5.1");
		c.toNextToken();

		c.insertNamespace(GML_PREFIX, "http://www.opengis.net/gml/3.2");
		c.toNextToken();
		
		c.insertNamespace("xlink", "http://www.w3.org/1999/xlink");
		c.dispose();
		
		return getFeatureDoc;
	}
}
