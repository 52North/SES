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
package org.n52.ses.util.geometry;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Helper class provides geodesic approximation methods.
 * 
 * E.g. approximate a Great Circle to a LineString representation (the
 * algorithm uses the intermediate point calculation documented at
 * {@link http://williams.best.vwh.net/avform.htm#Intermediate}).
 * 
 * @author matthes rieke
 *
 */
public class GeodesicApproximationTools {

	private static double radTodeg(double n) {
		return n * Math.PI/180;
	}

	private static Coordinate intermediatePoint(Coordinate start, Coordinate end, double fraction) {
		double lat1 = radTodeg(start.y);
		double lon1 = radTodeg(start.x);
		double lat2 = radTodeg(end.y);
		double lon2 = radTodeg(end.x);

		double d = 2 * Math.asin(
				Math.sqrt(Math.pow((Math.sin((lat1 - lat2) / 2)), 2) +
						Math.cos(lat1) * Math.cos(lat2) *
						Math.pow(Math.sin((lon1-lon2) / 2), 2)));
		double a = Math.sin((1 - fraction) * d) / Math.sin(d);
		double b = Math.sin(fraction * d) / Math.sin(d);
		double x = a * Math.cos(lat1) * Math.cos(lon1) + b *
				Math.cos(lat2) * Math.cos(lon2);
		double y = a * Math.cos(lat1) * Math.sin(lon1) + b *
				Math.cos(lat2) * Math.sin(lon2);
		double z = a * Math.sin(lat1) + b * Math.sin(lat2);
		double lat = Math.atan2(z, Math.sqrt(Math.pow(x, 2) +
				Math.pow(y, 2))) * 180 / Math.PI;
		double lng = Math.atan2(y, x) * 180 / Math.PI;

		return new Coordinate(lng, lat);
	}


	private static Coordinate[] approximateGreatCircle(Coordinate[] result, int subListStart, int subListEnd) {
		Coordinate midPoint = intermediatePoint(result[subListStart], result[subListEnd], 0.5);
		int targetIndex = (subListEnd+subListStart)/2;
		
		if (result[targetIndex] == null) {
			result[targetIndex] = midPoint;
			approximateGreatCircle(result, subListStart, targetIndex);
			approximateGreatCircle(result, targetIndex, subListEnd);
		}
		
		return result;
	}
	
	public static LineString approximateGreatCircle(int segmentsPerHalf, Coordinate start, Coordinate end) {
		Coordinate[] result = new Coordinate[segmentsPerHalf * 2 +1];
		result[0] = start;
		result[result.length-1] = end;
		return new GeometryFactory().createLineString(approximateGreatCircle(result, 0, result.length-1));
	}
	
	public static void main(String[] args) {
		System.out.println(approximateGreatCircle(20, new Coordinate(-87.90381968189912,41.97626011616167), new Coordinate(24.8242444289068, 59.41329527536156)).toText());
	}
	

}
