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
package org.n52.ses.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.util.common.SESProperties;


/**
 * Servlet which provides access to all properties of the
 * {@link ConfigurationRegistry} (per se, ses_config.properties).
 * 
 * It should be wrapped with a authentication mechanism inside the
 * web.xml.
 * 
 * @author matthes rieke <m.rieke@52north.org>
 *
 */
public class ServiceConfigurationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8033668772111543668L;
	private static Set<Object> secretParameters;
	private static Set<Object> hiddenParameters;
	private static Set<Object> optionParameters;
	private String configDefaultsRowMarkup;
	private String configRowMarkup;
	private String html;
	private static final String KEY_STRING = "${key}";
	private static final String VALUE_STRING = "${value}";
	private static final String TYPE_STRING = "${type}";
	private static final String NOTE_STRING = "${note}";
	private static final String ROW_MARKUP = "<tr><td class=\"config_label\">"+KEY_STRING+"</td><td>" +
			"<input type=\""+TYPE_STRING+"\" name=\""+KEY_STRING+"\" value=\""+VALUE_STRING+"\" /></td>" +
					"<td>${note}</td></tr>"; 

	static {
		/*
		 * set some parameters as secret/hidden
		 */
		secretParameters = new HashSet<Object>();
		secretParameters.add(ConfigurationRegistry.POSTGRES_PWD_KEY);
		secretParameters.add(ConfigurationRegistry.BASIC_AUTH_PASSWORD);

		hiddenParameters = new HashSet<Object>();
		hiddenParameters.add(ConfigurationRegistry.USED_FILTER_ENGINE);
		
		optionParameters = new HashSet<Object>();
		optionParameters.add(ConfigurationRegistry.EML_CONTROLLER);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		URL f = getClass().getResource("/"+ConfigurationRegistry.CONFIG_FILE);
		File file;
		try {
			file = new File(f.toURI());
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		}
		
		FileInputStream fis = new FileInputStream(file);
		
		Properties props = new SESProperties();
		props.load(fis);

		if (req.getParameterMap().size() == 0) {
			/*
			 * return config page
			 */
			if (this.html == null) {
				InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream("/config_page.html"));
				BufferedReader br = new BufferedReader(isr);
				StringBuilder sb = new StringBuilder();

				while (br.ready()) {
					sb.append(br.readLine());
				}
				html = sb.toString();
				isr.close();
			}

			generateRowMarkup(props);
			/*
			 * replace the rows
			 */
			synchronized (this) {
				String configPage = html.replace("${config_rows}", this.configRowMarkup);
				configPage = configPage.replace("${default_rows}", this.configDefaultsRowMarkup);

				String reqUrl = URLDecoder.decode(req.getRequestURL().toString(),
						(req.getCharacterEncoding() == null ? Charset.defaultCharset().name() : req.getCharacterEncoding()));
				String sesUrl = reqUrl.substring(0,
						reqUrl.indexOf(req.getContextPath())) +
						req.getContextPath();
				
				configPage = configPage.replace("${SES_URL}", sesUrl).replace("${SES_CONFIG}", req.getServletPath());
				
				resp.setContentType("text/html");
				resp.getWriter().print(configPage.replace("${action}", req.getRequestURL().substring(0,
						req.getRequestURL().indexOf(req.getServletPath()))+req.getServletPath()));
			}

		}

	}

	private void generateRowMarkup(Properties props) {

		/*
		 * go through the underlying keyset.
		 * this holds all user-setted parameters
		 */
		List<String> list = new ArrayList<String>(props.stringPropertyNames());
		Collections.sort(list);

		/*
		 * go through the all. check if default or not.
		 */
		StringBuilder cfgSb = new StringBuilder();
		StringBuilder defSb = new StringBuilder();
		
		for (String key : list) {
			if (hiddenParameters.contains(key)) continue;
			boolean secret = secretParameters.contains(key);
			
			if (props.keySet().contains(key)) {
				if (optionParameters.contains(key)) {
					cfgSb.append(generateOneOptionMarkup(key.toString(), props.getProperty(key)));
				} else {
					cfgSb.append(generateOneRowMarkup(key.toString(), props.getProperty(key), secret));	
				}
			} else {
				if (optionParameters.contains(key)) {
					defSb.append(generateOneOptionMarkup(key.toString(), props.getProperty(key)));
				} else {
					defSb.append(generateOneRowMarkup(key, props.getProperty(key), secret));
				}
			}
		}
		
		this.configRowMarkup = cfgSb.toString();
		this.configDefaultsRowMarkup = defSb.toString();
	}

	private String generateOneOptionMarkup(String key, Object value) {
		List<String> options = getOptionValuesForKey(key);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<tr><td class=\"config_label\">");
		sb.append(key);
		sb.append("</td><td>");
		sb.append("<select name=\"");
		sb.append(key);
		sb.append("\">");
		for (String op : options) {
			sb.append("<option value=\"");
			sb.append(op);
			sb.append("\"");
			if (value.toString().equals(op)){
				sb.append(" selected=\"selected\"");
			}
			sb.append(">");
			sb.append(op);
			sb.append("</option>");
		}
		sb.append("</select></td><td></td></tr>");
		return sb.toString();
	}

	private List<String> getOptionValuesForKey(String key) {
		List<String> result = new ArrayList<String>();
		
		if (key.equals(ConfigurationRegistry.EML_CONTROLLER)) {
			result.add(ConfigurationRegistry.EML_001_IMPL);
			result.add(ConfigurationRegistry.EML_002_IMPL);
		}
		
		return result;
	}

	private String generateOneRowMarkup(String key, Object value, boolean secret) {
		return ROW_MARKUP.replace(KEY_STRING, key).replace(VALUE_STRING,
				secret ? "" : (value == null) ? "" : value.toString())
				.replace(TYPE_STRING, secret ? "password" : "text")
				.replace(NOTE_STRING, secret ? "(security-related parameter, not provided; leave blank if you do not want to change it.)" : "");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		URL f = getClass().getResource("/"+ConfigurationRegistry.CONFIG_FILE);
		File file;
		try {
			file = new File(f.toURI());
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		}
		
		FileInputStream fis = new FileInputStream(file);
		
		Properties props = new SESProperties();
		props.load(fis);

	
		boolean changed = false;
		synchronized (this) {
			/*
			 * check if we have any changes
			 */
			Map<?, ?> map = req.getParameterMap();
			String[] tmp;
			for (Object key : map.keySet()) {
				String str;
				if (key != null && key instanceof String) {
					str = (String) key;
				} else continue;

				Object val = map.get(str);
				String strVal;
				if (val instanceof String[]) {
					tmp = (String[]) val;
					if (tmp.length > 0 ) {
						strVal = tmp[0];
					} else continue;
				} else continue;
				
				/*
				 * is it a hidden one and is it empty?
				 * then the user did not change it
				 */
				if (secretParameters.contains(str) && strVal.trim().isEmpty()) {
					continue;
				}
				
				if (!strVal.equals(props.getProperty(str))) {
					changed = true;
					props.put(str, strVal);
				
				}
			}
			if (changed) {
				FileWriter fw = new FileWriter(file);
				props.store(fw, null);
				
				String reqUrl = URLDecoder.decode(req.getRequestURL().toString(),
						(req.getCharacterEncoding() == null ? Charset.defaultCharset().name() : req.getCharacterEncoding()));
				String reloadLink = reqUrl.substring(0,
						reqUrl.indexOf(req.getContextPath())) +
						"/manager/html/reload?path="+ req.getContextPath();
				resp.setContentType("text/html");
				resp.getWriter().append("<html><body><p>succesfully changed. a restart of the service is needed to make changes affect.</p>" +
						"<p><a href=\""+ reloadLink +"\">Click here to reload the SES webapp.</a></p></body></html>");
			} else {
				resp.setContentType("text/html");
				resp.getWriter().append("<html><body><p>no changes.</p></body></html>");
			}
		}
		

	}




}
