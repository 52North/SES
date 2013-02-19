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
package org.n52.ses.api.ws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.WsnConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESFilterCollection implements Filter {

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
	public void addFilter(Filter filter) throws Throwable {
		if (filter instanceof IConstraintFilter && this.constraintFilter == null) {
			this.constraintFilter = (IConstraintFilter) filter;
		} else if (filter instanceof IConstraintFilter && this.constraintFilter != null) {
			throw new Throwable("Only one constraint filter per subscription is allowed.");
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