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

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import net.opengis.gml.v321.AbstractTimePrimitiveType;
import net.opengis.gml.v321.TimePeriodType;

import org.joda.time.DateTime;
import org.n52.aixm.roundtrip.AIXMUnmarshallerMarshaller;
import org.n52.epos.event.EposEvent;
import org.n52.epos.event.MapEposEvent;
import org.n52.epos.transform.EposTransformer;
import org.n52.epos.transform.TransformationException;
import org.w3c.dom.Element;

import aero.aixm.v510.AbstractAIXMFeatureType;
import aero.aixm.v510.NavaidTimeSlicePropertyType;
import aero.aixm.v510.NavaidTimeSliceType;
import aero.aixm.v510.NavaidType;
import aero.aixm.v510.event.EventTimeSlicePropertyType;
import aero.aixm.v510.event.EventTimeSliceType;
import aero.aixm.v510.event.EventType;
import aero.aixm.v510.event.NavaidExtensionType;
import aero.aixm.v510.message.AIXMBasicMessageType;
import aero.aixm.v510.message.BasicMessageMemberAIXMPropertyType;

public class AIXMJaxbTransformer implements EposTransformer {

	private static final QName BASIC_MESSAGE_QNAME = new QName("http://www.aixm.aero/schema/5.1/message", "AIXMBasicMessage");

	@Override
	public EposEvent transform(Object input) throws TransformationException {
		AIXMUnmarshallerMarshaller marshaller = new AIXMUnmarshallerMarshaller();
		try {
			@SuppressWarnings("unchecked")
			JAXBElement<AIXMBasicMessageType> aixm = (JAXBElement<AIXMBasicMessageType>) 
					marshaller.unmarshal(new DOMSource((Element) input), AIXMBasicMessageType.class);
			
			EposEvent result = parseAIXM(aixm.getValue());
			result.setOriginalObject(input);
			return result;
		} catch (JAXBException e) {
			throw new TransformationException(e);
		}
	}

	private EposEvent parseAIXM(AIXMBasicMessageType message) {
		EventType event = findEventType(message);
		
		if (event == null) {
			return null;
		}
		
		MapEposEvent result = parseEvent(event);
		
		includeMemberData(message.getHasMember(), result);
		
		return result;
	}

	private void includeMemberData(
			List<BasicMessageMemberAIXMPropertyType> hasMember,
			MapEposEvent result) {
		for (BasicMessageMemberAIXMPropertyType member : hasMember) {
			AbstractAIXMFeatureType value = member.getAbstractAIXMFeature().getValue();
			
			if (value instanceof NavaidType) {
				includeNavaidData((NavaidType) value, result);
			}
			
		}
	}

	private void includeNavaidData(NavaidType value, MapEposEvent result) {
		List<NavaidTimeSlicePropertyType> slice = value.getTimeSlice();
		
		if (slice != null && !slice.isEmpty()) {
			NavaidTimeSliceType timeSlice = slice.get(0).getNavaidTimeSlice();
			
			NavaidExtensionType extension = (NavaidExtensionType)
					timeSlice.getExtension().get(0).getAbstractNavaidExtension().getValue();
			
			if (extension.getTheEvent().get(0).getHref().endsWith((String) result.get("theEvent"))) {
				result.put("interpretation", timeSlice.getInterpretation());
				result.put(AIXMParser.DNOTAM_TYPE_KEY, "aixm:Navaid");
				result.put("identifier", value.getIdentifier().getValue());
			}
		}
	}

	private MapEposEvent parseEvent(EventType event) {
		List<EventTimeSlicePropertyType> slice = event.getTimeSlice();
		
		if (slice != null && !slice.isEmpty()) {
			EventTimeSliceType timeSlice = slice.get(0).getEventTimeSlice();
			
			AbstractTimePrimitiveType time = timeSlice.getValidTime().getAbstractTimePrimitive().getValue();
			
			if (time instanceof TimePeriodType) {
				TimePeriodType period = (TimePeriodType) time;
				DateTime begin = new DateTime(period.getBeginPosition().getValue().get(0));
				DateTime end = new DateTime(period.getEndPosition().getValue().get(0));
				
				MapEposEvent result = new MapEposEvent(begin.getMillis(), end.getMillis());
				result.put("theEvent", event.getIdentifier().getValue());
				
				return result;
			}
		}
		
		return null;
	}

	private EventType findEventType(AIXMBasicMessageType message) {
		for (BasicMessageMemberAIXMPropertyType member : message.getHasMember()) {
			if (member.getAbstractAIXMFeature().getValue() instanceof EventType) {
				return (EventType) member.getAbstractAIXMFeature().getValue();
			}
		}
		
		return null;
	}

	@Override
	public boolean supportsInput(Object input) {
		if (input instanceof Element) {
			Element elem = (Element) input;
			
			if (elem.getLocalName().equals(BASIC_MESSAGE_QNAME.getLocalPart())
					&& elem.getNamespaceURI().equals(BASIC_MESSAGE_QNAME.getNamespaceURI())) {
				return true;
			}
		}
		return false;
	}

}
