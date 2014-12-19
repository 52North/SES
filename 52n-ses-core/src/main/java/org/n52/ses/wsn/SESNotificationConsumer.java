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
package org.n52.ses.wsn;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.namespace.QName;

import org.apache.muse.core.routing.MessageHandler;
import org.apache.muse.util.LoggingUtils;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.NotificationMessageListener;
import org.apache.muse.ws.notification.impl.SimpleNotificationConsumer;
import org.n52.ses.api.common.FreeResourceListener;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.util.concurrent.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SESNotificationConsumer extends SimpleNotificationConsumer implements FreeResourceListener {

	private static final Logger logger = LoggerFactory.getLogger(SESNotificationConsumer.class);
	private ExecutorService executors = Executors.newSingleThreadExecutor(
			new NamedThreadFactory("NotificationConsumerPool"));
	
	@Override
	public void notify(NotificationMessage[] messages) {
        NotifyThread thread = new NotifyThread(messages);
        
        //use executors instead of always creating a new Thread (muse default)
        executors.submit(thread);
	}

	@Override
	public void initialize() throws SoapFault {
		logger.info("initialising SESNotificationConsumer...");
		ConfigurationRegistry.getInstance().registerFreeResourceListener(this);
		super.initialize();
	}

	@Override
	public void prepareShutdown() throws SoapFault {
		super.prepareShutdown();
		
		executors.shutdown();
	}
	
	@Override
	public void shutdown() throws SoapFault {
		super.shutdown();
		
		executors.shutdownNow();
	}
	

	@Override
	public void freeResources() {
		executors.shutdownNow();
	}

	@Override
	protected MessageHandler createNotifyHandler() {
		MessageHandler handler = new SESNotifyHandler();
        Method method = null;
        
        try
        {
            //
            // can't use ReflectUtils.getFirstMethod() because it might 
            // return Object.notify()
            //
            method = getClass().getMethod("notify", new Class[]{ NotificationMessage[].class });
        }
        
        catch (Throwable error)
        {
            throw new RuntimeException(error.getMessage(), error);
        }
        
        handler.setMethod(method);
        return handler;
	}
	
	
    /**
     * 
     * NotifyThread is a simple thread that iterates over the collection of 
     * message listeners and provides the latest message to each of them.
     *
     * @author Dan Jemiolo (danj)
     *
     */
    private class NotifyThread implements Runnable
    {
        private NotificationMessage[] _messages = null;
        
        public NotifyThread(NotificationMessage[] messages)
        {
            _messages = messages;
        }
        
        private void processMessageListeners(NotificationMessage message)
        {
            Iterator<?> i = getMessageListeners().iterator();
            
            while (i.hasNext())
            {
                NotificationMessageListener listener = (NotificationMessageListener)i.next();
                
                try
                {
                    if (listener.accepts(message))
                        listener.process(message);
                }
                
                catch (Throwable error)
                {
                    LoggingUtils.logError(getLog(), error);
                }
            }
        }
        
        private void processTopicListeners(NotificationMessage message)
        {
            QName topic = message.getTopic();
            Iterator<?> i = getTopicListeners(topic).iterator();
            
            while (i.hasNext())
            {
                NotificationMessageListener listener = (NotificationMessageListener)i.next();
                
                try
                {
                    //
                    // don't call accepts() - we assume that all listeners 
                    // added for the topic want the message and require no 
                    // further analysis
                    //
                    listener.process(message);
                }
                
                catch (Throwable error)
                {
                    LoggingUtils.logError(getLog(), error);
                }
            }
        }
        
        public void run()
        {
            for (int n = 0; n < _messages.length; ++n)
            {
            	
                //
                // if a topic is available, pass it along to the 
                // topic listeners
                //
                QName topic = _messages[n].getTopic();
                Collection<?> topicListeners = getTopicListeners(topic);
                
                //
                // our check is if there are zero topic listeners, not 
                // whether a topic merely exists in the message. this 
                // means that pre-2.2 users who created all listeners 
                // as message listeners will not experience a change 
                // in behavior for messages with topics in them
                //
                if (!topicListeners.isEmpty())
                    processTopicListeners(_messages[n]);
                
                else
                    processMessageListeners(_messages[n]);
            }
        }
    }


}
