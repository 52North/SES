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

package org.n52.ses.eml.v002.filterlogic.esper.customFunctions;

/**
 * contains the names of the custom functions (methods)
 * 
 * @author Thomas Everding
 *
 */
public class MethodNames {
	
	/**
	 * name for the method to test if an event is the causal ancestor of another event
	 */
	public static final String IS_CAUSAL_ANCESTOR_NAME = "CausalityMethods.isCausalAncestorOf";
	
	/**
	 * name for the method to test if an event is not the causal ancestor of another event
	 */
	public static final String IS_NOT_CAUSAL_ANCESTOR_NAME = "CausalityMethods.isNotCausalAncestorOf";
	
	/**
	 * name for the method to test if a property exists in a received event
	 */
	public static final String PROPERTY_EXISTS_NAME = "PropertyMethods.propertyExists";
	
	/**
	 * operation name to be used in esper statements for the any interacts filter
	 */
	public static final String ANY_INTERACTS_OPERATION = "TemporalMethods.anyInteracts";
	
	/**
	 * prefix for the spatial methods
	 */
	public static final String SPATIAL_METHODS_PREFIX = "SpatialMethods.";
}
