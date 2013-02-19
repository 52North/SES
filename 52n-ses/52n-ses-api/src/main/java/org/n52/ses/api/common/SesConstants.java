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
