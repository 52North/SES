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

import net.opengis.fes.x20.TemporalOpsType;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.Interval;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.FESParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Temporal filter that checks for the temporal 'after' condition.
 *
 */
public class AfterFilter extends ATemporalFilter {

	private static final Logger logger = LoggerFactory
			.getLogger(AfterFilter.class);
	
	/**
	 * 
	 * Constructor
	 *
	 * @param temporalOps FES temporal operator
	 */
	public AfterFilter(TemporalOpsType temporalOps) {
		super(temporalOps);
	}

	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		StringBuilder sb = new StringBuilder();
		
		XmlObject[] valRef = this.temporalOp.selectChildren(VALUE_REFERENCE_QNAME);
		
		Interval time = null;
		if (valRef != null) {
			try {
				time = getTimeFromValueReference(valRef[0]);
			} catch (FESParseException e) {
				//TODO log exc and throw
				logger.warn(e.getMessage(), e);
			}
		}
		
		if (time == null) {
			//error while parsing time
			return "";
		}
		
		sb.append(MapEvent.START_KEY +" > "+ time.getEndMillis());
		return sb.toString();
	}


	@Override
	public void setUsedProperty(String nodeValue) {
		/*empty*/
	}

}
