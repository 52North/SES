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
/**
 * Part of the diploma thesis of Thomas Everding.
 * @author Thomas Everding
 */

package org.n52.ses.eml.v001.filter.comparison;

import java.util.HashSet;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.n52.ses.api.event.DataTypesMap;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.eml.v001.filter.expression.ABinaryFilterExpression;
import org.n52.ses.eml.v001.filter.expression.AFilterExpression;
import org.n52.ses.eml.v001.filterlogic.esper.customFunctions.MethodNames;

import net.opengis.fes.x20.BinaryComparisonOpType;


/**
 * superclass of all binary comparison filters
 * 
 * @author Thomas Everding
 *
 */
/**
 * @author matthes
 *
 */
public abstract class ABinaryComparisonFilter extends AComparisonFilter {

	private static final QName PROPERTY_NAME_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyName");

	/**
	 * left part of comparison
	 */
	protected AFilterExpression first;

	/**
	 * right part of comparison
	 */
	protected AFilterExpression second;



	/**
	 * initializes the filter
	 * @param binaryOp the type of operator
	 * @param propertyNames a set of used property names.
	 */
	protected void initialize(BinaryComparisonOpType binaryOp, HashSet<Object > propertyNames) {
		//TODO parse expression

		if (binaryOp.getExpressionArray().length == 2) {
			this.first  = AFilterExpression.FACTORY.buildFilterExpression(binaryOp.getExpressionArray(0), propertyNames, this);
			this.second = AFilterExpression.FACTORY.buildFilterExpression(binaryOp.getExpressionArray(1), propertyNames, this);
		}
		else if (binaryOp.getExpressionArray().length == 1) {
			this.first  = AFilterExpression.FACTORY.buildFilterExpression(binaryOp.getExpressionArray(0), propertyNames, this);
			XmlObject[] props = binaryOp.selectChildren(PROPERTY_NAME_QNAME);
			if (props != null && props.length > 0) {
				this.second = AFilterExpression.FACTORY.buildFilterExpression(props[0], propertyNames, this);
			}
		}

	}


	/**
	 * Creates the String for the statement using the propertyExists method.
	 * @return the statement substring.
	 */
	public String createUsedPropertyString() {
		String result = "(";

		boolean noPropertyChecked = true;
		boolean hasToBeChecked;

		String usedProp;
		String usedEvent = "";
		String usedField = "";

		if (this.first.getUsedProperty() != null) {
			usedProp = this.first.getUsedProperty().replace("/", ".");

			//check if multiple used properties are registered
			if (usedProp.equals(ABinaryFilterExpression.MULTIPLE_USED_PROPERTIES_IDENTIFIER)) {
				String mups = createMultipleUsedPropertiesString(this.first);
				result += mups;

				//check if there is a property check
				noPropertyChecked = mups.equals("");
			} 
			else {
				if (usedProp.contains(".")) {
					usedEvent = usedProp.substring(0, usedProp.indexOf(".")+1);
					usedField = usedProp.substring(usedProp.indexOf(".")+1, usedProp.length());
				} 
				else {
					usedField = usedProp;
				}
				//check if field has to be checked
				hasToBeChecked = this.checkField(usedField);

				if (hasToBeChecked) {
					result += MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
					noPropertyChecked = false;
				}
			}
		}

		if (this.second.getUsedProperty() != null) {

			if (!noPropertyChecked) {
				result += "AND ";
			}

			usedProp = this.second.getUsedProperty().replace("/", ".");

			//check if multiple used properties are registered
			if (usedProp.equals(ABinaryFilterExpression.MULTIPLE_USED_PROPERTIES_IDENTIFIER)) {
				String mups = createMultipleUsedPropertiesString(this.second);
				result += mups;

				//check if there is a property check
				noPropertyChecked = mups.equals("");
			} 
			else {

				if (usedProp.contains(".")) {
					usedEvent = usedProp.substring(0, usedProp.indexOf(".")+1);
					usedField = usedProp.substring(usedProp.indexOf(".")+1, usedProp.length());
				} 
				else {
					usedField = usedProp;
				}
				//check if field has to be checked
				hasToBeChecked = this.checkField(usedField);

				if (hasToBeChecked) {
					result += MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
					noPropertyChecked = false;
				}
			}
		}

		/*
		 * If no property has to be checked return an empty string.
		 * 
		 * If a property has been checked the noPropertyChecked flad is false.
		 */
		if (noPropertyChecked) {
			return "";
		}

		result += ") AND ";
		return result;
	}



	private String createMultipleUsedPropertiesString(AFilterExpression expr) {
		String result = "";

		if (expr instanceof ABinaryFilterExpression) {
			ABinaryFilterExpression bexpr = (ABinaryFilterExpression) expr;

			boolean firstProp = true;
			for (String usedProp : bexpr.getUsedPropertyArray()) {
				String usedEvent = "";
				String usedField = "";

				if (usedProp.contains(".")) {
					usedEvent = usedProp.substring(0, usedProp.indexOf(".")+1);
					usedField = usedProp.substring(usedProp.indexOf(".")+1, usedProp.length());
				} 
				else {
					usedField = usedProp;
				}

				//check if field has to be checked
				boolean hasToBeChecked = this.checkField(usedField);

				if (hasToBeChecked) {
					if (firstProp) {
						result += MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
					} 
					else {
						result += "AND " + MethodNames.PROPERTY_EXISTS_NAME + "("+ usedEvent +"this, \"" + usedField + "\") ";
					}

					firstProp = false;
				}
			}
		}

		return result;
	}

	/**
	 * Checks if the statement is using correct comparisons.
	 * String comparisons need quotes around the string.
	 * @param complex is complex pattern?
	 * 
	 * @param str1 left part of the comparison
	 * @param str2 right part of the comparison
	 * @return str1 and str2 in an array after changing them.
	 */
	protected String[] checkDataTypes(boolean complex, String str1, String str2) {
		return checkDataTypes(str1, str2, false, complex);
	}

	private String[] checkDataTypes(String str1, String str2, boolean viceversa, boolean complex) {

		/*
		 * TODO workaround for same property of different patterns (why are
		 * they not found in the datatypesmap?)
		 */
		String str1Cut = str1;
		String str2Cut = str2;
		if (complex) {
			if (str1.contains(".")) {
				str1Cut = str1.substring(str1.indexOf(".")+1);
			}
			if (str2.contains(".")) {
				str2Cut = str1.substring(str1.indexOf(".")+1);
			}
		}
		if (str1.contains(".") && str2.contains(".")) {
			str1Cut = str1.substring(str1.indexOf(".")+1);
			str2Cut = str2.substring(str2.indexOf(".")+1);

			if (str1Cut.equals(str2Cut)) {
				return new String[]{str1, str2};
			}
		}

		boolean changed = false;

		/*
		 * check if first is a string -> check second
		 */
		Object type = DataTypesMap.getInstance().getDataType(str1Cut);
		if (type == String.class) {
			if (!str2Cut.startsWith("\"")) {
				if (!str2Cut.endsWith("\"")) {
					str2Cut = "\""+ str2Cut + "\"";
				} else {
					str2Cut =  "\""+ str2Cut;
				}
			}
			changed = true;
		} else if (type == Double.class || type == Long.class) { 
			/*
			 * try parsing the second as double -> if fails: exception
			 */
			try {
				Double.parseDouble(str2Cut);
				str1Cut = str1;
				changed = true;
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Could not parse '"+ str2Cut +"' as a number. Property '" +
						str1Cut +"' was registered as a number by default or by a Publisher.");
			}
		} else {
			if (viceversa) {
				/*
				 * everything went wrong.. users fault
				 */
				throw new IllegalArgumentException("Could not parse the (Not)EqualToFilter element pair '" +
						str1Cut +"', '"+ str2Cut +"'. " +
				"Please recheck your expression.");
			}
		}

		/*
		 * return, if changed, in original order.
		 */
		if (changed && !viceversa) {
			return new String[] {str1Cut, str2Cut};
		}

		if (viceversa) {
			/*
			 * return in switched order
			 */
			return new String[] {str2Cut, str1Cut};
		}
		/*
		 * call self with other direction.
		 */
		return checkDataTypes(str2Cut, str1Cut, true, complex);
	}

	/**
	 * Checks if a event field is mandatory or has to be checked for existance.
	 * 
	 * @param usedField name of the event field (property)
	 * 
	 * @return <code>true</code> if the field has to be checked
	 */
	private boolean checkField(String usedField) {
		/*
		 * mandatory field that do not have to be checked are:
		 * - this
		 * - start time
		 * - end time
		 * - causality
		 * - value
		 */
		if (usedField.equals(MapEvent.THIS_KEY) ||
				usedField.equals(MapEvent.START_KEY) ||
				usedField.equals(MapEvent.END_KEY) ||
				usedField.equals(MapEvent.CAUSALITY_KEY) ||
				usedField.equals(MapEvent.VALUE_KEY)) {
			//property has not to be checked
			return false;
		}
		return true;
	}
}