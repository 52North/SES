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
package org.n52.ses.wsn.dissemination.updateinterval;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.namespace.QName;

import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.impl.SimpleNotificationMessage;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.wsn.dissemination.updateinterval.batching.BatchingHandler;
import org.w3c.dom.Element;

public class BatchingMessageListener implements MessageListener {


	private NotificationMessage messageSkeleton;
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	private NotificationMessage batchedMessage;
	public AtomicBoolean firstRun = new AtomicBoolean(true);
	public BatchingHandler handler;

	@Override
	public void newMessage(NotificationMessage message) {
		executor.submit(new BatchUpdateReceived(message));
	}

	@Override
	public synchronized NotificationMessage pullMessage() {
		return this.batchedMessage;
	}

	@Override
	public void newMessageForFeature(NotificationMessage message, String feature) {
		
	}

	public NotificationMessage createBatchMessage(XmlObject content) {
		NotificationMessage result = new WrappedNotificationMessage(this.messageSkeleton);
		result.addMessageContent((Element) content.getDomNode());
		return result;
	}

	@Override
	public void shutdown() {
		this.executor.shutdownNow();
	}

	
	private class BatchUpdateReceived implements Runnable {

		private NotificationMessage latestMessage;
		
		public BatchUpdateReceived(NotificationMessage message) {
			this.latestMessage = message;
		}

		@Override
		public void run() {
			//TODO INCORPORATE NEW MESSAGE
			//TODO CALL BATCHING MECHANISM: e.g. identify xml type and then call the corrsponding handler
			if (firstRun.getAndSet(false)) {
				createHandler();
			}

			messageSkeleton = this.latestMessage;
			handler.incorporateNewMessage(this.latestMessage);
			
			synchronized (BatchingMessageListener.this) {
				batchedMessage = createBatchMessage(handler.getBatchedMessage());
			}
			
		}

		private void createHandler() {
			BatchingMessageListener.this.handler = BatchingHandler.createBatchingHandler(latestMessage);
		}
		
	}
	
	
	private class WrappedNotificationMessage extends SimpleNotificationMessage {

		private NotificationMessage wrapper;

		public WrappedNotificationMessage(NotificationMessage messageSkeleton) {
			this.wrapper = messageSkeleton;
		}

		@Override
		public EndpointReference getProducerReference() {
			return this.wrapper.getProducerReference();
		}

		@Override
		public EndpointReference getSubscriptionReference() {
			return this.wrapper.getSubscriptionReference();
		}

		@Override
		public QName getTopic() {
			return this.wrapper.getTopic();
		}

		@Override
		public String getTopicExpression() {
			return this.wrapper.getTopicExpression();
		}

		@Override
		public String getTopicDialect() {
			return this.wrapper.getTopicDialect();
		}
		
		
		
	}




}
