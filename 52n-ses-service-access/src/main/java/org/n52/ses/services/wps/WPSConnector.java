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
package org.n52.ses.services.wps;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.ses.util.http.BasicAuthenticator;
import org.n52.ses.util.http.SESHttpClient;
import org.n52.ses.util.http.SESHttpClient.SESHttpResponse;

/**
 * Provides methods to access and execute processes from a WPS. 
 * 
 * @author Matthes Rieke
 *
 */
public class WPSConnector {
	
	public final static String USER_KEY = "user";
	public final static String PASSWORD_KEY = "pw";
	
	private String wpsURL;
	
	private Map<String, String> userNamePW;
	private SESHttpClient client;
	
	public static XmlOptions requestOptions;
	
	static {
		requestOptions = new XmlOptions();
		requestOptions.setSavePrettyPrint();

		Map<String, String> suggestedPrefixes = new HashMap<String, String>();
		suggestedPrefixes.put("http://www.opengis.net/wps/1.0.0", "wps");
		suggestedPrefixes.put("http://www.aixm.aero/schema/5.1", "aixm");
		suggestedPrefixes.put("http://www.opengis.net/gml/3.2", "gml");
		requestOptions.setSaveSuggestedPrefixes(suggestedPrefixes);
		
	}
	
	/**
	 * Standard constructor
	 * 
	 * @param wfsURL
	 * @param usesSOAP
	 */
	public WPSConnector(String wpsURL) {
		this.wpsURL = wpsURL;
		client = new SESHttpClient();
	}

	/**
	 * Standard constructor using http-Authentication.
	 * 
	 * @param wfsURL
	 * @param usesSOAP
	 * @param userNamePW
	 */
	public WPSConnector(String wpsURL,
			Map<String, String> userNamePW) {
		this(wpsURL);
		this.userNamePW = userNamePW;
	}
	
	public ProcessOutputs executeRequest(XmlObject execute) throws Exception {
		XmlObject result = sendHttpPost(execute);
		
		if (result != null && result instanceof ExecuteResponseDocument) {
			ExecuteResponse response = ((ExecuteResponseDocument) result).getExecuteResponse();
			if (response != null && response.isSetProcessOutputs()) {
				return response.getProcessOutputs();
			}
		}
		
		throw new Exception("Could not retrieve result from WPS.");
	}
	
	/**
	 * Sends a request to a web service. Can deal with SOAP messages and http-authentification.
	 * 
	 * @param xmlRequest
	 * 		the request
	 * @return
	 * 		the response from the web service
	 * @throws Exception 
	 */
	private XmlObject sendHttpPost(XmlObject xmlRequest) throws Exception{
		String request = xmlRequest.xmlText(requestOptions);
		
		// authentication
		if (userNamePW != null){
			String username = userNamePW.get(USER_KEY);
			String password = userNamePW.get(PASSWORD_KEY);
			client.setAuthentication(new BasicAuthenticator(username, password));
		}

		SESHttpResponse resp = client.sendPost(new URL(wpsURL), request, "text/xml; charset=utf-8");
		if (resp.getContentType().contains("xml")) {
			return XmlObject.Factory.parse(resp.getContent());
		}
		return null;
	}

	
	public String getURL() {
		return wpsURL;
	}

	
}
