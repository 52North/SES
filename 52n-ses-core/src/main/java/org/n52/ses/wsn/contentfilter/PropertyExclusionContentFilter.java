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
package org.n52.ses.wsn.contentfilter;

import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.muse.ws.notification.NotificationMessage;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PropertyExclusionContentFilter implements MessageContentFiler {

	private List<QName> propertiesToExclude;
	
	public PropertyExclusionContentFilter(List<QName> properties) {
		this.propertiesToExclude = properties;
	}
	
	@Override
	public void filterMessage(NotificationMessage message) {
		Collection<?> contents = message.getMessageContentNames();
		for (Object object : contents) {
			QName qn = (QName) object;
			Element content = message.getMessageContent(qn);
			applyFilter(content);
		}
	}

	private void applyFilter(Element content) {
		if (content == null || this.propertiesToExclude == null) return;
		
		for (QName qn : this.propertiesToExclude) {
			NodeList toRemove = content.getElementsByTagNameNS(qn.getNamespaceURI(), qn.getLocalPart());
			
			if (toRemove == null) continue;
			
			for (int i = 0; i < toRemove.getLength(); i++) {
				Node parent = toRemove.item(i).getParentNode();
				if (parent != null)
					parent.removeChild(toRemove.item(i));
			}
		}
	}

}
