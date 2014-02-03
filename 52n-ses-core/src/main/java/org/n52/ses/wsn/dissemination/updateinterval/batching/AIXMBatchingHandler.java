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
package org.n52.ses.wsn.dissemination.updateinterval.batching;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import aero.aixm.schema.x51.AbstractAIXMFeatureDocument;
import aero.aixm.schema.x51.AbstractAIXMFeatureType;
import aero.aixm.schema.x51.message.AIXMBasicMessageDocument;
import aero.aixm.schema.x51.message.AIXMBasicMessageType;
import aero.aixm.schema.x51.message.BasicMessageMemberAIXMPropertyType;


@SupportedNamespace({"http://www.aixm.aero/schema/5.1", "http://www.aixm.aero/schema/5.1/message", "http://www.aixm.aero/schema/5.1/event"})
//@SupportedNamespace({"http://www.aixm.aero/schema/5.1/message"})
public class AIXMBatchingHandler extends BatchingHandler {

	private static final Logger logger = LoggerFactory.getLogger(AIXMBatchingHandler.class);
	
	private AIXMBasicMessageDocument messageContainer;
	private List<String> namespaces;
	
	public AIXMBatchingHandler() {
		this.namespaces = new ArrayList<String>();
		String[] values = getClass().getAnnotation(SupportedNamespace.class).value();
		for (String string : values) {
			this.namespaces.add(string);
		}
	}
	
	@Override
	public void incorporateNewMessage(NotificationMessage latestMessage) {
		List<Element> elements = extractMessageContentForNamespace(latestMessage, this.namespaces);
		
		List<XmlObject> elementObjects;
		try {
			elementObjects = convertToXmlObjects(elements);
		} catch (XmlException e) {
			logger.warn(e.getMessage(), e);
			return;
		}
		
		if (messageContainer == null) {
			this.messageContainer = getFirstAIXMBasicMessage(elementObjects);
		}
		
		if (messageContainer == null) {
			logger.info("Could not find message container AIXMBasicMessage. Creating one.");
			this.messageContainer = AIXMBasicMessageDocument.Factory.newInstance();
			this.messageContainer.addNewAIXMBasicMessage();
		}
		
		addMessagesToWrapper(elementObjects);
	}

	private void addMessagesToWrapper(List<XmlObject> elementObjects) {
		for (XmlObject xmlObject : elementObjects) {
			if (xmlObject == this.messageContainer) continue;
			
			if (xmlObject instanceof AIXMBasicMessageDocument) {
				addBasicMessageContents(((AIXMBasicMessageDocument) xmlObject).getAIXMBasicMessage());
			} 
			else if (xmlObject instanceof AIXMBasicMessageType) {
				addBasicMessageContents((AIXMBasicMessageType) xmlObject);
			}
			else if (xmlObject instanceof AbstractAIXMFeatureDocument) {
				addFeatureContents(((AbstractAIXMFeatureDocument) xmlObject).getAbstractAIXMFeature());
			}
			else if (xmlObject instanceof AbstractAIXMFeatureType) {
				addFeatureContents((AbstractAIXMFeatureType) xmlObject);
			}
		}
	}

	private void addFeatureContents(AbstractAIXMFeatureType abstractAIXMFeature) {
		removeSchemaLocationDeclaration(abstractAIXMFeature);
		AIXMBasicMessageType innerContainer = this.messageContainer.getAIXMBasicMessage();
		BasicMessageMemberAIXMPropertyType member = innerContainer.addNewHasMember();
		member.setAbstractAIXMFeature(abstractAIXMFeature);
		QName qn = new QName(abstractAIXMFeature.getDomNode().getNamespaceURI(), abstractAIXMFeature.getDomNode().getLocalName());
		XmlUtil.qualifySubstitutionGroup(member.getAbstractAIXMFeature(), qn);
	}

	private void addBasicMessageContents(AIXMBasicMessageType aixmBasicMessage) {
		removeSchemaLocationDeclaration(aixmBasicMessage);
		AIXMBasicMessageType innerContainer = this.messageContainer.getAIXMBasicMessage();
		for (BasicMessageMemberAIXMPropertyType member : aixmBasicMessage.getHasMemberArray()) {
			BasicMessageMemberAIXMPropertyType memberNew = innerContainer.addNewHasMember();
			memberNew.set(member);
		}
	}

	private AIXMBasicMessageDocument getFirstAIXMBasicMessage(
			List<XmlObject> elementObjects) {
		for (XmlObject xmlObject : elementObjects) {
			if (xmlObject instanceof AIXMBasicMessageDocument) {
				return (AIXMBasicMessageDocument) xmlObject;
			}
		}
		
		return null;
	}

	@Override
	public XmlObject getBatchedMessage() {
		return this.messageContainer.getAIXMBasicMessage();
	}

}
