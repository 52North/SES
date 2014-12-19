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
package org.n52.ses.io.parser.aixm;


import org.n52.oxf.conversion.unit.UOMTools;

import aero.aixm.schema.x51.ValDistanceType;
import aero.aixm.schema.x51.ValDistanceVerticalType;

/**
 * XMLBeans specific UOM helper methods.
 */
public class XBeansUOMTools {

	public static double parseValDistance(ValDistanceType val, String targetUom) {
		if (val == null) return 0d;
		
		if (val.isSetUom()) {
			return UOMTools.convertToTargetUnit(val.getBigDecimalValue().doubleValue(), val.getUom().trim(), targetUom);
		} else {
			return val.getBigDecimalValue().doubleValue();
		}
	}

	public static double parseValDistance(ValDistanceVerticalType val,
			String targetUom) {
		if (val == null) return 0d;
		
		double number = Double.parseDouble(val.getStringValue());
		if (val.isSetUom()) {
			return UOMTools.convertToTargetUnit(number, val.getUom().trim(), targetUom);
		} else {
			return number;
		}
	}
	
}
