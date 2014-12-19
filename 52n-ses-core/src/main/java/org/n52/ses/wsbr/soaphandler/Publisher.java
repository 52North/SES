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
package org.n52.ses.wsbr.soaphandler;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.muse.util.xml.XmlSerializable;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.faults.InvalidTopicExpressionFault;
import org.apache.muse.ws.resource.basefaults.BaseFault;
import org.apache.muse.ws.resource.ext.faults.InvalidMessageFormatFault;
import org.n52.ses.api.common.SensorMLConstants;
import org.n52.ses.api.common.WsbrConstants;
import org.n52.ses.wsbr.PublisherEndpoint;
import org.n52.ses.wsbr.RegisterPublisher;
import org.n52.ses.wsrf.WsrfRlConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Publisher object for an endpoint
 *
 */
public class Publisher implements XmlSerializable {

	private EndpointReference publicationEndpoint;
	private Date terminationTime;
	private List<QName> topics;
	private boolean demand;
	private Element sensorML;

	/**
	 * Creates a new Publisher which holds the reference to the new
	 * {@link PublisherEndpoint} instance.
	 * 
	 * @param xml endpoint reference encoded in XML
	 * @throws BaseFault if the XML cannot be parsed
	 */
	public Publisher(Element xml) throws BaseFault {
		Element eprXML = XmlUtils.getElement(xml, WsbrConstants.TOPIC);

		if (eprXML == null)
			throw new NullPointerException("NullTopicElement");

		/* SensorML */
		this.sensorML = XmlUtils.getElement(xml,SensorMLConstants.SENSORML);
		
		/* lifetime */
		Element timeXML = XmlUtils.getElement(xml, WsrfRlConstants.REQUESTED_LIFETIME_DURATION);
		
		/* topics */
		setTopic(xml);

		/* demand */
		Element demandElement = XmlUtils.getElement(xml, WsbrConstants.DEMAND);
		//TODO: fix? what are possible values?
		this.demand = demandElement == null ? false : true;
		
		try {
			if (timeXML != null)
				this.terminationTime = XmlUtils.getDate(timeXML);
		} catch (ParseException error) {
			throw new InvalidMessageFormatFault("InvalidTerminationTime",error);
		}
	}

	/**
	 * For direct instantiation of new Publisher serializer instances.
	 * @param publisherReference endpoint reference
	 * @param topic the topic of this publisher
	 * @param onDemand if on demand publishing is enabled
	 * @param initialTerminationTime the time when this publisher is terminated
	 * @throws BaseFault if an error occurred on creation
	 */
	public Publisher(EndpointReference publisherReference, Element[] topic, boolean onDemand, Date initialTerminationTime) throws BaseFault {
		this.publicationEndpoint = publisherReference;
		setTopic(topic);
		this.demand = onDemand;
		this.terminationTime = initialTerminationTime;
	}

	/**
	 * @return an Object array of arguments suitable for 
	 * invoking {@link RegisterPublisher#registerPublisher(EndpointReference, QName[], boolean, Date, Element)}
	 * or an empty array in case of errors.
	 */
	public Object[] toArray() {
		/* convert OName list to QName array */
		QName[] topicarray = new QName[this.topics.size()];
		topicarray = this.topics.toArray(topicarray);
		if (this.sensorML != null) {
			return new Object[] { null, topicarray, this.demand, getTerminationTime(), this.sensorML };
		}
		return new Object[] {};
	}
	
	/**
	 * sets the topics
	 * @param rootelement the root topic xml element
	 * @throws InvalidTopicExpressionFault 
	 */
	private void setTopic(Element rootelement) throws InvalidTopicExpressionFault {
		Element[] alltopics = XmlUtils.getAllElements(rootelement,WsbrConstants.NAMESPACE_URI, "Topic"); 
		setTopic(alltopics);
		
	}

	/**
	 * sets the topic
	 * @param newTopics an array of topic xml elements
	 * @throws InvalidTopicExpressionFault 
	 */
	private void setTopic(Element[] alltopics) throws InvalidTopicExpressionFault {
		this.topics = new ArrayList<QName>(alltopics.length);
		for(Element t: alltopics) {
			if(t.getAttribute("Dialect").equals("http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple")) {
				this.topics.add(XmlUtils.getQName(t));
			} else {
				throw new InvalidTopicExpressionFault(t.getAttribute("Dialect") + " is not a supported dialect. Only http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple is allowed");
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.muse.util.xml.XmlSerializable#toXML()
	 */
	@Override
	public Element toXML() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.muse.util.xml.XmlSerializable#toXML(org.w3c.dom.Document)
	 */
	@Override
	public Element toXML(Document factory) {
		return null;
	}

	/**
	 * 
	 * @return the termination time of this publisher
	 */
	public Date getTerminationTime() {
		return this.terminationTime;
	}

	
	/**
	 * 
	 * @return the endpoint reference of this publisher
	 */
	public EndpointReference getPublicationEndpoint() {
		return this.publicationEndpoint;
	}

}
