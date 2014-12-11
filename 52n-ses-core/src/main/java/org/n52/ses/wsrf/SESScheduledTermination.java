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
package org.n52.ses.wsrf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.resource.impl.AbstractWsResourceCapability;
import org.apache.muse.ws.resource.lifetime.ScheduledTermination;
import org.n52.ses.api.common.FreeResourceListener;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provided a more efficient implementation
 * of {@link ScheduledTermination} than muse's standard
 * one.
 * A singleton Thread checks all registered resources
 * if they should be terminated.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESScheduledTermination extends AbstractWsResourceCapability implements ScheduledTermination {

	private static final Logger logger = LoggerFactory.getLogger(SESScheduledTermination.class);

	@Override
	public Date getCurrentTime() {
		return new Date();
	}
	
    @Override
	public QName[] getPropertyNames()
    {
        return PROPERTIES;
    }

	@Override
	public Date getTerminationTime() {
		return TerminationWorkerThread.getInstance().getTerminationTime(this);
	}

	@Override
	public Date setTerminationTime(Date time) {
		TerminationWorkerThread.getInstance().addTerminationTime(this, time);
		return time;
	}

	

	@Override
	public void prepareShutdown() throws SoapFault {
		TerminationWorkerThread.getInstance().freeResources();
		super.prepareShutdown();
	}



	/**
	 * The singleton worker thread.
	 * 
	 * @author Matthes Rieke <m.rieke@uni-muenster.de>
	 *
	 */
	public static class TerminationWorkerThread implements Runnable, FreeResourceListener  {

		private static TerminationWorkerThread _instance;
		private Map<ScheduledTermination, Date> scheduledTerminations;
		private boolean running = true;

		private TerminationWorkerThread() {
			this.scheduledTerminations = new HashMap<ScheduledTermination, Date>();
			Thread t = new Thread(this);
			t.setName("SES-ScheduledTermination-Worker-Thread");
			t.setDaemon(true);
			t.start();
			ConfigurationRegistry.getInstance().registerFreeResourceListener(this);
		}

		public void shutdown() {
			this.running = false;
		}

		@Override
		public void freeResources() {
			shutdown();
		}
		
		/**
		 * @param st the resource
		 * @return the termination time for the given resource
		 */
		public Date getTerminationTime(ScheduledTermination st) {
			return this.scheduledTerminations.get(st);
		}

		/**
		 * @return the singleton instance
		 */
		public static synchronized TerminationWorkerThread getInstance() {
			if (_instance == null) {
				_instance = new TerminationWorkerThread();
			}

			return _instance;
		}

		/**
		 * @param st the ScheduledTermination instance
		 * @param time the time of termination
		 */
		public synchronized void addTerminationTime(ScheduledTermination st, Date time) {
			this.scheduledTerminations.put(st, time);
		}

		
		@Override
		public void run() {
			Date now;

			runningloop:
			while (this.running) {
				/*
				 * check every minute
				 */
				for (int i = 0; i < 6; i++) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						logger.warn(e.getMessage(), e);
					}
					
					if (!this.running) break runningloop;
				}
				

				now = new Date();
				
				/*
				 * synchronized on the list
				 */
				List<ScheduledTermination> removed = new ArrayList<ScheduledTermination>();
				synchronized (this) {
					for (ScheduledTermination st : this.scheduledTerminations.keySet()) {
						Date d = this.scheduledTerminations.get(st);
						
						/*
						 * do we have a infinite resource?
						 */
						if (d == null) continue;
						
						/*
						 * check if resource should be shutdown
						 */
						if (d.before(now)) {
							try {
								st.getResource().shutdown();
								removed.add(st);
							}

							catch (SoapFault fault) {
								//
								// If the resource destructor fails, there's not much 
								// we can do - there is no caller to report back to, 
								// so we just log the info
								//
								logger.warn(fault.getMessage(), fault);
							}
						}
					}
				}
				
				/*
				 * remove from this thread
				 */
				for (ScheduledTermination st : removed) {
					this.scheduledTerminations.remove(st);
				}
			}
		}
	}


}
