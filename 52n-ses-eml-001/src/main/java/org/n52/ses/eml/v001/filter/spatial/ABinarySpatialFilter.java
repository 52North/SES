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
package org.n52.ses.eml.v001.filter.spatial;


import net.opengis.fes.x20.BinarySpatialOpType;

import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.GMLParseException;
import org.n52.ses.eml.v001.filter.expression.LiteralExpression;
import org.n52.ses.eml.v001.filterlogic.esper.customFunctions.MethodNames;
import org.n52.ses.io.parser.GML32Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;


/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public abstract class ABinarySpatialFilter extends ASpatialFilter {

	/**
	 * the operator of this instance 
	 */
	protected BinarySpatialOpType bsOperator;

	/**
	 * the global logger
	 */
	private static final Logger logger = LoggerFactory
			.getLogger(ABinarySpatialFilter.class);

	/**
	 * 
	 * Constructor
	 *
	 * @param bsOp FES binary spatial operator
	 */
	public ABinarySpatialFilter(BinarySpatialOpType bsOp) {
		this.bsOperator = bsOp;
	}

	/**
	 * @param methodName the java method name of the spatial filter
	 * @return the expression string
	 */
	protected String createExpressionForBinaryFilter(String methodName) {
		Geometry geom = null;

		XmlObject[] literals = this.bsOperator.selectChildren(LiteralExpression.FES_2_0_LITERAL_NAME);

		if (literals.length > 1) {
			logger.warn("Multiple fes:Literal in expression. using the first.");
		}
		if (literals.length >= 1) {

			XmlObject[] children = XmlUtil.selectPath("./*", literals[0]);

			if (children.length > 1) {
				logger.warn("Multiple children in fes:Literal. using the first.");
			}
			if (children.length >= 1) {
				try {
					geom = GML32Parser.parseGeometry(children[0]);
				} catch (ParseException e) {
					logger.warn(e.getMessage(), e);
				} catch (GMLParseException e) {
					throw new UnsupportedOperationException(e);
				}
			}

		}

		if (geom == null) {
			logger.warn("Only gml:Envelope supported at the current developement state.");
			return null;
		}
		
		//TODO reihenfolge
		
		StringBuilder sb = new StringBuilder();

		sb.append(MethodNames.SPATIAL_METHODS_PREFIX);
		sb.append(methodName+ "(");
		//create WKT from corners
		sb.append(MapEvent.GEOMETRY_KEY );
		sb.append(", ");
		sb.append(MethodNames.SPATIAL_METHODS_PREFIX);
		sb.append("fromWKT(\""+ geom.toText() +"\"))");

		return sb.toString();
	}

}
