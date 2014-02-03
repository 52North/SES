/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * icense version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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
