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
