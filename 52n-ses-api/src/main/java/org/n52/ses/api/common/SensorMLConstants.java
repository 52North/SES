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
