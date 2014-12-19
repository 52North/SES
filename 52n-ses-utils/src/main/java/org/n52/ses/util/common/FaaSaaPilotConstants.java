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
package org.n52.ses.util.common;



/**
 * Constants for the {@link FaaSaaPilotParser}
 * 
 * @author Thomas Everding
 *
 */
public class FaaSaaPilotConstants {

	/**
	 * Name of the airspace element in FAA SAA Events
	 */
	public static final String AIRSPACE_NAME = "Airspace";
	
	/**
	 * Name of the identifier element in FAA SAA Events
	 */
	public static final String IDENTIFIER_NAME = "identifier";
	
	/**
	 * Name of the code space attribute of identifiers in FAA SAA Events
	 */
	public static final String CODE_SPACE_NAME = "codeSpace";
	
	/**
	 * Name for the value of elements with attributes in FAA SAA Events
	 */
	public static final String VALUE_NAME = "value";
	
	/**
	 * Name of the bounded by element in FAA SAA Events
	 */
	public static final String BOUNDED_BY_NAME = "boundedBy";
	
	/**
	 * Name of the envelope element in FAA SAA Events
	 */
	public static final String ENVELOPE_NAME = "Envelope";
	
	/**
	 * Name of the time slice element in FAA SAA Events
	 */
	public static final String TIMESLICE_NAME = "timeSlice";
	
	/**
	 * Name of the airspace time slice element in FAA SAA Events
	 */
	public static final String AIRSPACE_TS_NAME = "AirspaceTimeSlice";
	
	/**
	 * Name of the valid time element in FAA SAA Events
	 */
	public static final String VALID_TIME_NAME = "validTime";
	
	/**
	 * Name of the time period element in FAA SAA Events
	 */
	public static final String TIME_PERIOD_NAME = "TimePeriod";
	
	/**
	 * Name of the activation element in FAA SAA Events
	 */
	public static final String ACTIVATION_NAME = "activation";
	
	/**
	 * Name of the airspace activation element in FAA SAA Events
	 */
	public static final String AIRSPACE_ACTIVATION_NAME = "AirspaceActivation";
	
	/**
	 * Name of the status element in FAA SAA Events
	 */
	public static final String STATUS_NAME = "status";
	
	/**
	 * Name of the levels element in FAA SAA Events
	 */
	public static final String LEVELS_NAME = "levels";
	
	/**
	 * Name of the airspace layer element in FAA SAA Events
	 */
	public static final String AIRSPACE_LAYER_NAME = "AirspaceLayer";
	
	/**
	 * Name of the upper limit element in FAA SAA Events
	 */
	public static final String UPPER_LIMIT_NAME = "upperLimit";
	
	/**
	 * Name of the lower limit element in FAA SAA Events
	 */
	public static final String LOWER_LIMIT_NAME = "lowerLimit";
	
	/**
	 * Name of the extension element in FAA SAA Events
	 */
	public static final String EXTENSION_NAME = "extension";
	
	/**
	 * Name of the airspace activation extension element in FAA SAA Events
	 */
	public static final String AIRSPACE_ACTIVATION_EXTENSION_NAME = "AirspaceActiovationExtension";
	
	/**
	 * Name of the reservation phase element in FAA SAA Events
	 */
	public static final String RESERVATION_PHASE_NAME = "reservationPhase";
}
