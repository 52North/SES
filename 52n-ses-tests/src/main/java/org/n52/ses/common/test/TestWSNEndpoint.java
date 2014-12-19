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
package org.n52.ses.common.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.n52.oxf.ses.adapter.client.httplistener.HttpListener;
import org.n52.oxf.ses.adapter.client.httplistener.IWSNConsumer;
import org.n52.oxf.ses.adapter.client.httplistener.SimpleWSNConsumer;

public class TestWSNEndpoint {

	private static Map<Integer, TestWSNEndpoint> instances = new HashMap<Integer, TestWSNEndpoint>();

	public static TestWSNEndpoint getInstance(int port) {
		synchronized (TestWSNEndpoint.class) {
			if (!instances.containsKey(port)) {
				instances.put(port, new TestWSNEndpoint(port));
			}
		}
		return instances.get(port);
	}
	
	private List<HttpListener> listeners = new ArrayList<HttpListener>();
	private int port;

	private TestWSNEndpoint(int port) {
		this.port = port;
		
		IWSNConsumer consumer;
		try {
			consumer = new SimpleWSNConsumer(port, "http://localhost:"+port);
			consumer.setListener(new HttpListener() {
			

				@Override
				public String processRequest(String request, String uri, String method,
						Properties header) {
					synchronized (TestWSNEndpoint.this) {
						for (HttpListener l : listeners) {
							l.processRequest(request, uri, method, header);
						}	
					}
					return null;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	public void addListener(HttpListener l) {
		synchronized (TestWSNEndpoint.this) {
			this.listeners.add(l);
		}
	}

	public String getPublicURL() {
		return "http://localhost:"+port;
	}

}
