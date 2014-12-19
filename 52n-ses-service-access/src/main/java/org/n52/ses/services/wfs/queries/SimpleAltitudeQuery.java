/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
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

import net.opengis.fes.x20.DWithinDocument;
import net.opengis.fes.x20.DistanceBufferType;
import net.opengis.fes.x20.FilterDocument;
import net.opengis.fes.x20.FilterType;
import net.opengis.fes.x20.MeasureType;
import net.opengis.fes.x20.PropertyIsEqualToDocument;
import net.opengis.fes.x20.SpatialOpsDocument;
import net.opengis.fes.x20.SpatialOpsType;
import net.opengis.fes.x20.ValueReferenceDocument;
import net.opengis.wfs.x20.MemberPropertyType;
import net.opengis.wfs.x20.SimpleFeatureCollectionDocument;
import net.opengis.wfs.x20.SimpleFeatureCollectionType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.services.wfs.WFSAdHocGetFeatureQuery;
import org.n52.ses.services.wfs.WFSConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aero.aixm.schema.x51.AbstractAIXMTimeSliceType;
import aero.aixm.schema.x51.RouteSegmentType;

public class SimpleAltitudeQuery extends WFSAdHocGetFeatureQuery{

	private static final Logger logger = LoggerFactory.getLogger(SimpleAltitudeQuery.class);
	
	public SimpleAltitudeQuery(String featureType, int maxFeatureCount,
			String gmlIdentifier, XmlObject altitudeFeature) {
		super(featureType, maxFeatureCount);
		this.filter = createFilter(gmlIdentifier, altitudeFeature);
	}
	
	public SimpleAltitudeQuery(String featureType, int maxFeatureCount,
			String gmlIdentifier, SpatialOpsType altitudeFilter) {
		super(featureType, maxFeatureCount);
		SpatialOpsDocument spatial = SpatialOpsDocument.Factory.newInstance();
		insertXml(spatial.addNewSpatialOps(), altitudeFilter);
		this.filter = createFilter(gmlIdentifier, spatial);
	}

	private FilterDocument createFilter(String gmlIdentifier,
			XmlObject altitudeFilterPart) {
		
		XmlObject altitudeFilter;
		if (altitudeFilterPart instanceof SpatialOpsDocument) {
			altitudeFilter = altitudeFilterPart;
		}
		else {
			XmlObject altitudeOperand;
			if (!(altitudeFilterPart instanceof SimpleFeatureCollectionDocument)) {
				altitudeOperand  = wrapWithFeatureCollection(altitudeFilterPart);
			} else {
				altitudeOperand = altitudeFilterPart;
			}
			
			altitudeFilter = createDWithinFilter("wfs-aixm:extentOf(.)", altitudeOperand);
		}

		PropertyIsEqualToDocument propertyIsEqualTo = createPropertyIsEqualTo(GML_IDENTIFIER, gmlIdentifier);
		
		FilterDocument filDoc = FilterDocument.Factory.newInstance();
		FilterType filter = filDoc.addNewFilter();
		filter.set(createAndFilter(propertyIsEqualTo, altitudeFilter));

		return filDoc;
	}


	private XmlObject wrapWithFeatureCollection(XmlObject altitudeFeature) {
		SimpleFeatureCollectionDocument collectionDoc = SimpleFeatureCollectionDocument.Factory.newInstance();
		SimpleFeatureCollectionType simpleCollection = collectionDoc.addNewSimpleFeatureCollection();
		MemberPropertyType member = simpleCollection.addNewMember();
		
		if (altitudeFeature instanceof RouteSegmentType) {
			insertXml(member, ((RouteSegmentType) altitudeFeature).getTimeSliceArray(0));
		}
		else if (altitudeFeature instanceof AbstractAIXMTimeSliceType) {
			try {
				insertXml(member, XmlObject.Factory.parse(altitudeFeature.xmlText(WFSConnector.requestOptions.setSaveOuter())));
			} catch (XmlException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return collectionDoc;
	}

	private DWithinDocument createDWithinFilter(String string,
			XmlObject altitudeFeatureTimeSlice) {
		DWithinDocument dWithinDoc = DWithinDocument.Factory.newInstance();
		DistanceBufferType dWithin = dWithinDoc.addNewDWithin();
		
		insertXml(dWithin, altitudeFeatureTimeSlice);
		
		ValueReferenceDocument valueRef = ValueReferenceDocument.Factory.newInstance();
		valueRef.setValueReference(string);
		insertXml(dWithin, valueRef);
		
//		dWithin.set(altitudeFeatureTimeSlice);
		
		MeasureType distance = MeasureType.Factory.newInstance();
		distance.setUom("m");
		distance.setDoubleValue(10000.0);
		dWithin.setDistance(distance);
		
		return dWithinDoc;
	}

}
