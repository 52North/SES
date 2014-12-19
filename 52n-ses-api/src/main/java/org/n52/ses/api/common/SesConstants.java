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
package org.n52.ses.api.common;
import javax.xml.namespace.QName;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 * 
 * Constants from the SES namespace
 *
 */
public interface SesConstants {

	/**
	 * SES namespece
	 */
	public final static String NAMESPACE = "http://www.opengis.net/ses/0.0";
	
	/**
	 * QNAme of the destroy registration response
	 */
	public final static QName DESTROY_REGISTRATION_RESPONSE = new QName(NAMESPACE, "DestroyRegistrationResponse");
	
	/**
	 * QName of the renew registration response
	 */
	public final static QName RENEW_REGISTRATION_RESPONSE = new QName(NAMESPACE,"RenewRegistrationResponse");
	
	/**
	 * URI of the service capabilities
	 * 
	 * TODO: check if this works
	 */
	public final static String CAPABILITES_URI = NAMESPACE + "/GetCapabilities";
	
	/**
	 * URI of the sensor descriptions
	 * 
	 * TODO: check if this works
	 */
	public final static String DESCRIBE_SENSOR_URI = NAMESPACE + "/DescribeSensor";
	
	/**
	 * Identifier of the filter level 2 dialect (filter encoding)
	 * 
	 * TODO: should be replaced by FES namespace and not be fixed in the code
	 * TODO: replace filter levels by a set of different available dialects
	 */
	public static final String SES_FILTER_LEVEL_2_DIALECT = "http://www.opengis.net/ses/filter/level2";
	
	/**
	 * Identifier of the filter level 3 dialect (EML)
	 * 
	 * TODO: see level 2
	 */
	public static final String SES_FILTER_LEVEL_3_DIALECT = "http://www.opengis.net/ses/filter/level3";
	
	/**
	 * Code for unspecified exceptions
	 */
	public static final String EXCEPTION_CODE_UNSPECIFIED = "Unspecified";
	
	/**
	 * Code for subscription failed exceptions
	 */
	public static final String EXCEPTION_CODE_SUBSCRIBE_FAILED = "SubscribeFailure";

	/*
	 * Filter dialect for pure EPL subscriptions
	 */
	public static final String EPL_PURE_DIALECT = "http://esper.codehaus.org/epl";
	
}
