/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.ses.wsn.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.muse.core.Resource;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.junit.Test;
import org.n52.ses.common.test.SimpleWSResourceMockup;
import org.n52.ses.storedfilters.StoredFilterHandler;
import org.n52.ses.storedfilters.StoredFilterInstance;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class StoredFilterTest {

	@Test
	public void testStoredFilter() throws IOException, SAXException, SoapFault {
		registerDummyStoredFilter();
		
		new StoredFilterHandler().newInstance(readFilter());
	}
	
	private void registerDummyStoredFilter() throws SoapFault {
		StoredFilterInstance instance = new StoredFilterInstance() {
			
			@Override
			public Resource getResource() {
				SimpleWSResourceMockup resource = new SimpleWSResourceMockup();
				EndpointReference epr;
				try {
					resource.initialize();
					epr = new EndpointReference(new URI("http://test.test"));
					Element desc = readDescription();
					epr.addParameter(STORED_FILTER_DESCRIPTION_QNAME, desc);
					resource.setEndpointReference(epr);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (SoapFault e) {
					e.printStackTrace();
				}
				
				return resource;
			}
		};
		instance.initialize();
	}

	protected Element readDescription() throws IOException, SAXException {
		return XmlUtils.createDocument(getClass().getResourceAsStream("storedFilterDescTest.xml")).getDocumentElement();
	}

	private Element readFilter() throws IOException, SAXException {
		return XmlUtils.createDocument(getClass().getResourceAsStream("storedFilterTest.xml")).getDocumentElement();
	}

	
}
