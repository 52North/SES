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
package org.n52.ses.common.https;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class AcceptAllSocketFactory {

	private static SSLSocketFactory factory;

	private static final Logger logger = LoggerFactory.getLogger(AcceptAllSocketFactory.class);


	/**
	 * @return a new {@link SSLSocketFactory} that trusts everything.
	 */
	public static final SSLSocketFactory getSocketFactory()	{
		if (factory == null) {
			try {
				TrustManager[] tm = new TrustManager[] {new AcceptAllTrustManager()};
				SSLContext context = SSLContext.getInstance("SSL");
				context.init(new KeyManager[0], tm, new SecureRandom());

				factory = context.getSocketFactory();
			} catch (KeyManagementException e) {
				logger.warn("An error occured concerning key management: " + e.getMessage(), e); 
			} catch (NoSuchAlgorithmException e) {
				logger.warn("An error occured while creating AcceptAllSocketFactory.", e);
			}
		}
		return factory;
	}


}
