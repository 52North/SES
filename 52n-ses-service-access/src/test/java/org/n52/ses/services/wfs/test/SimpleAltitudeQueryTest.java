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
package org.n52.ses.services.wfs.test;

import java.math.BigInteger;
import java.util.Collection;

import junit.framework.Assert;

import net.opengis.fes.x20.DWithinDocument;
import net.opengis.fes.x20.DistanceBufferType;
import net.opengis.fes.x20.MeasureType;
import net.opengis.fes.x20.SpatialOpsDocument;
import net.opengis.gml.x32.CurveSegmentArrayPropertyType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.LineStringSegmentDocument;
import net.opengis.gml.x32.LineStringSegmentType;
import net.opengis.gml.x32.TimePeriodDocument;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.common.test.ConfigurationRegistryMockup;
import org.n52.ses.services.wfs.WFSAdHocGetFeatureQuery;
import org.n52.ses.services.wfs.queries.SimpleAltitudeQuery;

import aero.aixm.schema.x51.CodeVerticalReferenceType;
import aero.aixm.schema.x51.CurvePropertyType;
import aero.aixm.schema.x51.CurveType;
import aero.aixm.schema.x51.InterpretationDocument;
import aero.aixm.schema.x51.RouteSegmentDocument;
import aero.aixm.schema.x51.RouteSegmentTimeSlicePropertyType;
import aero.aixm.schema.x51.RouteSegmentTimeSliceType;
import aero.aixm.schema.x51.RouteSegmentType;
import aero.aixm.schema.x51.ValDistanceVerticalType;

public class SimpleAltitudeQueryTest {

	@Test
	public void testQueryCreation() throws Exception {
		ConfigurationRegistryMockup.init();
		
		RouteSegmentType route = createRouteSegment();
		Collection<XmlError> errors = XMLBeansParser.validate(route);
		Assert.assertTrue("RouteSegment is not valid! "+ errors, errors.isEmpty());
		
		SimpleAltitudeQuery query = new SimpleAltitudeQuery("aixm:Airspace", 1, "0003D6AF-8619-4165-9FAA-8299303AA418", route.getTimeSliceArray(0).getRouteSegmentTimeSlice());
		Assert.assertTrue("Could not create simple altitude query!", query.createQuery() != null);
		
		query = new SimpleAltitudeQuery("aixm:Airspace", 1, "0003D6AF-8619-4165-9FAA-8299303AA418", createDWithin());
		Assert.assertTrue("Could not create simple altitude query!", query.createQuery() != null);
//		Map<String,String> user = new HashMap<String, String>();
//		user.put(WFSConnector.USER_KEY, "IfGI");
//		user.put(WFSConnector.PASSWORD_KEY, "test");
//		WFSConnector wfs = new WFSConnector("http://91.221.120.150:13871/cadas-aimdb/wfs", true, user);
//		XmlObject[] result = wfs.executeQuery(query);
//		
//		System.out.println(result);
	}

	private SpatialOpsDocument createDWithin() {
		DWithinDocument doc = DWithinDocument.Factory.newInstance();
		DistanceBufferType dw = doc.addNewDWithin();
		dw.setExpression(XmlObject.Factory.newInstance());
		dw.setDistance(MeasureType.Factory.newInstance());
		return doc;
	}

	private RouteSegmentType createRouteSegment() {
		RouteSegmentDocument routeDoc = RouteSegmentDocument.Factory.newInstance();
		RouteSegmentType route = routeDoc.addNewRouteSegment();
		route.setId("route-idididi");
		RouteSegmentTimeSlicePropertyType slice = route.addNewTimeSlice();
		RouteSegmentTimeSliceType routeSlice = slice.addNewRouteSegmentTimeSlice();
		routeSlice.setInterpretation(InterpretationDocument.Interpretation.BASELINE);
		routeSlice.setSequenceNumber(1);
		routeSlice.setCorrectionNumber(0);
		routeSlice.setId("route-id-1");
		
		ValDistanceVerticalType upper = routeSlice.addNewUpperLimit();
		upper.setUom("FL");
		upper.setStringValue("260"); //999
		CodeVerticalReferenceType upperRef = routeSlice.addNewUpperLimitReference();
		upperRef.setStringValue("STD");

		ValDistanceVerticalType lower = routeSlice.addNewLowerLimit();
		lower.setUom("FL");
		lower.setStringValue("100"); //245
		CodeVerticalReferenceType lowerRef = routeSlice.addNewLowerLimitReference();
		lowerRef.setStringValue("STD");
		
		TimePeriodDocument timeDoc = TimePeriodDocument.Factory.newInstance();
		TimePeriodType time = timeDoc.addNewTimePeriod();
		TimePositionType begin = time.addNewBeginPosition();
		begin.setStringValue(new DateTime().toString(ISODateTimeFormat.dateTime()));
		TimePositionType end = time.addNewEndPosition();
		end.setStringValue(new DateTime().toString(ISODateTimeFormat.dateTime()));
		time.setId("time-id-1");
		
		WFSAdHocGetFeatureQuery.insertXml(routeSlice.addNewValidTime(), timeDoc);
		XmlUtil.qualifySubstitutionGroup(routeSlice.getValidTime().getAbstractTimePrimitive(), timeDoc.schemaType().getDocumentElementName());
		
		CurvePropertyType curveExtent = routeSlice.addNewCurveExtent();
		CurveType curve = curveExtent.addNewCurve();
		curve.setId("curve-id-1");
		curve.setSrsName("urn:ogc:def:crs:OGC:1.3:CRS84");
		CurveSegmentArrayPropertyType segments = curve.addNewSegments();
		
		LineStringSegmentDocument segmentDoc = LineStringSegmentDocument.Factory.newInstance();
		LineStringSegmentType segment = segmentDoc.addNewLineStringSegment();
		DirectPositionListType posList = segment.addNewPosList();
		posList.setStringValue("7.478475 49.494366667 8.228444444 48.970891667");
		posList.setSrsDimension(BigInteger.valueOf(2));
		posList.setCount(BigInteger.valueOf(2));
		
		WFSAdHocGetFeatureQuery.insertXml(segments, segmentDoc);
		XmlUtil.qualifySubstitutionGroup(segments.getAbstractCurveSegmentArray(0), segmentDoc.schemaType().getDocumentElementName());
		
		return route;
	}
}
