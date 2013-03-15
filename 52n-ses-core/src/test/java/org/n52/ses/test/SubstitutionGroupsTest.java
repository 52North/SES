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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import net.opengis.fes.x20.AndDocument;
import net.opengis.fes.x20.BinaryLogicOpType;
import net.opengis.fes.x20.FilterDocument;
import net.opengis.fes.x20.FilterType;
import net.opengis.gml.FeatureCollectionDocument;
import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.GridCoverageDocument;
import net.opengis.gml.GridCoverageType;
import net.opengis.gml.StringOrRefType;
import net.opengis.sampling.x10.SamplingFeatureCollectionDocument;
import net.opengis.sampling.x10.SamplingFeatureDocument;
import net.opengis.sampling.x10.SamplingFeatureType;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.oxf.xmlbeans.parser.GMLAbstractFeatureCase;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubstitutionGroupsTest extends TestCase {
	
	private static final Logger logger = LoggerFactory.getLogger(SubstitutionGroupsTest.class);
	private static XmlOptions options;
	
	static {
		options = new XmlOptions();
		options.setSavePrettyPrint();
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("http://www.opengis.net/sampling/1.0", "sa");
		options.setSaveSuggestedPrefixes(prefixes);
		
		XMLBeansParser.registerLaxValidationCase(GMLAbstractFeatureCase.getInstance());
	}

	public static void main(String[] args) {
		new SubstitutionGroupsTest().testQualifySubstitutionGroups();
	}

	public void testQualifySubstitutionGroups() {
		createFeatureCollectionWithSamplingFeature();

		createFeatureCollectionWithGridCoverage();
		
		createFilterDocument();
	}

	
	private void createFeatureCollectionWithSamplingFeature() {
		FeatureCollectionDocument doc = FeatureCollectionDocument.Factory.newInstance();
		FeaturePropertyType member = doc.addNewFeatureCollection().addNewFeatureMember();

		SamplingFeatureDocument samplingFeature = SamplingFeatureDocument.Factory.newInstance();
		SamplingFeatureType feature2 = samplingFeature.addNewSamplingFeature();
		StringOrRefType desc = feature2.addNewDescription();
		desc.setStringValue("test");
		member.setFeature(feature2);
		
		logger.debug(doc.xmlText(options));
		
		XmlObject xo = XmlUtil.qualifySubstitutionGroup(member.getFeature(), SamplingFeatureDocument.type.getDocumentElementName(), SamplingFeatureType.type);
		if (xo == null) {
			logger.debug("Disconnected object.");
		}
		logger.debug(doc.xmlText(options));
		
		XmlUtil.qualifySubstitutionGroup(doc.getFeatureCollection(), SamplingFeatureCollectionDocument.type.getDocumentElementName());
		logger.debug(doc.xmlText(options));
		assertTrue(XMLBeansParser.validate(doc).isEmpty());
	}
	
	private void createFeatureCollectionWithGridCoverage() {
		FeatureCollectionDocument doc = FeatureCollectionDocument.Factory.newInstance();
		FeaturePropertyType member = doc.addNewFeatureCollection().addNewFeatureMember();
		XmlUtil.qualifySubstitutionGroup(member.addNewFeature(), GridCoverageDocument.type.getDocumentElementName(), GridCoverageType.type);
		
		XmlUtil.qualifySubstitutionGroup(doc.getFeatureCollection(), SamplingFeatureCollectionDocument.type.getDocumentElementName());
		logger.debug(doc.xmlText(options));
		assertTrue(XMLBeansParser.validate(doc).isEmpty());
	}
	
	private void createFilterDocument() {
		FilterDocument filterDoc = FilterDocument.Factory.newInstance();
		FilterType filter = filterDoc.addNewFilter();
		XmlObject andOp = XmlUtil.qualifySubstitutionGroup(filter.addNewLogicOps(), AndDocument.type.getDocumentElementName(), BinaryLogicOpType.type);
		XmlUtil.qualifySubstitutionGroup(((BinaryLogicOpType) andOp).addNewLogicOps(), AndDocument.type.getDocumentElementName(), BinaryLogicOpType.type);
		logger.debug(filterDoc.xmlText(options));
	}
}
