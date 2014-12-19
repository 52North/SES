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
package org.n52.ses.api.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.notification.impl.FilterCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESFilterCollection extends FilterCollection {

	private Collection<Filter> _filters = new ArrayList<Filter>();
	
	/**
	 * Holds the IConstraintFilter (can only appear once
	 * per SESFilterCollection?!) 
	 */
	private IConstraintFilter constraintFilter = null;

	private boolean hasEPLFilter;

	/* (non-Javadoc)
	 * @see org.apache.muse.ws.notification.Filter#accepts(org.apache.muse.ws.notification.NotificationMessage)
	 */
	@Override
	public boolean accepts(NotificationMessage message) {
		Iterator<Filter> i = getFilters().iterator();
		
		if (message == null) {
			return false;
		}

		while (i.hasNext())	{
			Filter next = i.next();

			//
			// only one filter has to fail for the whole thing to fail.
			// only check non IConstraintFilter
			//
			if ( !(next instanceof IConstraintFilter)){
				if (!next.accepts(message))
					return false;	
			}
			
		}

		return true;
	}

	/**
	 * Adds a filter to this {@link SESFilterCollection}
	 * 
	 * @param filter a valid {@link Filter}
	 * @throws Throwable if more than one {@link IConstraintFilter} is added.
	 */
	public void addFilter(Filter filter) {
		if (filter instanceof IConstraintFilter && this.constraintFilter == null) {
			this.constraintFilter = (IConstraintFilter) filter;
		} else if (filter instanceof IConstraintFilter && this.constraintFilter != null) {
			throw new RuntimeException("Only one constraint filter per subscription is allowed.");
		}
		this._filters.add(filter);
	}

	/**
	 * 
	 * @return an unmodifiable collection of the actual filters
	 */
	public Collection<Filter> getFilters() {
		return Collections.unmodifiableCollection(this._filters);
	}

	/**
	 * Holds the IConstraintFilter (can only appear once
	 * per SESFilterCollection?!) 
	 * 
	 * @return the IConstraintFilter
	 */
	public IConstraintFilter getConstraintFilter() {
		return this.constraintFilter;
	}

	@Override
	public Element toXML() {
		return toXML(XmlUtils.createDocument());
	}

	@Override
	public Element toXML(Document doc) {
		Element filterXML = XmlUtils.createElement(doc, WsnConstants.FILTER_QNAME);

		Iterator<Filter> i = getFilters().iterator();

		while (i.hasNext())
		{
			Filter next = i.next();
			Element nextXML = next.toXML(doc);

			//
			// we have to 'move' instead of 'appendChild' because the other 
			// Filter types already add a <Filter/> element as part of their 
			// toXML() implementations, and we can't change this for reasons 
			// of backwards compatibility. therefore, we just take the element 
			// under the <Filter/> and move it under our new <Filter/>
			//
			XmlUtils.moveSubTree(nextXML, filterXML);
		}

		return filterXML;
	}

	public boolean hasEPLFilter() {
		return this.hasEPLFilter;
	}

}