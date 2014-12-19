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
package org.n52.ses.persistency.streams;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.event.PersistedEvent;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2StreamPersistence extends AbstractStreamPersistence {

	private Connection connection;

	
	private static final Logger logger = LoggerFactory.getLogger(H2StreamPersistence.class);
	
	private static final String TABLE_NAME = "EVENTS";
	private static final String TIMESTAMP_COLUMN = "TIME";
	private static final String TIMESTAMP_COLUMN_TYPE = "TIMESTAMP";
	private static final String VALUE_COLUMN = "VALUE";
	private static final String VALUE_COLUMN_TYPE = "BINARY(256000)";
	private static final String STREAM_NAME_COLUMN = "STREAM_NAME";
	private static final String STREAM_NAME_COLUMN_TYPE = "VARCHAR(255)";
	private static final String ID_COLUMN = "ID";

	private int maxEventCount = 10;

	@Override
	protected void initialize(ISubscriptionManager subMgr, File baseLocation, int maxEvents) throws Exception {
		this.maxEventCount = maxEvents;
		
		Class.forName("org.h2.Driver");
		
		String connString = "jdbc:h2:"+baseLocation.getAbsolutePath()+"/streamPersistence/"+subMgr.getUniqueID();
		this.connection = DriverManager.getConnection(connString);
		this.connection.setAutoCommit(true);
		
		try {
			validateTable();
		}
		catch (IllegalStateException e) {
			logger.warn("Stream database in an illegal state", e.getMessage());
			createTable();
		}
	}
	
	private void createTable() throws SQLException {
		Statement stmt = this.connection.createStatement();
		stmt.execute("CREATE TABLE " +TABLE_NAME+
				"("+ ID_COLUMN+" bigint auto_increment, "+
				TIMESTAMP_COLUMN +" "+TIMESTAMP_COLUMN_TYPE +", "+
				STREAM_NAME_COLUMN +" "+STREAM_NAME_COLUMN_TYPE+", "+
				VALUE_COLUMN +" "+VALUE_COLUMN_TYPE+ ")" );
	}

	private void validateTable() throws SQLException {
		DatabaseMetaData md = this.connection.getMetaData();
		ResultSet rs = md.getTables(null, null, TABLE_NAME, null);
		
		boolean valid = true;
		if (rs.next()) {
			rs.close();
			rs = md.getColumns(null, null, null, TIMESTAMP_COLUMN);
			if (rs.next()) {
				rs.close();
			}
			else {
				valid = false;
			}
			
			rs = md.getColumns(null, null, null, VALUE_COLUMN);
			if (rs.next()) {
				rs.close();
			}
			else {
				valid = false;
			}
			
			rs = md.getColumns(null, null, null, STREAM_NAME_COLUMN);
			if (rs.next()) {
				rs.close();
			}
			else {
				valid = false;
			}
		}
		else {
			throw new IllegalStateException(TABLE_NAME + " not found.");
		}
		
		if (!valid) {
			Statement stmt = this.connection.createStatement();
			stmt.execute("DROP TABLE " +TABLE_NAME+ " CASCADE");
			throw new IllegalStateException("Database malformed.");
		}
	}

	@Override
	public synchronized List<PersistedEvent> getPersistedEvents() {
		List<PersistedEvent> result = new ArrayList<PersistedEvent>();
		Statement stmt;
		try {
			stmt = this.connection.createStatement();
			if (stmt.execute("select "+VALUE_COLUMN+ ", "+STREAM_NAME_COLUMN+" from "+ TABLE_NAME + " ORDER BY "+ID_COLUMN)) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					result.add(new PersistedEvent(rs.getString(2),
							MapEvent.deserialize(new ByteArrayInputStream(rs.getBytes(1)))));
				}
			}
			
		} catch (SQLException e) {
			logger.warn("could not deserialize event", e);
		} catch (IOException e) {
			logger.warn("could not deserialize event", e);
		}
		
		return result;
	}

	@Override
	public synchronized void persistEvent(MapEvent eve, String streamName) {
		logger.debug("Persisting MapEvent: "+eve);
		
		try {
			int number = computeRemainingSize();
			if (number >= 0) {
				removeOldestEvents(number+1);
			}
			
			PreparedStatement prep = this.connection.prepareStatement(
				    "insert into "+ TABLE_NAME +" ("+ TIMESTAMP_COLUMN+ ", "+ STREAM_NAME_COLUMN+ ", "+ VALUE_COLUMN +") values (?,?,?)");
			prep.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
			prep.setString(2, streamName);
			prep.setBytes(3, eve.serialize());
			prep.execute();
		} catch (SQLException e) {
			logger.warn("could not serialize event "+ eve, e);
		} catch (IOException e) {
			logger.warn("could not serialize event "+ eve, e);
		}
	}
	
	private int computeRemainingSize() throws SQLException {
		Statement stmt = this.connection.createStatement();
		stmt.execute("select count(*) from "+TABLE_NAME);
		ResultSet rs = stmt.getResultSet();
		
		if (rs.next()) {
			int count = rs.getInt(1);
			return count - maxEventCount;
		}
		
		return 0;
	}

	private void removeOldestEvents(int count) throws SQLException {
		Statement stmt = this.connection.createStatement();
		stmt.execute("delete from events where ROWNUM() <= "+count);
	}

	@Override
	public void shutdown() throws Exception {
		if (this.connection != null) {
			this.connection.close();
			logger.info("Connection closed.");
		}
		else {
			logger.info("Connection was already null.");
		}
	}
	
	@Override
	public void destroy() throws Exception {
		logger.info("Removing stream persistence database for the calling SubscriptionManager.");
		
		if (this.connection != null) {
			Statement stmt = this.connection.createStatement();
			stmt.execute("DROP ALL OBJECTS DELETE FILES");
		}
		
		shutdown();
	}
	
	@Override
	public int getMaximumEventCount() {
		return maxEventCount;
	}

	@Override
	public void freeResources() {
		try {
			shutdown();
		} catch (Exception e) {
			logger.warn("error while freeing resources!", e);
		}
	}
	
}
