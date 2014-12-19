/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.ses.eml.v001.filter.spatial.methods;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.n52.ses.util.postgres.PostgresConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * @author Matthes Rieke
 *
 */
public class PostGisTransformPoint {
	
	private static final Logger logger = LoggerFactory
			.getLogger(PostGisTransformPoint.class);

	private static final String WGS_SRID = "4326";
	private static final String TARGET_SRID = "93786";

	private static final String SRID_INSERT_STMT = "INSERT into spatial_ref_sys" +
	" (srid, auth_name, auth_srid, proj4text, srtext) values" +
	" ( 93786, 'epsg', 3786, '+proj=eqc +lat_ts=0 +lat_0=0 +lon_0=0 +x_0=0" +
	" +y_0=0 +a=6371007 +b=6371007 +units=m +no_defs '," +
	" 'PROJCS[\"World Equidistant Cylindrical (Sphere)\"," +
	"GEOGCS[\"Unspecified datum based upon the GRS 1980 Authalic Sphere\"," +
	"DATUM[\"Not_specified_based_on_GRS_1980_Authalic_Sphere\"," +
	"SPHEROID[\"GRS 1980 Authalic Sphere\",6371007,0,AUTHORITY[\"EPSG\",\"7048\"]]," +
	"AUTHORITY[\"EPSG\",\"6047\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]]," +
	"UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]]," +
	"AUTHORITY[\"EPSG\",\"4047\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]]," +
	"PROJECTION[\"Equirectangular\"],PARAMETER[\"latitude_of_origin\",0]," +
	"PARAMETER[\"central_meridian\",0],PARAMETER[\"false_easting\",0]," +
	"PARAMETER[\"false_northing\",0],AUTHORITY[\"EPSG\",\"3786\"],AXIS[\"X\",EAST]," +
	"AXIS[\"Y\",NORTH]]');";

	private static final String CHECK_SRID_STMT = "select * from spatial_ref_sys " +
	"where srid="+ TARGET_SRID;

	private Connection conn;
	private boolean hasTargetSRID = false;

	/**
	 * 
	 * Constructor
	 *
	 */
	public PostGisTransformPoint() {
		try {
			initDBConnection();
			initTransfrom();
		} catch (SQLException e) {
			logger.warn(e.getMessage(), e);
		}
	}
	
	
	/**
	 * transforms a Point from WGS 84 to the target system (fixed)
	 * 
	 * @param source the point to transform
	 * 
	 * @return the transformed point
	 */
	public Point transform (Point source) {
		//get wkt from source
		String wkt = source.toText();
		
		//transform and store wkt result
		try {
			wkt = this.invokeQuery(this.createTransformStatement(wkt)).get(0);
		}
		catch (SQLException e) {
			logger.warn(e.getMessage(), e);
			return source;
		}
		try {
			return (Point) new WKTReader().read(wkt);
		}
		catch (Exception e) {
			logger.warn(e.getMessage(), e);
			return source;
		}
	}



	private void initTransfrom() throws SQLException {

		if (!this.hasTargetSRID) {
			List<String> r = invokeQuery(SRID_INSERT_STMT);
			if (r != null) {
				logger.info("Insert of SRID returned null - bad?");
			}
		}

	}

	private void initDBConnection() throws SQLException {

		this.conn = PostgresConnection.getInstance().getConnection();

		List<String> r = invokeQuery(CHECK_SRID_STMT);

		if (r != null) {
			for (String string : r) {
				if (string.equals(TARGET_SRID)) {
					this.hasTargetSRID = true;
				}
			}	
		}
		
	}

	private List<String> invokeQuery(String query) throws SQLException {
		if (this.conn != null) {

			ArrayList<String> result = new ArrayList<String>();
			Statement st = this.conn.createStatement();
			ResultSet rs = st.executeQuery(query);

			while (rs.next()) {
				result.add(rs.getString(1));
			}

			rs.close();
			st.close();
			return result;
		}
		
		return null;
	}

	
	private String createTransformStatement(String wktGeom) {
		StringBuilder sb = new StringBuilder();

		sb.append("select AsText(");
		sb.append("transform(");
		sb.append("GeometryFromText('"+ wktGeom +"', "+ WGS_SRID +"), ");
		sb.append(TARGET_SRID);
		sb.append("), "); //close transform 2
		sb.append(")"); //close AsText

		return sb.toString();
	}

}
