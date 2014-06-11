/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.ses.api;

import org.n52.ses.api.IFilterEngine;
import org.n52.ses.api.IUnitConverter;



/**
 * Interface that provides a method to
 * request an instance of a specific class. 
 *
 */
public interface IClassProvider {

	/**
	 * returns an instance of {@link IFilterEngine} depending
	 * on the parameter set in the properties file.
	 * 
	 * @param converter UnitConverter implementing {@link IUnitConverter}
	 * @param log global Logger of muse
	 * @return an instance of {@link IFilterEngine}
	 */
	IFilterEngine getFilterEngine(IUnitConverter converter);

}
