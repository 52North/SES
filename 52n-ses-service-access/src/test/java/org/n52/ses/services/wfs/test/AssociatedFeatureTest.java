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
package org.n52.ses.services.wfs.test;

import net.opengis.fes.x20.BinaryComparisonOpType;
import net.opengis.fes.x20.ComparisonOpsType;

import org.apache.xmlbeans.XmlObject;
import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.io.parser.OWS8Parser;
import org.n52.ses.services.wfs.queries.GetAssociatedFeatureByGMLIdentifier;


public class AssociatedFeatureTest {
	
	@Test
	public void testAssociatedFeatureQueryCreation() {
		GetAssociatedFeatureByGMLIdentifier request = new GetAssociatedFeatureByGMLIdentifier(OWS8Parser.AIXM_APRON_KEY, 10, "test");
		ComparisonOpsType comparison = request.getFilter().getFilter().getComparisonOps();
		if (comparison instanceof BinaryComparisonOpType) {
			BinaryComparisonOpType bop = (BinaryComparisonOpType) comparison;
			XmlObject exp = bop.getExpressionArray(0);
			Assert.assertTrue("invalid filter expression", exp.xmlText().contains("wfs:valueOf(*/*/aixm:associatedApron)/*/gml:identifier"));
		} else {
			Assert.fail("no PropertyIsEqualTo found.");
		}
	}

}
