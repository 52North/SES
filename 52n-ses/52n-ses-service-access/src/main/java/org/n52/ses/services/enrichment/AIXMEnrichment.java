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
package org.n52.ses.services.enrichment;

import java.util.ArrayList;
import java.util.List;

import org.n52.ses.api.IEnrichment;
import org.n52.ses.api.event.MapEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for enriching AIXM messages with additional information from a WFS.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de
 *
 */
public class AIXMEnrichment implements IEnrichment {

	private static final Logger logger = LoggerFactory.getLogger(AIXMEnrichment.class);
	private List<EnrichmentHandler> handlers;


	/**
	 * Constructor, initializes connectors to WFSs. 
	 */
	public AIXMEnrichment() {
		this.handlers = new ArrayList<EnrichmentHandler>();
		this.handlers.add(new WFSHandler());
		this.handlers.add(new WPSHandler());
	}


	@Override
	public MapEvent enrichEvent(MapEvent mapEvent) {
		List<EnrichmentHandler> handler = resolveHandler((String) mapEvent.get(MapEvent.FEATURE_TYPE_KEY));

		String identifier = (String) mapEvent.get(MapEvent.IDENTIFIER_VALUE_KEY);
		String featureType = (String) mapEvent.get(MapEvent.FEATURE_TYPE_KEY);

		for (EnrichmentHandler enrichmentHandler : handler) {
			try {
				if (enrichmentHandler.enrichFeature(mapEvent, identifier, featureType))
					break;
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}
		}

		return mapEvent;
	}


	private List<EnrichmentHandler> resolveHandler(String featureType) {
		List<EnrichmentHandler> resolved = new ArrayList<EnrichmentHandler>();

		if (featureType == null) return resolved;

		for (EnrichmentHandler h : this.handlers) {
			if (h.canHandle(featureType)) {
				resolved.add(h);
			}
		}
		return resolved;
	}


}