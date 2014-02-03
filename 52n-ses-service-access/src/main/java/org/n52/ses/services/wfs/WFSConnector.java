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
package org.n52.ses.services.wfs;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wfs.x20.FeatureCollectionDocument;
import net.opengis.wfs.x20.FeatureCollectionType;
import net.opengis.wfs.x20.MemberPropertyType;

import org.apache.http.entity.ContentType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.util.http.BasicAuthenticator;
import org.n52.ses.util.http.SESHttpClient;
import org.n52.ses.util.http.SESHttpClient.SESHttpResponse;

import aero.aixm.schema.x51.message.AIXMBasicMessageDocument;
import aero.aixm.schema.x51.message.AIXMBasicMessageType;

/**
 * Provides methods to access and retrieve features from a WFS. 
 * Supports authentication and simple filter encoding.
 * 
 * @author Klaus Drerup <klaus.drerup@uni-muenster.de>
 * @author Matthes Rieke
 *
 */
public class WFSConnector {
	
	// identifiers for mappings
	public final static String FEATURE_TYPE_KEY = "featureType";
	public final static String FEATURE_KEY = "feature";
	public final static String DESIGNATOR_KEY = "designator";
	public final static String GML_IDENTIFIER_KEY = "identifier";
	public final static String USER_KEY = "user";
	public final static String PASSWORD_KEY = "pw";
	
	private String wfsURL;
	private boolean usesSOAP;
	
	// if user and PW not given, no need to set this flag
	private Map<String, String> userNamePW;
	private SESHttpClient client;
	
	// defines namespaces in request
	public static XmlOptions requestOptions;
	private static String preSoap;
	private static String postSoap;
	
	static {
		requestOptions = new XmlOptions();
		requestOptions.setSavePrettyPrint();

		// set proper namespaces
		Map<String, String> suggestedPrefixes = new HashMap<String, String>();
		suggestedPrefixes.put("http://www.opengis.net/wfs/2.0", "wfs");
		suggestedPrefixes.put("http://www.opengis.net/fes/2.0", "fes");
		suggestedPrefixes.put("http://www.aixm.aero/schema/5.1", "aixm");
		suggestedPrefixes.put("http://www.opengis.net/gml/3.2", "gml");
		requestOptions.setSaveSuggestedPrefixes(suggestedPrefixes);
		
		/*
		 * SOAP Wrapper elements
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"");
		sb.append(" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">");
		sb.append("<soap:Header><wsa:From><wsa:Address>http://www.w3.org/2005/08/addressing/role/anonymous</wsa:Address></wsa:From></soap:Header>");
		sb.append("<soap:Body>");
		
		preSoap = sb.toString();
		postSoap = "</soap:Body></soap:Envelope>";
	}
	
	/**
	 * Standard constructor
	 * 
	 * @param wfsURL
	 * @param usesSOAP
	 */
	public WFSConnector(String wfsURL, boolean usesSOAP) {
		this.wfsURL = wfsURL;
		this.usesSOAP = usesSOAP;
		this.userNamePW = null;
		client = new SESHttpClient();
	}

	/**
	 * Standard constructor using http-Authentication.
	 * 
	 * @param wfsURL
	 * @param usesSOAP
	 * @param userNamePW
	 */
	public WFSConnector(String wfsURL, boolean usesSOAP,
			Map<String, String> userNamePW) {
		this.wfsURL = wfsURL;
		this.usesSOAP = usesSOAP;
		this.userNamePW = userNamePW;
		client = new SESHttpClient();
	}
	
	

	/**
	 * Wraps the request in a SOAP message
	 * 
	 * @param request
	 * 		the request
	 * @return
	 * 		the SOAP message
	 * @throws XmlException
	 */
	private XmlObject wrapWithSoap(XmlObject request) throws XmlException{
		
		// put message in soap message body
		StringBuilder wrappedContent = new StringBuilder();
		wrappedContent.append(preSoap);
		wrappedContent.append(request.xmlText(requestOptions));
		wrappedContent.append(postSoap);
		XmlObject wrappedXml = XmlObject.Factory.parse(wrappedContent.toString());
		
		return wrappedXml;
	}
	
	
	/**
	 * Retrieves a message from a SOAP message
	 * 
	 * @param response
	 * 		the SOAP message
	 * @return
	 * 		the extracted response
	 * @throws XmlException
	 */
	private XmlObject removeSoap(XmlObject response) throws XmlException{
		if (response == null) return null;
		XmlObject[] body = XmlUtil.selectPath("declare namespace soap='http://schemas.xmlsoap.org/soap/envelope/'; .//soap:Body", response);
		if (body != null && body.length == 1) {
			return XmlObject.Factory.parse(body[0].getDomNode().getFirstChild());
		}
		return null;
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

		SESHttpResponse resp = client.sendPost(new URL(wfsURL), request, ContentType.create("text/xml", "utf-8"));
		if (resp.getContentType().contains("xml")) {
			return XmlObject.Factory.parse(resp.getContent());
		}
		return null;
	}

	

	
	public String getURL() {
		return wfsURL;
	}

	/**
	 * Retrieves members of a FeatureCollection.
	 * 
	 * @param response 
	 * 		the xml-response. 
	 * @return  
	 * 		the features
	 * @throws XmlException 
	 */
	private MemberPropertyType[] parseFeatureCollection(XmlObject response) throws XmlException {	
		MemberPropertyType[] members = null;
		FeatureCollectionDocument featColDoc = 
			(FeatureCollectionDocument)response;
		FeatureCollectionType featureCollection = featColDoc.getFeatureCollection();
		
		if (featureCollection.sizeOfMemberArray()  > 0 ){
			members = featureCollection.getMemberArray(); 
		}
		
		return members;
	}
	
	/**
	 * Retrieves member of a AIXM Basic Message.
	 * 
	 * @param wfsResponse
	 * 		the xml-response
	 * @return
	 * 		the features
	 * @throws XmlException
	 */
	private XmlObject[] parseAIXMBasicMsg(XmlObject wfsResponse) throws XmlException {
		XmlObject[] members = null;
		AIXMBasicMessageDocument aixmBasicMsgDoc = (AIXMBasicMessageDocument)wfsResponse;
		AIXMBasicMessageType aixmBasicMessage = aixmBasicMsgDoc.getAIXMBasicMessage();
		if (aixmBasicMessage.sizeOfHasMemberArray() > 0){
			members = aixmBasicMessage.getHasMemberArray();
		}
		
		return members;
	}

	/**
	 * Gets features from a WFS. Controls the process by creating the request, sending it 
	 * and parsing the response to features.
	 * 
	 * @param params
	 * 		the list of parameters
	 * @return
	 * 		the features from the WFS
	 * @throws Exception 
	 */
	public XmlObject[] executeQuery(WFSQuery query) throws Exception {
		
		// create the request
		XmlObject getFeatureRequest = query.createQuery();
		if (usesSOAP){
			getFeatureRequest = wrapWithSoap(getFeatureRequest);
		}		
		
		// send request
		XmlObject wfsResponse = sendHttpPost(getFeatureRequest);
		
		if (usesSOAP){
			wfsResponse = removeSoap(wfsResponse);
		}
		
		// parse the response
		XmlObject[] features = null;
		if (wfsResponse instanceof AIXMBasicMessageDocument){
			features = parseAIXMBasicMsg(wfsResponse);
		} 
		else if (wfsResponse instanceof FeatureCollectionDocument){
			features = parseFeatureCollection(wfsResponse);
		}
		
		return features;
	}
	
	
}
