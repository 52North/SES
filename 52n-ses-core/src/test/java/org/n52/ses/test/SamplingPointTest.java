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
