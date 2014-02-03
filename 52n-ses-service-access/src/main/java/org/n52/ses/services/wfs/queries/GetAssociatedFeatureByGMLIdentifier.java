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
