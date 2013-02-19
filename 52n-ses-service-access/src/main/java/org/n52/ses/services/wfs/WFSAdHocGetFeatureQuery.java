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
package org.n52.ses.services.wfs;

import java.io.IOException;
import java.util.Map;

import net.opengis.fes.x20.AndDocument;
import net.opengis.fes.x20.BinaryComparisonOpType;
import net.opengis.fes.x20.BinaryLogicOpType;
import net.opengis.fes.x20.ComparisonOpsDocument;
import net.opengis.fes.x20.FilterDocument;
import net.opengis.fes.x20.FilterType;
import net.opengis.fes.x20.LiteralDocument;
import net.opengis.fes.x20.LiteralType;
import net.opengis.fes.x20.LogicOpsDocument;
import net.opengis.fes.x20.PropertyIsEqualToDocument;
import net.opengis.fes.x20.SpatialOpsDocument;
import net.opengis.fes.x20.TemporalOpsDocument;
import net.opengis.fes.x20.ValueReferenceDocument;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.oxf.xmlbeans.tools.XmlUtil;

public class WFSAdHocGetFeatureQuery extends WFSQuery {
	
	private Map<String, String> params;
	private String featureType;
	private int maxFeatureCount;
	protected FilterDocument filter;

	protected WFSAdHocGetFeatureQuery(String featureType, int maxFeatureCount) {
		this.featureType = featureType;
		this.maxFeatureCount = maxFeatureCount;	
	}
	
	public WFSAdHocGetFeatureQuery(String featureType, int maxFeatureCount, FilterDocument filter) {
		this(featureType, maxFeatureCount);
		this.filter = filter;
	}

	public XmlObject createQuery() throws IOException, XmlException {
		return buildGetFeatureRequest(featureType, filter, maxFeatureCount);
	}
	
	public String getFeatureType() {
		return params.get(WFSConnector.FEATURE_TYPE_KEY);
	}

	public FilterDocument getFilter() {
		return filter;
	}

	/**
	 * Inserts a child xmlbean
	 * 
	 * @param target 
	 *            parent node
	 * @param newChild
	 *            the new child node
	 */
	protected void addXSAnyElement(XmlObject target, XmlObject newChild) {
		XmlCursor childCur = newChild.newCursor();

		XmlCursor targetCur = target.newCursor();
		targetCur.toNextToken();

		childCur.moveXml(targetCur);

		childCur.dispose();
		targetCur.dispose();
	}
	
	/**
	 * Creates a simple Filter based on Filter Encoding.
	 * Compares a Value Reference to a Literal.
	 * 
	 * @param valueRef
	 * 		the Value Reference
	 * @param literalString
	 * 		the name of the feature
	 */
	protected FilterDocument createFilterByValueReference(String valueRef, String literalString){
		// Filter
		FilterDocument filDoc = FilterDocument.Factory.newInstance();
		FilterType filter = filDoc.addNewFilter();

		PropertyIsEqualToDocument equalToDoc = createPropertyIsEqualTo(
				valueRef, literalString);

		filter.set(equalToDoc);

		return filDoc;
	}

	protected PropertyIsEqualToDocument createPropertyIsEqualTo(String valueRef,
			String literalString) {
		// PropertyIsEqualTo
		PropertyIsEqualToDocument equalToDoc = PropertyIsEqualToDocument.Factory.newInstance();
		BinaryComparisonOpType binaryComparison = equalToDoc.addNewPropertyIsEqualTo();

		// add literal
		LiteralDocument litDoc = LiteralDocument.Factory.newInstance();
		LiteralType literalType = litDoc.addNewLiteral();
		XmlString xmlString = XmlString.Factory.newInstance();
		xmlString.setStringValue(literalString);
		literalType.set(xmlString);
		addXSAnyElement(binaryComparison, literalType);

		// add value reference
		ValueReferenceDocument valRefDoc = ValueReferenceDocument.Factory.newInstance();
		valRefDoc.setValueReference(valueRef);
		addXSAnyElement(binaryComparison, valRefDoc.getExpression());
		return equalToDoc;
	}
	

	protected XmlObject createAndFilter(XmlObject first, XmlObject second) {
		AndDocument andDoc = AndDocument.Factory.newInstance();
		BinaryLogicOpType and = andDoc.addNewAnd();
		
		addOperand(and, first);
		addOperand(and, second);
		
		return andDoc;
	}

	private void addOperand(BinaryLogicOpType logicOp, XmlObject operand) {
		if (operand instanceof ComparisonOpsDocument) {
			insertXml(logicOp, operand);
			XmlUtil.qualifySubstitutionGroup(logicOp.getComparisonOpsArray(0), operand.schemaType().getDocumentElementName());
		}
		else if (operand instanceof LogicOpsDocument) {
			insertXml(logicOp, operand);
			XmlUtil.qualifySubstitutionGroup(logicOp.getLogicOpsArray(0), operand.schemaType().getDocumentElementName());
		}
		else if (operand instanceof SpatialOpsDocument) {
			insertXml(logicOp, operand);
			XmlUtil.qualifySubstitutionGroup(logicOp.getSpatialOpsArray(0), operand.schemaType().getDocumentElementName());
		}
		else if (operand instanceof TemporalOpsDocument) {
			insertXml(logicOp, operand);
			XmlUtil.qualifySubstitutionGroup(logicOp.getTemporalOpsArray(0), operand.schemaType().getDocumentElementName());
		}
	}

	public static void insertXml(XmlObject target, XmlObject content) {
		XmlCursor cur = content.newCursor();
		cur.toFirstContentToken();
		XmlCursor targetCur = target.newCursor();
		targetCur.toFirstContentToken();
		cur.copyXml(targetCur);
		cur.dispose();
		targetCur.dispose();
	}


}
