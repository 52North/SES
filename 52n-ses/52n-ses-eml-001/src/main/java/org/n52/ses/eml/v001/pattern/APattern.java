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

package org.n52.ses.eml.v001.pattern;

import java.util.HashSet;
import java.util.Vector;

import org.n52.ses.eml.v001.filterlogic.EMLParser;


/**
 * superclass of all patterns
 * 
 * @author Thomas Everding
 *
 */
public abstract class APattern{
	
	/**
	 * pattern id
	 */
	protected String patternID;
	
	/**
	 * description for this pattern
	 */
	protected String description = "";
	
	/**
	 * the collection of {@link SelFunction}s
	 */
	protected Vector<SelFunction> selectFunctions = new Vector<SelFunction>();
	
	/**
	 * the used property names
	 */
	protected HashSet<Object> propertyNames = new HashSet<Object>();

	/**
	 * @return the patternID
	 */
	public String getPatternID() {
		return this.patternID;
	}

	/**
	 * sets the patternID
	 * 
	 * @param patternID the patternID to set
	 */
	public void setPatternID(String patternID) {
		this.patternID = patternID;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * sets the pattern description
	 * 
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the select functions
	 */
	public Vector<SelFunction> getSelectFunctions() {
		return this.selectFunctions;
	}
	
	
	/**
	 * adds a select function
	 * 
	 * @param selectFunction the new select function
	 */
	public void addSelectFunction(SelFunction selectFunction) {
		this.selectFunctions.add(selectFunction);
	}
	
	
	/**
	 * creates an esper EPL statement
	 * 
	 * @return this pattern in esper EPL, one statement for every select clause
	 */
	public abstract Statement[] createEsperStatements();

	
	/**
	 * creates an esper EPL statement
	 * 
	 * @param parser the parser to build the statements
	 * 
	 * @return this pattern in esper EPL, one statement for every select clause
	 */
	public abstract Statement[] createEsperStatements(EMLParser parser);
	
	
	/**
	 * 
	 * @return all found property names of this pattern
	 */
	public HashSet<Object> getPropertyNames() {
		return this.propertyNames;
	}
	
}
