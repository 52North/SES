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


package org.n52.ses.eml.v002.filter.logical;

import java.util.HashSet;

import org.n52.ses.eml.v002.filter.IFilterElement;
import org.n52.ses.eml.v002.filter.comparison.AComparisonFilter;
import org.n52.ses.eml.v002.filter.spatial.ASpatialFilter;
import org.n52.ses.eml.v002.filter.temporal.ATemporalFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.fes.x20.UnaryLogicOpType;


/**
 * Representation of not filters.
 * 
 * @author Thomas Everding
 *
 */
public class NotFilter extends ALogicFilter{
	
	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(NotFilter.class);
	
	private IFilterElement element;
	
	
	/**
	 * 
	 * Constructor
	 *
	 * @param unaryOp filter definition
	 * @param propertyNames names of the properties used in this filter / pattern
	 */
	public NotFilter(UnaryLogicOpType unaryOp, HashSet<Object > propertyNames) {
		if (unaryOp.isSetComparisonOps()) {
			//element is comparison operator
			this.element = AComparisonFilter.FACTORY.buildComparisonFilter(unaryOp.getComparisonOps(), propertyNames);
		}
		else if (unaryOp.isSetLogicOps()) {
			//element is logical operator
			this.element = ALogicFilter.FACTORY.buildLogicFilter(unaryOp.getLogicOps(), propertyNames);
		}
		else if (unaryOp.isSetSpatialOps()) {
			//element is spatial operator
			this.element = ASpatialFilter.FACTORY.buildSpatialFilter(unaryOp.getSpatialOps());
		}
		else if (unaryOp.isSetTemporalOps()) {
			//element is temporal operator
			this.element = ATemporalFilter.FACTORY.buildTemporalFilter(unaryOp.getTemporalOps());
		}
		else {
			//not supported
			logger.warn("the operator type is not supported");
			throw new RuntimeException("the operator type is not supported");
		}
	}


	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		String result = "( not ("
						+ this.element.createExpressionString(complexPatternGuard)
						+ "))";
		return result;
	}
}
