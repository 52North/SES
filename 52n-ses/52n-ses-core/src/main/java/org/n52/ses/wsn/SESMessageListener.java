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
