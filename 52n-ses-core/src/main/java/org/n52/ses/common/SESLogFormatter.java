/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
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

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * Standard formatter for the SES logger
 * 
 * Allows a prefix to be set for a service instance
 * 
 * @author Thomas Everding
 *
 */
public class SESLogFormatter extends Formatter{
	
	private StringBuilder sb;
	private DateTime dt;
	private DateTimeFormatter dtf;
	
	private String logPrefix;
	
	/**
	 * 
	 * Constructor
	 * @param prefix prefix for each log message
	 *
	 */
	public SESLogFormatter(String prefix) {
		//set prefix
		this.logPrefix = prefix;
		
		//build date time formatter
		this.buildDateTimeFormatter();
	}

	@Override
	public String format(LogRecord record) {
		this.sb = new StringBuilder();
		
		/*
		 * format each log message like this:
		 * 
		 * 'prefix' 'date and time' 'class with namespace' 'method name'
		 * 'log level' 'log message'
		 */
		this.sb.append(this.logPrefix + " - ");
		
		this.dt = new DateTime(record.getMillis());
		this.sb.append(this.dtf.print(this.dt) + " ");
		
		this.sb.append(record.getSourceClassName() + ".");
		this.sb.append(record.getSourceMethodName() + "(..)");
		
		this.sb.append("\n");
		
		this.sb.append(record.getLevel() + ": ");
		
		this.sb.append(record.getMessage());
		
		//important, otherwise next log starts in this row
		this.sb.append("\n");
		
		return this.sb.toString();
	}
	
	/**
	 * builds the date time formatter
	 */
	private void buildDateTimeFormatter() {
		this.dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
	}

	
	/**
	 * 
	 * @param args the
	 */
	public static void main(String[] args) {
		Logger logger = Logger.getLogger("test");
		logger.info("test");
		
		logger.addHandler(new ConsoleHandler());
		logger.info("tes");
		
		Formatter f = new SESLogFormatter("prefix");
		
		Handler[] handlers = logger.getHandlers();
		Handler handler;
		
		logger.info("" + handlers.length);
		for (int i = 0; i < handlers.length; i++) {
			handler = handlers[i];
			handler.setFormatter(f);
		}
		
		logger.info("test2");
	}
}
