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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v001;

/**
 * contains constants for the EML plugin
 * 
 * @author Thomas Everding
 *
 */
public class Constants {
	
	/**
	 * SelectEvent function
	 */
	public static final String FUNC_SELECT_EVENT_NAME = "SelectEvent";
	
	/**
	 * SelectProperty function
	 */
	public static final String FUNC_SELECT_PROPERTY_NAME = "SelectProperty";
	
	/**
	 * SelectSum function
	 */
	public static final String FUNC_SELECT_SUM_NAME = "SelectSum";
	
	/**
	 * SelectAvg function
	 */
	public static final String FUNC_SELECT_AVG_NAME = "SelectAvg";
	
	/**
	 * SelectMax function
	 */
	public static final String FUNC_SELECT_MAX_NAME = "SelectMax";
	
	/**
	 * SelectMin function
	 */
	public static final String FUNC_SELECT_MIN_NAME = "SelectMin";
	
	/**
	 * SelectCount function
	 */
	public static final String FUNC_SELECT_COUNT_NAME = "SelectCount";
	
	/**
	 * NotifyOnSelect function
	 */
	public static final String FUNC_NOTIFY_ON_SELECT_NAME = "NotifyOnSelect";
	
	/**
	 * SelectStdDev function (user defined)
	 */
	public static final String FUNC_SELECT_STDDEV_NAME = "SelectStdDev";
	
	/**
	 * GetAdditiveInverseValue function (user defined)
	 */
	public static final String FUNC_GET_ADD_INV_VALUE_NAME = "GetAdditiveInverseValue";
	
	/**
	 * SelectFirst function (user defined)
	 */
	public static final String FUNC_SELECT_FIRST = "SelectFirst";
	
	/**
	 * select function parameter name for attribute eventName
	 */
	public static final String SELECT_PARAM_EVENT_NAME = "eventName";
	
	/**
	 * select function parameter name for attribute propertyName
	 */
	public static final String SELECT_PARAM_PROPERTY_NAME = "propertyName";
	
	/**
	 * select function parameter name for attribute Message
	 */
	public static final String SELECT_PARAM_MESSAGE_NAME = "message";
	
	/**
	 * length view name
	 */
	public static final String VIEW_LENGTH_NAME = "lengthView";
	
	/**
	 * time view name
	 */
	public static final String VIEW_TIME_NAME = "timeView";
	
	/**
	 * time length view name
	 */
	public static final String VIEW_TIME_LENGTH_NAME = "timeLengthView";
	
	/**
	 * all view name
	 */
	public static final String VIEW_ALL_NAME = "allView";
	
	/**
	 * view parameter for event count
	 */
	public static final String VIEW_PARAM_EVENT_COUNT_NAME = "EventCount";
	
	/**
	 * view parameter for duration
	 * (stored as long)
	 */
	public static final String VIEW_PARAM_DURATION_NAME = "Duration";
	
	/**
	 * Time view with additional parameters (user defined view)
	 */
	public static final String VIEW_TIME_WITH_PARAMETER = "TimeBatchWithParameter";
	
	/**
	 * SelectLast view (user defined)
	 */
	public static final String VIEW_SELECT_LAST = "SelectLast";
	
	/**
	 * view parameter for USD view SelectLast to differ between length and time view
	 */
	public static final String VIEW_PARAM_USD_TYPE = "type";
	
	/**
	 * allowed value for usd view SelectLast.parameter::type
	 */
	public static final String VIEW_PARAM_USD_TYPE_TIME = "time";
	
	/**
	 * allowed value for usd view SelectLast.parameter::type
	 */
	public static final String VIEW_PARAM_USD_TYPE_LENGTH = "length";
	
	/**
	 * parameter name for usd view SelectLast - boolean scale
	 */
	public static final String VIEW_PARAM_USD_IS_BATCH = "isBatch";
		
	/**
	 * view parameter to force updates (boolean) in case of empty batch as well
	 */
	public static final String VIEW_PARAM_USD_FORCE_UPDATES = "isForceUpdates";
	
	/**
	 * view parameter to start eager with pattern evaluation
	 */
	public static final String VIEW_PARAM_USD_START_EAGER = "isStartEager";
	
	/**
	 * cause operator
	 */
	public static final String OPERATOR_CAUSE_NAME = "cause";
	
	/**
	 * parallel operator
	 */
	public static final String OPERATOR_PARALLEL_NAME = "parallel";
	
	/**
	 * before operator
	 */
	public static final String OPERATOR_BEFORE_NAME = "before";
	
	/**
	 * and operator
	 */
	public static final String OPERATOR_AND_NAME = "and";
	
	/**
	 * and_not operator
	 */
	public static final String OPERATOR_AND_NOT_NAME = "and-not";
	
	/**
	 * or operator
	 */
	public static final String OPERATOR_OR_NAME = "or";
	
	/**
	 * esper EPL "where" - keyword
	 */
	public static final String EPL_WHERE = "where";
	
	/**
	 * esper EPL "having" - keyword
	 */
	public static final String EPL_HAVING = "having";
	
	/**
	 * esper EPL "select" - keyword
	 */
	public static final String EPL_SELECT = "select";
	
	/**
	 * esper EPL "from" - keyword
	 */
	public static final String EPL_FROM = "from";
	
	/**
	 * esper EPL "pattern" - keyword
	 */
	public static final String EPL_PATTERN = "pattern";
	
	/**
	 * esper EPL "insert" - keyword
	 */
	public static final String EPL_INSERT = "insert";
	
	/**
	 * esper EPL "into" - keyword
	 */
	public static final String EPL_INTO = "into";
	
	/**
	 * esper EPL part for the "at timer" 
	 */
	public static final String TIMER_AT = "timer:at";
	
	/**
	 * esper EPL part for the "interval timer"
	 */
	public static final String TIMER_INTERVAL = "timer:interval";
	
//	/**
//	 * event name prefix used to generate names for 
//	 * internally used timer events
//	 */
//	public static final String TIMER_INTERNAL_EVENT_PREFIX = "_internal_";
	
	/**
	 * value of events generated by timer patterns with SelectEvent
	 */
	public static final String TIMER_EVENT_VALUE = "TimerEvent";
	
	/**
	 * event name suffix used to generate names for
	 * result events of the counting statement of
	 * repetitive patterns.
	 */
	public static final String REPETIVITE_COUNT_EVENT_SUFFIX = "_count";
	
	/**
	 * namespace for custom comparison guards
	 */
	public static final String GUARD_COMPARISON_NAMESPACE = "comparison";
	
	/**
	 * namespace for custom logic guards
	 */
	public static final String GUARD_LOGIC_NAMESPACE = "logic";

	/**
	 * function name of the select difference user defined select function
	 */
	public static final String	FUNC_GET_DIFFERENCE	= "SelectDifference";

	/**
	 * name of the parameter to the first reference 
	 */
	public static final String	SELECT_PARAM_1_NAME	= "firstReference";
	
	/**
	 * name of the parameter to the second reference 
	 */
	public static final String	SELECT_PARAM_2_NAME	= "secondReference";	
}
