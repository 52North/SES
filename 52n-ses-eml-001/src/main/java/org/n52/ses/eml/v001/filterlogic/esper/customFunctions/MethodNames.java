/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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

package org.n52.ses.eml.v001.filterlogic.esper.customFunctions;

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
