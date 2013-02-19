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
