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
package org.n52.ses.eml.v002.constants;


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
