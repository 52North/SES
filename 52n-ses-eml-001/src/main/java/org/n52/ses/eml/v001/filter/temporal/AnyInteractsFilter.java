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
package org.n52.ses.eml.v001.filter.temporal;

import net.opengis.fes.x20.BinaryTemporalOpType;
import net.opengis.fes.x20.TemporalOpsType;

import org.joda.time.Interval;
import org.n52.ses.eml.v001.filterlogic.esper.customFunctions.MethodNames;
import org.n52.ses.eml.v001.filterlogic.esper.customFunctions.TemporalMethods;

/**
 * Implementation of the FES2.0 AnyInteracts filter 
 * that checks for any interactions (intersections)
 * of a time primitive against a time interval.
 * 
 * @author Thomas Everding
 *
 */
public class AnyInteractsFilter extends ATemporalFilter {

	/**
	 * 
	 * Constructor
	 *
	 * @param temporalOp the FES temporal operator
	 */
	public AnyInteractsFilter(TemporalOpsType temporalOp) {
		super(temporalOp);
	}


	@Override
	public String createExpressionString(boolean complexPatternGuard) {

		//get reference interval
		BinaryTemporalOpType anyInteracts = (BinaryTemporalOpType) this.temporalOp;
		Interval intersectsInterval = this.parseGMLTimePeriodFromBinaryTemporalOp(anyInteracts);
		
		//build expression
		StringBuilder sb = new StringBuilder();
		
		//add property check
		sb.append("(");
		sb.append(MethodNames.PROPERTY_EXISTS_NAME);
		sb.append("(this, \"");
		sb.append(anyInteracts.getValueReference());
		sb.append("\") AND "); //property check close
		
		//add any interacts
		sb.append(MethodNames.ANY_INTERACTS_OPERATION);
		sb.append("(this, \"");
		
		//add test time reference
		sb.append(anyInteracts.getValueReference());
		
		//add reference interval
		sb.append("\", \"");
		sb.append(intersectsInterval.getStartMillis());
		sb.append(TemporalMethods.INTERVAL_SEPARATOR);
		sb.append(intersectsInterval.getEndMillis());
		sb.append("\")"); //any interacts close
		
		sb.append(")"); //all close
		
		return sb.toString();
	}


	@Override
	public void setUsedProperty(String nodeValue) {
		/*empty*/
	}

}
