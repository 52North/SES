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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.muse.util.xml.XPathUtils;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.notification.faults.InvalidFilterFault;
import org.apache.muse.ws.notification.impl.FilterFactoryHandler;
import org.apache.muse.ws.resource.basefaults.BaseFault;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.epos.engine.filter.XPathConfiguration;
import org.n52.epos.filter.EposFilter;
import org.n52.epos.filter.FilterInstantiationException;
import org.n52.epos.filter.FilterInstantiationRepository;
import org.n52.ses.api.common.SesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EposFilterFactory implements FilterFactoryHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(EposFilterFactory.class);

	@Override
	public boolean accepts(QName filterName, String filterDialect) {
		if (filterDialect.equals(SesConstants.SES_FILTER_LEVEL_2_DIALECT)
				|| filterDialect
						.equals(SesConstants.SES_FILTER_LEVEL_3_DIALECT)) {
			return true;
		}

		if (filterName.equals(WsnConstants.MESSAGE_CONTENT_QNAME)
				&& filterDialect.equals(XPathUtils.NAMESPACE_URI)) {
			return true;
		}
		
		if (filterDialect.equals(SesConstants.EPL_PURE_DIALECT)) {
			return true;
		}

		return false;
	}

	@Override
	public Filter newInstance(Element filterXML) throws BaseFault {
		Object input = prepareInput(filterXML);

		try {
			EposFilter result = FilterInstantiationRepository.Instance
					.instantiate(input);
			return new EposFilterWrapper(result);
		} catch (FilterInstantiationException e) {
			logger.warn(e.getMessage(), e);
			throw new InvalidFilterFault(e);
		}
	}

	private Object prepareInput(Element filterXML) throws InvalidFilterFault {
		Object input = null;
		QName qn = new QName(filterXML.getNamespaceURI(),
				filterXML.getLocalName());
		String dialect = filterXML.getAttribute(WsnConstants.DIALECT);

		if (qn.equals(WsnConstants.MESSAGE_CONTENT_QNAME)
				&& dialect.equals(XPathUtils.NAMESPACE_URI)) {
		    input = new XPathConfiguration(XmlUtils.extractText(filterXML), 
		    		getNamespaceDeclarations(filterXML));
		} else {
			try {
				input = XmlObject.Factory.parse(findContentChild(filterXML));
			} catch (XmlException e) {
				logger.warn(e.getMessage(), e);
				throw new InvalidFilterFault(e);
			}
		}
		return input;
	}


	private Element findContentChild(Element filterXML) {
		NodeList nodeList = filterXML.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) node;
			}
		}
		return null;
	}

	/**
	 * 
	 * This is an auxiliary method used to recursively search an Element and its
	 * parent nodes for namespace/prefix definitions. It is used to implement
	 * getNamespaceDeclarations(Element).
	 * 
	 * @param xml
	 *            The current Element in the recursive search. All namespace
	 *            prefixes will be searched. , and then its children will be
	 *            searched.
	 * 
	 * @return The same Map as the second parameter (namespacesByPrefix), but
	 *         with more entries.
	 * 
	 */
	private static Map<String, String> getNamespaceDeclarations(Element xml) {
		Map<String,String> namespacesByPrefix = new HashMap<String, String>();
		
		Node parent = xml;

		int type;
		while ((null != parent)
				&& (((type = parent.getNodeType()) == Node.ELEMENT_NODE) ||
						(type == Node.ENTITY_REFERENCE_NODE))) {
			if (type == Node.ELEMENT_NODE) {
				NamedNodeMap nnm = parent.getAttributes();

				for (int i = 0; i < nnm.getLength(); i++) {
					Node attr = nnm.item(i);
					String aname = attr.getNodeName();
					boolean isPrefix = aname.startsWith("xmlns:");

					if (isPrefix || aname.equals("xmlns")) {
						int index = aname.indexOf(':');
						String pre = isPrefix ? aname.substring(index + 1) : "";
						String namespace = attr.getNodeValue();

						namespacesByPrefix.put(pre, namespace);
					}
				}
			}

			parent = parent.getParentNode();
		}

		return namespacesByPrefix;
	}

}
