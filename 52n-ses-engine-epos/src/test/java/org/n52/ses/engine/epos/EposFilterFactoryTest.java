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
package org.n52.ses.engine.epos;

import java.io.IOException;


import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.resource.basefaults.BaseFault;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.api.ws.EngineCoveredFilter;
import org.w3c.dom.Element;

import static org.hamcrest.CoreMatchers.*;

public class EposFilterFactoryTest {
	
	@Test
	public void shouldCreateXPathFilter() throws BaseFault, XmlException, IOException {
		EposFilterFactory fac = new EposFilterFactory();
		
		Filter filter = fac.newInstance(readFilter("xpath-filter.xml"));
		
		Assert.assertThat(filter, is(instanceOf(EngineCoveredFilter.class)));
	}
	
	@Test
	public void shouldCreateFESFilter() throws BaseFault, XmlException, IOException {
		EposFilterFactory fac = new EposFilterFactory();
		
		Filter filter = fac.newInstance(readFilter("fes-filter.xml"));
		
		Assert.assertThat(filter, is(instanceOf(EngineCoveredFilter.class)));
	}

	private Element readFilter(String file) throws XmlException, IOException {
		XmlObject xo = XmlObject.Factory.parse(getClass().getResourceAsStream(file));
		return (Element) xo.getDomNode().getFirstChild();
	}


}
