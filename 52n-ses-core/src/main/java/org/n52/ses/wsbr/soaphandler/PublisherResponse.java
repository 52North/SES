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
