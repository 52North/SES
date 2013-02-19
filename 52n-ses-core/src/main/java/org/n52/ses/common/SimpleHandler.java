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
package org.n52.ses.common;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.apache.muse.core.routing.AbstractMessageHandler;
import org.apache.muse.util.ReflectUtils;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.resource.impl.AbstractWsResourceCapability;
import org.w3c.dom.Element;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 * 
 * Simple Handler that just passes through
 *
 */
public class SimpleHandler extends AbstractMessageHandler {

	/**
	 * Creates a new Handler that just passes every request or response to the next layer
	 * @param actionURI the URI of the associates action
	 * @param requestQName the QName
	 * @param capability The WS Resource
	 * @param methodName the name of the method that should be called
	 */
	public SimpleHandler(String actionURI, QName requestQName, AbstractWsResourceCapability capability, String methodName) {
		super(actionURI, requestQName);
		
		Method method = ReflectUtils.getFirstMethod(capability.getClass(), methodName);
		this.setMethod(method);

	}


	/**
	 * Deserializes the given DOM Element into a set of POJOs 
	 * that can be used when invoking a Java method. Implementations 
	 * should use Muse's registered Serializers in order to
	 * deserialize the XML into objects of the proper types.
	 * 
	 * @throws SoapFault if an error occurred during deserialisation
	 */
	@Override
	public Object[] fromXML(Element xml) throws SoapFault {
		return new Object[]{xml};
	}

	/**
	 * Serializes the given object into a DOM Element. Implementations 
	 * should use Muse's registered Serializers in order to serialize 
	 * the objects into the proper formats.
	 * 
	 * @throws SoapFault if an error occurred during serialisation
	 */
	@Override
	public Element toXML(Object result) throws SoapFault {
		return (Element) result;
	}
}
