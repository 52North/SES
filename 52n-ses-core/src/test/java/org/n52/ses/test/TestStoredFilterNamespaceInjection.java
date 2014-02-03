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
package org.n52.ses.test;

import java.io.IOException;

import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.Filter;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.ses.common.test.ConfigurationRegistryMockup;
import org.n52.ses.storedfilters.StoredFilterHandler;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class TestStoredFilterNamespaceInjection {

	private static final Logger logger = LoggerFactory.getLogger(TestStoredFilterNamespaceInjection.class);
	
	@Before
	public void init() {
		ConfigurationRegistryMockup.init();
		if (ConfigurationRegistry.getInstance().getFilterEngine() == null)
			throw new IllegalStateException("FilterEngine is required for this test.");
	}
	
	@Test public void
	shouldInjectNamespaceIntoTemplate()
			throws XmlException, IOException, SoapFault {
		new StoredFilterInstanceMockup().initialize();
		
		StoredFilterHandler handler = new StoredFilterHandler();
		Filter instance = handler.newInstance(readXml());
		logger.info("Filter toXML: {}", instance.toXML());
		XmlObject instanceXml = XmlObject.Factory.parse(instance.toXML());
		logger.info("Filter XML via XMLBeans: {}", instanceXml.xmlText());
		Assert.assertTrue("Prefix om: not injected!", instanceXml.xmlText().contains("xmlns:om"));
	}

	private Element readXml() throws XmlException, IOException {
		XmlObject xo = XmlObject.Factory.parse(getClass().getResourceAsStream("StoredFilter.xml"));
		return (Element) xo.getDomNode().getFirstChild();
	}
	
}
