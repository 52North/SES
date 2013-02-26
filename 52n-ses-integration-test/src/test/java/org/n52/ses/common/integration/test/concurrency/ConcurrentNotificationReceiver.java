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
package org.n52.ses.common.integration.test.concurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.n52.oxf.ses.adapter.client.httplistener.HttpListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentNotificationReceiver implements HttpListener {

	private static final Logger logger = LoggerFactory.getLogger(ConcurrentNotificationReceiver.class);
	private Map<String, List<String>> uriToNotifications;
	
	public ConcurrentNotificationReceiver() {
		this.uriToNotifications = new HashMap<String, List<String>>();
	}
	
	
	@Override
	public String processRequest(String request, String uri, String method,
			Properties header) {
		logger.info("Received Notification for consumer at {}", uri);
		synchronized (this) {
			List<String> list;
			if (!this.uriToNotifications.containsKey(uri)) {
				list = new ArrayList<String>();
				this.uriToNotifications.put(uri, list);
			}
			else {
				list = this.uriToNotifications.get(uri);
			}
			
			list.add(request);
		}
		
		return null;
	}


	public synchronized Map<String, List<String>> getUriToNotifications() {
		return new HashMap<String, List<String>>(uriToNotifications);
	}
	
	
	
}
