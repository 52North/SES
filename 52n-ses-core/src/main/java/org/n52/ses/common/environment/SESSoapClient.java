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
package org.n52.ses.common.environment;


import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.entity.ContentType;
import org.apache.muse.util.messages.Messages;
import org.apache.muse.util.messages.MessagesFactory;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.soap.SimpleSoapClient;
import org.apache.muse.ws.addressing.soap.SoapClient;
import org.apache.muse.ws.addressing.soap.SoapConnectionHandler;
import org.apache.muse.ws.addressing.soap.SoapConstants;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.n52.ses.util.http.SESHttpClient;
import org.n52.ses.util.http.SESHttpClient.SESHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Replacement for the default {@link SoapClient}.
 * This implementation uses Apache HttpClient 4.x.
 * 
 * @author matthes rieke
 *
 */
public class SESSoapClient extends SimpleSoapClient {

	private static final Logger logger = LoggerFactory.getLogger(SESSoapClient.class);
	private static Messages _MESSAGES = MessagesFactory.get(SimpleSoapClient.class);
	private static final Element[] _EMPTY_ARRAY = new Element[0];
	private SESHttpClient httpClient;

	public SESSoapClient() {
		super();

		this.httpClient = new SESHttpClient();
	}

	private SoapConnectionHandler handler = null;

	public SoapConnectionHandler getConnectionHandler() {
		return this.handler;
	}

	public void setConnectionHandler(SoapConnectionHandler h) {
		this.handler = h;
	}

	@Override
	public Element[] send(EndpointReference src, EndpointReference dest,
			String wsaAction, Element[] body, Element[] extraHeaders) {
		if (dest == null) {
			throw new NullPointerException(_MESSAGES.get("NullDestinationEPR"));
		}

		if (wsaAction == null) {
			throw new NullPointerException(_MESSAGES.get("NullActionURI"));
		}

		if (body == null) {
			body = _EMPTY_ARRAY;
		}

		if (extraHeaders == null) {
			extraHeaders = _EMPTY_ARRAY;
		}

		Element soapRequest = createMessage(src, dest, wsaAction, body, extraHeaders);
		String soapString = XmlUtils.toString(soapRequest);

		if (isUsingTrace()) {
			trace(soapRequest, false);
		}

		Element soapResponse = null;

		try {
			soapResponse = sendHTTPPost(dest, soapString);

			if (soapResponse == null) {
				return new Element[0];
			}
		}
		catch (Throwable error) {
			SoapFault soapFault = new SoapFault(error.getMessage(), error);
			return new Element[]{ soapFault.toXML() };
		}

		if (isUsingTrace()) {
			trace(soapResponse, true);
		}

		Element responseBody = XmlUtils.getElement(soapResponse, SoapConstants.BODY_QNAME);
		return XmlUtils.getAllElements(responseBody);
	}


	private Element sendHTTPPost(EndpointReference dest, String soapString)
			throws IllegalStateException, IOException, URISyntaxException, Exception {
		SESHttpResponse postResponse = this.httpClient.sendPost(getDestinationURL(dest), soapString,
				ContentType.create("application/soap+xml", "UTF-8"));

		Element soapResponse = null;
		//
		// read in the response and build an XML document from it
		//
		if (postResponse != null && postResponse == SESHttpResponse.NO_CONTENT_RESPONSE) {
			return null;
		}
		else if (postResponse != null && postResponse.getContentType().contains("xml")) {
			Document responseDoc = XmlUtils.createDocument(postResponse.getContent());
			soapResponse = XmlUtils.getFirstElement(responseDoc);	
		} 
		else {
			logger.warn("received a null or unsupported response.");
		}

		postResponse.getContent().close();
		return soapResponse;
	}
	
	public void initialize() {
		this.httpClient.initialize();
	}



}
