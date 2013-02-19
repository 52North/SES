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
