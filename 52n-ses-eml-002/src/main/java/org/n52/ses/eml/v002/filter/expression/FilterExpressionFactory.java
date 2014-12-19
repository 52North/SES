/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v002.filter.expression;

import java.util.HashSet;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.ExpressionDocument;
import net.opengis.fes.x20.FunctionType;
import net.opengis.fes.x20.LiteralType;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.n52.ses.eml.v002.constants.Filter20FunctionConstants;
import org.n52.ses.eml.v002.filter.IFilterElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


/**
 * Builds {@link AFilterExpression}s.
 * 
 * @author Thomas Everding
 *
 */
public class FilterExpressionFactory {
	
	/*
	 * Logger instance for this class
	 */
	private Logger logger = LoggerFactory.getLogger(FilterExpressionFactory.class);
	
//	/*
//	 * FES v1 qnames (not used currently)
//	 */
//	
//	private static final QName ADD_QNAME_1 = new QName("http://www.opengis.net/ogc", "Add");
//	
//	private static final QName DIV_QNAME_1 = new QName("http://www.opengis.net/ogc", "Div");
//	
//	private static final QName LITERAL_QNAME_1 = new QName("http://www.opengis.net/ogc", "Literal");
//	
//	private static final QName MUL_QNAME_1 = new QName("http://www.opengis.net/ogc", "Mul");
//	
//	private static final QName PROPERTY_NAME_QNAME_1 = new QName("http://www.opengis.net/ogc", "PropertyName");
//	
//	private static final QName SUB_QNAME_1 = new QName("http://www.opengis.net/ogc", "Sub");
	
	/*
	 * FES v2 qnames
	 */
	private static final QName LITERAL_QNAME_2 = new QName("http://www.opengis.net/fes/2.0", "Literal");
	
	private static final QName VALUE_REFERENCE_QNAME_2 = new QName("http://www.opengis.net/fes/2.0", "ValueReference");
	
	private static final QName FUNCTION_QNAME_2 = new QName("http://www.opengis.net/fes/2.0", "Function");
	
	/**
	 * Parses an {@link ExpressionDocument}.
	 * 
	 * @param expressionType the expression to parse
	 * @param propertyNames the property names
	 * @param parent the parent filter statement of a nested expression
	 * @return the parsed expression
	 */
	public AFilterExpression buildFilterExpression (XmlObject expressionType, HashSet<Object> propertyNames, IFilterElement parent) {
		QName exprQName = expressionType.newCursor().getName();
		
		/*
		 * non binary expression 
		 */
		
		//check literal (FES v2)
		if (LITERAL_QNAME_2.equals(exprQName)) {
			LiteralType lt = (LiteralType) expressionType;
			return new LiteralExpression(lt);
		}
		
		//check value reference (FES v2)
		else if (VALUE_REFERENCE_QNAME_2.equals(exprQName)) {
			ValueReferenceExpression result = new ValueReferenceExpression(expressionType, propertyNames);
			
			Element elem = (Element) expressionType.getDomNode();
			String name = XmlUtils.toString(elem.getFirstChild()).trim();
			//replaceAll(":", "__")
			name = name.replaceAll("/", ".");
			
			result.setUsedProperty(name);
			return result;
		}
		
		//check function (FES v2)
		else if (FUNCTION_QNAME_2.equals(exprQName)) {
			FunctionType functionType = (FunctionType) expressionType;
			String functionName = functionType.getName();
			
			if (functionName.equals(Filter20FunctionConstants.ADD_FUNC_NAME)) {
				//build add expression
				return new AddExpression(functionType.getExpressionArray(), propertyNames);
			}
			else if (functionName.equals(Filter20FunctionConstants.SUB_FUNC_NAME)) {
				//build sub expression
				return new SubExpression(functionType.getExpressionArray(), propertyNames);
			}
			else if (functionName.equals(Filter20FunctionConstants.MUL_FUNC_NAME)) {
				//build mul expression
				return new MulExpression(functionType.getExpressionArray(), propertyNames);
			}
			else if (functionName.equals(Filter20FunctionConstants.DIV_FUNC_NAME)) {
				//build div expression
				return new DivExpression(functionType.getExpressionArray(), propertyNames);
			}
			else if (functionName.equals(Filter20FunctionConstants.DISTANCE_TO_NAME)) {
				// build distance to expression
				return new DistanceToExpression(functionType.getExpressionArray(), propertyNames);
			}
			
			/*
			 * implement other functions here
			 */
		}
		
//		/*
//		 * FES v1 not supported currently
//		 */
//		//check literal (FES v1)
//		else if (LITERAL_QNAME_1.equals(exprQName)) {
//			LiteralType lt = (LiteralType) expressionType;
//			return new LiteralExpression(lt);
//		}
//		
//		//check PropertyName (FES v1)
//		else if (PROPERTY_NAME_QNAME_1.equals(exprQName)) {
//			
//			PropertyNameExpression result = new PropertyNameExpression(expressionType, propertyNames);
//			
//			Element elem = (Element) expressionType.getDomNode();
//			String name = XmlUtils.toString(elem.getFirstChild()).trim();
//			//replaceAll(":", "__")
//			name = name.replaceAll("/", ".");
//			
//			result.setUsedProperty(name);
//			return result;
//		}
//		
//		/*
//		 * binary expressions (FES v1)
//		 */
//		BinaryOperatorType binaryOP = (BinaryOperatorType) expressionType;
//		
//		//check Add
//		if (ADD_QNAME_1.equals(exprQName)) {
//			//create new AddExpression
//			return new AddExpression(binaryOP, propertyNames);
//		}
//		
//		//check Div
//		else if (DIV_QNAME_1.equals(exprQName)) {
//			//create new DivExpression
//			return new DivExpression(binaryOP, propertyNames);
//		}
//		
//		//check Mul
//		else if (MUL_QNAME_1.equals(exprQName)) {
//			//create new MulExpression
//			return new MulExpression(binaryOP, propertyNames);
//		}
//		
//		//check Sub
//		else if (SUB_QNAME_1.equals(exprQName)) {
//			//create new SubExpression
//			return new SubExpression(binaryOP, propertyNames);
//		}
		
		this.logger.warn("unable to build filter expression for '" + exprQName.toString() + "'");
		return null;
	}
	
}
