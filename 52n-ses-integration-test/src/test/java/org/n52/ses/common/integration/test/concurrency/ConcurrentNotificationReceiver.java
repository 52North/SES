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
