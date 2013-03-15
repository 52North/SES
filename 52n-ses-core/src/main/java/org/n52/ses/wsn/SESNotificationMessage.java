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

import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.muse.util.messages.Messages;
import org.apache.muse.util.messages.MessagesFactory;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extension of {@link SimpleNotificationMessage} to
 * fix some bugs in the standard implementation.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESNotificationMessage extends SimpleNotificationMessage {

	//
	// Used to look up all exception messages
	//
	private static Messages _MESSAGES = MessagesFactory.get(SimpleNotificationMessage.class);

	private String _topicPrefix;

	private String _topicNamespace;

	@Override
	public void setTopicExpression(String namespace, String prefix, String expression) {
		super.setTopicExpression(namespace, prefix, expression);
		this._topicNamespace = namespace;
		this._topicPrefix = prefix;
	}

	@Override
	public Element toXML(Document doc) {
		if (doc == null)
			throw new NullPointerException(_MESSAGES.get("NullDocument"));

		Element root = XmlUtils.createElement(doc, WsnConstants.NOTIFICATION_MSG_QNAME);

		EndpointReference sub = getSubscriptionReference();

		if (sub != null)
		{
			Element eprXML = sub.toXML(doc);
			XmlUtils.setElement(root, WsnConstants.SUBSCRIPTION_EPR_QNAME, eprXML);
		}

		String topicExpression = getTopicExpression();

		if (topicExpression != null) {
			Element topic = XmlUtils.createElement(doc, WsnConstants.TOPIC_QNAME, topicExpression);
			topic.setAttribute(WsnConstants.DIALECT, getTopicDialect());
			topic.setAttribute("xmlns:"+ this._topicPrefix, this._topicNamespace);
			root.appendChild(topic);

		}
		else {       
			QName topicPath = getTopic();

			if (topicPath != null)
			{
				Element topic = XmlUtils.createElement(doc, WsnConstants.TOPIC_QNAME, topicPath);
				topic.setAttribute(WsnConstants.DIALECT, getTopicDialect());
				root.appendChild(topic);
			}
		}

		EndpointReference producer = getProducerReference();

		if (producer != null)
		{
			Element eprXML = producer.toXML(doc);
			XmlUtils.setElement(root, WsnConstants.PRODUCER_QNAME, eprXML);
		}

		Element message = XmlUtils.createElement(doc, WsnConstants.MESSAGE_QNAME);
		root.appendChild(message);

		Iterator<?> i = getMessageContentNames().iterator();

		while (i.hasNext())
		{
			QName name = (QName)i.next();
			Element next = getMessageContent(name);
			next = (Element)doc.importNode(next, true);
			message.appendChild(next);
		}

		//
		// add the message's various namespaces to the root element 
		// so we can easily query them with XPath 1.0
		//
		Map<?, ?> namespacesByPrefix = XmlUtils.getAllNamespaces(root);
		i = namespacesByPrefix.keySet().iterator();

		while (i.hasNext())
		{
			String prefix = (String)i.next();
			
			/*
			 * BUGFIX: ignore namespace without
			 * prefix. they are only used in a local part of
			 * the document
			 */
			if (prefix.equals("")) continue;
			
			String namespace = (String)namespacesByPrefix.get(prefix);
			XmlUtils.setNamespaceAttribute(root, prefix, namespace);
		}

		return root;
	}


}