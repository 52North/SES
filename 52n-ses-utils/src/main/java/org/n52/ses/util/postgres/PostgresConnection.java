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