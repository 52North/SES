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
package org.n52.ses.eml.v001.filter.expression;

import java.util.HashSet;

import org.apache.xmlbeans.XmlObject;
import org.n52.ses.eml.v001.filterlogic.esper.customFunctions.MethodNames;


/**
 * 
 * @author Thomas Everding
 *
 */
public class DistanceToExpression extends ABinaryFilterExpression{
	
	private boolean initialized = false;
	
	/**
	 * 
	 * Constructor for OGC filter encoding 2 mul functions
	 * 
	 * @param args arguments of the addition function.
	 * @param propertyNames name of the known event properties
	 */
	public DistanceToExpression(XmlObject[] args, HashSet<Object> propertyNames) {
		//check arguments
		if (args.length < 2) {
			throw new RuntimeException("illegal argument count for distance to function");
		}
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
			StringBuilder sb = new StringBuilder();
			sb.append(MethodNames.SPATIAL_METHODS_PREFIX);
			sb.append("distanceTo(");
			sb.append(this.first.createExpressionString(complexPatternGuard));
			sb.append(", ");
			sb.append(this.second.createExpressionString(complexPatternGuard));
			sb.append(")"); //distance to
			
			result = sb.toString();
		}
		return result;
	}

}
