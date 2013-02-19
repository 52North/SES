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
