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
package org.n52.ses.util.filter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;


/**
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 */
public class SpatialMethods {

	private static final Logger logger = LoggerFactory
	.getLogger(SpatialMethods.class);


	/**
	 * Calculates the distance between two points
	 * 
	 * @param f from point
	 * @param t to point
	 * 
	 * @return the distance (if possible) or -1
	 */
	public static double distanceTo(Object f, Object t) {
		if  (f == null || t == null) return -1;
		
		if (!(f instanceof Geometry) || !(t instanceof Geometry)) return -1;
		
		Geometry from = (Point) f;
		Geometry to = (Point) t;
		double distance = from.distance(to);

		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("distanceTo result:");
			sb.append("\n\tfrom: " + from.toText());
			sb.append("\n\tto:   " + to.toText());
			sb.append("\n\tdistance: " + distance);
			logger.debug(sb.toString());	
		}

		return distance;
	}


	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @return <code>true</code> if the first geometry contains the second
	 */
	public static boolean contains(Geometry geom, Geometry g) {
		if  (geom == null || g == null) return false;
		return geom.contains(g);
	}


	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @return <code>true</code> if the first geometry crosses the second
	 */
	public static boolean crosses(Geometry geom, Geometry g) {
		if  (geom == null || g == null) return false;
		return geom.crosses(g);
	}


	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @return <code>true</code> if the first geometry and the second are disjoint
	 */
	public static boolean disjoint(Geometry geom, Geometry g) {
		if  (geom == null || g == null) return false;
		return geom.disjoint(g);
	}

	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @return <code>true</code> if the first geometry and the second are not disjoint
	 */
	public static boolean bbox(Geometry geom, Geometry g) {
		if  (geom == null || g == null) return false;
		return !geom.disjoint(g);
	}

	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @return <code>true</code> if the first geometry equals the second
	 */
	public static boolean equals(Geometry geom, Geometry g) {
		if  (geom == null || g == null) return false;
		return geom.equals(g);
	}

	//TODO: make extract geometry private method, use it everywhere

	/**
	 * @param g0 first geometry
	 * @param g1 second geometry
	 * 
	 * @return <code>true</code> if the first geometry intersects the second
	 */
	public static boolean intersects(Object g0, Object g1) {
		if  (g0 == null || g1 == null) return false;
		Geometry geom;
		Geometry g;

		//extract geometry 0
		geom = extractGeometry(g0);
		g = extractGeometry(g1);

		if  (geom == null || g == null) return false;
		return geom.intersects(g);
	}

	private static Geometry extractGeometry(Object g0) {
		if (g0 instanceof Geometry) {
			//geometry given -> cast
			return (Geometry) g0;
		}
		else {
			//parse string
			WKTReader r = new WKTReader();
			try {
				return r.read(g0.toString());
			}
			catch (ParseException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return null;
	}


	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @param distance distance
	 * @return <code>true</code> if the first geometry is within a given distance of the second
	 */
	public static boolean distanceWithin(Geometry geom, Geometry g, double distance) {
		if  (geom == null || g == null) return false;
		return geom.isWithinDistance(g, distance);
	}

	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @param distance the distance
	 * @return <code>true</code> if the first geometry is beyond a given distance to the second (!withinDistance)
	 */
	public static boolean beyond(Geometry geom, Geometry g, double distance) {
		if  (geom == null || g == null) return false;
		return !geom.isWithinDistance(g, distance);
	}

	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @return <code>true</code> if the first geometry overlaps the second
	 */
	public static boolean overlaps(Geometry geom, Geometry g) {
		if  (geom == null || g == null) return false;
		return geom.overlaps(g);
	}

	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @return <code>true</code> if the first geometry touches the second
	 */
	public static boolean touches(Geometry geom, Geometry g) {
		if  (geom == null || g == null) return false;
		return geom.touches(g);
	}

	/**
	 * @param geom first geometry
	 * @param g second geometry
	 * @return <code>true</code> if the first geometry is within the second
	 */
	public static boolean within(Geometry geom, Geometry g) {
		if  (geom == null || g == null) return false;
		return geom.within(g);
	}


	/**
	 * 
	 * @param geomAsWkt WKT representation of a geometry
	 * 
	 * @return the JTS representation of the geometry
	 * 
	 * @throws ParseException exception while parsing the WKT
	 */
	public static Geometry fromWKT(String geomAsWkt) throws ParseException {
		WKTReader wktReader = new WKTReader(); //TODO: consider use of GeometryFactory
		return wktReader.read(geomAsWkt);
	}


	/**
	 * 
	 * @param geom JTS geometry representation
	 * 
	 * @return The WKT representation of the geometry
	 * 
	 * @throws ParseException error while creating the WKT
	 */
	public static String toWKT(Geometry geom) throws ParseException {
		WKTWriter wktWriter = new WKTWriter(); //TODO: consider use of GeometryFactory
		return wktWriter.write(geom);
	}

	/**
	 * test main
	 * @param args none
	 */
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		String test = "LINESTRING (-97.03833333 32.89666667, -96.995 32.78333333, -96.94 32.765, -96.83166667 32.76833333, -96.33166667 32.97, -95.56333333 33.075, -94.07333333 33.51333333, -92.64333333 34.06, -92.55 34.095, -89.98333333 35.015, -85.18166667 36.61333333, -84.56666667 36.80166667, -84.05 36.95666667, -83.03 37.255, -82.13666667 37.505, -81.80666667 37.59666667, -81.12333333 37.78, -80.39 38.04333333, -79.73166667 38.27166667, -78.99833333 38.50666667, -77.46666667 38.935, -76.97833333 39.495, -76.29166667 40.12, -75.68333333 40.58166667, -75.455 40.72666667, -74.86833333 40.995, -73.82166667 41.665, -72.71666667 42.16166667, -70.61333333 43.425, -69.49166667 43.925, -67.15666667 44.90166667, -65.87166667 45.40666667, -64.57166667 46.18833333, -61.77333333 47.43, -58.67 48.58333333, -55 49.71666667, -52.06833333 50.50333333, -50 52, -40 56, -30 59, -20 60, -10 60, -9.5 59.965, -8.568333333 60.02, -1.286666667 59.87833333, 0.015 59.99166667, 5.211666667 60.31166667, 7.5 60.295, 9.915 60.23666667, 11.075 60.19166667, 12.51333333 60.12833333, 14.17 60.03, 14.40833333 60.01833333, 16.73833333 59.845, 16.99 59.825, 17.91833333 59.65166667)";

	}
}
