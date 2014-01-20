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
package org.n52.ses.engine.epos;

import java.io.IOException;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.WsnConstants;
import org.n52.epos.filter.EposFilter;
import org.n52.ses.api.common.SesConstants;
import org.n52.ses.api.ws.EngineCoveredFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class EposFilterWrapper implements Filter, EngineCoveredFilter {

	private static final Logger logger = LoggerFactory.getLogger(EposFilterWrapper.class);
	private EposFilter filter;

	public EposFilterWrapper(EposFilter eposf) {
		this.filter = eposf;
	}

	@Override
	public Element toXML() {
		return toXML(XmlUtils.createDocument());
	}

	@Override
	public Element toXML(Document doc) {
		Element filter = XmlUtils.createElement(doc, WsnConstants.FILTER_QNAME);

		String xmlText = "<MessageContent>"+System.getProperty("line.separator");
		xmlText += this.filter.serialize();
		xmlText += System.getProperty("line.separator")+"</MessageContent>";

		Element message = null;
		try {
			Element node = XmlUtils.createDocument(xmlText).getDocumentElement();
			message = XmlUtils.createElement(doc, WsnConstants.MESSAGE_CONTENT_QNAME,
					node);
			message.setAttribute(WsnConstants.DIALECT, SesConstants.SES_FILTER_LEVEL_3_DIALECT);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		} catch (SAXException e) {
			logger.warn(e.getMessage(), e);
		}

		filter.appendChild(message);

		return filter;
	}

	@Override
	public boolean accepts(NotificationMessage message) {
		return true;
	}
	
	@Override
	public Object getEngineSpecificFilter() {
		return this.filter;
	}

}
