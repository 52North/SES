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
