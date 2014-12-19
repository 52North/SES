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
package org.n52.ses.wsn;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.Policy;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.notification.impl.Subscribe;
import org.apache.muse.ws.resource.basefaults.BaseFault;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SubscribeWithPolicy extends Subscribe {

	private Policy policy;

	public SubscribeWithPolicy(Element xml) throws BaseFault {
		super(xml);
		NodeList policyList = xml.getElementsByTagNameNS(WsnConstants.NAMESPACE_URI, WsnConstants.POLICY_QNAME.getLocalPart());
		if (policyList != null && policyList.getLength() == 1) {
			this.policy = new SimplePolicy(policyList.item(0));
		}
	}
	
	@Override
	public Object[] toArray() {
		Object[] result = super.toArray();
		result[3] = this.policy;
		return result;
	}
	

	@Override
	public Element toXML(Document doc) {
		Element root = super.toXML(doc);
		
		if (this.policy != null)
			XmlUtils.setElement(root, WsnConstants.POLICY_QNAME, this.policy);
		
		return root;
	}
	
	public static class SimplePolicy implements Policy {

		private Node xml;

		public SimplePolicy(Node item) {
			this.xml = item;
		}

		@Override
		public Element toXML() {
			return toXML(XmlUtils.createDocument());
		}

		@Override
		public Element toXML(Document factory) {
			Node result = factory.importNode(xml, true);
			return (Element) result;
		}
		
	}
}
