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

package org.n52.ses.eml.v001.filter.logical;

import java.util.HashSet;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import net.opengis.fes.x20.BinaryLogicOpType;
import net.opengis.fes.x20.LogicOpsType;
import net.opengis.fes.x20.UnaryLogicOpType;


/**
 * Builds logic filters.
 * 
 * @author Thomas Everding
 *
 */
public class LogicFilterFactory {
	
	/*
	 * Logger instance for this class
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(LogicFilterFactory.class);
	
	private static final QName AND_QNAME = new QName("http://www.opengis.net/fes/2.0", "And");
	
	private static final QName OR_QNAME = new QName("http://www.opengis.net/fes/2.0", "Or");
	
	private static final QName NOT_QNAME = new QName("http://www.opengis.net/fes/2.0", "Not");
	
	/**
	 * Builds a new logic filter
	 * 
	 * @param logicOp definition of the filter
	 * @param propertyNames names of the properties used in this filter / pattern
	 * 
	 * @return the new {@link ALogicFilter}
	 */
	public ALogicFilter buildLogicFilter(LogicOpsType logicOp, HashSet<Object > propertyNames) {
		//TODO
		QName loQName = logicOp.newCursor().getName();
		
		/*
		 * non binary operators 
		 */
		
		//check Not
		if (NOT_QNAME.equals(loQName)) {
			//create new NotFilter
			UnaryLogicOpType unaryOp = (UnaryLogicOpType) logicOp;
			return new NotFilter(unaryOp, propertyNames);
		}
		
		/*
		 * binary operators
		 */
		BinaryLogicOpType binaryOp = (BinaryLogicOpType) logicOp;
		
		//check And
		if (AND_QNAME.equals(loQName)) {
			//create new AndFilter
			return new AndFilter(binaryOp, propertyNames);
		}
		
		//check Or
		else if (OR_QNAME.equals(loQName)) {
			//create new OrFilter
			return new OrFilter(binaryOp, propertyNames);
		}
		
		LogicFilterFactory.logger.warn("unable to build filter expression for '" + loQName.toString() + "'");
		return null;
	}
	
}
