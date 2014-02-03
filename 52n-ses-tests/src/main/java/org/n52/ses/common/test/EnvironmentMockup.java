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
package org.n52.ses.common.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.muse.core.Environment;
import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.MessageHeaders;
import org.apache.muse.ws.addressing.soap.SoapClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class EnvironmentMockup implements Environment {

	@Override
	public void addAddressingContext(MessageHeaders context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String createRelativePath(String originalPath,
			String relativePath) {
		return originalPath.substring(0, originalPath.lastIndexOf("/")) +"/"+ relativePath;
	}

	@Override
	public MessageHeaders getAddressingContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getDataResource(String path) {
		if (path.startsWith("/")) {
			return getClass().getResource(path);
		} else {
			return getClass().getResource("/"+path);
		}
	}

	@Override
	public InputStream getDataResourceStream(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultURI() {
		return "http://localhost:8080/EventService/services/Broker";
	}

	@Override
	public EndpointReference getDeploymentEPR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document getDocument(String path) {
		try {
			return XmlUtils.createDocument(getClass().getResourceAsStream(path));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public File getRealDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SoapClient getSoapClient() {
		return new SoapClientMockup();
	}

	@Override
	public void removeAddressingContext() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaultURI(String defaultURI) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSoapClient(SoapClient soapClient) {
		// TODO Auto-generated method stub
		
	}
	
	
}