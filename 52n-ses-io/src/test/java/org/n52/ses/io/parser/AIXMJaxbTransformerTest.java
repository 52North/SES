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
package org.n52.ses.io.parser;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.n52.epos.event.EposEvent;
import org.n52.epos.transform.TransformationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class AIXMJaxbTransformerTest {
	
	@Test
	public void testParsing() throws SAXException, IOException,
			ParserConfigurationException, TransformationException {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true);
		DocumentBuilder builder = fac.newDocumentBuilder();
		
		Document doc = builder.parse(readXml());
		
		AIXMJaxbTransformer parser = new AIXMJaxbTransformer();
		Assert.assertTrue(parser.supportsInput(doc.getDocumentElement()));
		
		EposEvent event = parser.transform(doc.getDocumentElement());
		
		DateTime targetStart = new DateTime("2011-08-01T00:00:00.000Z");
		Assert.assertTrue(event.getStartTime() == targetStart.getMillis());
		
		Assert.assertEquals(event.getValue("interpretation"), "TEMPDELTA");
		Assert.assertEquals(event.getValue("identifier"), "BCB4F904-9AEA-4F2E-97AE-2D4A4344BD6C");
		Assert.assertEquals(event.getValue("theEvent"), "BCB4F904-9AEA-4F2E-97AE-2D4A4344BD6B");
	}

	private InputStream readXml() {
		return getClass().getResourceAsStream("Navaid.xml");
	}

}
