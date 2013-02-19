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
package org.n52.ses.wsn.dissemination.updateinterval;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class NoNewMessagesMessage extends SimpleNotificationMessage {

	public NoNewMessagesMessage(EndpointReference subscription) {
		this.setSubscriptionReference(subscription);
		this.addMessageContent(createContent());
	}

	private Element createContent() {
		Element content = XmlUtils.createElement(UpdateIntervalDisseminationMethod.NO_NEW_MESSAGES_NAME);
		Attr time = content.getOwnerDocument().createAttribute("currentTime");
		time.setValue(new DateTime().toString(ISODateTimeFormat.dateTime()));
		content.setAttributeNode(time);
		return content;
	}
	
}
