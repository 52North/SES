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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Assert;
import org.junit.Test;
import org.n52.oxf.OXFException;
import org.n52.oxf.adapter.OperationResult;
import org.n52.oxf.adapter.ParameterContainer;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ows.capabilities.Operation;
import org.n52.oxf.ses.adapter.SESAdapter;
import org.n52.oxf.ses.adapter.SESRequestBuilder_00;
import org.n52.oxf.ses.adapter.client.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

public class RegisterPublisherWorkflowIT {
	
	private static final Logger logger = LoggerFactory.getLogger(RegisterPublisherWorkflowIT.class);
	private SESAdapter adapter;

	public RegisterPublisherWorkflowIT() {
		this.adapter = new SESAdapter("0.0.0");
	}
	
	@Test
	public void
	shouldRegisterPublisherAndRemoveAgain()
			throws OXFException, ExceptionReport, XmlException, IOException {
		ServiceInstance.getInstance().waitUntilAvailable();
		
		EnvelopeDocument envelope = registerPublisher();
		logger.info("RegisterPublisher Response from Service: {}", envelope);

		Publisher pub = new Publisher(null);
		pub.parseResponse(envelope);
		
		Assert.assertNotNull(pub.getResourceID());
		
		envelope = removePublisher(pub.getResourceID(), pub.getPublisherAddress());
		logger.info("DestroyRegistration Response from Service: {}", envelope);
	}


	private EnvelopeDocument registerPublisher() throws OXFException, ExceptionReport, XmlException, IOException {
		Operation op = new Operation(SESAdapter.REGISTER_PUBLISHER, null,
				ServiceInstance.getInstance().getHost().toExternalForm());
		
		ParameterContainer parameters = new ParameterContainer();
		parameters.addParameterShell(SESRequestBuilder_00.REGISTER_PUBLISHER_SES_URL, ServiceInstance.getInstance().getHost().toExternalForm());
		parameters.addParameterShell(SESRequestBuilder_00.REGISTER_PUBLISHER_SENSORML, readSensorML());
		parameters.addParameterShell(SESRequestBuilder_00.REGISTER_PUBLISHER_TOPIC, "test-topic");
		
		OperationResult response = adapter.doOperation(op, parameters);
		EnvelopeDocument envelope = EnvelopeDocument.Factory.parse(response.getIncomingResultAsStream());
		
		return envelope;
	}
	
	private EnvelopeDocument removePublisher(String resourceId, String managerHost) throws XmlException, IOException, ExceptionReport, OXFException {
		Operation op = new Operation(SESAdapter.DESTROY_REGISTRATION, null,
				managerHost);
		
		ParameterContainer parameters = new ParameterContainer();
		parameters.addParameterShell(SESRequestBuilder_00.DESTROY_REGISTRATION_SES_URL, managerHost);
		parameters.addParameterShell(SESRequestBuilder_00.DESTROY_REGISTRATION_REFERENCE, resourceId);
		
		OperationResult response = adapter.doOperation(op, parameters);
		EnvelopeDocument envelope = EnvelopeDocument.Factory.parse(response.getIncomingResultAsStream());
		return envelope;
	}

	private String readSensorML() throws XmlException, IOException {
		return XmlObject.Factory.parse(getClass().getResourceAsStream("RegPub_SensorML.xml")).
				xmlText();
	}

}
