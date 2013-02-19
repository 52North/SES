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
