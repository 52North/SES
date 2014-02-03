/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
import org.n52.oxf.ses.adapter.SESAdapter;
import org.n52.oxf.ses.adapter.SESRequestBuilder_00;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

public class GetCapabilitiesIT {

	private static final Logger logger = LoggerFactory.getLogger(GetCapabilitiesIT.class);
	
	@Test
	public void shouldReceiveValidCapabilities() throws OXFException, XmlException, ExceptionReport, IOException {
		ServiceInstance.getInstance().waitUntilAvailable();
		
		logger.info("Requesting Capabilities...");
		
		EnvelopeDocument response = requestCapabilities();
		
		logger.info("Response from SES: {}", response);
		
		Collection<XmlError> errors = XMLBeansParser.validate(response);
		Assert.assertTrue("Capabilities are not valid!", errors.isEmpty());
	}

	private EnvelopeDocument requestCapabilities() throws OXFException, XmlException, ExceptionReport, IOException {
		SESAdapter adapter = new SESAdapter("0.0.0");

		Operation op = new Operation(SESAdapter.GET_CAPABILITIES, null,
				ServiceInstance.getInstance().getHost().toExternalForm());
		
		ParameterContainer parameters = new ParameterContainer();
		parameters.addParameterShell(SESRequestBuilder_00.GET_CAPABILITIES_SES_URL, ServiceInstance.getInstance().getHost().toExternalForm());

		OperationResult response = adapter.doOperation(op, parameters);
		EnvelopeDocument envelope = EnvelopeDocument.Factory.parse(response.getIncomingResultAsStream());

		return envelope;
	}
	
}
