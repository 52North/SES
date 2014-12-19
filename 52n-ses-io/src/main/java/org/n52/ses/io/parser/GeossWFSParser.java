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
package org.n52.ses.io.parser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.GMLParseException;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/**
 * A class providing parsing capabilities for
 * GEOSS WFS 1.0.0 server.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class GeossWFSParser extends AbstractParser {

	private static final String WFS_NAMESPACE = "http://www.opengis.net/wfs";

	private static final String MS_NAMESPACE = "http://mapserver.gis.umn.edu/mapserver";
	private static final String GML_NAMESPACE = "http://www.opengis.net/gml";

	private static final QName FEATURE_COLLECTION_NAME = new QName(WFS_NAMESPACE, "FeatureCollection");

	private static final QName DROUGHT_AFFECTED_NAME = new QName(MS_NAMESPACE, "Norm_Drought_Affected_Area_by_NUTS");



	private void parseDroughtAffectedArea(XmlObject area,
			List<MapEvent> result) throws ParseException, GMLParseException, XmlException {
		/*
		 * no time to parse, use server time
		 */
		long time = System.currentTimeMillis();
		MapEvent newEvent = new MapEvent(time, time);

		/*
		 * ms:NUTS_ID
		 */
		String id = XmlUtil.stripText(area.selectChildren(new QName(MS_NAMESPACE, "NUTS_ID")));
		newEvent.put("NUTS_ID", id);

		String tmp = null;
		/*
		 * ms:NUTS_LEVEL
		 */
		tmp = XmlUtil.stripText(area.selectChildren(new QName(MS_NAMESPACE, "NUTS_LEVEL")));
		if (tmp  != null) {
			double level = Double.parseDouble(tmp);
			newEvent.put("NUTS_LEVEL", level);
		}

		/* 
		 * ms:NUTS_AREA
		 */
		tmp = XmlUtil.stripText(area.selectChildren(new QName(MS_NAMESPACE, "NUTS_AREA")));
		if (tmp  != null) {
			double narea = Double.parseDouble(tmp);
			newEvent.put("NUTS_AREA", narea);
		}

		/*
		 * ms:SUMMARIZED_DROUGHT_AREA
		 */ 
		tmp = XmlUtil.stripText(area.selectChildren(new QName(MS_NAMESPACE,
		"SUMMARIZED_DROUGHT_AREA")));
		if (tmp  != null) {
			double sumDroughtArea = Double.parseDouble(tmp);
			newEvent.put("SUMMARIZED_DROUGHT_AREA", sumDroughtArea);
		}

		/*
		 * ms:NORMALIZED_DROUGHT_AFFECTED_AREA
		 */
		tmp = XmlUtil.stripText(area.selectChildren(new QName(MS_NAMESPACE,
		"NORMALIZED_DROUGHT_AFFECTED_AREA")));
		if (tmp  != null) {
			double normDroughtArea = Double.parseDouble(tmp);
			newEvent.put("NORMALIZED_DROUGHT_AFFECTED_AREA", normDroughtArea);
		}

		/*
		 * geometry
		 */
		XmlObject[] geoms = area.selectChildren(new QName(MS_NAMESPACE, "msGeometry"));
		if (geoms.length == 1) {
			Geometry geom = GML31Parser.parseGeometry(XmlObject.Factory.parse(geoms[0].toString()));
			newEvent.put(MapEvent.GEOMETRY_KEY, geom);
		}

		result.add(newEvent);
	}

	@Override
	public boolean accept(NotificationMessage message) {
		QName qn = null;
		for (Object ob : message.getMessageContentNames()) {
			if (ob instanceof QName) {
				qn = (QName) ob;
				if (qn.equals(new QName(GeossWFSParser.WFS_NAMESPACE, "FeatureCollection"))) {
					return true;
				}
				else if (qn.equals(new QName(MS_NAMESPACE, "Norm_Drought_Affected_Area_by_NUTS"))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<MapEvent> parse(NotificationMessage message) throws Exception {
		List<MapEvent> result = null;

		Element content = message.getMessageContent(FEATURE_COLLECTION_NAME);
		if (content != null) {
			XmlObject collection = XMLBeansParser.parse(content, false);

			XmlObject[] collections = collection.selectChildren(FEATURE_COLLECTION_NAME);

			if (collections.length != 1) {
				throw new IllegalArgumentException("Currently only one wfs:FeatureCollection is supported.");
			}

			XmlObject[] members = collections[0].selectChildren(new QName(GML_NAMESPACE, "featureMember"));

			for (XmlObject memb : members) {
				XmlObject[] nuts = memb.selectChildren(DROUGHT_AFFECTED_NAME);
				if (nuts.length != 1) {
					throw new Exception("Currently only one ms:Norm_Drought_Affected_Area_by_NUTS is supported.");
				}

				parseDroughtAffectedArea(nuts[0], result);
			}
		}
		else {
			content = message.getMessageContent(new QName(MS_NAMESPACE, "Norm_Drought_Affected_Area_by_NUTS"));
			if (content == null) {
				return null;
			}
			XmlObject area = XMLBeansParser.parse(content, false);

			result = new ArrayList<MapEvent>();

			for (XmlObject ar : area.selectChildren(DROUGHT_AFFECTED_NAME)) {
				parseDroughtAffectedArea(ar, result);	
			}
			
		}


		return result;
	}

	@Override
	protected String getName() {
		return "GeossWFSParser [Drought Affected NUTS Area]";
	}

}
