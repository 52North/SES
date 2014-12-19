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
package org.n52.ses.persistency;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.event.PersistedEvent;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.persistency.streams.AbstractStreamPersistence;

public class H2StreamPersistenceTest {
	
	private static final String TEST_NAME = "testStream";
	@Mock
	ISubscriptionManager subMgr;
	private int control = 0;
	private String uuid = UUID.randomUUID().toString();
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(subMgr.getUniqueID()).thenReturn(uuid);
	}
	
	@After
	public void free() throws Exception {
		AbstractStreamPersistence.newInstance(subMgr, null).destroy();
	}
	
	@Test
	public void testRountrip() throws Exception {
		control = 0;
		AbstractStreamPersistence asp = AbstractStreamPersistence.newInstance(subMgr, null);
		
		List<PersistedEvent> list = asp.getPersistedEvents();
		
		Assert.assertTrue(list.isEmpty());
		
		MapEvent me = createEvent();
		asp.persistEvent(me, TEST_NAME);
		
		list = asp.getPersistedEvents();
		Assert.assertTrue(list.size() == 1);
		
		me = createEvent();
		asp.persistEvent(me, TEST_NAME);
		
		list = asp.getPersistedEvents();
		Assert.assertTrue(list.size() == 2);
		
		int index = 0;
		for (PersistedEvent pe : list) {
			MapEvent ome = pe.getEvent();
			Assert.assertTrue(pe.getStreamName().equals(TEST_NAME));
			Assert.assertTrue(XmlObject.Factory.parse("<test><content>123</content></test>").valueEquals((XmlObject) ome.get(MapEvent.ORIGNIAL_MESSAGE_KEY)));
			Assert.assertTrue((Double) ome.get(MapEvent.DOUBLE_VALUE_KEY) == 23.5);
			Assert.assertTrue(ome.get(MapEvent.SENSORID_KEY).equals("theSensor"));
			Assert.assertTrue((Long) ome.get(MapEvent.START_KEY) == index);
			Assert.assertTrue((Long) ome.get(MapEvent.END_KEY) == index++);			
		}

		asp.destroy();
	}
	
	@Test
	public void testMaximumCount() throws Exception {
		control = 0;
		
		AbstractStreamPersistence asp = AbstractStreamPersistence.newInstance(subMgr, null);
		
		List<MapEvent> events = new ArrayList<MapEvent>();
		for (int i = 0; i < asp.getMaximumEventCount()+1; i++) {
			events.add(createEvent());
		}
		
		for (MapEvent mapEvent : events) {
			asp.persistEvent(mapEvent, TEST_NAME);
		}
		
		List<PersistedEvent> list = asp.getPersistedEvents();
		Assert.assertTrue(list.size() == asp.getMaximumEventCount());
		
		int index = 1;
		for (PersistedEvent pe : list) {
			MapEvent ome = pe.getEvent();
			Assert.assertTrue((Long) ome.get(MapEvent.START_KEY) == index++);
		}
		
		asp.destroy();
	}

	private MapEvent createEvent() throws XmlException {
		MapEvent me = new MapEvent(control, control++);
		me.put(MapEvent.ORIGNIAL_MESSAGE_KEY, XmlObject.Factory.parse("<test><content>123</content></test>"));
		me.put(MapEvent.DOUBLE_VALUE_KEY, 23.5);
		me.put(MapEvent.SENSORID_KEY, "theSensor");
		return me;
	}

}
