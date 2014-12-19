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
package org.n52.ses.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.muse.ws.notification.NotificationMessage;
import org.n52.ses.api.event.MapEvent;
import org.w3c.dom.Element;


/**
 * An abstract class representing a parser.
 * Every parsers {@link #accept(NotificationMessage)} method is called whenever a new
 * NotificationMessage arrives.
 * If true is returned the parsers {@link #parse(NotificationMessage)} method is called
 * by the {@link IFilterEngine} implementation.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public abstract class AbstractParser {
	
	protected IUnitConverter unitConverter;

	/**
	 * Checks if the Parser can handle this xml data by checking
	 * {@link NotificationMessage#getMessageContent(javax.xml.namespace.QName)}.
	 * @param message the new message
	 * @return true if can handle, false else
	 */
	public abstract boolean accept(NotificationMessage message);
	
	/**
	 * If {@link #accept(NotificationMessage)} returned true, this
	 * method is called.
	 * @param message the message to parse
	 * @return a {@link List} of {@link MapEvent}s.
	 * @throws Exception if parsing failed for some reason
	 */
	public abstract List<MapEvent> parse(NotificationMessage message) throws Exception;

	@Override
	public String toString() {
		return this.getName();
	}

	/**
	 * @return a desribing name (could be the class name)
	 */
	protected abstract String getName();
	
	/**
	 * @param con the unit converter to be used when converting UCUM units
	 */
	public void setUnitConverter(IUnitConverter con) {
		this.unitConverter = con;
	}
	
	protected List<Element> extractMessageContent(NotificationMessage message, Set<QName> qnames) {
		List<Element> result = new ArrayList<Element>();
		
		Element content;
		for (QName qn : qnames) {
			content = message.getMessageContent(qn);
			if (content != null) {
				result.add(content);
			}
		}
		
		return result;
	}
	
	protected List<Element> extractMessageContent(NotificationMessage message, QName qname) {
		return extractMessageContent(message, Collections.singleton(qname));
	}
	
	protected boolean hasMessageContent(NotificationMessage message, Set<QName> qnames) {
		for (QName qName : qnames) {
			if (message.getMessageContentNames().contains(qName)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean hasMessageContent(NotificationMessage message, QName qn) {
		return hasMessageContent(message, Collections.singleton(qn));
	}
}
