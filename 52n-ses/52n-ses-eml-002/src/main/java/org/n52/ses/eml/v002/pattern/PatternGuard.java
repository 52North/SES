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

package org.n52.ses.eml.v002.pattern;

import java.util.HashSet;

import net.opengis.fes.x20.FilterType;

import org.n52.ses.eml.v002.Constants;
import org.n52.ses.eml.v002.filter.StatementFilter;


/**
 * representation of a guard
 * 
 * @author Thomas Everding
 *
 */
public class PatternGuard {
	
	private StatementFilter filter;
	
	private String statement = "";
	
//	private long maxListeningDuration = -1;

	/**
	 * @param filter the filter to set
	 * @param propertyNames all found property names of this pattern
	 */
	public void setFilter(FilterType filter, HashSet<Object> propertyNames) {
		this.filter = new StatementFilter(filter, propertyNames);
	}
	
	
	/**
	 * creates the esper statement for this guard
	 * @param complexPatternGuard if <code>true</code> the property names are used with the event names, else only the
	 * property names are used
	 * 
	 * @return the guard as esper where clause
	 */
	public String createStatement(boolean complexPatternGuard) {
		if (!this.statement.equals("")) {
			//statement already created
			return this.statement;
		}
		
//		if (complexPatternGuard) {
//			//create statement for complex patterns
//			this.statement += Constants.EPL_WHERE 
//							  + " "
//							  + this.filter.createExpressionString(complexPatternGuard);
//			
//			return this.statement;
//		}
		
		//create statement for simple patterns
		this.statement = Constants.EPL_WHERE
						 + " ";
		
//		if (propertyName != null) {
//			String usedEvent = "";
//			String usedField = "";
//			if (propertyName.contains(".")) {
//				usedEvent = propertyName.substring(0, propertyName.indexOf(".")+1);
//				usedField = propertyName.substring(propertyName.indexOf(".")+1, propertyName.length());
//			} else {
//				usedField = propertyName;
//			}
//			
//			this.statement += "(" + MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\")) AND "; 
//		
//		}
		this.statement += this.filter.createExpressionString(complexPatternGuard);
		
		return this.statement;
	}


//	/**
//	 * sets the max Listening Duration
//	 * @param maxListeningDuration maximum duration for listening
//	 */
//	public void setMaxListeningDuration(long maxListeningDuration) {
//		this.maxListeningDuration = maxListeningDuration;
//	}
	
}
