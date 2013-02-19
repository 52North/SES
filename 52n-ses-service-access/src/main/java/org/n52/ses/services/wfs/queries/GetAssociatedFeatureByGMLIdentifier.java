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
package org.n52.ses.services.wfs.queries;

import java.util.HashMap;
import java.util.Map;

import net.opengis.fes.x20.FilterDocument;

import org.n52.ses.io.parser.OWS8Parser;
import org.n52.ses.services.wfs.WFSAdHocGetFeatureQuery;

public class GetAssociatedFeatureByGMLIdentifier extends WFSAdHocGetFeatureQuery {

	private static Map<String, AssociatedFeature> associations = new HashMap<String, AssociatedFeature>();
	
	static {
		associations.put(OWS8Parser.AIXM_RUNWAY_KEY, new AssociatedFeature(OWS8Parser.AIXM_RUNWAY_KEY,
				"aixm:RunwayElement", "aixm:associatedRunway"));
		associations.put(OWS8Parser.AIXM_TAXIWAY_KEY, new AssociatedFeature(OWS8Parser.AIXM_TAXIWAY_KEY,
				"aixm:TaxiwayElement", "aixm:associatedTaxiway"));
		associations.put(OWS8Parser.AIXM_APRON_KEY, new AssociatedFeature(OWS8Parser.AIXM_APRON_KEY,
				"aixm:ApronElement", "aixm:associatedApron"));
	}
	
	public GetAssociatedFeatureByGMLIdentifier(String featureType, int maxFeatureCount,
			String identifier) {
		super(resolveAssociationFeature(featureType), maxFeatureCount);
		this.filter = createAssociatedFeatureFilter(featureType, identifier);
	}

	private static String resolveAssociationFeature(String featureType) {
		if (associations.containsKey(featureType)) {
			AssociatedFeature associate = associations.get(featureType);
			return associate.getAssociatedFeature();
		}
		throw new UnsupportedOperationException(featureType + " is not supported for non-spatial feature query.");
	}

	private FilterDocument createAssociatedFeatureFilter(String featureType,
			String identifier) {
		if (associations.containsKey(featureType)) {
			AssociatedFeature associate = associations.get(featureType);
			FilterDocument doc = createFilterByValueReference("wfs:valueOf(*/*/"+
					associate.getAssociatedFeatureElement() +")/*/gml:identifier", identifier);
			return doc;
		}
		return null;
	}

	
	public static class AssociatedFeature {

		private String feature;
		private String associatedFeature;
		private String associatedFeatureElement;

		public AssociatedFeature(String feature, String associatedFeature, String associatedFeatureElement) {
			this.feature = feature;
			this.associatedFeature = associatedFeature;
			this.associatedFeatureElement = associatedFeatureElement;
		}

		public String getFeature() {
			return feature;
		}

		public String getAssociatedFeature() {
			return associatedFeature;
		}

		public String getAssociatedFeatureElement() {
			return associatedFeatureElement;
		}
		

	}
	

}
