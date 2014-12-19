/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.ses.common.integration.test;

import java.util.Properties;

import org.n52.oxf.ses.adapter.client.httplistener.HttpListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationReceiver implements Runnable, HttpListener {

	private static final Logger logger = LoggerFactory.getLogger(NotificationReceiver.class);
	
	private Object waitMutex = new Object();
	private boolean hasReceived = false;
	private String path;
	
	public NotificationReceiver(String path) {
		if (path != null) {
			if (path.startsWith("/")) {
				this.path = path;
			}
			else {
				this.path = "/"+path;
			}
		}
	}
	
	@Override
	public void run() {
		synchronized (waitMutex) {
			while (!hasReceived) {
				try {
					waitMutex.wait();
				} catch (InterruptedException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
	}
	
	@Override
	public String processRequest(String request, String uri, String method,
			Properties header) {
		synchronized (waitMutex) {
			if (!uri.equals(this.path)) return null;
			hasReceived = true;
			waitMutex.notifyAll();
		}
		return null;
	}

	public String getPath() {
		return path;
	}
	
	

}