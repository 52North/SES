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
package org.n52.ses.api;

import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.SubscriptionManager;

/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public interface IFilterEngine  {
	
	/**
	 * Filter incoming data. Method parses the data from the
	 * given XML document (O&M).
	 * 
	 * @param message the notification message to filter
	 */
	public abstract void filter(NotificationMessage message);
	
	/**
	 * register a new subscription filter.
	 * @param subMgr the subscription manager
	 * @throws Exception exception if error occurs during the registration
	 */
	public abstract void registerFilter(SubscriptionManager subMgr) throws Exception;
	
	/**
	 * remove a subscription from the filter engine 
	 * @param subMgr the subscription manager responsible for the subscription
	 * @throws Exception if error occurs during unregistration
	 */
	public abstract void unregisterFilter(SubscriptionManager subMgr) throws Exception;

	
	/**
	 * shutdown the resource
	 */
	public abstract void shutdown();

}
