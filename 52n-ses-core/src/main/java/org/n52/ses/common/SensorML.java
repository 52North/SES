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
package org.n52.ses.common;
import javax.xml.namespace.QName;

import org.apache.muse.ws.resource.impl.AbstractWsResourceCapability;
import org.n52.ses.api.common.SensorMLConstants;
import org.w3c.dom.Element;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 *
 */
public class SensorML extends AbstractWsResourceCapability implements ISensorML {

	QName[] PROPERTIES = new QName[] {SensorMLConstants.SENSORML
	};
	private Element sensorML;
	
	/* (non-Javadoc)
	 * @see org.n52.ses.common.ISensorML#getSensorML()
	 */
	@Override
	public Element getSensorML() {
		return this.sensorML;
	}

	/* (non-Javadoc)
	 * @see org.n52.ses.common.ISensorML#setSensorML(org.w3c.dom.Element)
	 */
	@Override
	public void setSensorML(Element param0) {
		this.sensorML = param0;
	}
	

	@Override
	public QName[] getPropertyNames() {
		return this.PROPERTIES;
	}
}
