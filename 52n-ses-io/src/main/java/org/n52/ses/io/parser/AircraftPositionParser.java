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
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.event.MapEvent;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Parser for the OWS8 AircraftPosition data.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class AircraftPositionParser extends AbstractParser {

	private static final QName MESSAGE_ROOT_QNAME = new QName("AircraftPositionUpdate");
	
	/**
	 * global key for the aircraft position
	 */
	public static final String POSITON = "aircraftPosition";
	
	/**
	 * global key for the aircraft call sign
	 */
	public static final String CALL_SIGN = "callSign";

	@Override
	public boolean accept(NotificationMessage message) {
		Element elem = message.getMessageContent(MESSAGE_ROOT_QNAME);
		if (elem != null) {
			return true;
		}
		return false;
	}

	@Override
	public List<MapEvent> parse(NotificationMessage message) throws Exception {
		ArrayList<MapEvent> events = new ArrayList<MapEvent>();
		
		Element root = message.getMessageContent(MESSAGE_ROOT_QNAME);
		XmlObject xobj = XmlObject.Factory.parse(root);
		
		XmlObject[] callsign = XmlUtil.selectPath("//CallSign", xobj);
		XmlObject[] pos = XmlUtil.selectPath("declare namespace gml='" +GML32Parser.GML32_NAMESPACE+ "'; //Position", xobj);
		
		String sign = "";
		if (callsign.length > 0) {
			sign = XmlUtil.stripText(callsign[0]);
		}
		
		Geometry geom = null;
		if (pos.length > 0) {
			geom = GML32Parser.parseGeometry(XmlObject.Factory.parse(pos[0].xmlText()));
		}
		long time = System.currentTimeMillis();
		
		MapEvent event = new MapEvent(time, time);
		event.put(AircraftPositionParser.CALL_SIGN, sign);
		event.put(AircraftPositionParser.POSITON, geom);
		events.add(event);
		
		return events;
	}

	@Override
	protected String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
