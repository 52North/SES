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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
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
import org.n52.oxf.util.web.HttpClient;
import org.n52.oxf.util.web.HttpClientException;
import org.n52.oxf.util.web.ProxyAwareHttpClient;
import org.n52.oxf.util.web.SimpleHttpClient;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.Fault;

public class UTF8SpecialCharacterIT {

	private static final Logger logger = LoggerFactory.getLogger(UTF8SpecialCharacterIT.class);

	@Test
	public void
	shouldSuccesfullyPostNotification()
			throws XmlException, IOException, OXFException, ExceptionReport, InterruptedException {
		ServiceInstance.getInstance().waitUntilAvailable();

		String notify = readNotification();
		SESAdapter adapter = new SESAdapter("0.0.0");

		Operation op = new Operation(SESAdapter.NOTIFY, null, ServiceInstance.getInstance().getHost().toExternalForm());

		ParameterContainer parameter = new ParameterContainer();
		parameter.addParameterShell(ISESRequestBuilder.NOTIFY_SES_URL, ServiceInstance.getInstance().getHost().toExternalForm());
		parameter.addParameterShell(ISESRequestBuilder.NOTIFY_XML_MESSAGE, notify);

		logger.info(SESRequestBuilderFactory.generateRequestBuilder("0.0.0").buildNotifyRequest(parameter));

		OperationResult result = adapter.doOperation(op, parameter);
		Assert.assertNull("A null result was expected.", result);
	}

	@Test
	public void
	shouldCreateSoapFault()
			throws OXFException, XmlException, IOException {
		ServiceInstance.getInstance().waitUntilAvailable();

		String notify = readNotification();
		ParameterContainer parameter = new ParameterContainer();
		parameter.addParameterShell(ISESRequestBuilder.NOTIFY_SES_URL, ServiceInstance.getInstance().getHost().toExternalForm());
		parameter.addParameterShell(ISESRequestBuilder.NOTIFY_XML_MESSAGE, notify);

		try {
			String payload = SESRequestBuilderFactory.generateRequestBuilder("0.0.0").buildNotifyRequest(parameter);
			HttpClient httpClient = new ProxyAwareHttpClient(new SimpleHttpClient());
			HttpResponse httpResponse = httpClient.executePost(ServiceInstance.getInstance().getHost().toExternalForm(), payload, ContentType.TEXT_XML);
			HttpEntity responseEntity = httpResponse.getEntity();
			if (responseEntity != null && responseEntity.getContent() != null) {
				EnvelopeDocument xo = EnvelopeDocument.Factory.parse(responseEntity.getContent());
				Collection<XmlError> errors = XMLBeansParser.validate(xo);

				Assert.assertTrue("Response is not valid", errors.isEmpty());
				Body body = xo.getEnvelope().getBody();
				XmlCursor cur = body.newCursor();
				cur.toFirstChild();

				Assert.assertTrue("Not a SoapFault!", cur.getObject() instanceof Fault);
				cur.dispose();
				
				logger.info("Received an expected SoapFault: "+ xo);
			}
			else throw new IllegalStateException("No response available.");
		}
		catch (HttpClientException e) {
			throw new OXFException("Could not send request.", e);
		} catch (IllegalStateException e) {
			throw new OXFException("Could not send request.", e);
		} catch (IOException e) {
			throw new OXFException("Could not send request.", e);
		} catch (XmlException e) {
			throw new OXFException("Could not send request.", e);
		}

	}

	private String readNotification() throws XmlException, IOException {
		return readXmlContent("UTF8SpecialCharacterIT.xml");
	}

	private String readXmlContent(String string) throws XmlException, IOException {
		XmlObject xo = XmlObject.Factory.parse(getClass().getResourceAsStream(string));
		return xo.xmlText(new XmlOptions().setSavePrettyPrint());
	}

}
