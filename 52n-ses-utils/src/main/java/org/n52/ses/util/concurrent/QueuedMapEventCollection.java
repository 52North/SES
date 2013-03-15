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
