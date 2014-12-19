/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
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
