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
package org.n52.ses.util.unitconversion;


import java.util.HashMap;
import java.util.Map;

import org.n52.oxf.conversion.unit.NumberWithUOM;
import org.n52.oxf.conversion.unit.ucum.UCUMTools;
import org.n52.oxf.conversion.unit.ucum.UCUMTools.UnitConversionFailedException;
import org.n52.ses.api.IUnitConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.unit.*;

/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESUnitConverter implements IUnitConverter {
	

	private Map<String, Unit> unitPhenomenonMap;
	private Logger logger = LoggerFactory.getLogger(SESUnitConverter.class);


	/**
	 * Instance for converting units using UCUM codes.
	 * @param log the global logger.
	 */
	public SESUnitConverter() {
		this.unitPhenomenonMap = new HashMap<String, Unit>();
	}

	/* (non-Javadoc)
	 * @see org.n52.ses.unitconversion.IUnitConverter#convert(java.lang.String, double)
	 */
	@Override
	public NumberWithUOM convert(String unitString, double value) {
		return UCUMTools.convert(unitString, value);
	}
	
	/* (non-Javadoc)
	 * @see org.n52.ses.unitconversion.IUnitConverter#convert(java.lang.String, java.lang.String, double)
	 */
	@Override
	public NumberWithUOM convert(String unitString, String phenomenon, double value) throws UnitConversionFailedException {
		
		
		Unit unit2 = this.unitPhenomenonMap.get(phenomenon);
		if (unit2 == null) {
			logger.warn("Phenomenon not registered. Should not happen, sensor should have" +
					" registered phenomenon. Using base units for conversion.");
			unit2 = UCUMTools.getBaseUnit(unitString);
		}
		
		
		return UCUMTools.convert(unitString, unit2.getUCUMExpression(), value); 
	}



	/* (non-Javadoc)
	 * @see org.n52.ses.unitconversion.IUnitConverter#registerNewUnit(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean registerNewUnit(String unitString, String phenomenon)
		throws UnitConversionFailedException {
		
		Unit unit = UCUMTools.getBaseUnit(unitString);
		if (this.unitPhenomenonMap.containsKey(phenomenon)) {
			if (!this.unitPhenomenonMap.get(phenomenon).isCompatible(unit)) {
				throw new UnitConversionFailedException("Phenomenon already registered, but registered" +
						" units are not compatible!");
			}
			//return false: phenomnen already registered with comparable unit
			return false;
		}
		this.unitPhenomenonMap.put(phenomenon, unit);
		//return true: new phenomenon registered
		return true;
	}

	/* (non-Javadoc)
	 * @see org.n52.ses.unitconversion.IUnitConverter#isCompatibleWithPhenomenon(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isCompatibleWithPhenomenon(String unitString, String phenomenon) {
		Unit unit = UCUMTools.getBaseUnit(unitString);
		Unit unit2 = this.unitPhenomenonMap.get(phenomenon);
		if (unit2 != null) {
			return unit2.isCompatible(unit);
		}
		//return true: no phenomenon registered - continue
		return true;
	}

	@Override
	public boolean isCompatible(String unitString1, String unitString2) {
		return UCUMTools.isCompatible(unitString1, unitString2);
	}



}
