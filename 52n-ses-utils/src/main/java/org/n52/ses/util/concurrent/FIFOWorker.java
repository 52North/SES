/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.n52.ses.api.event.MapEvent;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default concurrent implementation of the SES.
 * It internally stores received {@link QueuedMapEventCollection}s
 * in a {@link ConcurrentLinkedQueue}. 
 * 
 * @author matthes rieke <m.rieke@52north.org>
 *
 */
public class FIFOWorker implements IConcurrentNotificationHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(FIFOWorker.class);

	private boolean running = true;
	private ConcurrentLinkedQueue<QueuedMapEventCollection> queue;
	private IPollListener listener;
	private Object queueWaiter = new Object();
	private long timeout = 5000;

	private int notProcessedFailureCount;

	private Thread thread;

	private boolean useIntelligentTimeout;

	private ITimeoutEstimation estimation;

	protected boolean stopped;

	public FIFOWorker() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this.queue = new ConcurrentLinkedQueue<QueuedMapEventCollection>();
		Class<?> clazz = Class.forName(ConfigurationRegistry.getInstance().getPropertyForKey(
				ConfigurationRegistry.TIMEOUT_ESTIMATION));
		this.estimation = (ITimeoutEstimation) clazz.newInstance();
		this.estimation.setMinimumTimeout(Integer.parseInt(ConfigurationRegistry.getInstance().getPropertyForKey(
				ConfigurationRegistry.CONCURRENT_MINIMUM_TIMEOUT)));
		this.estimation.setMaximumTimeout(Integer.parseInt(ConfigurationRegistry.getInstance().getPropertyForKey(
				ConfigurationRegistry.CONCURRENT_MAXIMUM_TIMEOUT)));
	}

	/**
	 * @param listener the callback which process the message
	 * @param timeout the timeout the worker should wait until discarding a non-processed event
	 * @throws IllegalAccessException due to reflections
	 * @throws InstantiationException due to reflections
	 * @throws ClassNotFoundException due to reflections
	 */
	public FIFOWorker(IPollListener listener, int timeout) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		this();
		this.listener = listener;
		setTimeout(timeout);
	}


	private void callPollListeners(MapEvent alert) {
		if (this.listener != null) this.listener.onElementPolled(alert);
	}

	@Override
	public QueuedMapEventCollection insertPendingEventCollection(
			QueuedMapEventCollection result) {
		synchronized (this.queueWaiter) {
			this.queue.offer(result);
			this.queueWaiter.notifyAll();
		}
		return result;
	}

	@Override
	public void notifyOnDataAvailability(QueuedMapEventCollection event) {
	}

	@Override
	public void startWorking() {
		this.running = true;
		this.thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (FIFOWorker.this.running) {

					QueuedMapEventCollection alerts;
					synchronized (FIFOWorker.this.queueWaiter) {
						while (FIFOWorker.this.queue.isEmpty()) {
							/*
							 * is this a system shutdown?
							 */
							if (!FIFOWorker.this.running) return;

							try {
								FIFOWorker.this.queueWaiter.wait();
							} catch (InterruptedException e) {
								logger.warn(e.getMessage(), e);
							}
						}

					}

					while (!FIFOWorker.this.queue.isEmpty()) {
						if (FIFOWorker.this.stopped) return;
						
						/*
						 * get message from queue
						 */
						alerts = queue.poll();
						if (logger.isDebugEnabled())
							logger.debug("####### current queue size: "+FIFOWorker.this.queue.size());

						/*
						 * queue was empty
						 */
						if (alerts == null) continue;

						/*
						 * check if we got the next valid element,
						 * otherwise wait.
						 */
						if (!alerts.getFuture().isDone()) {
							try {
								/*
								 * wait the timeout time and check again one time,
								 * else discard
								 */
								long t = FIFOWorker.this.getCurrenTimeout();

								if (logger.isDebugEnabled()) {
									logger.debug("current estimated timeout: "+t);
								}
								try {
									if (alerts.getFuture().get(t, TimeUnit.MILLISECONDS) != null) {
										/*
										 * we have waited the timeout period,
										 * processing did not take place.
										 * discard this one and go for the next.
										 */
										alerts.getFuture().cancel(true);
										FIFOWorker.this.notProcessedFailureCount++;
										continue;
									}

								} catch (ExecutionException e) {
									logger.warn(e.getMessage());
									FIFOWorker.this.notProcessedFailureCount++;
									continue;
								} catch (TimeoutException e) {
									logger.warn(e.getMessage());
									FIFOWorker.this.notProcessedFailureCount++;
									continue;
								}

								/*
								 * update the timeout
								 */
								if (FIFOWorker.this.useIntelligentTimeout) {
									alerts.setElapsedTime();
									estimation.updateTimeout(alerts.getElapsedTime(), true);
								}

							} catch (InterruptedException e) {
								logger.warn(e.getMessage(), e);
							}
						}

						if (!alerts.isFilled()) {
							/*
							 * it was cancelled, ignore it
							 */
							FIFOWorker.this.notProcessedFailureCount++;
							continue;
						}

						/*
						 * update the timeout
						 */
						if (FIFOWorker.this.useIntelligentTimeout) {
							estimation.updateTimeout(alerts.getElapsedTime());
						}

						/*
						 * we polled a processed element, yay!
						 * forward it to our listener
						 */
						if (alerts.getCollection() != null) {
							for (MapEvent alert : alerts.getCollection()) {
								if (logger.isInfoEnabled()) {
									logger.info(alert.toString());
								}

								callPollListeners(alert);
							}	
						}
					}
				}
			}
		});
		this.thread.start();
	}

	protected long getCurrenTimeout() {
		if (this.useIntelligentTimeout) {
			return this.estimation.getCurrenTimeout();
		}
		return this.timeout;
	}

	@Override
	public void stopWorking() {
		this.stopped = true;
		this.running = false;
		synchronized (this.queueWaiter) {
			this.queueWaiter.notifyAll();
		}
	}

	@Override
	public int getNotProcessedFailureCount() {
		return this.notProcessedFailureCount;
	}

	@Override
	public void resetFailures() {
		this.notProcessedFailureCount = 0;
	}

	@Override
	public void joinUntilEmpty() {

		this.running = false;
		synchronized (this.queueWaiter) {
			this.queueWaiter.notifyAll();
		}
		try {
			this.thread.join();
		} catch (InterruptedException e) {
			logger.warn(e.getMessage(), e);
		}
		startWorking();

	}

	@Override
	public void setTimeout(long l) {
		this.timeout = l;
		if (this.estimation != null) {
			this.estimation.setMaximumTimeout((int) timeout);
		}
	}

	@Override
	public void setPollListener(IPollListener pl) {
		this.listener = pl;
	}

	@Override
	public void setUseIntelligentTimeout(boolean b) {
		this.useIntelligentTimeout = b;
	}

}