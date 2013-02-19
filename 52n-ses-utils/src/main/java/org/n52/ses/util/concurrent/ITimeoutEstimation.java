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

/**
 * Interface for a timeout estimation algorithm impl used
 * by instances of {@link IConcurrentNotificationHandler}.
 * 
 * @author matthes rieke <m.rieke@52north.org>
 *
 */
public interface ITimeoutEstimation {

	/**
	 * @param timeout the initial timeout
	 */
	void setMaximumTimeout(int timeout);

	/**
	 * @param l update the data with a new measured processing period
	 */
	void updateTimeout(long l);

	/**
	 * @param l update the data with a new measured processing period
	 * @param onFailure flag for processing failure, could be used to weight periods
	 */
	void updateTimeout(long l, boolean onFailure);

	/**
	 * @return the estimated timeout calculated by the underlying algorithm
	 */
	int getCurrenTimeout();

	/**
	 * @param l the minimum timeout
	 */
	void setMinimumTimeout(int l);

}
