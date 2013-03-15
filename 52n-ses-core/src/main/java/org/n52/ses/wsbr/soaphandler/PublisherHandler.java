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
