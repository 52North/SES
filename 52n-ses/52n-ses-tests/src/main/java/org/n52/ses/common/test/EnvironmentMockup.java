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