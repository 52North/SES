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

package org.n52.ses.eml.v001.filter.comparison;

import java.util.HashSet;

import org.n52.ses.eml.v001.filter.expression.AFilterExpression;
import org.n52.ses.eml.v001.filterlogic.esper.customFunctions.MethodNames;

import net.opengis.fes.x20.PropertyIsBetweenType;


/**
 * Filter to compare a value and a range.
 * 
 * @author Thomas Everding
 *
 */
public class BetweenFilter extends AComparisonFilter{
	
	private AFilterExpression lower;
	
	private AFilterExpression upper;
	
	private AFilterExpression test;
	
	
	/**
	 * 
	 * Constructor
	 *
	 * @param betweenOp filter definition
	 * @param propertyNames names of the properties used in this filter / pattern
	 */
	public BetweenFilter(PropertyIsBetweenType betweenOp, HashSet<Object > propertyNames) {
		//TODO parse expression
		
		this.test  = AFilterExpression.FACTORY.buildFilterExpression(betweenOp.getExpression(), propertyNames, this);
		this.lower = AFilterExpression.FACTORY.buildFilterExpression(betweenOp.getLowerBoundary().getExpression(), propertyNames, this);
		this.upper = AFilterExpression.FACTORY.buildFilterExpression(betweenOp.getUpperBoundary().getExpression(), propertyNames, this);
	}

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		String result = "";
		
		if (this.lower.getUsedProperty() != null || this.upper.getUsedProperty() != null || this.test.getUsedProperty() != null) {
			result += "(";
			
			boolean first = true;
			String usedProp;
			String usedEvent = "";
			String usedField = "";
			if (this.lower.getUsedProperty() != null) {
				usedProp = this.lower.getUsedProperty();
				if (usedProp.contains(".")) {
					usedEvent = usedProp.substring(0, usedProp.indexOf(".")+1);
					usedField = usedProp.substring(usedProp.indexOf(".")+1, usedProp.length());
				} else {
					usedField = usedProp;
				}
				
				result += MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
				first = false;
			}
			
			if (this.upper.getUsedProperty() != null) {
				
				if (!first) {
					result += "AND ";
				}
				
				usedProp = this.upper.getUsedProperty();
				if (usedProp.contains(".")) {
					usedEvent = usedProp.substring(0, usedProp.indexOf(".")+1);
					usedField = usedProp.substring(usedProp.indexOf(".")+1, usedProp.length());
				} else {
					usedField = usedProp;
				}
				
				result += MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
				first = false;
			}
			
			if (this.test.getUsedProperty() != null) {
				
				if (!first) {
					result += "AND ";
				}
				
				
				usedProp = this.test.getUsedProperty();
				if (usedProp.contains(".")) {
					usedEvent = usedProp.substring(0, usedProp.indexOf(".")+1);
					usedField = usedProp.substring(usedProp.indexOf("."), usedProp.length());
				} else {
					usedField = usedProp;
				}
				
				result += MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
				first = false;
			}
			
			result += ") AND ";
		}
		
		result += "("
						+ this.test.createExpressionString(complexPatternGuard)
						+ " between "
						+ this.lower.createExpressionString(complexPatternGuard)
						+ " and "
						+ this.upper.createExpressionString(complexPatternGuard)
						+ ")";
		return result;
	}
}
