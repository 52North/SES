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
