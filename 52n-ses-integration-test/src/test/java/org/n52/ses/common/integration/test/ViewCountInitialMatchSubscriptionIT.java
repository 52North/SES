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
import java.util.concurrent.TimeoutException;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.oxf.OXFException;
import org.n52.oxf.ows.ExceptionReport;
import org.n52.oxf.ses.adapter.client.Subscription;
import org.n52.ses.common.test.TestWSNEndpoint;

public class ViewCountInitialMatchSubscriptionIT extends AbstractSubscriptionWorkflow {

	private TestWSNEndpoint endpoint;

	private CountNotificationReceiver notificationReceiver;


	@Test
	public void shouldCompleteRoundtripForNotification() throws IOException, InterruptedException,
				OXFException, ExceptionReport, XmlException, ExecutionException, TimeoutException {
		notificationReceiver = initializeConsumer();
		
		ServiceInstance.getInstance().waitUntilAvailable();
		
		Subscription subscription = subscribe();
		
		Thread.sleep(1000);
		
		notification();
		
		Thread.sleep(1000);
		
		unsubscribe(subscription);
		
		Thread.sleep(1000);
		
		Assert.assertTrue("Noticiation count did not match the expected count!",
				notificationReceiver.hasReceivedExpectedCount());
	}

	private CountNotificationReceiver initializeConsumer() throws IOException, InterruptedException {
		endpoint = TestWSNEndpoint.getInstance(IntegrationTestConfig.getInstance().getConsumerPort());
		CountNotificationReceiver notificationReceiver = new CountNotificationReceiver("initial-count", 1);
		endpoint.addListener(notificationReceiver);
		return notificationReceiver;
	}

	private Subscription subscribe() throws OXFException, ExceptionReport, XmlException, IOException {
		return super.subscribe(endpoint.getPublicURL()+notificationReceiver.getPath(),
				"http://www.opengis.net/ses/filter/level3");
	}


	public List<String> readNotifications() throws XmlException, IOException {
		List<String> result = new ArrayList<String>();
		result.add(readXmlContent("InitialMatch_Notify1.xml"));
		result.add(readXmlContent("InitialMatch_Notify1.xml"));
		return result;
	}

	public String readSubscription() throws XmlException, IOException {
		return readXmlContent("InitialMatch_Subscribe1.xml");
	}


}
