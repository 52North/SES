/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * icense version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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
