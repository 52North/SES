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
package org.n52.ses.filter.dialects;

import java.util.ArrayList;
import java.util.List;

import org.apache.muse.ws.notification.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public abstract class DialectConstraintFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(DialectConstraintFilter.class);
	private static List<Class<? extends DialectConstraintFilter>> customFilters = new ArrayList<Class<? extends DialectConstraintFilter>>();
	
	static {
		registerCustomFilter(SelectiveMetadataFilter.class);
	}
	
	public static synchronized void registerCustomFilter(Class<? extends DialectConstraintFilter> class1) {
		customFilters.add(class1);
	}
	
	public static synchronized List<DialectConstraintFilter> getCustomFilters() {
		List<DialectConstraintFilter> result = new ArrayList<DialectConstraintFilter>();
		for (Class<? extends DialectConstraintFilter> customFESFilter : customFilters) {
			try {
				result.add(customFESFilter.newInstance());
			} catch (InstantiationException e) {
				logger.warn(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return result;
	}

	public abstract boolean canHandle(Element filterXml);

	public abstract void initialize(Element filterXml);

}
