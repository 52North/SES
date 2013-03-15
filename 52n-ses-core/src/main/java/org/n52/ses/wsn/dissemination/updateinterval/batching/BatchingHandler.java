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
package org.n52.ses.wsn.dissemination.updateinterval.batching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.muse.util.ReflectUtils;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;

public abstract class BatchingHandler {

	private static final QName XSI_SCHEMA_LOCATION = new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation");
	private static List<Class<? extends BatchingHandler>> availableHandlers;
	
	static {
		availableHandlers = new ArrayList<Class<? extends BatchingHandler>>();
		availableHandlers.add(AIXMBatchingHandler.class);
	}
	
	public static BatchingHandler createBatchingHandler(NotificationMessage message) {
		for (Class<? extends BatchingHandler> handler : availableHandlers) {
			SupportedNamespace namespaces = handler.getAnnotation(SupportedNamespace.class);
			if (namespaces == null) continue;
			
			Collection<?> contents = message.getMessageContentNames();
			for (String space : namespaces.value()) {
				for (Object object : contents) {
					if (object instanceof QName) {
						if (((QName) object).getNamespaceURI().equals(space)) {
							return instantiateHandler(handler);
						}
					}
					
				}
			}
		}
		return null;
	}

	private static BatchingHandler instantiateHandler(
			Class<? extends BatchingHandler> handler) {
		Object instance = ReflectUtils.newInstance(handler);
		return (BatchingHandler) instance;
	}

	public abstract void incorporateNewMessage(NotificationMessage latestMessage);
	
	protected List<Element> extractMessageContentForNamespace(NotificationMessage message, List<String> namespaces) {
		List<Element> result = new ArrayList<Element>();
		
		Collection<?> contents = message.getMessageContentNames();
		for (String space : namespaces) {
			for (Object object : contents) {
				if (object instanceof QName) {
					if (((QName) object).getNamespaceURI().equals(space)) {
						result.add(message.getMessageContent((QName) object));
					}
				}
			}
		}
		
		return result;
	}
	
	protected List<XmlObject> convertToXmlObjects(List<Element> elements) throws XmlException {
		List<XmlObject> result = new ArrayList<XmlObject>();
		for (Element element : elements) {
			result.add(XmlObject.Factory.parse(element));
		}
		return result;
	}

	protected void removeSchemaLocationDeclaration(XmlObject ob) {
		XmlCursor cur = ob.newCursor();
		cur.removeAttribute(XSI_SCHEMA_LOCATION);
		cur.dispose();
	}

	public abstract XmlObject getBatchedMessage();

	
}
