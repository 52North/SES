/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 *
 */
public interface SensorMLConstants {

	/**
	 * namespace for SensorML 1.0.1
	 */
	public static final String NAMESPACE = "http://www.opengis.net/sensorML/1.0.1";
	
	/**
	 * OName of the SensorML root element
	 */
	public static final QName SENSORML = new QName(NAMESPACE,"SensorML");
	
	/**
	 * QName of the SensorML member element
	 */
	public static final QName MEMBER = new QName(NAMESPACE,"member");
	
	/**
	 * QName of the SensorML System element
	 */
	public static final QName SYSTEM = new QName(NAMESPACE,"System");
	
	/**
	 * QName of the SensorML identification element
	 */
	public static final QName IDENTIFICATION = new QName(NAMESPACE,"identification");
	
	/**
	 * QName of the SensorML Term element
	 */
	public static final QName TERM = new QName(NAMESPACE,"Term");
	
	/**
	 * QName of the SensorML value element
	 */
	public static final QName VALUE = new QName(NAMESPACE,"value");
	
	/**
	 * URN for unique sensor ID attributes
	 */
	public static final String SENSOR_ID_ATTRIBUTE = "urn:ogc:def:identifier:OGC:1.0:uniqueID";
	
	//TODO: workaround. should be done with RegEx
	/**
	 * different representations of the unique sensor ID URN
	 */
	public static final String[] SENSOR_ID_UNIQUEIDS_ARRAY = {"urn:ogc:def:identifier:OGC:1.0:uniqueID",
		"urn:ogc:def:identifier:OGC:1.0.1:uniqueID", "urn:ogc:def:identifier:OGC:uniqueID", "urn:ogc:def:identifier:OGC::uniqueID"};
	
	/**
	 * set including different representations of the unique sensor ID URN
	 */
	public static final Set<String> SENSOR_ID_UNIQUEIDS = 
		new HashSet<String>(Arrays.asList(SENSOR_ID_UNIQUEIDS_ARRAY));
}
