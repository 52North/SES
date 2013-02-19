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
package org.n52.ses.filter.dialects;

import java.util.List;


import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.common.SesConstants;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.io.parser.OWS8Parser;
import org.n52.ses.services.enrichment.WFSHandler;
import org.n52.ses.services.wfs.WFSQuery;
import org.n52.ses.services.wfs.queries.SimpleAltitudeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.opengis.fes.x20.DWithinDocument;
import net.opengis.fes.x20.DistanceBufferType;
import net.opengis.fes.x20.FilterDocument;
import net.opengis.fes.x20.SpatialOpsDocument;
import net.opengis.fes.x20.SpatialOpsType;

public class SimpleAltitudeQueryFilter extends CustomFESFilter {

	private static final Logger logger = LoggerFactory.getLogger(SimpleAltitudeQueryFilter.class);
	private static WFSHandler enrichment = new WFSHandler();

	private FilterDocument filter;
	private SpatialOpsDocument filterOperator;

	@Override
	public boolean canHandle(FilterDocument filter) {
		if (filter.getFilter().isSetSpatialOps()) {
			SpatialOpsType spatial = filter.getFilter().getSpatialOps();
			if (spatial instanceof DistanceBufferType) {
				String distance = ((DistanceBufferType) spatial).getExpression().xmlText();
				if (distance.contains("wfs-aixm:extentOf")) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean accepts(NotificationMessage message) {
		AbstractParser parser = new OWS8Parser();
		if (parser.accept(message)) {
			List<MapEvent> event;
			try {
				event = parser.parse(message);
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
				return false;
			}
			
			for (MapEvent mapEvent : event) {
				Object featureType = mapEvent.get(MapEvent.FEATURE_TYPE_KEY);
				Object identifier = mapEvent.get(MapEvent.IDENTIFIER_VALUE_KEY);
				if (identifier != null && featureType != null) {
					if (evaluateAltitudeQuery(featureType.toString(), identifier.toString())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean evaluateAltitudeQuery(String featureType, String gmlIdentifier) {
		WFSQuery altitudeQuery = new SimpleAltitudeQuery(featureType, 1, gmlIdentifier, this.filterOperator);
		try {
			XmlObject[] result = enrichment.executeQuery(altitudeQuery);
			return result != null && result.length > 0;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public Element toXML() {
		return toXML(XmlUtils.createDocument());
	}

	@Override
	public Element toXML(Document doc) {
		Element filter = XmlUtils.createElement(doc, WsnConstants.FILTER_QNAME);
		
		Element message = XmlUtils.createElement(doc, WsnConstants.MESSAGE_CONTENT_QNAME);
		message.appendChild(doc.importNode(this.filter.getDomNode().getFirstChild(), true));
		message.setAttribute(WsnConstants.DIALECT, SesConstants.SES_FILTER_LEVEL_2_DIALECT);

		filter.appendChild(message);

		return filter;
	}

	@Override
	public void initialize(FilterDocument filter) {
		this.filter = filter;
		this.filterOperator = prepareSpatialOperator();
	}

	private SpatialOpsDocument prepareSpatialOperator() {
		/*
		 * TODO support others
		 */
		DWithinDocument spatial = DWithinDocument.Factory.newInstance();
		
		SpatialOpsType spatialOps = this.filter.getFilter().getSpatialOps();
		DistanceBufferType dwithin = spatial.addNewDWithin();
		dwithin.set(spatialOps);
		dwithin.substitute(DWithinDocument.type.getDocumentElementName(), DWithinDocument.type);
		
		return spatial;
	}
	
}
