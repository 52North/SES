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
