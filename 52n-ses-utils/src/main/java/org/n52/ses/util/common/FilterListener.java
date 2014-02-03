/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
