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

import java.util.HashSet;

import org.n52.ses.eml.v001.filter.comparison.AComparisonFilter;
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

	private IFilterElement child;

	/**
	 * the used property of this Filter
	 */
	protected String usedProperty = null;


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

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		return this.child.createExpressionString(complexPatternGuard);
	}
	
	@Override
	public void setUsedProperty(String nodeValue) {
		this.usedProperty = nodeValue;
	}

}
