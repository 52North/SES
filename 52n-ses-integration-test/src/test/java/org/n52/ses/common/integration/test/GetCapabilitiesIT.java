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
