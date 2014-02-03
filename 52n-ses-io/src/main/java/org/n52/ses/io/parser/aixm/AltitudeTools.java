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
package org.n52.ses.io.parser.aixm;


import org.n52.oxf.conversion.gml32.geometry.AltitudeLimits;
import org.n52.oxf.conversion.gml32.geometry.AltitudeLimits.AltitudeReferences;
import org.n52.oxf.conversion.unit.UOMTools;

import aero.aixm.schema.x51.CodeVerticalReferenceType;
import aero.aixm.schema.x51.ValDistanceVerticalType;

/**
 * Helper class for creating altitude related objects
 * from AIXM features.
 */
public class AltitudeTools {

	/**
	 * @param lowerLimitElement the lowerLimit
	 * @param lowerLimitReferenceElement the lowerLimitReference
	 * @param upperLimitElement the upperLimit
	 * @param upperLimitReferenceElement the upperLimitReference
	 * @return the converted altitudes
	 */
	public static AltitudeLimits createAltitueLimits(ValDistanceVerticalType lowerLimitElement,
			CodeVerticalReferenceType lowerLimitReferenceElement, ValDistanceVerticalType upperLimitElement,
			CodeVerticalReferenceType upperLimitReferenceElement) {
		double lowerLimit;
		if (lowerLimitElement != null) {
			lowerLimit = XBeansUOMTools.parseValDistance(lowerLimitElement, UOMTools.METER_UOM);	
		} else {
			lowerLimit = 5000;
		}
		double upperLimit;
		if (upperLimitElement != null) {
			upperLimit = XBeansUOMTools.parseValDistance(upperLimitElement, UOMTools.METER_UOM);	
		} else {
			upperLimit = 6000;
		}
		
		AltitudeReferences lowerReference;
		if (lowerLimitReferenceElement != null) {
			lowerReference = parseReference(lowerLimitReferenceElement.getStringValue().trim());
		} else {
			lowerReference = AltitudeReferences.SFC;
		}
		
		AltitudeReferences upperReference;
		if (upperLimitReferenceElement != null) {
			upperReference = parseReference(upperLimitReferenceElement.getStringValue().trim());
		} else {
			upperReference = AltitudeReferences.SFC;
		}
		
		return new AltitudeLimits(lowerLimit, lowerReference, upperLimit, upperReference);
	}

	private static AltitudeReferences parseReference(String ref) {
		if (ref.equals(AltitudeReferences.MSL.toString())) {
			return AltitudeReferences.MSL;
		}
		else if (ref.equals(AltitudeReferences.FL.toString())) {
			return AltitudeReferences.FL;
		}
		else if (ref.equals(AltitudeReferences.STD.toString())) {
			return AltitudeReferences.STD;
		}
		else if (ref.equals(AltitudeReferences.W84.toString())) {
			return AltitudeReferences.W84;
		}
		else {
			return AltitudeReferences.SFC;
		}
	}

	
}
