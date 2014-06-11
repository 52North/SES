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
package org.n52.ses.wsn;


import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.NotificationMessageListener;


/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESMessageListener implements NotificationMessageListener {

	private SESNotificationProducer producer;

	/**
	 * 
	 * Constructor
	 *
	 * @param np SES notification producer
	 */
	public SESMessageListener(SESNotificationProducer np) {
		this.producer = np;
	}

	@Override
	public boolean accepts(NotificationMessage message) {
		/*
		 * TODO check against ProducerReferences? safety
		 * message.getProducerReference(); ?
		 */

		return true;
	}

	@Override
	public void process(NotificationMessage message) throws SoapFault {
		/*
		 * forward message to NotificationProducer
		 * everything is supported - if not parsable for
		 * the filter engine it will be ignored.
		 */
		
		this.producer.publishCompleteMessage(message);


	}

}
