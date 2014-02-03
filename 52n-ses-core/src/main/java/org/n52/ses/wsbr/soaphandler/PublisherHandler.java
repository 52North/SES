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
package org.n52.ses.wsbr.soaphandler;

import java.util.Date;

import org.apache.muse.core.routing.AbstractMessageHandler;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.resource.WsResource;
import org.apache.muse.ws.resource.lifetime.ScheduledTermination;
import org.apache.muse.ws.resource.lifetime.WsrlConstants;
import org.n52.ses.api.ws.IPublisherEndpoint;
import org.n52.ses.wsbr.PublisherEndpoint;
import org.n52.ses.wsbr.RegisterPublisherHandlerConstants;
import org.w3c.dom.Element;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 * 
 * @see SubscribeHandler
 */
public class PublisherHandler extends AbstractMessageHandler {

	/**
	 * 
	 * Constructor
	 *
	 */
	public PublisherHandler() {
		super(RegisterPublisherHandlerConstants.ADVERTISE_URI, RegisterPublisherHandlerConstants.ADVERTISE_QNAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.muse.core.routing.MessageHandler#fromXML(org.w3c.dom.Element)
	 */
	@Override
	public Object[] fromXML(Element xml) throws SoapFault {
		Publisher adv = new Publisher(xml);
		return adv.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.muse.core.routing.MessageHandler#toXML(java.lang.Object)
	 */
	@Override
	public Element toXML(Object result) throws SoapFault {
		WsResource resource = (WsResource) result;

		PublisherEndpoint endpointInstance = (PublisherEndpoint) resource.getCapability(IPublisherEndpoint.NAMESPACE_URI);

		/* add termination time to the response, if it exists */
        ScheduledTermination wsrl = (ScheduledTermination)resource.getCapability(WsrlConstants.SCHEDULED_TERMINATION_URI);
        Date time = null;
        
        if (wsrl != null)
            time = wsrl.getTerminationTime();

		PublisherResponse response = new PublisherResponse(endpointInstance,time);
		return response.toXML();
	}
}
