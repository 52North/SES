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
package org.n52.ses.common.integration.test;

import java.io.IOException;
import java.util.Collection;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.ses.adapter.ISESRequestBuilder;
import org.n52.oxf.ses.adapter.SESAdapter;
import org.n52.oxf.ses.adapter.SESRequestBuilderFactory;
import org.n52.oxf.ses.adapter.client.Subscription;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

public class Level1SubscriptionIT {

	private static final Logger logger = LoggerFactory.getLogger(Level1SubscriptionIT.class);
	
	@Test
	public void shouldSuccesfullySubscribe()
			throws OXFException, XmlException, ExceptionReport, IOException {
		ServiceInstance.getInstance().waitUntilAvailable();
		
		logger.info("Subscribing Level 1 (XPath)...");
		
		EnvelopeDocument response = subscribe();
		
		logger.info("Response from SES: {}", response);
		
		Collection<XmlError> errors = XMLBeansParser.validate(response);
		Assert.assertTrue("Response are not valid!", errors.isEmpty());
		
		response = unsubscribe(response);
		
		logger.info("Response from SES: {}", response);
		
		errors = XMLBeansParser.validate(response);
		Assert.assertTrue("Response are not valid!", errors.isEmpty());
	}

	private EnvelopeDocument unsubscribe(EnvelopeDocument response) throws OXFException, ExceptionReport, XmlException, IOException {
		Subscription sub = new Subscription(null);
		sub.parseResponse(response);
	    
        SESAdapter adapter = new SESAdapter("0.0.0");
        
        Operation op = new Operation(SESAdapter.UNSUBSCRIBE, null, sub.getManager().getHost().toExternalForm());
        
        StringBuilder sb = new StringBuilder();
        sb.append("<rid:ResourceId wsa:IsReferenceParameter=\"true\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" ");
        sb.append("xmlns:rid=\"http://ws.apache.org/muse/addressing\">");
        sb.append(sub.getResourceID());
        sb.append("</rid:ResourceId>");
        
        ParameterContainer parameter = new ParameterContainer();
        parameter.addParameterShell(ISESRequestBuilder.UNSUBSCRIBE_SES_URL, sub.getManager().getHost().toExternalForm());
        parameter.addParameterShell(ISESRequestBuilder.UNSUBSCRIBE_REFERENCE_XML, sb.toString());
        
        logger.info(SESRequestBuilderFactory.generateRequestBuilder("0.0.0").buildUnsubscribeRequest(parameter));
        
        OperationResult opResult = adapter.doOperation(op, parameter);

		EnvelopeDocument envelope = EnvelopeDocument.Factory.parse(opResult.getIncomingResultAsStream());

		return envelope;
	}

	private EnvelopeDocument subscribe() throws OXFException, XmlException, ExceptionReport, IOException {
        
        SESAdapter adapter = new SESAdapter("0.0.0");
        
        Operation op = new Operation(SESAdapter.SUBSCRIBE, null, ServiceInstance.getInstance().getHost().toExternalForm());
        
        ParameterContainer parameter = new ParameterContainer();
        parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_SES_URL, ServiceInstance.getInstance().getHost().toExternalForm());
        parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_CONSUMER_REFERENCE_ADDRESS,"http://localhost:9090/GSM2SWE/sesl");
        parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_FILTER_TOPIC,"ses:Measurements");
        parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_FILTER_MESSAGE_CONTENT_DIALECT, "http://www.w3.org/TR/1999/REC-xpath-19991116");
        parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_FILTER_MESSAGE_CONTENT, "//@xlink:href='urn:ogc:object:procedure:CITE:WeatherService:LGA'");
        
        logger.info(SESRequestBuilderFactory.generateRequestBuilder("0.0.0").buildSubscribeRequest(parameter));
        
        OperationResult opResult = adapter.doOperation(op, parameter);

		EnvelopeDocument envelope = EnvelopeDocument.Factory.parse(opResult.getIncomingResultAsStream());

		return envelope;
	}
	
}
