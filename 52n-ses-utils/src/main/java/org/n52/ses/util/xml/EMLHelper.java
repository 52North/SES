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
package org.n52.ses.util.xml;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.BinaryComparisonOpType;
import net.opengis.fes.x20.BinaryLogicOpType;
import net.opengis.fes.x20.ComparisonOpsType;
import net.opengis.fes.x20.FilterType;
import net.opengis.fes.x20.LiteralType;
import net.opengis.fes.x20.LogicOpsType;
import net.opengis.fes.x20.PropertyIsBetweenType;
import net.opengis.fes.x20.PropertyIsLikeType;
import net.opengis.fes.x20.PropertyIsNullType;
import net.opengis.fes.x20.UnaryLogicOpType;
import net.opengis.swe.x101.QuantityDocument;
import net.opengis.swe.x101.UomPropertyType;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.notification.faults.SubscribeCreationFailedFault;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.conversion.unit.NumberWithUOM;
import org.n52.ses.api.IUnitConverter;
import org.w3c.dom.Element;

public class EMLHelper {

	private static final QName AND_QNAME = new QName("http://www.opengis.net/fes/2.0", "And");
	
	private static final QName OR_QNAME = new QName("http://www.opengis.net/fes/2.0", "Or");
	
	private static final QName NOT_QNAME = new QName("http://www.opengis.net/fes/2.0", "Not");

	private static final QName VALUE_REFERENCE_QNAME =
		new QName("http://www.opengis.net/fes/2.0", "ValueReference");
	
	/**
	 * helpermethod for replacePhenomenonStringsAndConvertUnits().
	 * @throws Exception 
	 */
	public static void replaceForFilter(FilterType filter, IUnitConverter converter) throws Exception {
		if (filter.isSetLogicOps()) {
			replaceForLogicOp(filter.getLogicOps(), converter);
		}
		
		if (filter.isSetComparisonOps()) {
			ComparisonOpsType cOps = filter.getComparisonOps();
			replaceForComparisonOp(cOps, converter);	
		}
		
	}
	
	/**
	 * helpermethod for replacePhenomenonStringsAndConvertUnits().
	 * @throws Exception 
	 */
	public static void replaceForLogicOp(LogicOpsType logicOps, IUnitConverter converter) throws Exception {
		QName loQName = logicOps.newCursor().getName();
		
		/*
		 * check Not
		 */
		if (NOT_QNAME.equals(loQName)) {
			//create new NotFilter
			UnaryLogicOpType unaryOp = (UnaryLogicOpType) logicOps;
			if (unaryOp.isSetComparisonOps()) {
				replaceForComparisonOp(unaryOp.getComparisonOps(), converter);
			}
			if (unaryOp.isSetLogicOps()) {
				//rekursion
				replaceForLogicOp(unaryOp.getLogicOps(), converter);
			}
		}
		
		/*
		 * binary operators
		 */
		if (AND_QNAME.equals(loQName) || OR_QNAME.equals(loQName)) {
			BinaryLogicOpType binaryOp = (BinaryLogicOpType) logicOps;
			
			ComparisonOpsType[] array = binaryOp.getComparisonOpsArray();
			for (ComparisonOpsType cOps : array) {
				replaceForComparisonOp(cOps, converter);
			}
			
			LogicOpsType[] array2 = binaryOp.getLogicOpsArray();
			for (LogicOpsType logicOp : array2) {
				replaceForLogicOp(logicOp, converter);
			}
		}
				
	}
	
	/**
	 * helpermethod for replacePhenomenonStringsAndConvertUnits().
	 * @throws Exception 
	 */
	public static void replaceForComparisonOp(ComparisonOpsType cOps, IUnitConverter converter) throws Exception {
		if (cOps instanceof PropertyIsBetweenType) {
			PropertyIsBetweenType pibt = (PropertyIsBetweenType) cOps;
			
			XmlObject[] exps = new XmlObject[3];
			exps[0] = pibt.getExpression();
			exps[1] = pibt.getLowerBoundary().getExpression();
			exps[2] = pibt.getUpperBoundary().getExpression();
			
			for (XmlObject et : exps) {
				replaceForExpression(et, converter);
			}
			
		} else if (cOps instanceof BinaryComparisonOpType) {

			BinaryComparisonOpType bcot = (BinaryComparisonOpType) cOps;
			XmlObject[] exps = bcot.getExpressionArray();
			for (XmlObject et : exps) {
				replaceForExpression(et, converter);
			}
		} else if (cOps instanceof PropertyIsNullType) {
			PropertyIsNullType pint = (PropertyIsNullType) cOps;
			XmlObject pnt = pint.getExpression();
			replaceForExpression(pnt, null);

		} else if (cOps instanceof PropertyIsLikeType) {
			PropertyIsLikeType pilt = (PropertyIsLikeType) cOps;

			XmlObject[] lt = pilt.getExpressionArray();
			
			if (lt != null && lt.length > 1) {
				replaceForExpression(lt[0], converter);
				replaceForExpression(lt[1], null);	
			}
			
		}
	}

	/**
	 * helpermethod for replacePhenomenonStringsAndConvertUnits().
	 * @throws Exception 
	 */
	public static void replaceForExpression(XmlObject et, IUnitConverter converter) throws Exception {
		QName etQn = et.newCursor().getName();
		if (et instanceof LiteralType) {
			LiteralType lt = (LiteralType) et;

			XmlObject xmlContent = XmlObject.Factory.parse(lt.toString());
			if (xmlContent instanceof QuantityDocument) {
				QuantityDocument sweQ = (QuantityDocument) xmlContent;
				if (sweQ.getQuantity() != null) {
					if (!sweQ.getQuantity().isSetValue()) {
						throw new SubscribeCreationFailedFault("There was" +
								" no Value specified in the swe:Quantity element");
					}
					Double value = sweQ.getQuantity().getValue();
					
					UomPropertyType uom = sweQ.getQuantity().getUom();
					String uomCode = "";
					if (uom != null) {
						uomCode = uom.getCode();
					}
					else {
						//no ucum-code, just return without converting
						return;
					}
					
					NumberWithUOM result = converter.convert(uomCode, value);
					sweQ.getQuantity().setValue(result.getValue());
					sweQ.getQuantity().getUom().setCode(result.getUom());
					et.set(sweQ);
				}
			}
		
		} 

		else if (VALUE_REFERENCE_QNAME.equals(etQn)) {
			Element elem = (Element) et.getDomNode();
			String urn = XmlUtils.toString(elem.getFirstChild()).trim();
			urn = urn.replaceAll(":", "__").replaceAll("\\.", "_");
			XmlUtils.setElementText(elem, urn);
			
			
		}

	}

	
}
