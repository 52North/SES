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
package org.n52.ses.common;

import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;

import org.apache.muse.core.routing.ResourceIdFactory;
import org.apache.muse.ws.addressing.WsaConstants;

/**
 * Default {@link ResourceIdFactory} implementation.
 * 
 * @author matthes rieke <m.rieke@52north.org>
 *
 */
public class SESResourceIdFactory implements ResourceIdFactory {

    public static final String DEFAULT_PREFIX = "EventService-";
    private AtomicInteger counter = new AtomicInteger(1);
    private String prefix;
    
    public SESResourceIdFactory() {
        this(DEFAULT_PREFIX);
    }
    
    public SESResourceIdFactory(String pf) {
        prefix = pf;
    }
    
    public QName getIdentifierName() {
        return WsaConstants.DEFAULT_RESOURCE_ID_QNAME;
    }
    
    public String getNextIdentifier() {
        return prefix + counter.getAndIncrement();
    }
    
    /**
     * method to set the identifier count. needed
     * for persistency.
     */
    public void setIdentifierCount(int c) {
    	if (c > this.counter.get()) this.counter.set(c); 
    }
    
	/**
	 * @return the prefix string
	 */
	public String getPrefix() {
		return this.prefix;
	}
    
    
    public static class ProducerIdFactory extends SESResourceIdFactory {
    	
    	public ProducerIdFactory() {
    		super("Producer-");
    	}
    	
    }
    
    public static class SubscriptionIdFactory extends SESResourceIdFactory {
    	
    	public SubscriptionIdFactory() {
    		super("Subscription-");
    	}
    	
    }
    
    public static class PublisherIdFactory extends SESResourceIdFactory {
    	
    	public PublisherIdFactory() {
    		super("Publisher-");
    	}
    	
    }

}
