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
