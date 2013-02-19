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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.n52.oxf.ses.adapter.SESRequestBuilder_00;
import org.n52.oxf.ses.adapter.client.Subscription;
import org.n52.oxf.ses.adapter.client.httplistener.HttpListener;
import org.n52.ses.common.test.TestWSNEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OvershootUndershootSubscriptionIT {

	private static final Logger logger = LoggerFactory.getLogger(OvershootUndershootSubscriptionIT.class);

	private int port;

	private TestWSNEndpoint endpoint;

	public OvershootUndershootSubscriptionIT() throws MalformedURLException {
		port = IntegrationTestConfig.getInstance().getConsumerPort();
	}


	@Test
	public void shouldCompleteRoundtripForNotification() throws IOException, InterruptedException,
				OXFException, ExceptionReport, XmlException, ExecutionException, TimeoutException {
		NotificationReceiver notificationReceiver = initializeConsumer();
		
		ServiceInstance.getInstance().waitUntilAvailable();
		
		Subscription subscription = subscribe();
		
		Thread.sleep(1000);
		
		notification();
		
		Future<?> future = Executors.newSingleThreadExecutor().submit(notificationReceiver);
		Object hasReceived = new Object();
		try {
			//null upon success
			hasReceived = future.get(5000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		
		unsubscribe(subscription);
		
		Thread.sleep(1000);
		
		Assert.assertNull("Noticiation not received!", hasReceived);
	}



	private void unsubscribe(Subscription subscription) throws OXFException, ExceptionReport, XmlException, IOException {
		SESAdapter adapter = new SESAdapter("0.0.0");

		Operation op = new Operation(SESAdapter.UNSUBSCRIBE, null,
				subscription.getManager().getHost().toExternalForm());

		ParameterContainer parameter = new ParameterContainer();
		parameter.addParameterShell(ISESRequestBuilder.UNSUBSCRIBE_SES_URL,
				subscription.getManager().getHost().toExternalForm());
		
		StringBuilder sb = new StringBuilder();
		sb.append("<muse-wsa:ResourceId xmlns:muse-wsa=\"");
		sb.append(subscription.getResourceIdInstance().getNamespace());
		sb.append("\" wsa:IsReferenceParameter=\"true\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">");
		sb.append(subscription.getResourceID());
		sb.append("</muse-wsa:ResourceId>");
		
		parameter.addParameterShell(SESRequestBuilder_00.UNSUBSCRIBE_REFERENCE_XML, sb.toString());

		logger.info(SESRequestBuilderFactory.generateRequestBuilder("0.0.0").buildUnsubscribeRequest(parameter));

		OperationResult opResult = adapter.doOperation(op, parameter);
		XmlObject xo = XmlObject.Factory.parse(opResult.getIncomingResultAsStream());
		logger.info(xo.xmlText());
	}


	private NotificationReceiver initializeConsumer() throws IOException, InterruptedException {
		endpoint = TestWSNEndpoint.getInstance(port);
		NotificationReceiver notificationReceiver = new NotificationReceiver();
		endpoint.addListener(notificationReceiver);
		return notificationReceiver;
	}

	private Subscription subscribe() throws OXFException, ExceptionReport, XmlException, IOException {
		SESAdapter adapter = new SESAdapter("0.0.0");

		Operation op = new Operation(SESAdapter.SUBSCRIBE, null, ServiceInstance.getInstance().getHost().toExternalForm());

		ParameterContainer parameter = new ParameterContainer();
		parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_SES_URL, ServiceInstance.getInstance().getHost().toExternalForm());
		parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_CONSUMER_REFERENCE_ADDRESS,
				endpoint.getPublicURL());
		parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_FILTER_MESSAGE_CONTENT_DIALECT,
				"http://www.opengis.net/ses/filter/level3");
		parameter.addParameterShell(ISESRequestBuilder.SUBSCRIBE_FILTER_MESSAGE_CONTENT, readSubscription());

		logger.info(SESRequestBuilderFactory.generateRequestBuilder("0.0.0").buildSubscribeRequest(parameter));

		OperationResult opResult = adapter.doOperation(op, parameter);
		XmlObject xo = XmlObject.Factory.parse(opResult.getIncomingResultAsStream());
		logger.info(xo.xmlText());
		Subscription sub = new Subscription(null);
		sub.parseResponse(xo);
		return sub;
	}



	private void notification() throws XmlException, IOException, OXFException, ExceptionReport, InterruptedException {
		for (String notify : readNotifications()) {
			SESAdapter adapter = new SESAdapter("0.0.0");

			Operation op = new Operation(SESAdapter.NOTIFY, null, ServiceInstance.getInstance().getHost().toExternalForm());

			ParameterContainer parameter = new ParameterContainer();
			parameter.addParameterShell(ISESRequestBuilder.NOTIFY_SES_URL, ServiceInstance.getInstance().getHost().toExternalForm());
			parameter.addParameterShell(ISESRequestBuilder.NOTIFY_XML_MESSAGE, notify);

			logger.info(SESRequestBuilderFactory.generateRequestBuilder("0.0.0").buildNotifyRequest(parameter));
			
			adapter.doOperation(op, parameter);
			
			Thread.sleep(1000);
		}
	}
	
	private List<String> readNotifications() throws XmlException, IOException {
		List<String> result = new ArrayList<String>();
		result.add(readXmlContent("Overshoot_Notify1.xml"));
		result.add(readXmlContent("Overshoot_Notify2.xml"));
		return result;
	}

	private String readSubscription() throws XmlException, IOException {
		return readXmlContent("Overshoot_Subscribe1.xml");
	}


	private String readXmlContent(String string) throws XmlException, IOException {
		XmlObject xo = XmlObject.Factory.parse(getClass().getResourceAsStream(string));
		return xo.xmlText(new XmlOptions().setSavePrettyPrint());
	}

	private static class NotificationReceiver implements Runnable, HttpListener {

		private Object waitMutex = new Object();
		private boolean hasReceived = false;
		
		@Override
		public void run() {
			synchronized (waitMutex) {
				while (!hasReceived) {
					try {
						waitMutex.wait();
					} catch (InterruptedException e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		}
		
		@Override
		public String processRequest(String request, String uri, String method,
				Properties header) {
			synchronized (waitMutex) {
				hasReceived = true;
				waitMutex.notifyAll();
			}
			return null;
		}
		
	}
}
