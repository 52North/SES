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
package org.n52.ses.common.environment.handler;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.muse.core.platform.mini.MiniIsolationLayer;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.ses.util.common.ConfigurationRegistry;

public class XSDProvisionHandler extends GetRequestHandler {

	@Override
	public boolean canHandle(HttpServletRequest req) {
		String query = req.getRequestURI();
		if (query != null && query.endsWith(".xsd")) return true;
		return false;
	}

	@Override
	public String handleRequest(HttpServletRequest req,
			HttpServletResponse resp, ConfigurationRegistry conf,
			MiniIsolationLayer isolationLayer) throws Exception {
		String query = req.getRequestURI();
		int index = query.lastIndexOf("/");
		String lastPart = query.substring(index+1, query.length());
		return resolveXsdContents(lastPart, resp);
	}

	private String resolveXsdContents(String xsdFile, HttpServletResponse resp) throws IOException {
		InputStream in = getClass().getResourceAsStream("/wsdl/"+xsdFile);
		
		if (in == null) throw new IOException("Could not find file '"+xsdFile+ "'.");
		
		XmlObject xo;
		try {
			xo = XmlObject.Factory.parse(in);
		} catch (XmlException e) {
			throw new IOException(e);
		}
		
		resp.setContentType("text/xml");
		
		return xo.xmlText(new XmlOptions().setSavePrettyPrint());
	}

}
