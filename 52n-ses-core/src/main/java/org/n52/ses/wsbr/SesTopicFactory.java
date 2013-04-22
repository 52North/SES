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
package org.n52.ses.wsbr;

import javax.xml.namespace.QName;

import org.apache.muse.ws.notification.NotificationProducer;
import org.apache.muse.ws.resource.basefaults.BaseFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 *
 * Handles available topics for notification producers
 */
public class SesTopicFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(SesTopicFactory.class);
	
	/**
	 * namespace for the SES topics
	 */
	public static final String NAMESPACE = "http://www.opengis.net/ses/0.0";
	
	
	/** 
	 * static topics for the SES 
	 */
	public static final QName[] STATIC_TOPICS = new QName[]{
	    new QName(NAMESPACE, "Measurements", "sestopic"), 
	    new QName(NAMESPACE, "AIXMData", "sestopic"), 
	    new QName(NAMESPACE, "Cap", "sestopic"),
	    new QName(NAMESPACE, "SensorManagement", "sestopic"),
	    new QName(NAMESPACE, "ExpirationInformation", "sestopic")
	};
	
	
	/**
	 * adds all default topics to the supplied producer
	 * @param producer the supplied producer
	 */
	public static void addDefaultTopics(NotificationProducer producer) {
		for (QName topic : STATIC_TOPICS) {
			try {
				producer.addTopic(topic);
			} catch (BaseFault e) {
				logger.warn("error adding topic " + topic.toString(), e);
			}
		}
	}
}
