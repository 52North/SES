/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.muse.core.platform.mini.MiniIsolationLayer;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.ses.util.common.ConfigurationRegistry;

public abstract class GetRequestHandler {
	
	public abstract boolean canHandle(HttpServletRequest req);

	public abstract String handleRequest(HttpServletRequest req, HttpServletResponse resp,
			ConfigurationRegistry conf, MiniIsolationLayer isolationLayer) throws Exception;

	
	/**
	 * Sends xml to the response
	 * 
	 * @param wsdlXml the xml object
	 * @param resp the response object where to send the file
	 * @throws IOException if IO error
	 */
	protected String prepareXMLFile(XmlObject wsdlXml,
			HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (wsdlXml == null) {
			throw new IOException("Could not read wsdl file.");
		}
		
		XmlOptions opts = new XmlOptions();
		opts.setSaveNamespacesFirst();
		opts.setSavePrettyPrint();
		opts.setLoadStripComments();

		resp.setContentType("text/xml");

		return wsdlXml.xmlText(opts);
	}

	/**
	 * Sends xml (as input stream) to the response
	 * 
	 * @param in resource as inputstream
	 * @param resp the response where to write the file
	 * @param conf the config to retrieve the global known url
	 * @return the xmlobject for caching purposes
	 * @throws IOException if IO error
	 */
	protected XmlObject createXMLFile(InputStream in, ConfigurationRegistry conf) throws IOException {
		XmlOptions opts = new XmlOptions();
		opts.setSaveNamespacesFirst();
		opts.setSavePrettyPrint();
		opts.setLoadStripComments();

		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();

		while (br.ready()) {
			sb.append(br.readLine());
		}

		/*
		 * set location
		 */
		String xmltmp = sb.toString().replace("http://localhost:8080/52nSES", conf.getEnvironment().getDefaultURI().substring(0,
				conf.getEnvironment().getDefaultURI().lastIndexOf("/services")));

		XmlObject wsdlObj = null;
		try {
			wsdlObj  = XmlObject.Factory.parse(xmltmp, opts);
		} catch (XmlException e) {
			throw new IOException(e);
		}

		return wsdlObj;
	}
	
	

}
