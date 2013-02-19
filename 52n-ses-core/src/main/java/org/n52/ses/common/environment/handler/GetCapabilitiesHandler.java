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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.muse.core.platform.mini.MiniIsolationLayer;
import org.apache.muse.util.xml.XmlUtils;
import org.n52.ses.common.Capabilites;
import org.n52.ses.common.environment.SESMiniServlet;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class GetCapabilitiesHandler extends GetRequestHandler {

	@Override
	public boolean canHandle(HttpServletRequest req) {
		String key = req.getParameter("request") == null ? req.getParameter("REQUEST") : req.getParameter("request"); 
		if (key != null && key.equalsIgnoreCase("GetCapabilities")) {
			return true;
		}
		return false;
	}

	@Override
	public String handleRequest(HttpServletRequest req,
			HttpServletResponse resp, ConfigurationRegistry conf, MiniIsolationLayer isolationLayer) throws Exception {
		try {
			Document soapResponse = isolationLayer.handleRequest(getStaticCapabilitiesRequest());
			Element body = XmlUtils.findFirstInSubTree(soapResponse.getDocumentElement(), Capabilites.CAPABILITIES_QNAME);

			if (body == null) throw new ServletException("Internal error while creating Capabilities response. Please try again later.");

			resp.setContentType("text/xml");
			return XmlUtils.toString(body);
		} catch (SAXException e) {
			throw new ServletException("Internal error while creating Capabilities response. Please try again later.");
		}	
	}

	

	private static synchronized Document getStaticCapabilitiesRequest() throws IOException, SAXException {
		StringBuilder sb = new StringBuilder();

		BufferedReader br = new BufferedReader(new InputStreamReader(SESMiniServlet.class.getResourceAsStream(
		"/sesconfig/wakeup_capabilities_start.xml")));

		while (br.ready()) sb.append(br.readLine());
		br.close();

		sb.append(ConfigurationRegistry.getInstance().getEnvironment().getDefaultURI());

		br = new BufferedReader(new InputStreamReader(SESMiniServlet.class.getResourceAsStream(
		"/sesconfig/wakeup_capabilities_end.xml")));

		while (br.ready()) sb.append(br.readLine());
		br.close();

		return XmlUtils.createDocument(sb.toString());
	}
}
