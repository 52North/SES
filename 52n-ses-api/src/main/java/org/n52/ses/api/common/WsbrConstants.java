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
	String NAMESPACE_URI = "http://docs.oasis-open.org/wsn/br-2";

	/**
	 * standard namespace prefix
	 */
	String PREFIX = "wsnt";

	/**
	 * QName for the CreationTime
	 */
	QName CREATION_QNAME = new QName(NAMESPACE_URI, "CreationTime", PREFIX);

	//QName CURRENT_TIME_QNAME = new QName(NAMESPACE_URI, "CurrentTime", PREFIX);

	/**
	 * URI for the publisher reference
	 */
	String PUBLISHER_REFERENCE_URI = NAMESPACE_URI+"/PublisherReference";
	
	/**
	 * QName of the publisher reference
	 */
	QName PUBLISHER_REFERENCE_QNAME = new QName(NAMESPACE_URI, "PublisherReference", PREFIX);
	
	/**
	 * Qname of the Demand element
	 */
	QName DEMAND = new QName(NAMESPACE_URI, "Demand", PREFIX);
	
	/**
	 * QName of the topic
	 */
	QName TOPIC = new QName(NAMESPACE_URI, "Topic", PREFIX);

	/**
	 * QName of the register publisher
	 */
	QName REGISTER_PUBLISHER = new QName(NAMESPACE_URI,"RegisterPublisher");
	
	/**
	 * the register publisher URI
	 */
	String REGISTER_PUBLISHER_URI = NAMESPACE_URI + "/RegisterPublisher";

	/**
	 * the publisher endpoint URI
	 */
	String PUBLISHER_ENDPOINT_URI = NAMESPACE_URI+"/PublisherEndpoint";
	
	/**
	 * QName of the publisher registration reference
	 */
	QName PUBLISHER_REGISTRATION_REFERENCE = new QName(NAMESPACE_URI,"PublisherRegistrationReference");
	
	/**
	 * QName of the consumer reference
	 */
	QName CONSUMER_REFERENCE_QNAME = new QName(NAMESPACE_URI,"ConsumerReference");
}
