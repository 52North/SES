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
