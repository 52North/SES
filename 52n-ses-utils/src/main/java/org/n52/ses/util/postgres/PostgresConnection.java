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
package org.n52.ses.util.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Connection to a postgres data base. 
 * 
 * Singleton
 *
 */
public class PostgresConnection {
	
	private Connection connection;
	private static PostgresConnection _instance;
	
	/**
	 * Object serves as lock. 
	 * Needed in the constructor as
	 * 'synchronized(_instance)' does 
	 * block permanently.
	 * (Re-entrant seems not working between 
	 * static and non static context)
	 */
	private static Object lock = new Object();
	
	private String user;
	private String password;
	private String database;
	private String port;
	private String host;
	
	private static final Logger logger = LoggerFactory
			.getLogger(PostgresConnection.class);
	
	/**
	 * 
	 * private Constructor
	 *
	 */
	private PostgresConnection() {
		synchronized (lock) {
			ConfigurationRegistry registry = ConfigurationRegistry.getInstance();
			if (registry != null) {
				this.user = registry.getPropertyForKey(ConfigurationRegistry.POSTGRES_USER_KEY);
				this.password = registry.getPropertyForKey(ConfigurationRegistry.POSTGRES_PWD_KEY);
				this.database = registry.getPropertyForKey(ConfigurationRegistry.POSTGRES_DATABASE);
				this.port = registry.getPropertyForKey(ConfigurationRegistry.POSTGRES_PORT_KEY);
				this.host = registry.getPropertyForKey(ConfigurationRegistry.POSTGRES_HOST_KEY);
			}
			try {
				Class.forName("org.postgresql.Driver");
			}
			catch (ClassNotFoundException e) {
				PostgresConnection.logger.warn(e.getMessage());
				
				StringBuilder sb = new StringBuilder();
				
				for (StackTraceElement ste : e.getStackTrace()) {
					sb.append("\n" + ste.toString());
				}
				
				PostgresConnection.logger.warn(sb.toString());
			}
		}
	}
	
	
	/**
	 * 
	 * @return the single instance of this class
	 */
	public static PostgresConnection getInstance() {
		synchronized (lock) {
			if (_instance == null) {
				_instance = new PostgresConnection();
			}
			return _instance;
		}
	}
	
	
	/**
	 * 
	 * @return the connection to the data base
	 */
	public Connection getConnection() {
		
		if (this.connection == null) {
			String url = "";
			if (this.port.equals("default")) {
				url = "jdbc:postgresql://"+this.host+ "/"+ this.database;	
			} else {
				url = "jdbc:postgresql://"+this.host+ ":"+ this.port +"/"+ this.database;
			}
			
			Properties props = new Properties();
			props.setProperty("user",this.user);
			props.setProperty("password",this.password);

			try {
				PostgresConnection.logger.info("try to open connection to DB\n\turl: " + url + "\n\tuser: " + this.user + "\n\tpassword: " + this.password);
				this.connection = DriverManager.getConnection(url, props);
			} catch (SQLException e) {
				PostgresConnection.logger.warn(e.getMessage(), e);
			}
		}
		return this.connection;
	}
}