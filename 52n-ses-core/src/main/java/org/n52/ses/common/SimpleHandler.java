/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
