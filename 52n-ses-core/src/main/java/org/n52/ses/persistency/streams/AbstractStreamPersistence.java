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
package org.n52.ses.persistency.streams;

import java.io.File;
import java.util.List;
import java.util.ServiceLoader;

import org.n52.ses.api.common.FreeResourceListener;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.event.PersistedEvent;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.util.common.ConfigurationRegistry;

public abstract class AbstractStreamPersistence implements FreeResourceListener {

	private static final int MAX_EVENTS_DEFAULT = 5;

	public static AbstractStreamPersistence newInstance(ISubscriptionManager subMgr, File baseLocation) throws Exception {
		ServiceLoader<AbstractStreamPersistence> asps = ServiceLoader.load(AbstractStreamPersistence.class);
		
		if (baseLocation == null) {
			baseLocation = new File(AbstractStreamPersistence.class.getResource("/").getFile());
		}
		
		/*
		 * return the first available
		 */
		for (AbstractStreamPersistence abstractStreamPersistence : asps) {
			int maxEvents = MAX_EVENTS_DEFAULT;
			if (ConfigurationRegistry.isAvailable()) {
				ConfigurationRegistry.getInstance().registerFreeResourceListener(abstractStreamPersistence);
				Integer confEvents = ConfigurationRegistry.getInstance().getIntegerProperty(ConfigurationRegistry.MAX_PERSISTED_EVENTS);
				if (confEvents != null) {
					maxEvents = confEvents.intValue();
				}
			}
			
			abstractStreamPersistence.initialize(subMgr, baseLocation, maxEvents);

			return abstractStreamPersistence;
		}
		
		return null;
	}

	protected abstract void initialize(ISubscriptionManager subMgr, File baseLocation, int maxEvents) throws Exception;
	
	/**
	 * An implementation shall return the persisted events (count
	 * depends on the actual implementation) in the order
	 * it received it (timestamp ordered, early to late).
	 * 
	 * @return the list of persisted events, timestamp ordered
	 */
	public abstract List<PersistedEvent> getPersistedEvents();

	/**
	 * An implementation shall store the incoming event and
	 * return it as as list value of the {@link #getPersistedEvents()} method.
	 * 
	 * @param eve the event
	 * @param streamName 
	 */
	public abstract void persistEvent(MapEvent eve, String streamName);

	/**
	 * After calling this, the persisted events will be lost
	 * and all data related is deleted.
	 * 
	 * @throws Exception
	 */
	public abstract void destroy() throws Exception;
	
	/**
	 * free runtime resources
	 * 
	 * @throws Exception
	 */
	public abstract void shutdown() throws Exception;

	public abstract int getMaximumEventCount();
}
