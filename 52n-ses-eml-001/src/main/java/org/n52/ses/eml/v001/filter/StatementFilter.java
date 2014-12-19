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

package org.n52.ses.eml.v001.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.n52.ses.api.common.CustomStatementEvent;
import org.n52.ses.eml.v001.Constants;
import org.n52.ses.eml.v001.filter.comparison.AComparisonFilter;
import org.n52.ses.eml.v001.filter.custom.CustomGuardFactory;
import org.n52.ses.eml.v001.filter.custom.CustomGuardFilter;
import org.n52.ses.eml.v001.filter.logical.ALogicFilter;
import org.n52.ses.eml.v001.filter.spatial.ASpatialFilter;
import org.n52.ses.eml.v001.filter.temporal.ATemporalFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.fes.x20.FilterType;


/**
 * Representation of a filter. (a complete guard)
 * 
 * @author Thomas Everding
 * 
 */
public class StatementFilter implements IFilterElement {

	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(StatementFilter.class);

	private static ArrayList<CustomGuardFactory> customGuardFactories;

	private IFilterElement child;

	/**
	 * the used property of this Filter
	 */
	protected String usedProperty = null;
	
	static {
		ServiceLoader<CustomGuardFactory> loader = ServiceLoader.load(CustomGuardFactory.class);
		
		customGuardFactories = new ArrayList<CustomGuardFactory>();
		
		for (CustomGuardFactory customGuardFactory : loader) {
			customGuardFactories.add(customGuardFactory);
		}
	}


	/**
	 * 
	 * Constructor
	 * 
	 * @param filter the OGC filter encoding complaint filter statement
	 * @param propertyNames all found property names of this filter or pattern
	 */
	public StatementFilter(FilterType filter, HashSet<Object> propertyNames) {
		this.initialize(filter, propertyNames);
	}

	/**
	 * initializes the filter
	 * 
	 * @param filter
	 *            Filter definition
	 */
	private void initialize(FilterType filter, HashSet<Object> propertyNames) {
		this.child = findCustomGuardFilter(filter, propertyNames);
		
		if (this.child != null) return;
		
		if (filter.isSetLogicOps()) {
			// parse logic operator
			this.child = ALogicFilter.FACTORY.buildLogicFilter(filter
					.getLogicOps(), propertyNames);
		} else if (filter.isSetComparisonOps()) {
			// parse comparison operator
			this.child = AComparisonFilter.FACTORY.buildComparisonFilter(filter
					.getComparisonOps(), propertyNames);
		} else if (filter.isSetSpatialOps()) {
			//parse spatial filter
			this.child = ASpatialFilter.FACTORY.buildSpatialFilter(filter.getSpatialOps());
		} else if (filter.isSetTemporalOps()) {
			//parse temporal filter
			this.child = ATemporalFilter.FACTORY.buildTemporalFilter(filter.getTemporalOps());
		} else {
			logger.warn("operator type not supported");
			return;
		}
	}

	private IFilterElement findCustomGuardFilter(FilterType filter,
			Set<Object> propertyNames) {
		for (CustomGuardFactory cgf : customGuardFactories) {
			if (cgf.supports(filter, propertyNames)) {
				return cgf.createInstance(filter, propertyNames);
			}
		}
		return null;
	}

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		StringBuilder sb = new StringBuilder();
		sb.append(" ");
		if (this.child instanceof CustomGuardFilter) {
			CustomGuardFilter custom = (CustomGuardFilter) this.child;
			sb.append(custom.getEPLClauseOperator());
		} else {
			sb.append(Constants.EPL_WHERE);
		}
		sb.append(" ");
		sb.append(this.child.createExpressionString(complexPatternGuard));
		return sb.toString();
	}
	
	@Override
	public void setUsedProperty(String nodeValue) {
		this.usedProperty = nodeValue;
	}

	@Override
	public List<CustomStatementEvent> getCustomStatementEvents() {
		return this.child == null ? null : this.child.getCustomStatementEvents();
	}

}
