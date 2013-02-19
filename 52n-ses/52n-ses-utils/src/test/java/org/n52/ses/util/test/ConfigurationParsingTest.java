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
package org.n52.ses.util.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.n52.ses.util.common.SESProperties;

public class ConfigurationParsingTest {

	@Test
	public void testConfigParsingAndWriting() throws IOException {
		SESProperties props = new SESProperties();
		props.load(getClass().getResourceAsStream("ses_config_test.xml"));
		Assert.assertTrue("No parsers found", props.getRegisteredParsers() != null && props.getRegisteredParsers().size() > 0);
		
		props.setProperty("testWriter", "works");
		
		File tmp = File.createTempFile("config", ".tmp");
		tmp.deleteOnExit();
		FileWriter fw = new FileWriter(tmp);
		
		props.store(fw, null);
		Assert.assertTrue("Could not write Configuration!", tmp.exists() && tmp.length() >= 0);
	}
	
}
