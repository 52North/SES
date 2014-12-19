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
package org.n52.ses.storedfilters;


import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.esSf.x00.StoredFilterDescriptionDocument;

import org.apache.muse.core.Resource;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.resource.impl.AbstractWsResourceCapability;
import org.apache.xmlbeans.XmlException;
import org.n52.ses.api.common.WsbrConstants;
import org.n52.ses.common.SimpleHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class StoredFilterInstance extends AbstractWsResourceCapability {
	
	public static final String STORED_FILTER_NAMESPACE = "http://www.opengis.net/es-sf/0.0";
	public static final QName STORED_FILTER_DESCRIPTION_QNAME = new QName(STORED_FILTER_NAMESPACE, "StoredFilterDescription");
	public static final String NAMESPACE_URI = "http://www.opengis.net/es-sf/0.0";
	private static Map<String, StoredFilterInstance> _instances = new HashMap<String, StoredFilterInstance>();
	QName[] PROPERTIES = new QName[] { WsbrConstants.CREATION_QNAME, STORED_FILTER_DESCRIPTION_QNAME };
	private Node description;
	private String id;
	
	public StoredFilterInstance() {
		super();
		
		setMessageHandler(new SimpleHandler("http://www.opengis.net/es-sf/0.0/RemoveStoredFilterRequest",null,this,"removeStoredFilter"));
	}

	
	public Element removeStoredFilter(Element storedFilterID) {
		
		return null;
	}
	
	
	public Element createStoredFilter(Element storedFilterDescription) {
		
		return null;
	}
	
	
	@Override
	public QName[] getPropertyNames() {
		return this.PROPERTIES;
	}
	
	
	@Override
	public void initialize() throws SoapFault {
		super.initialize();
		
		Resource resource = getResource();

		/*
		 * TODO check if this is a created or a persistent stored filter
		 * currently only persistent are supported, but this may change
		 */
		
		StoredFilterDescriptionDocument storedFilter = null;
		try {
			if (resource.getEndpointReference() != null) {
				Element param = resource.getEndpointReference().getParameter(STORED_FILTER_DESCRIPTION_QNAME);
				
				if (param != null)
					storedFilter = StoredFilterDescriptionDocument.Factory.parse(param);
			}
		} catch (XmlException e) {
			throw new SoapFault("Could not parse the Stored Filter XML representation.", e);
		}
		
		if (storedFilter == null) return;
		
		this.description = storedFilter.getStoredFilterDescription().getDomNode();
		this.id = storedFilter.getStoredFilterDescription().getId();
		
		addInstance(this);
	}
	
	private static synchronized void addInstance(StoredFilterInstance storedFilterInstance) {
		_instances.put(storedFilterInstance.id, storedFilterInstance);
	}

	private static synchronized void removeInstance(StoredFilterInstance storedFilterInstance) {
		_instances.remove(storedFilterInstance.id);
	}
	
	public static synchronized StoredFilterInstance getByID(String id) {
		return _instances.get(id);
	}
	
	public static synchronized Collection<StoredFilterInstance> getAvailableInstances() {
		return _instances.values();
	}

	@Override
	public void shutdown() throws SoapFault {
		super.shutdown();
		removeInstance(this);
	}


	
	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	/**
	 * 
	 * 
	 * @return Capability creation time
	 */
	public Date getCreationTime() {
		return new Date();
	}
	
	public Element getStoredFilterDescription() {
		return (Element) this.description;
	}
}
