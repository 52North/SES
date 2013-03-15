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

package org.n52.ses.eml.v002.filter.expression;

import java.util.HashSet;

import org.apache.xmlbeans.XmlObject;



/**
 * Represents an expression for a multiplication 
 * 
 * @author Thomas Everding
 *
 */
public class MulExpression extends ABinaryFilterExpression{
	
	private boolean initialized = false;
	
//	/**
//	 * 
//	 * Constructor for OGC filter encoding 1 mul expressions
//	 *
//	 * @param binaryOp the expression definition
//	 * @param propertyNames name of the known event properties
//	 */
//	public MulExpression(BinaryOperatorType binaryOp, HashSet<Object > propertyNames) {
//		this.initialize(binaryOp, propertyNames);
//		this.initialized = true;
//	}
	
	
	/**
	 * 
	 * Constructor for OGC filter encoding 2 mul functions
	 * 
	 * @param args arguments of the addition function.
	 * @param propertyNames name of the known event properties
	 */
	public MulExpression(XmlObject[] args, HashSet<Object> propertyNames) {
		if (args.length < 2) {
			throw new RuntimeException("illegal argument count for mul function");
		}
		
		//set arguments
		XmlObject firstArg = args[0];
		XmlObject secondArg = args[1];
		
		//initialize
		if (firstArg != null && secondArg != null) {
			this.initialize(firstArg, secondArg, propertyNames);
			this.initialized = true;
		}
	}
	

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		String result = "";
		
		if (this.initialized) {
			result = "("
					+ this.first.createExpressionString(complexPatternGuard)
					+ " * "
					+ this.second.createExpressionString(complexPatternGuard)
					+ ")";
		}
		return result;
	}
	
}
