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
package org.n52.ses.eml.v002.filterlogic.esper.customFunctions.test;


import org.n52.ses.eml.v002.filterlogic.esper.customFunctions.SpatialMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Thomas Everding
 *
 */
public class SpatialMethodsTest {
	
	private static final Logger logger = LoggerFactory
			.getLogger(SpatialMethodsTest.class);

	/**
	 * @param args args
	 */
	public static void main(String[] args) {
		String g0 = "POLYGON ((34.0006359157073 -87.0006359157073, 34.0004996357652 -87.0007477577654, 34.0003441551008 -87.000830863912, 34.0001754487489 -87.0008820404243, 34 -87.0008993206178, 33.9998245512511 -87.0008820404243, 33.9996558448992 -87.000830863912, 33.9995003642348 -87.0007477577654, 33.9993640842927 -87.0006359157073, 33.9992522422346 -87.0004996357652, 33.999169136088 -87.0003441551008, 33.9991179595757 -87.0001754487489, 33.9991006793822 -87, 33.9991006793822 -86, 33.9991179595757 -85.9998245512511, 33.999169136088 -85.9996558448992, 33.9992522422346 -85.9995003642348, 33.9993640842927 -85.9993640842927, 33.9995003642348 -85.9992522422346, 33.9996558448992 -85.9991691360881, 33.9998245512511 -85.9991179595757, 34 -85.9991006793822, 35 -85.9991006793822, 35.0001754487489 -85.9991179595757, 35.0003441551008 -85.9991691360881, 35.0004996357652 -85.9992522422346, 35.0006359157073 -85.9993640842927, 35.0007477577654 -85.9995003642348, 35.000830863912 -85.9996558448992, 35.0008820404243 -85.9998245512511, 35.0008993206178 -86, 35.0008820404243 -86.0001754487489, 35.000830863912 -86.0003441551008, 35.0007477577654 -86.0004996357652, 35.0006359157073 -86.0006359157073, 34.0006359157073 -87.0006359157073))";
		String g00 = "POLYGON ((34 -87, 34 -86, 35 -86, 34 -87))";
		String g01 = "POLYGON ((34.0006359157073 -87.0006359157073, 34.0004996357652 -87.0007477577654, 34.0003441551008 -87.000830863912, 34.0001754487489 -87.0008820404243, 34 -87.0008993206178, 33.9998245512511 -87.0008820404243, 33.9996558448992 -87.000830863912, 33.9995003642348 -87.0007477577654, 33.9993640842927 -87.0006359157073, 33.9992522422346 -87.0004996357652, 33.999169136088 -87.0003441551008, 33.9991179595757 -87.0001754487489, 33.9991006793822 -87, 33.9991006793822 -86, 33.9991179595757 -85.9998245512511, 33.999169136088 -85.9996558448992, 33.9992522422346 -85.9995003642348, 33.9993640842927 -85.9993640842927, 33.9995003642348 -85.9992522422346, 33.9996558448992 -85.9991691360881, 33.9998245512511 -85.9991179595757, 34 -85.9991006793822, 35 -85.9991006793822, 35.0001754487489 -85.9991179595757, 35.0003441551008 -85.9991691360881, 35.0004996357652 -85.9992522422346, 35.0006359157073 -85.9993640842927, 35.0007477577654 -85.9995003642348, 35.000830863912 -85.9996558448992, 35.0008820404243 -85.9998245512511, 35.0008993206178 -86, 35.0008820404243 -86.0001754487489, 35.000830863912 -86.0003441551008, 35.0007477577654 -86.0004996357652, 35.0006359157073 -86.0006359157073, 34.0006359157073 -87.0006359157073))";
		String g1 = "POINT (34.739429354667664 -86.72852575778961)";

		boolean intersects = SpatialMethods.intersects(g01, g1);
		logger.info("g1 intersects: " + intersects);
		logger.info("g1 intersects: " + SpatialMethods.intersects(g00, g0));
	}

}