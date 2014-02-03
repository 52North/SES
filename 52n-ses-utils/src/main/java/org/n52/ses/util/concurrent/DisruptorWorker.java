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
package org.n52.ses.util.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.n52.ses.api.event.MapEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * This is a testing implementation of concurrent message handling
 * using the disruptor (http://code.google.com/p/disruptor/).
 * Note, that the current implementation is in a very early beta stage
 * and is yet not capable of handling exception cases. Thus, it will stuck and
 * eventually cause an out-of-memory exception whenever
 * processing of a message has been cancelled for any reason.
 * 
 * Only use it, if you are 100% sure, that every message is going to be
 * processed correctly. Then, it is lightning fast :-)
 * 
 * @author matthes rieke <m.rieke@52north.org>
 *
 */
public class DisruptorWorker implements IConcurrentNotificationHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(DisruptorWorker.class);
	
	int RING_SIZE = (int) Math.pow(2, 5);
	Executor EXECUTOR = Executors.newSingleThreadExecutor();
	private Disruptor<QueuedMapEventCollection> disruptor;
	private RingBuffer<QueuedMapEventCollection> ringBuffer;
	private IPollListener listener;

	@SuppressWarnings("unchecked")
	public DisruptorWorker() {
		QueuedMapEventCollectionFactory factory = new QueuedMapEventCollectionFactory();
		QueuedMapEventCollectionHandler handler = new QueuedMapEventCollectionHandler();

		this.disruptor = new Disruptor<QueuedMapEventCollection>(factory, EXECUTOR, 
						new MultiThreadedClaimStrategy(RING_SIZE),
						new SleepingWaitStrategy());
		this.disruptor.handleEventsWith(handler);
		
		this.ringBuffer = disruptor.start();
	}
	
	/**
	 * @param listener the listener for processed messages
	 */
	public DisruptorWorker(IPollListener listener) {
		this();
		this.listener = listener;
	}

	@Override
	public void notifyOnDataAvailability(QueuedMapEventCollection event) {
		if (event != null) {
			this.ringBuffer.publish(event.getID());
		}
	}

	@Override
	public void startWorking() {
		//TODO
	}

	@Override
	public void stopWorking() {
		// TODO Auto-generated method stub

	}

	@Override
	public QueuedMapEventCollection insertPendingEventCollection(
			QueuedMapEventCollection event) {
		long sequence = this.ringBuffer.next();
		event.setID(sequence);
		return event;
	}


	@Override
	public int getNotProcessedFailureCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetFailures() {
		// TODO Auto-generated method stub

	}

	@Override
	public void joinUntilEmpty() {
		// TODO Auto-generated method stub

	}


	private class QueuedMapEventCollectionFactory implements EventFactory<QueuedMapEventCollection> {

		@Override
		public QueuedMapEventCollection newInstance() {
			return new QueuedMapEventCollection();
		}

	}

	private class QueuedMapEventCollectionHandler implements EventHandler<QueuedMapEventCollection> {

		@Override
		public void onEvent(QueuedMapEventCollection event, long sequence,
				boolean endOfBatch) throws Exception {
			for (MapEvent e : event.getCollection()) {
				if (logger.isDebugEnabled())
					logger.debug("Disruptor Event #"+sequence+"#="+e.get(MapEvent.DOUBLE_VALUE_KEY));
				listener.onElementPolled(e);
			}
		}

	}

	@Override
	public void setTimeout(long l) {
	}

	@Override
	public void setPollListener(IPollListener pl) {
		this.listener = pl;
	}

	@Override
	public void setUseIntelligentTimeout(boolean t) {
	}

}
