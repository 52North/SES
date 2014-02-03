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

import java.util.List;
import java.util.concurrent.Future;

import org.n52.ses.api.event.MapEvent;

/**
 * This class provides mechanisms for concurrent handling of incoming MapEvent.
 * 
 * 
 * @author matthes rieke <m.rieke@52north.org>
 *
 */
/**
 * @author matthes rieke <m.rieke@52north.org>
 *
 */
public class QueuedMapEventCollection {
	
	public static final int INITIAL_PRIORITY = Integer.MIN_VALUE;
	
	private List<MapEvent> collection;
	private boolean filled;
	private Object processedBarrier = new Object();
	private long id;
	private long startTime;
	private long elapsedTime;

	private Future<?> future;


	/**
	 * Constructor which takes a priority id.
	 * Some concurrent handlers (including the SES' default one)
	 * will not use the priority.
	 * 
	 * @param pid priority
	 */
	public QueuedMapEventCollection() {
	}

	
	/**
	 * Use this method to set the data of the container.
	 * here, waiting threads (which are waiting using the {@link #waitUntilProcessingFinished(long)}
	 * method) are notified.
	 * 
	 * @param collection the processed result of the NotificationMessage contents
	 */
	public synchronized void setCollection(List<MapEvent> collection) {
		this.collection = collection;
		this.filled = true;
		setElapsedTime();
		
		synchronized (this.processedBarrier) {
			this.processedBarrier.notifyAll();
		}
	}
	
	/**
	 * wait at a barrier until processing is finished.
	 * {@link Object#wait(long)} is being called here.
	 * 
	 * @param timeoutMillis the wait timeout
	 * @throws InterruptedException if the wait got interuppted
	 */
	public void waitUntilProcessingFinished(long timeoutMillis) throws InterruptedException {
		synchronized (this.processedBarrier) {
			this.processedBarrier.wait(timeoutMillis);
		}
	}

	/**
	 * @return the event data
	 */
	public synchronized List<MapEvent> getCollection() {
		return this.collection;
	}

	/**
	 * @return true if {@link #getCollection()} returns the processed data.
	 */
	public synchronized boolean isFilled() {
		return this.filled;
	}


	public void setID(long sequence) {
		this.id = sequence;
	}


	public long getID() {
		return this.id;
	}


	/**
	 * set the time delta, starting from creation to processing finished
	 */
	public void setElapsedTime() {
		this.elapsedTime = System.currentTimeMillis() - this.startTime;
	}


	/**
	 * @return the elapsed time delta, starting from creation to processing finished
	 */
	public long getElapsedTime() {
		return this.elapsedTime;
	}


	public void updateStartTime() {
		this.startTime = System.currentTimeMillis();
	}


	/**
	 * @param f the future object
	 */
	public void setFuture(Future<?> f) {
		this.future = f;
	}


	/**
	 * @return the future object
	 */
	public Future<?> getFuture() {
		return this.future;
	}
	
	
	

	
}
