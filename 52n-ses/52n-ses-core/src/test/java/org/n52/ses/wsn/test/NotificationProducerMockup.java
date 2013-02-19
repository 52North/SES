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
