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


import net.opengis.fes.x20.BinaryLogicOpType;


/**
 * Representation of or filters.
 * 
 * @author Thomas Everding
 *
 */
public class OrFilter extends ABinaryLogicFilter{
	
	/**
	 * 
	 * Constructor
	 *
	 * @param binaryOp filter definition
	 * @param propertyNames names of the properties used in this filter / pattern
	 */
	public OrFilter(BinaryLogicOpType binaryOp, HashSet<Object > propertyNames) {
		this.initialize(binaryOp, propertyNames);
	}

	
	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		StringBuilder result = new StringBuilder("(");

		for (IFilterElement	elem : this.elements) {
			result.append("(");
			result.append(elem.createExpressionString(complexPatternGuard));
			result.append(")");
			result.append(" or ");
		}
		
		//remove last 'or'
		result.delete(result.length() - 4, result.length());
		result.append(")");
		
		return result.toString();
	}

}
