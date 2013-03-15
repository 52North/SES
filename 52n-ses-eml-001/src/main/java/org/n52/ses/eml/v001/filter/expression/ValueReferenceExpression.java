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

package org.n52.ses.eml.v001.filter.expression;

import java.util.HashSet;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;




/**
 * Represents an expression for a property name
 * 
 * @author Thomas Everding
 * 
 */
public class ValueReferenceExpression extends AFilterExpression {
	
	private String valueReference;
	
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param expressionType name of a property
	 * @param propertyNames hash map with the known property names
	 */
	public ValueReferenceExpression(XmlObject expressionType, HashSet<Object> propertyNames) {
		Element elem = (Element) expressionType.getDomNode();
		String name = XmlUtils.toString(elem.getFirstChild()).trim();
		//replaceAll(":", "__")
		this.valueReference = name.replaceAll("/", ".");
		
		if (!propertyNames.contains(this.valueReference)) {
			propertyNames.add(this.valueReference);
		}
	}
	

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		if (complexPatternGuard) {
//			StringBuilder log = new StringBuilder();
//			log.append("ValueReference in complex pattern guard found");
//			log.append("\n\t value reference: " + this.valueReference);
//			logger.info(log.toString());
			
			String result = this.valueReference;//.replaceAll("\\.", ":");
			return result;
		}
		
		//remove event name part
		int i;
		if (((i = this.valueReference.indexOf(".")) > 0)) {
			return this.valueReference.substring(i + 1);
		}
		
		return this.valueReference;
	}
	
}
