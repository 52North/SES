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

import org.apache.muse.core.routing.SimpleResourceRouter;
import org.n52.ses.requestlogger.RequestLoggerWrapper;
import org.w3c.dom.Element;

/**
 * A helper class for unloading of file logging.
 * see {@link RequestLoggerWrapper}.
 * 
 * @author Matthes Rieke
 *
 */
public class SESResourceRouter extends SimpleResourceRouter {

	@Override
	public Element invoke(Element soapBody) {
		/*
		 * check if we have the wrapper activated.
		 * otherwise, ignore
		 * MOVED to org.n52.ses.common.SESMiniServlet
		 */
//		if (RequestLoggerWrapper.isActive()) {
//			RequestLoggerWrapper inst = RequestLoggerWrapper.getInstance();
//			if (inst != null)
//				inst.logRequest(System.currentTimeMillis(), soapBody);
//		}
		
		return super.invoke(soapBody);
	}

	
}
