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
package org.n52.ses.filter.dialects;

import java.util.ArrayList;
import java.util.List;

import org.apache.muse.ws.notification.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public abstract class DialectConstraintFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(DialectConstraintFilter.class);
	private static List<Class<? extends DialectConstraintFilter>> customFilters = new ArrayList<Class<? extends DialectConstraintFilter>>();
	
	static {
		registerCustomFilter(SelectiveMetadataFilter.class);
	}
	
	public static synchronized void registerCustomFilter(Class<? extends DialectConstraintFilter> class1) {
		customFilters.add(class1);
	}
	
	public static synchronized List<DialectConstraintFilter> getCustomFilters() {
		List<DialectConstraintFilter> result = new ArrayList<DialectConstraintFilter>();
		for (Class<? extends DialectConstraintFilter> customFESFilter : customFilters) {
			try {
				result.add(customFESFilter.newInstance());
			} catch (InstantiationException e) {
				logger.warn(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return result;
	}

	public abstract boolean canHandle(Element filterXml);

	public abstract void initialize(Element filterXml);

}
