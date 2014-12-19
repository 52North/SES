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
package org.n52.ses.eml.v002.filterlogic.esper;

import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.eml.v002.pattern.Statement;

import com.espertech.esper.client.EventBean;

/**
 * This class extends the StatementListener. It only processes
 * the first event of an incoming event-array. This functionality
 * is close to impossible to create within esper using EPL.
 * Only constructor and the {@link StatementListener#update(EventBean[], EventBean[])}
 * method are needed.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class FirstEventStatementListener extends StatementListener {

	
	
	/**
	 * see {@link StatementListener#StatementListener(Statement, EsperController, SubscriptionManager)}
	 */
	public FirstEventStatementListener(Statement statement,
			EsperController controller, ISubscriptionManager sub) {
		super(statement, controller, sub);
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if (newEvents != null && newEvents.length > 0) {
			this.handleMatch(newEvents[0]);
		}
	}

	
	
}
