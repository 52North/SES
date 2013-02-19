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

import org.n52.ses.eml.v002.filterlogic.esper.EsperController;

/**
 * contains a statement string (for esper) and possibly a SelFunction
 * 
 * @author Thomas Everding
 *
 */
public class Statement {
	
	private String statement;
	
	private SelFunction selectFunction;

	private DataView view;
	

	/**
	 * @return the statement
	 */
	public String getStatement() {
		return this.statement;
	}

	/**
	 * @param statement the statement to set
	 */
	public void setStatement(String statement) {
		this.statement = statement;
	}

	/**
	 * @return the selectFunction
	 */
	public SelFunction getSelectFunction() {
		return this.selectFunction;
	}

	/**
	 * @param selectFunction the selectFunction to set
	 */
	public void setSelectFunction(SelFunction selectFunction) {
		this.selectFunction = selectFunction;
	}

	/**
	 * Needed for last/first event workaround (see {@link EsperController}s
	 * buildListener private method.
	 * @param view the View of the statements pattern
	 */
	public void setView(DataView view) {
		this.view = view;
	}

	/**
	 * Needed for last/first event workaround (see {@link EsperController}s
	 * buildListener private method.
	 * @return the View of the statements pattern
	 */
	public DataView getView() {
		return this.view;
	}

	@Override
	public String toString() {
		return this.statement;
	}
	
	
}
