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
package org.n52.ses.persistency;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.api.event.MapEvent;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class MapEventSerializationTest {

	@Test
	public void testSerialization() throws XmlException, IOException, ClassNotFoundException {
		MapEvent me = new MapEvent(2, 3);
		me.put(MapEvent.ORIGNIAL_MESSAGE_KEY, XmlObject.Factory.parse("<test><content>123</content></test>"));
		me.put(MapEvent.DOUBLE_VALUE_KEY, 23.5);
		me.put(MapEvent.SENSORID_KEY, "theSensor");
		GeometryFactory gf = new GeometryFactory();
		Point geom = gf.createPoint(new Coordinate(52.1, 7.44112233));
		me.put(MapEvent.GEOMETRY_KEY, geom);
		
		Object o = MapEvent.deserialize(new ByteArrayInputStream(me.serialize()));
		
		Assert.assertTrue(o instanceof MapEvent);
		MapEvent ome = (MapEvent) o;
		Assert.assertTrue(XmlObject.Factory.parse("<test><content>123</content></test>").valueEquals((XmlObject) ome.get(MapEvent.ORIGNIAL_MESSAGE_KEY)));
		Assert.assertTrue((Double) ome.get(MapEvent.DOUBLE_VALUE_KEY) == 23.5);
		Assert.assertTrue(ome.get(MapEvent.SENSORID_KEY).equals("theSensor"));
		Assert.assertTrue((Long) ome.get(MapEvent.START_KEY) == 2);
		Assert.assertTrue((Long) ome.get(MapEvent.END_KEY) == 3);
		Assert.assertTrue(((Point) ome.get(MapEvent.GEOMETRY_KEY)).equals(geom));
	}
	
}
