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
package org.n52.ses.eml.v001.constants;


/**
 * Constants used in OGC filter encoding 2.0 functions.
 * Every FES2.0 function needs an entry here.
 * 
 * @author Thomas Everding
 *
 */
public class Filter20FunctionConstants {
	
	/**
	 * Name addition function
	 */
	public static final String ADD_FUNC_NAME = "add";
	
	/**
	 * Name of the first argument of the add function
	 */
	public static final String ADD_FUNC_ARG_1_NAME = "firstSummand";
	
	/**
	 * Name of the second argument of the add function
	 */
	public static final String ADD_FUNC_ARG_2_NAME = "secondSummand";
	
	/**
	 * Name subtraction function
	 */
	public static final String SUB_FUNC_NAME = "sub";
	
	/**
	 * Name multiplication function
	 */
	public static final String MUL_FUNC_NAME = "mul";
	
	/**
	 * Name division function
	 */
	public static final String DIV_FUNC_NAME = "div";

	/**
	 * Name of the first argument of the sub function
	 */
	public static final String	SUB_FUNC_ARG_1_NAME	= "minuend";
	
	/**
	 * Name of the second argument of the sub function
	 */
	public static final String	SUB_FUNC_ARG_2_NAME	= "subtrahend";

	/**
	 * Name of the first argument of the mul function
	 */
	public static final String	MUL_FUNC_ARG_1_NAME	= "firstFactor";
	
	/**
	 * Name of the second argument of the mul function
	 */
	public static final String	MUL_FUNC_ARG_2_NAME	= "secondFactor";

	/**
	 * Name of the first argument of the div function
	 */
	public static final String	DIV_FUNC_ARG_1_NAME	= "dividend";
	
	/**
	 * Name of the second argument of the div function
	 */
	public static final String	DIV_FUNC_ARG_2_NAME	= "divisor";

	/**
	 * Name of the distance to function
	 */
	public static final Object	DISTANCE_TO_NAME	= "distanceTo";

}
