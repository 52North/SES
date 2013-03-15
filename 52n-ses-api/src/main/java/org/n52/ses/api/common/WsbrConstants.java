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
package org.n52.ses.api.common;

import javax.xml.namespace.QName;

/**
 * Constants for WS-BrokeredNotification
 * 
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 * 
 */
public interface WsbrConstants {
	
	/**
	 * URI of the namespace
	 */
	public static final String NAMESPACE_URI = "http://docs.oasis-open.org/wsn/br-2";

	/**
	 * standard namespace prefix
	 */
	public static final String PREFIX = "wsnt";

	/**
	 * QName for the CreationTime
	 */
	public static final QName CREATION_QNAME = new QName(NAMESPACE_URI, "CreationTime", PREFIX);

	//public static final QName CURRENT_TIME_QNAME = new QName(NAMESPACE_URI, "CurrentTime", PREFIX);

	/**
	 * URI for the publisher reference
	 */
	public static final String PUBLISHER_REFERENCE_URI = NAMESPACE_URI+"/PublisherReference";
	
	/**
	 * QName of the publisher reference
	 */
	public static final QName PUBLISHER_REFERENCE_QNAME = new QName(NAMESPACE_URI, "PublisherReference", PREFIX);
	
	/**
	 * Qname of the Demand element
	 */
	public static final QName DEMAND = new QName(NAMESPACE_URI, "Demand", PREFIX);
	
	/**
	 * QName of the topic
	 */
	public static final QName TOPIC = new QName(NAMESPACE_URI, "Topic", PREFIX);

	/**
	 * QName of the register publisher
	 */
	public static final QName REGISTER_PUBLISHER = new QName(NAMESPACE_URI,"RegisterPublisher");
	
	/**
	 * the register publisher URI
	 */
	public static final String REGISTER_PUBLISHER_URI = NAMESPACE_URI + "/RegisterPublisher";

	/**
	 * the publisher endpoint URI
	 */
	public static final String PUBLISHER_ENDPOINT_URI = NAMESPACE_URI+"/PublisherEndpoint";
	
	/**
	 * QName of the publisher registration reference
	 */
	public static final QName PUBLISHER_REGISTRATION_REFERENCE = new QName(NAMESPACE_URI,"PublisherRegistrationReference");
	
	/**
	 * QName of the consumer reference
	 */
	public static final QName CONSUMER_REFERENCE_QNAME = new QName(NAMESPACE_URI,"ConsumerReference");
}
