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
