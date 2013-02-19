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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.muse.core.platform.mini.MiniIsolationLayer;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.wsbr.RegisterPublisher;
import org.n52.ses.wsn.SESSubscriptionManager;

public class WSDLProvisionHandler extends GetRequestHandler {

	private XmlObject sesPortTypeWsdl;
	private XmlObject subMgrWsdl;
	private XmlObject prmWsdl;

	@Override
	public boolean canHandle(HttpServletRequest req) {
		if (req.getParameterMap().size() == 1 &&
				req.getParameterMap().containsKey("wsdl")) {
			return true;
		}
		
		return false;
	}

	@Override
	public String handleRequest(HttpServletRequest req, HttpServletResponse resp,
			ConfigurationRegistry conf, MiniIsolationLayer isolationLayer) throws Exception {
		/*
		 * check if its a wsdl request
		 */
		return createWSDL(req, resp, conf);
	}

	private String createWSDL(HttpServletRequest req, HttpServletResponse resp,
			ConfigurationRegistry conf) throws IOException {
		/*
		 * do we have the config init?
		 */
		if (conf != null) {

			String uri = req.getRequestURI();

			/*
			 * TODO: is there some way to retrieve the port types dynamically?
			 */
			if (uri.endsWith(SESSubscriptionManager.CONTEXT_PATH)) {
				if (this.subMgrWsdl == null) {
					this.subMgrWsdl = createXMLFile(getClass().getResourceAsStream("/wsdl/public/SESsubmgr.wsdl"), conf);
				}
				return prepareWSDLFile(this.subMgrWsdl, req, resp);
			} else if (uri.endsWith(RegisterPublisher.RESOURCE_TYPE)) {
				if (this.prmWsdl == null) {
					this.prmWsdl = createXMLFile(getClass().getResourceAsStream("/wsdl/public/SESprm.wsdl"), conf);
				}
				return prepareWSDLFile(this.prmWsdl, req, resp);
			} else {
				if (this.sesPortTypeWsdl == null) {
					this.sesPortTypeWsdl = createXMLFile(getClass().getResourceAsStream("/wsdl/public/SESallinone.wsdl"), conf);
				}
				return prepareWSDLFile(this.sesPortTypeWsdl, req, resp);
			}
		}
		else {
			/*
			 * throw exception, the environment was not init yet
			 */
			throw new IOException("The resource environment has not been initialized yet.");
		}
	}

	private String prepareWSDLFile(XmlObject wsdlObject, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String result = prepareXMLFile(wsdlObject, req, resp);
		resp.setContentType("application/wsdl+xml");
		return result;
	}
	
}
