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
