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
package org.n52.ses.filter.epl;

import javax.xml.namespace.QName;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.faults.InvalidFilterFault;
import org.apache.muse.ws.notification.faults.SubscribeCreationFailedFault;
import org.apache.muse.ws.notification.impl.FilterFactoryHandler;
import org.apache.muse.ws.resource.basefaults.BaseFault;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.parser.XMLHandlingException;
import org.n52.ses.api.common.SesConstants;
import org.w3c.dom.Element;

/**
 * FilterHandler for {@link EPLFilterImpl}.
 * 
 * @author matthes rieke
 *
 */
public class EPLFilterHandler implements FilterFactoryHandler {

	/* (non-Javadoc)
	 * @see org.apache.muse.ws.notification.impl.FilterFactoryHandler#accepts(javax.xml.namespace.QName, java.lang.String)
	 */
	@Override
	public boolean accepts(QName filterName, String filterDialect ) {
		if (filterDialect.equals(SesConstants.EPL_PURE_DIALECT)) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.muse.ws.notification.impl.FilterFactoryHandler#newInstance(org.w3c.dom.Element)
	 */
	@Override
	public Filter newInstance(Element filterXml) throws BaseFault {
		Element elem = null;
		QName qnEml = new QName("http://www.opengis.net/ses/0.0", "EPLFilters");
		elem = XmlUtils.getElement(filterXml, qnEml);
		if (elem != null) {
			XmlObject obj = null;
			//return a new SESConstraintFilter with EML
			try {
				obj = XMLBeansParser.parse(elem, true);
			} catch (XMLHandlingException e) {
				throw new InvalidFilterFault(e);
			}
			return new EPLFilterImpl(obj);
		}
		throw new SubscribeCreationFailedFault("Could not instantiate EPLFilter");
	}

}
