/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
