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

import java.util.List;

import org.n52.ses.api.common.CustomStatementEvent;
import org.n52.ses.eml.v001.filter.IFilterElement;

/**
 * represents a single ogc:expression
 * 
 * @author Thomas Everding
 *
 */
public abstract class AFilterExpression implements IFilterElement{
	
	/**
	 * Factory to build {@link AFilterExpression}s.
	 */
	public static final FilterExpressionFactory FACTORY = new FilterExpressionFactory();
	
	/**
	 * the used property of this expression
	 */
	protected String usedProperty = null;

	/**
	 * 
	 * @return the name of the property that is used in this filter expression (if any)
	 */
	public String getUsedProperty() {
		return this.usedProperty;
	}

	@Override
	public void setUsedProperty(String usedProperty) {
		this.usedProperty = usedProperty;
	}


	@Override
	public List<CustomStatementEvent> getCustomStatementEvents() {
		return null;
	}

}
