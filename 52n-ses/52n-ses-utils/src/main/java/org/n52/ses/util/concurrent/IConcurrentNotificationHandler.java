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

import org.n52.ses.api.event.MapEvent;

/**
 * Interface for a concurrent Notification processor.
 * An implementation should inform at least one implementation
 * of {@link IPollListener} about availability of inserted
 * {@link QueuedMapEventCollection}. These get claimed before
 * the actual processing using the {@link #insertPendingEventCollection(QueuedMapEventCollection)}
 * method.
 * 
 * @author matthes rieke <m.rieke@52north.org>
 *
 */
public interface IConcurrentNotificationHandler {
	
	/**
	 * notify the worker that new data is available
	 * @param coll the collection which has been processed
	 */
	public void notifyOnDataAvailability(QueuedMapEventCollection coll);

	/**
	 * start the concurrent processing
	 */
	public void startWorking();

	/**
	 * stop the concurrent processing
	 */
	public void stopWorking();
	
	/**
	 * @param coll the collection of MapEvents to be filled
	 * @return an empty collection of {@link MapEvent}s. This will get filled after
	 * processing has finished. 
	 */
	public QueuedMapEventCollection insertPendingEventCollection(QueuedMapEventCollection coll);

	/**
	 * @return number of errors due to unprocessed elements
	 */
	public int getNotProcessedFailureCount();

	/**
	 * reset the failure counters
	 */
	public void resetFailures();

	/**
	 * Join the working thread until all elements got processed and forwarded
	 * to the {@link IPollListener} implementation.
	 */
	public void joinUntilEmpty();

	/**
	 * @param l the timeout the handler should wait for messages to be processed
	 */
	public void setTimeout(long l);
	
	/**
	 * @param pl the impl of {@link IPollListener}, getting called for each event item
	 */
	public void setPollListener(IPollListener pl);
	
	
	/**
	 * set this flag to enable training an benchmarking of timeout deltas
	 * by taking actual processing periods into account.
	 * @param b true?
	 */
	public void setUseIntelligentTimeout(boolean b);
	
	
	/**
	 * A listener interface which gets called when
	 * a MapEvent is processed.
	 * 
	 * @author matthes rieke <m.rieke@52north.org>
	 *
	 */
	public static interface IPollListener {
		
		/**
		 * @param alert the next available MapEvent 
		 */
		public void onElementPolled(MapEvent alert);
		
	}


}
