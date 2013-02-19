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
package org.n52.ses.eml.v002.filter.spatial;


import net.opengis.fes.x20.BinarySpatialOpType;

import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.io.parser.GML32Parser;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.GMLParseException;
import org.n52.ses.eml.v002.filter.expression.LiteralExpression;
import org.n52.ses.eml.v002.filterlogic.esper.customFunctions.MethodNames;
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
	protected static final Logger logger = LoggerFactory.getLogger(ABinarySpatialFilter.class);

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
