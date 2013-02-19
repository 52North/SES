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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.muse.ws.addressing.soap.SoapConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling outgoing https connections.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class HTTPSConnectionHandler implements SoapConnectionHandler {

	private static final Logger logger = LoggerFactory.getLogger(HTTPSConnectionHandler.class);


	/* (non-Javadoc)
	 * @see org.apache.muse.ws.addressing.soap.SoapConnectionHandler#afterSend(java.net.URLConnection)
	 */
	@Override
	public void afterSend(URLConnection conn) {
		logger.debug("post sending ops..");
	}

	/* (non-Javadoc)
	 * @see org.apache.muse.ws.addressing.soap.SoapConnectionHandler#beforeSend(java.net.URLConnection)
	 */
	@Override
	public void beforeSend(URLConnection conn) {
		if (conn.getURL().getProtocol().equals("https")) {
			try {
				conn.getURL().openConnection();
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}
		}		
	}

	/**
	 * @param args as
	 */
	public static void main(String[] args) {
		URLConnection conn;
		try {
			conn = new URL("https://project.gwdi.eu/ows-client/consumer").openConnection();
			new HTTPSConnectionHandler().beforeSend(conn);
		} catch (MalformedURLException e) {
			logger.warn(e.getMessage(), e);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}

	}


}
