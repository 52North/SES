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
package org.n52.ses.eml.v002.filter.temporal;

import javax.xml.namespace.QName;

import org.n52.ses.eml.v002.filter.IFilterElement;

import net.opengis.fes.x20.TemporalOpsType;

/**
 * Factory that builds the temporal filter objects.
 *
 */
public class TemporalFilterFactory {
	
	private static final String FES_NAMESPACE = "http://www.opengis.net/fes/2.0";
	
	private static final QName BEFORE_QNAME = new QName(FES_NAMESPACE, "Before");
	private static final QName AFTER_QNAME = new QName(FES_NAMESPACE, "After");
	private static final QName MEETS_QNAME = new QName(FES_NAMESPACE, "Meets");
	private static final QName MET_BY_QNAME = new QName(FES_NAMESPACE, "MetBy");
	private static final QName ANY_INTERACTS_QNAME = new QName(FES_NAMESPACE, "AnyInteracts");

	
	/**
	 * Builds the temporal filter objects
	 * 
	 * @param temporalOps FES temporal operator
	 * 
	 * @return object representing the temporal operator
	 */
	//TODO: property names not necessary (compare to ALogicalFilter.FACTORY)?
	public IFilterElement buildTemporalFilter(TemporalOpsType temporalOps) {
		QName tOpName = temporalOps.newCursor().getName();
		
		if (tOpName.equals(BEFORE_QNAME)) {
			return new BeforeFilter(temporalOps);
		}
		
		if (tOpName.equals(AFTER_QNAME)) {
			return new AfterFilter(temporalOps);
		}
		
		if (tOpName.equals(MEETS_QNAME)) {
			return new MeetsFilter(temporalOps);
		}
		
		if (tOpName.equals(MET_BY_QNAME)) {
			return new MetByFilter(temporalOps);
		}
		if (tOpName.equals(ANY_INTERACTS_QNAME)) {
			return new AnyInteractsFilter(temporalOps);
		}
		
		return null;
	}

}
