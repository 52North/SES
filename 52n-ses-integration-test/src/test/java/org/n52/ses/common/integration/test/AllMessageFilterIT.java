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
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Assert;
import org.junit.Test;
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ses.adapter.client.Subscription;
import org.n52.oxf.util.web.HttpClient;
import org.n52.oxf.util.web.HttpClientException;
import org.n52.oxf.util.web.SimpleHttpClient;
import org.n52.ses.common.test.TestWSNEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllMessageFilterIT extends AbstractSubscriptionWorkflow {
	
	private static final Logger logger = LoggerFactory.getLogger(AllMessageFilterIT.class);
	private TestWSNEndpoint endpoint;
	private NotificationReceiver notificationReceiver;

	@Test public void
	testAllMessagesSubscription()
			throws Exception {
		notificationReceiver = initializeConsumer();
		
		ServiceInstance.getInstance().waitUntilAvailable();
		
		Subscription subscription = subscribe(endpoint.getPublicURL()+notificationReceiver.getPath());
		
		Thread.sleep(1000);
		
		notification();
		
		Future<?> future = Executors.newSingleThreadExecutor().submit(notificationReceiver);
		Object hasReceived = new Object();
		try {
			//null upon success
			hasReceived = future.get(IntegrationTestConfig.getInstance().getNotificationTimeout(), TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		
		unsubscribe(subscription);
		
		Thread.sleep(1000);
		
		Assert.assertNull("Noticiation not received!", hasReceived);
	}
	
	@Override
	protected Subscription subscribe(String consumerURL) throws OXFException,
			ExceptionReport, XmlException, IOException {
		String xml = readXmlContent("AllMessagesSubscription.xml").replace("${consumer}", consumerURL);
	
		URL host = ServiceInstance.getInstance().getHost();
		
		xml = xml.replace("${ses_host}", host.toExternalForm());
		logger.info("Subscription: {}", xml);
		
		HttpClient client = new SimpleHttpClient();
		HttpResponse result;
		try {
			result = client.executePost(host.toExternalForm(), xml);
		} catch (HttpClientException e) {
			throw new IOException(e);
		}
		
		XmlObject xo = XmlObject.Factory.parse(result.getEntity().getContent());
		logger.info(xo.xmlText());
		Subscription sub = new Subscription(null);
		sub.parseResponse(xo);
		return sub;
	}
	
	private NotificationReceiver initializeConsumer() throws IOException, InterruptedException {
		endpoint = TestWSNEndpoint.getInstance(IntegrationTestConfig.getInstance().getConsumerPort());
		NotificationReceiver notificationReceiver = new NotificationReceiver("stored-filter");
		endpoint.addListener(notificationReceiver);
		return notificationReceiver;
	}

	@Override
	public List<String> readNotifications() throws XmlException, IOException {
		return Collections.singletonList(readXmlContent("StoredFilter_Notify1.xml"));
	}

	@Override
	public String readSubscription() throws XmlException, IOException {
		return "";
	}

}
