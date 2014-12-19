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
package org.n52.ses.wsbr.soaphandler;

import java.util.Date;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.notification.impl.SimpleNotificationProducer;
import org.n52.ses.api.common.WsbrConstants;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.wsbr.PublisherEndpoint;
import org.n52.ses.wsbr.RegisterPublisherHandlerConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 * @see SimpleNotificationProducer for developer help
 */
public class PublisherResponse {
	private PublisherEndpoint endpoint;
	private Date time;

	/**
	 * 
	 * Constructor
	 *
	 * @param endpoint endpoint of the response
	 * @param time time of the response
	 */
	public PublisherResponse(PublisherEndpoint endpoint, Date time) {
		this.endpoint = endpoint;
		this.time = time;
	}
	
	/**
	 * 
	 * @return a new advertise response document
	 */
	public Element toXML() {
		return toXML(XmlUtils.createDocument());
	}

	/**
	 * creates an advertise response
	 * @param doc the response content
	 * @return an {@link Element} for the response
	 */
	public Element toXML(Document doc) {
		Element root = XmlUtils.createElement(doc,
				RegisterPublisherHandlerConstants.ADVERTISE_RESPONSE_QNAME);
		
		final EndpointReference producerReference = this.endpoint.getPublisherReference();
		
		if(this.time != null) {
			XmlUtils.setElement(root, WsnConstants.TERMINATION_TIME_QNAME, this.time);
		}
		
		XmlUtils.setElement(root, WsbrConstants.PUBLISHER_REGISTRATION_REFERENCE,
				producerReference.toXML());
		XmlUtils.setElement(root, WsnConstants.CONSUMER_QNAME,
				ConfigurationRegistry.getInstance().getSesPortTypeEPR().toXML());
		
		return root;
	}
}
