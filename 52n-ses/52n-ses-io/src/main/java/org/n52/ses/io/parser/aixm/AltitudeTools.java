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
