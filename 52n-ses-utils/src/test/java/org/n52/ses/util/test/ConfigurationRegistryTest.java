/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.ses.util.test;

import java.io.IOException;

import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.impl.FilterCollection;
import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.api.IFilterEngine;
import org.n52.ses.api.ISESFilePersistence;
import org.n52.ses.api.ws.INotificationMessage;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.common.test.EnvironmentMockup;
import org.n52.ses.util.common.ConfigurationRegistry;

public class ConfigurationRegistryTest {

	
	protected ConfigurationRegistry concurrentInstance;

	@Test
	public void testProperties() throws InterruptedException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ConfigurationRegistry inst = ConfigurationRegistry.getInstance();
				concurrentInstance = inst;
			}
		}).start();
		
		Thread.sleep(500);
		
		ConfigurationRegistry.init(getClass().getResourceAsStream("ses_config_test.xml"), new EnvironmentMockup());
		
		ConfigurationRegistry conf = ConfigurationRegistry.getInstance();
		Assert.assertNull(conf.getPropertyForKey("testttt"));
		Assert.assertTrue(conf.getPropertyForKey(ConfigurationRegistry.TIME_TO_WAKEUP).equals("1000"));
		ISESFilePersistence fp = new ISESFilePersistence() {
			
			@Override
			public void removePattern(EndpointReference endpointReference,
					String patternXpath) throws XmlException, IOException {
			}
			
			@Override
			public int getPersistentSubscriberCount() {
				return 0;
			}
			
			@Override
			public int getPersistentPublisherCount() {
				return 0;
			}
		};
		
		conf.setFilePersistence(fp);
		Assert.assertTrue(conf.getFilePersistence().equals(fp));
		
		IFilterEngine fe = new IFilterEngine() {
			
			@Override
			public void unregisterFilter(ISubscriptionManager subMgr) throws Exception {
			}
			
			@Override
			public void shutdown() {
			}
			
			@Override
			public boolean registerFilter(ISubscriptionManager subMgr,
					FilterCollection engineCoveredFilters) throws Exception {
				return false;
			}
			
			@Override
			public void filter(INotificationMessage message) {
			}
		};
		
		conf.setFilterEngine(fe);
		Assert.assertTrue(conf.getFilterEngine().equals(fe));
		
		Thread.sleep(200);
		
		Assert.assertNotNull(concurrentInstance);
		
		conf.waitForAllPersistentPublishers();
	}
	
}
