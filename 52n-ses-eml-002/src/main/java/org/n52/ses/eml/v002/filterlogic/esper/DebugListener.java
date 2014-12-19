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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v002.filterlogic.esper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.*;
import com.espertech.esper.client.EventBean;

/**
 * Statement aware update listener to debug the plugin
 * 
 * @author Thomas Everding
 *
 */
public class DebugListener implements StatementAwareUpdateListener{
	
	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(DebugListener.class);

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement epStatement, EPServiceProvider serviceProvider) {
		DebugListener.logger.debug("");
		DebugListener.logger.debug("-------------------------------");
		DebugListener.logger.debug("Update received for statement:");
		DebugListener.logger.debug("\tname: " + epStatement.getName());
		DebugListener.logger.debug("\ttext: " + epStatement.getText());
		
		if (newEvents != null) {
			DebugListener.logger.debug("new events:");
			DebugListener.logger.debug("\tsize: " + newEvents.length);
			
			for (int i = 0; i < newEvents.length; i++) {
				DebugListener.logger.debug("\tnumber " + i);
				DebugListener.logger.debug("\t\tbean:  " + newEvents[i]);
				
				try{
					Object obj = newEvents[i].get("value");
					DebugListener.logger.debug("\t\tvalue: " + obj);
				}
				catch (Throwable t) {/*empty*/}
			}
		}
		else {
			DebugListener.logger.debug("new events are null");
		}
		
		if (oldEvents != null) {
			DebugListener.logger.debug("old events:");
			DebugListener.logger.debug("\tsize: " + oldEvents.length);
			
			for (int i = 0; i < oldEvents.length; i++) {
				DebugListener.logger.debug("\tnumber " + i);
				DebugListener.logger.debug("\t\tbean: " + oldEvents[i]);
			}
		}
		else {
			DebugListener.logger.debug("old events are null");
		}
		
		DebugListener.logger.debug("service provider: ");
		DebugListener.logger.debug("\t" + serviceProvider);
		
		DebugListener.logger.debug("-------------------------------");
	}
	
}
