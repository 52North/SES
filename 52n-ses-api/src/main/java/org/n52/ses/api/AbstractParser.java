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
