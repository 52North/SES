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
