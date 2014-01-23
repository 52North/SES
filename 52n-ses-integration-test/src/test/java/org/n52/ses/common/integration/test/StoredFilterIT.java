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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.oxf.ses.adapter.client.Subscription;
import org.n52.ses.common.test.TestWSNEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoredFilterIT extends AbstractSubscriptionWorkflow {
	
	private static final Logger logger = LoggerFactory.getLogger(StoredFilterIT.class);
	private TestWSNEndpoint endpoint;
	private NotificationReceiver notificationReceiver;

	@Test public void
	testStoredFilterSubscription()
			throws Exception {
		notificationReceiver = initializeConsumer();
		
		ServiceInstance.getInstance().waitUntilAvailable();
		
		Subscription subscription = subscribe(endpoint.getPublicURL()+notificationReceiver.getPath(),
				"http://www.opengis.net/es-sf/0.0");
		
		Thread.sleep(1000);
		
		notification();
		
		Future<?> future = Executors.newSingleThreadExecutor().submit(notificationReceiver);
		Object hasReceived = new Object();
		try {
			//null upon success
			hasReceived = future.get(IntegrationTestConfig.getInstance().getNotificationTimeout()*2, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		
		unsubscribe(subscription);
		
		Thread.sleep(1000);
		
		Assert.assertNull("Noticiation not received!", hasReceived);
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
		return readXmlContent("StoredFilter_Subscribe1.xml");
	}

}
