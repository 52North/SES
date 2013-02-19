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
package org.n52.ses.common.integration.test;

import java.io.IOException;
import java.net.SocketException;
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
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

public class Level1SubscriptionIT {

	private static final Logger logger = LoggerFactory.getLogger(Level1SubscriptionIT.class);
	
	@Test
	public void shouldSuccesfullySubscribe() throws OXFException, XmlException, ExceptionReport, IOException {
		try {
		ServiceInstance.getInstance().waitUntilAvailable();
		
		logger.info("Subscribing Level 1 (XPath)...");
		
		EnvelopeDocument response = subscribe();
		
		logger.info("Response from SES: {}", response);
		
		Collection<XmlError> errors = XMLBeansParser.validate(response);
		Assert.assertTrue("Response are not valid!", errors.isEmpty());
	} catch (SocketException e) {
		logger.warn("Here we go SocketException!", e);
	}	
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
