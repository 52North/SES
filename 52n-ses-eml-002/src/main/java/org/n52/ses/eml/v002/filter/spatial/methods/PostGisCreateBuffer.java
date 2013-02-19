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
package org.n52.ses.eml.v002.filter.spatial.methods;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.n52.oxf.conversion.unit.NumberWithUOM;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.n52.ses.util.postgres.PostgresConnection;
import org.n52.ses.api.IUnitConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * @author Matthes Rieke
 *
 */
public class PostGisCreateBuffer implements ICreateBuffer {
	
	private static final Logger logger = LoggerFactory
			.getLogger(PostGisCreateBuffer.class);

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
	private IUnitConverter converter;

	/**
	 * 
	 * Constructor
	 *
	 */
	public PostGisCreateBuffer() {
		this.converter = ConfigurationRegistry.getInstance().getUnitConverter();

		try {
			initDBConnection();
			initTransfrom();
		} catch (SQLException e) {
			
			StringBuilder sb = new StringBuilder();
			
			for (StackTraceElement ste : e.getStackTrace()) {
				sb.append("\n" + ste.toString());
			}
			PostGisCreateBuffer.logger.warn(sb.toString());
		}
	}


	/* (non-Javadoc)
	 * @see de.ifgi.lehre.thesisEverding.eml.filter.spatial.methods.ICreateBuffer#buffer(com.vividsolutions.jts.geom.Geometry, java.lang.Double, java.lang.String, java.lang.String)
	 */
	@Override
	public Geometry buffer(Geometry geom, double distance, String ucumUom,
			String crs) {

		String wktString = geom.toText();

		NumberWithUOM values = this.converter.convert(ucumUom, distance);

		if (!values.getUom().equals("m")) {
			return null;
		}

		List<String> resultSet = null;

		try {
			resultSet = invokeQuery(createBufferStatement(wktString, values.getValue() ));
		} catch (NumberFormatException e1) {
			logger.warn(e1.getMessage(), e1);
		} catch (SQLException e1) {
			logger.warn(e1.getMessage(), e1);
		}

		if (resultSet != null) {
			wktString = resultSet.get(0);
		} else {
			logger.warn("Buffer with postgis failed - using old geometry (unbuffered).");
		}
		

		Geometry newGeom = null;
		try {
			newGeom = new WKTReader().read(wktString);
		} catch (com.vividsolutions.jts.io.ParseException e) {
			logger.warn(e.getMessage(), e);
		}

		return newGeom;
	}



	private void initTransfrom() throws SQLException {
//		logger.info("initializing transformation via postgis...");

		if (!this.hasTargetSRID) {
			List<String> r = invokeQuery(SRID_INSERT_STMT);
			if (r != null) {
				PostGisCreateBuffer.logger.info("Insert of SRID returned null - bad?");
			}
		}
//		logger.info("... transformation initialized");
	}

	private void initDBConnection() throws SQLException {
		this.conn = PostgresConnection.getInstance().getConnection();

		List<String> r = invokeQuery(CHECK_SRID_STMT);

//		StringBuilder log = new StringBuilder();
//		log.append("SRID check:");
//		log.append("\n\ttarget SRID: " + TARGET_SRID);
//		log.append("\n\twgs SRID: " + WGS_SRID);
		if (r != null) {
			for (String string : r) {
//				log.append("\n\tSRID: " + string);
				if (string.equals(TARGET_SRID)) {
					this.hasTargetSRID = true;
				}
			}	
		}
//		log.append("\n\thas target SRID: " + hasTargetSRID);
//		logger.info(log.toString());
		PostGisCreateBuffer.logger.debug("... connection initalized");
	}

	private List<String> invokeQuery(String query) throws SQLException {
		PostGisCreateBuffer.logger.debug("invoking pstgis query:\n\t" + query);
		if (this.conn != null) {

			ArrayList<String> result = new ArrayList<String>();
			Statement st = this.conn.createStatement();
			ResultSet rs = st.executeQuery(query);

			StringBuilder log = new StringBuilder();
			log.append("query result:");
			while (rs.next()) {
				//TODO why iterate when always selecting string number 1?
				result.add(rs.getString(1));
				log.append("\n\t" + rs.getString(1));
			}
			PostGisCreateBuffer.logger.debug(log.toString());

			rs.close();
			st.close();
			return result;
		}
		PostGisCreateBuffer.logger.warn("connection is null");
		return null;
	}

	private String createBufferStatement(String wktGeom, double distMeter) {
		StringBuilder sb = new StringBuilder();

		sb.append("select AsText(");
		sb.append(" transform(");
		sb.append(" buffer(");
		sb.append(" transform(");
		sb.append("GeometryFromText('"+ wktGeom +"', "+ WGS_SRID +"), ");
		sb.append(TARGET_SRID);

		sb.append("), "); //close transform 2
		sb.append(distMeter +"), "+ WGS_SRID); //close buffer
		sb.append(")"); //close transform 1
		sb.append(")"); //close AsText

		return sb.toString();
	}

}
