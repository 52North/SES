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
package org.n52.ses.test;

import java.util.Arrays;
import java.util.Collection;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlValidationError;
import org.n52.oxf.xmlbeans.parser.LaxValidationCase;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.tools.XmlUtil;

import net.opengis.gml.BoundingShapeType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.FeatureDocument;
import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.PointPropertyType;
import net.opengis.gml.PointType;
import net.opengis.sampling.x10.SamplingFeatureDocument;
import net.opengis.sampling.x10.SamplingFeatureType;
import net.opengis.sampling.x10.SamplingPointDocument;
import net.opengis.sampling.x10.SamplingPointType;

import junit.framework.TestCase;

public class SamplingPointTest extends TestCase {
	
	public void testSamplingPointCreation() {
		SamplingPointDocument sa = SamplingPointDocument.Factory.newInstance();
		SamplingPointType point = sa.addNewSamplingPoint();
		FeaturePropertyType feat = point.addNewSampledFeature();
		feat.setHref("ha");
		PointPropertyType pos = point.addNewPosition();;
		PointType posP = pos.addNewPoint();
		DirectPositionType posPPos = posP.addNewPos();
		posPPos.setListValue(Arrays.asList(52.0, 6.0));
		
		SamplingFeatureType feature = SamplingFeatureType.Factory.newInstance();
		feat.setFeature(feature);
		XmlUtil.qualifySubstitutionGroup(feat.getFeature(), SamplingFeatureDocument.type.getDocumentElementName());
		
		BoundingShapeType bb = point.addNewBoundedBy();
		EnvelopeType env = bb.addNewEnvelope();
		env.setSrsName("EPSG:4326");
		DirectPositionType low = env.addNewPos();
		low.setListValue(Arrays.asList(52.0, 7.0));
		DirectPositionType up = env.addNewPos();
		up.setListValue(Arrays.asList(53.0, 8.0));
		
		XMLBeansParser.registerLaxValidationCase(new LaxValidationCase() {
			@Override
			public boolean shouldPass(XmlValidationError xve) {
				if (xve.getExpectedQNames() != null &&
						xve.getExpectedQNames().contains(
								FeatureDocument.type.getDocumentElementName())) {
					return true;
				}
				return false;
			}

			@Override
			public boolean shouldPass(XmlError validationError) {
				if (validationError instanceof XmlValidationError) {
					return shouldPass((XmlValidationError) validationError);
				}
				return false;
			}
		});
		Collection<XmlError> err = XMLBeansParser.validate(sa);
		assertTrue(err.isEmpty());
		
		SamplingPointDocument parsedSa;
		try {
			parsedSa = SamplingPointDocument.Factory.parse(sa.toString());
			err = XMLBeansParser.validate(parsedSa);
			assertTrue(err.isEmpty());
		} catch (XmlException e) {
			e.printStackTrace();
		}
		
	}

}
