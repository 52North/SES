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
