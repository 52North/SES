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
package org.n52.ses.util.common;
import org.apache.muse.core.ResourceManagerListener;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.soap.SoapFault;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 *
 * Extension of the {@link ResourceManagerListener}.
 * @param <T> type of the resource
 */
public interface AdvancedResourceManagerListener<T> {

	/**
	 * A new Resource has been added
	 * @param epr the reference to the endpoint of the resource
	 * @param resource the new resource
	 * @throws SoapFault if an error occurred on adding
	 */
	public void resourceAdded(EndpointReference epr, T resource)  throws SoapFault;
	
	/**
	 * A Resource has been removed
	 * @param epr the endpoint reference of the removed resource
	 * @param resource the removed resource
	 * @throws SoapFault if an error occurred while removing
	 */
	public void resourceRemoved(EndpointReference epr, T resource)  throws SoapFault;
	
}
