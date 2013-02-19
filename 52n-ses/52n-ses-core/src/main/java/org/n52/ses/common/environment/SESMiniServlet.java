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
package org.n52.ses.common.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.muse.core.platform.mini.MiniIsolationLayer;
import org.apache.muse.core.platform.mini.MiniServlet;
import org.apache.muse.util.xml.XmlUtils;
import org.n52.ses.common.environment.handler.GetRequestHandler;
import org.n52.ses.common.environment.handler.GetCapabilitiesHandler;
import org.n52.ses.common.environment.handler.WSDLProvisionHandler;
import org.n52.ses.common.environment.handler.XSDProvisionHandler;
import org.n52.ses.requestlogger.RequestLoggerWrapper;
import org.n52.ses.startupinit.StartupInitServlet;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.wsbr.RegisterPublisher;
import org.n52.ses.wsn.SESNotificationProducer;
import org.n52.ses.wsn.SESSubscriptionManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Extended Servlet with support for shutdown and wsdl provision.
 * 
 * @author Matthes Rieke
 *
 */
public class SESMiniServlet extends MiniServlet {


	private static final long serialVersionUID = 1L;
	private String landingPage;
	private MiniIsolationLayer sesIsolationLayer;
	private List<GetRequestHandler> getRequestHandlers = new ArrayList<GetRequestHandler>();
	private static int minimumContentLengthForGzip = 500000;
	private static RequestLoggerWrapper loggerInst;
	private static final AtomicBoolean firstResponsePrint = new AtomicBoolean(true);

	public SESMiniServlet() {
		this.getRequestHandlers.add(new GetCapabilitiesHandler());
		this.getRequestHandlers.add(new WSDLProvisionHandler());
		this.getRequestHandlers.add(new XSDProvisionHandler());
	}

	@Override
	public void destroy() {
		if (ConfigurationRegistry.getInstance() != null) {
			ConfigurationRegistry.getInstance().shutdown();
		}
		super.destroy();
	}



	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		if (this.sesIsolationLayer == null)
			this.sesIsolationLayer = createIsolationLayer(request, getServletContext());

		InputStream input = request.getInputStream();
		Document soapRequest = null;

		long time = System.currentTimeMillis();

		try
		{
			soapRequest = XmlUtils.createDocument(input);
		}

		catch (SAXException error)
		{
			throw new IOException(error);
		}

		handleSoapRequest(request, response, soapRequest);

		if (RequestLoggerWrapper.isActive()) {
			if (loggerInst == null)	{
				loggerInst = RequestLoggerWrapper.getInstance();
			}
			if (loggerInst != null)
				loggerInst.logRequest(time, soapRequest);
		}
	}

	private void handleSoapRequest(HttpServletRequest request, HttpServletResponse response,
			Document soapRequest) throws IOException {
		Document soapResponse;
		try {
			soapResponse = this.sesIsolationLayer.handleRequest(soapRequest);
		} catch (RuntimeException e) {
			throw new IOException(e);
		}

		/*
		 * is null? return a http response code.
		 * change made by Matthes Rieke <m.rieke@uni-muenster.de>
		 */
		if (soapResponse == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			//TODO check support of ' ; charset=utf-8'
			response.setContentType("application/soap+xml; charset=utf-8");
			printResponse(request, response, XmlUtils.toString(soapResponse));
		}
	}


	private boolean clientSupportsGzip(HttpServletRequest request) {
		String header = request.getHeader("Accept-Encoding");
	      if (header != null && !header.isEmpty()) {
	         String[] split = header.split(",");
	         for (String string : split) {
	            if (string.equalsIgnoreCase("gzip")) {
	               return true;
	            }
	         }
	      }
	      return false;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		ConfigurationRegistry conf = ConfigurationRegistry.getInstance();

		for (GetRequestHandler handler : this.getRequestHandlers) {
			if (handler.canHandle(req)) {
				try {
					printResponse(req, resp, handler.handleRequest(req, resp, conf, this.sesIsolationLayer));
				} catch (Exception e) {
					throw new ServletException(e);
				}
				return;
			}
		}
		
		provideLandingPage(req, resp, conf);
	}


	private void provideLandingPage(HttpServletRequest req,
			HttpServletResponse resp, ConfigurationRegistry conf)
	throws IOException, UnsupportedEncodingException {
		/*
		 * return landing page
		 */
		if (this.landingPage != null) {
			resp.setContentType("text/html");
			synchronized (this) {
				printResponse(req, resp, this.landingPage);
			}

			return;
		}

		InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("/landing_page.html"));
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();

		while (br.ready()) {
			sb.append(br.readLine());
		}
		String html = sb.toString();

		String reqUrl = URLDecoder.decode(req.getRequestURL().toString(),
				(req.getCharacterEncoding() == null ? Charset.defaultCharset().name() : req.getCharacterEncoding()));
		html = html.replace("[SES_URL]", reqUrl.substring(0,
				reqUrl.indexOf(req.getContextPath())) + req.getContextPath());

		/*
		 * check if we are init yet
		 */
		String sesPortTypeUrl, subMgrUrl, prmUrl = "";
		if (conf != null) {
			String defaulturi = conf.getEnvironment().getDefaultURI().substring(0,
					conf.getEnvironment().getDefaultURI().lastIndexOf("/services"));
			sesPortTypeUrl = defaulturi + "/services/" + SESNotificationProducer.CONTEXT_PATH;
			subMgrUrl = defaulturi + "/services/" + SESSubscriptionManager.CONTEXT_PATH;
			prmUrl = defaulturi + "/services/" + RegisterPublisher.RESOURCE_TYPE;

			conf.setSubscriptionManagerWsdl(subMgrUrl + "?wsdl");

			html = html.replace("<p id=\"ses-status\"><p>",
			"<p style=\"color:#0f0\">The service is active and available.</p>");

			html = html.replace("[GET_CAPS]", StringEscapeUtils.escapeHtml4(StartupInitServlet.getGetCapabilitiesRequest(sesPortTypeUrl)));
			/*
			 * replace the url
			 */
			synchronized (this) {
				if (this.landingPage == null) {
					this.landingPage = html.replace("[SES_PORT_TYPE_URL]", sesPortTypeUrl);
					this.landingPage = this.landingPage.replace("[SUB_MGR_URL]", subMgrUrl);
					this.landingPage = this.landingPage.replace("[PRM_URL]", prmUrl);	

					resp.setContentType("text/html");
					printResponse(req, resp, this.landingPage);
				}


			}

		}
		else {
			/*
			 * we do not have the config, warn the user
			 */
			html = html.replace("<p id=\"ses-status\"><p>",
			"<p style=\"color:#f00\">The service is currently not available due to unfinished or failed initialization.</p>");

			resp.setContentType("text/html");
			printResponse(req, resp, html);
		}
	}


	private void printResponse(HttpServletRequest request, HttpServletResponse response,
			String string) throws IOException {
		int contentLength = string.getBytes("UTF-8").length;

		if (firstResponsePrint.getAndSet(false)) {
			ConfigurationRegistry conf = ConfigurationRegistry.getInstance();
			if (conf == null) {
				firstResponsePrint.getAndSet(true);
			}
			else {
				minimumContentLengthForGzip = Integer.parseInt(conf.getPropertyForKey(
						ConfigurationRegistry.MINIMUM_GZIP_SIZE));	
			}
		}
		
		// compressed response
		if (contentLength > minimumContentLengthForGzip && clientSupportsGzip(request)) {
			response.addHeader("Content-Encoding", "gzip");
			GZIPOutputStream gzip = new GZIPOutputStream(response.getOutputStream(), contentLength);
			String type = response.getContentType();
			if (!type.contains("charset")) {
				response.setContentType(type + "; charset=utf-8");
			}
			gzip.write(string.getBytes(Charset.forName("UTF-8")));
			gzip.flush();
			gzip.finish();
		} 
		// uncompressed response
		else {
			response.setContentLength(contentLength);
			response.setCharacterEncoding("UTF-8");
			PrintWriter writer = response.getWriter();
			writer.write(string);
			writer.flush();
		}


	}


	@Override
	protected MiniIsolationLayer createIsolationLayer(
			HttpServletRequest request, ServletContext context) {
		MiniIsolationLayer isolationLayer = new SESMiniIsolationLayer(request, context);
		isolationLayer.initialize();
		return isolationLayer;
	}


}
