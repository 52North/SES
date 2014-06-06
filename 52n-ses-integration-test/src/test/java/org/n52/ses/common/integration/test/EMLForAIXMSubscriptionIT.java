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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ses.adapter.client.Subscription;
import org.n52.ses.common.test.TestWSNEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EMLForAIXMSubscriptionIT extends AbstractSubscriptionWorkflow {

	private static final Logger logger = LoggerFactory.getLogger(EMLForAIXMSubscriptionIT.class);

	private TestWSNEndpoint endpoint;

	private NotificationReceiver notificationReceiver;


	@Test
	public void shouldCompleteRoundtripForNotification() throws IOException, InterruptedException,
				OXFException, ExceptionReport, XmlException, ExecutionException, TimeoutException {
		notificationReceiver = initializeConsumer();
		
		ServiceInstance.getInstance().waitUntilAvailable();
		
		Subscription subscription = subscribe();
		
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
		
		evaluate(hasReceived);
	}



	protected void evaluate(Object hasReceived) {
		Assert.assertNull("Noticiation not received!", hasReceived);		
	}



	private NotificationReceiver initializeConsumer() throws IOException, InterruptedException {
		endpoint = TestWSNEndpoint.getInstance(IntegrationTestConfig.getInstance().getConsumerPort());
		NotificationReceiver notificationReceiver = new NotificationReceiver("aixm-consumer");
		endpoint.addListener(notificationReceiver);
		return notificationReceiver;
	}

	protected Subscription subscribe() throws OXFException, ExceptionReport, XmlException, IOException {
		return super.subscribe(getConsumerUrl(),
				"http://www.opengis.net/ses/filter/level3");
	}


	protected String getConsumerUrl() {
		return endpoint.getPublicURL()+notificationReceiver.getPath();
	}


	public List<String> readNotifications() throws XmlException, IOException {
		List<String> result = new ArrayList<String>();
		result.add(readXmlContent("Navaid.xml"));
		return result;
	}

	public String readSubscription() throws XmlException, IOException {
		return readXmlContent("EMLFilterForAIXM.xml");
	}


}
