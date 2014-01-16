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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.muse.core.Resource;
import org.apache.muse.core.SimpleResource;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.common.test.SimpleWSResourceMockup;
import org.n52.ses.storedfilters.StoredFilterInstance;
import org.w3c.dom.Element;

public class StoredFilterInstanceMockup extends StoredFilterInstance {
	
	private SimpleResource resource;

	public StoredFilterInstanceMockup() {
		resource = new SimpleWSResourceMockup() {
			
			@Override
			public EndpointReference getEndpointReference() {
				try {
					EndpointReference er = new EndpointReference(new URI("http://test.test"));
					er.addParameter(STORED_FILTER_DESCRIPTION_QNAME, readXml());
					return er;
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (XmlException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			
		};
		
		try {
			resource.initialize();
		} catch (SoapFault e) {
			e.printStackTrace();
		}
	}
	
	protected Element readXml() throws XmlException, IOException {
		XmlObject xo = XmlObject.Factory.parse(getClass().getResource("StoredFilterDescriptionMockup.xml"));
		return (Element) xo.getDomNode().getFirstChild();
	}

	@Override
	public void initialize() throws SoapFault {
		super.initialize();
	}
	
	@Override
	public Resource getResource() {
		return resource;
	}

}
