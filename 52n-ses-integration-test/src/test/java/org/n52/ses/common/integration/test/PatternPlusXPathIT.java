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
package org.n52.ses.common.integration.test;

import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ses.adapter.client.Subscription;
import org.n52.oxf.util.web.HttpClient;
import org.n52.oxf.util.web.HttpClientException;
import org.n52.oxf.util.web.SimpleHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternPlusXPathIT extends OvershootUndershootSubscriptionIT {
	
	private static final Logger logger = LoggerFactory.getLogger(PatternPlusXPathIT.class);
	
	@Override
	protected Subscription subscribe() throws OXFException,
			ExceptionReport, XmlException, IOException {
		String xml = readXmlContent("PatternPlusXPathSubscription.xml").replace("${consumer}", getConsumerUrl());
		
		URL host = ServiceInstance.getInstance().getHost();
		
		xml = xml.replace("${ses_host}", host.toExternalForm());
		logger.info("Subscription: {}", xml);
		
		HttpClient client = new SimpleHttpClient();
		HttpResponse result;
		try {
			result = client.executePost(host.toExternalForm(), xml);
		} catch (HttpClientException e) {
			throw new IOException(e);
		}
		
		XmlObject xo = XmlObject.Factory.parse(result.getEntity().getContent());
		logger.info(xo.xmlText());
		Subscription sub = new Subscription(null);
		sub.parseResponse(xo);
		return sub;
	}

}
