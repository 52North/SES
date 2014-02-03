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
package org.n52.ses.wsn.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.muse.core.Environment;
import org.apache.muse.core.SimpleResourceManager;
import org.apache.muse.core.descriptor.CapabilityDefinition;
import org.apache.muse.core.descriptor.ResourceDefinition;
import org.apache.muse.core.descriptor.WsdlConfig;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.notification.WsnConstants;
import org.apache.muse.ws.resource.basefaults.BaseFault;
import org.apache.muse.ws.resource.impl.SimpleWsResource;
import org.n52.ses.common.test.EnvironmentMockup;
import org.n52.ses.wsn.SESNotificationProducer;
import org.n52.ses.wsn.SESSubscriptionManager;
import org.n52.ses.wsrf.SESScheduledTermination;

@SuppressWarnings("rawtypes")
public class NotificationProducerMockup extends SESNotificationProducer {
	
	static QName TOPIC = new QName("topicTest");
	private static SESSubscriptionManager SUB = new SESSubscriptionManager();
	private static Environment ENV = new EnvironmentMockup();
	
	static {
		SUB.setCapabilityURI(WsnConstants.SUBSCRIPTION_MGR_URI);
		SUB.setEnvironment(ENV);
		SUB.setInitializationParameters(new HashMap());
	}
	
	
	public NotificationProducerMockup() throws BaseFault, URISyntaxException {
		this.addTopic(TOPIC);
		this.getSubscriptionContextPath();
		
		SimpleWsResource res = new SubscriptionManagerResourceMockup();
		res.setEndpointReference(new EndpointReference(new URI("http://endpoint.reference")));
		SimpleResourceManager man = new SimpleResourceManager();
		
		List<ResourceDefinition> definitions = new ArrayList<ResourceDefinition>();
		ResourceDefinition def = new ResourceDefinition() {

			
			
			@Override
			public WsdlConfig getWsdlConfig() {
				return new WsdlConfig() {

					@Override
					public String getWsdlPath() {
						return "/wsdl/SESsubmgr.wsdl";
					}

					@Override
					public QName getWsdlPortType() {
						return new QName("http://docs.oasis-open.org/wsn/bw-2", "SubscriptionManager");
					}
					
				};
			}

			@Override
			public Collection getCapabilityDefinitions() {
				return new ArrayList<CapabilityDefinition>();
			}

			@Override
			public Logger getLog() {
				return Logger.getLogger(NotificationProducerMockup.class.getName());
			}

			@Override
			public Environment getEnvironment() {
				return ENV;
			}
			
		};
		def.setContextPath(SESSubscriptionManager.CONTEXT_PATH);
		def.setResourceClass(SubscriptionManagerResourceMockup.class);
		definitions.add(def);
		
		man.addResourceDefinitions(definitions);
		res.setResourceManager(man);
		this.setResource(res);
	}

	@Override
	protected String getSubscriptionContextPath() {
		return SESSubscriptionManager.CONTEXT_PATH;
	}
	
	
	public static class SubscriptionManagerResourceMockup extends SimpleWsResource {

		public SubscriptionManagerResourceMockup() {
			setEnvironment(ENV);
			addCapability(SUB);
			addCapability(new SESScheduledTermination());
		}
		
		
		@Override
		public EndpointReference getEndpointReference() {
			try {
				return new EndpointReference(new URI("http://endpointer.referencoo"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		
	}
	
	
}
