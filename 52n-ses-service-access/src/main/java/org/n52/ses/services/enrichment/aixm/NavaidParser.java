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
package org.n52.ses.services.enrichment.aixm;

import java.util.ArrayList;
import java.util.List;

import net.opengis.gml.x32.DirectPositionType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.api.exception.GMLParseException;
import org.n52.ses.io.parser.GML32Parser;

import aero.aixm.schema.x51.NavaidDocument;
import aero.aixm.schema.x51.NavaidTimeSlicePropertyType;
import aero.aixm.schema.x51.NavaidType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/**
 * Parser of features of an aixm Navaid. 
 * Creates the bounding box of all its geometries.
 * 
 * @author Klaus Drerup <klaus.drerup@uni-muenster.de>
 *
 */
public class NavaidParser extends AbstractGeometryParser {


	/* (non-Javadoc)
	 * @see org.n52.ses.enrichment.parser.AIXMAbstractGeometryParser#parseGeometries(org.apache.xmlbeans.XmlObject)
	 */
	@Override
	protected List<Geometry> parseGeometries(XmlObject xmlO)
			throws XmlException, ParseException, GMLParseException {
		List<Geometry> geometries = new ArrayList<Geometry>();
		
		// parse the feature's xml-document
		NavaidDocument navaidDoc = NavaidDocument.Factory.parse(xmlO.xmlText());
		NavaidType navaid = navaidDoc.getNavaid();
	
		
		// geometry from location, defined by  a point
		for (NavaidTimeSlicePropertyType ts : navaid.getTimeSliceArray()) {
			if (ts.getNavaidTimeSlice().isSetLocation()) {
				if (ts.getNavaidTimeSlice().getLocation().getElevatedPoint().isSetPos()) {
					DirectPositionType pointPos = ts.getNavaidTimeSlice().getLocation().getElevatedPoint().getPos();
					geometries.add(GML32Parser.parseGeometry(pointPos));	
				}
			}
		}
		
		// parse bounded by
		if (geometries.size() == 0 && navaid.isSetBoundedBy()){
			geometries.add(super.parseBoundedBy(navaid.getBoundedBy()));
		}
		
		return geometries;
	}

}
