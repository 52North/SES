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
package org.n52.ses.util.common;
import java.util.HashMap;
import java.util.Map;

import org.apache.muse.core.Resource;
import org.apache.muse.core.ResourceManagerListener;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.soap.SoapFault;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 * @param <T> type of the resource
 *
 */
public class FilterListener<T> implements ResourceManagerListener {

	private Class<T> clazz;
	private AdvancedResourceManagerListener<T> listener;
	private Map<EndpointReference, T> map = new HashMap<EndpointReference, T>();
	
	/**
	 * Creates filter for a {@link ResourceManagerListener}. Only Resources from the given type are forwarded to the listener
	 * @param clazz implementation of the resource
	 * @param listener listener for the resource
	 */
	public FilterListener(Class<T> clazz, AdvancedResourceManagerListener<T> listener) {
		this.clazz = clazz;
		this.listener = listener;
	}


	/**
	 * @see org.apache.muse.core.ResourceManagerListener#resourceAdded(org.apache.muse.ws.addressing.EndpointReference, org.apache.muse.core.Resource)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void resourceAdded(EndpointReference epr, Resource resource) throws SoapFault {
		if(resource.getClass().equals(this.clazz)) {
			this.map.put(epr, (T) resource);
			this.listener.resourceAdded(epr, (T) resource);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.muse.core.ResourceManagerListener#resourceRemoved(org.apache.muse.ws.addressing.EndpointReference)
	 */
	@Override
	public void resourceRemoved(EndpointReference epr) throws SoapFault {
		T resource = this.map.get(epr);
		if(resource != null) {
			this.listener.resourceRemoved(epr, resource);
		}
	}

}
