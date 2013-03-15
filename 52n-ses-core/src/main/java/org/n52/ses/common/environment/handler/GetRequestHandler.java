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
