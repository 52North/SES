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

import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.epos.filter.EposFilter;
import org.n52.ses.api.ws.EngineCoveredFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EposFilterWrapper implements Filter, EngineCoveredFilter {

	private static final Logger logger = LoggerFactory.getLogger(EposFilterWrapper.class);
	private EposFilter filter;

	public EposFilterWrapper(EposFilter eposf) {
		this.filter = eposf;
	}

	@Override
	public Element toXML() {
		CharSequence xmlString = filter.serialize();
		XmlObject xo;
		try {
			logger.debug("Filter {} serialized as '{}'", filter, xmlString);
			xo = XmlObject.Factory.parse(xmlString.toString());
		} catch (XmlException e) {
			logger.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return (Element) xo.getDomNode().getFirstChild();
	}

	@Override
	public Element toXML(Document factory) {
		Element result = toXML();
		factory.appendChild(factory.importNode(result, true));
		return factory.getDocumentElement();
	}

	@Override
	public boolean accepts(NotificationMessage message) {
		//never used
		return false;
	}
	
	@Override
	public Object getEngineSpecificFilter() {
		return this.filter;
	}

}
