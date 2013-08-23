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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v001.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

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

}
