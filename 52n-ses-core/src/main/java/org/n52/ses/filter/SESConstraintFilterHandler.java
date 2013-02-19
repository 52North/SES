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
package org.n52.ses.filter;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.FilterDocument;

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
import org.n52.ses.api.eml.IEML;
import org.n52.ses.api.ws.SESConstraintFilter;
import org.n52.ses.filter.dialects.DialectConstraintFilter;
import org.n52.ses.filter.dialects.CustomFESFilter;
import org.n52.ses.filter.emlimpl.EML001Impl;
import org.n52.ses.filter.emlimpl.EML002Impl;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.w3c.dom.Element;


/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESConstraintFilterHandler implements FilterFactoryHandler {


	/* (non-Javadoc)
	 * @see org.apache.muse.ws.notification.impl.FilterFactoryHandler#accepts(javax.xml.namespace.QName, java.lang.String)
	 */
	@Override
	public boolean accepts(QName filterName, String filterDialect ) {
		
		if (filterDialect.equals(SesConstants.SES_FILTER_LEVEL_2_DIALECT) ||
				filterDialect.equals(SesConstants.SES_FILTER_LEVEL_3_DIALECT)) {
			return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.muse.ws.notification.impl.FilterFactoryHandler#newInstance(org.w3c.dom.Element)
	 */
	@Override
	public Filter newInstance(Element filterXml) throws BaseFault {
		
		XmlObject obj = null;
		try {
			Element elem = null;
			QName qnEml = new QName("http://www.opengis.net/eml/0.0.2", "EML");
			elem = XmlUtils.getElement(filterXml, qnEml);
			if (elem != null) {
				//return a new SESConstraintFilter with EML
				try {
					obj = XMLBeansParser.parse(elem, true);
				} catch (XMLHandlingException e) {
					throw new InvalidFilterFault(e);
				}
				return new SESConstraintFilter(new EML002Impl((net.opengis.eml.x002.EMLDocument) obj));
			}
			
			qnEml = new QName("http://www.opengis.net/eml/0.0.1", "EML");
			elem = XmlUtils.getElement(filterXml, qnEml);
			if (elem != null) {
				//return a new SESConstraintFilter with EML
				try {
					obj = XMLBeansParser.parse(elem, true);
				} catch (XMLHandlingException e) {
					throw new InvalidFilterFault(e);
				}
				return new SESConstraintFilter(new EML001Impl((net.opengis.eml.x001.EMLDocument) obj));
			}
			
			QName qnFilter = new QName("http://www.opengis.net/fes/2.0", "Filter");
			elem = XmlUtils.getElement(filterXml, qnFilter);
			if (elem != null) {
				//return a new SESConstraintFilter with FilterDocument
				obj = XMLBeansParser.parse(elem, true);
				
				String controller = ConfigurationRegistry.getInstance().getPropertyForKey(
						ConfigurationRegistry.EML_CONTROLLER);
				
				Filter customs = checkForCustomFESFilters((FilterDocument) obj);
				if (customs != null ) {
					return customs;
				}
				
				IEML eml;
				if (controller.contains("v001")) {
					eml = EML001Impl.generateStaticDocument(((FilterDocument) obj).getFilter());
				} else {
					eml = EML002Impl.generateStaticDocument(((FilterDocument) obj).getFilter());
				}
				
				return new SESConstraintFilter(eml);
			}
		} catch (XMLHandlingException e) {
			throw new SubscribeCreationFailedFault(e);
		}
		
		Filter custom = checkForCustomContraintFilter(filterXml);
		if (custom != null) {
			return custom;
		}
		
		throw new SubscribeCreationFailedFault("Either an ogc:Filter or EML is needed.");
		
	}

	private Filter checkForCustomContraintFilter(Element filterXml) {
		for (DialectConstraintFilter customFilter : DialectConstraintFilter.getCustomFilters()) {
			if (customFilter.canHandle(filterXml)) {
				customFilter.initialize(filterXml);
				return customFilter;
			}
		}
		return null;
	}

	private Filter checkForCustomFESFilters(FilterDocument obj) {
		for (CustomFESFilter customFilter : CustomFESFilter.getCustomFilters()) {
			if (customFilter.canHandle(obj)) {
				customFilter.initialize(obj);
				return customFilter;
			}
		}
		return null;
	}

}
