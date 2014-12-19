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
package org.n52.ses.eml.v001.filter.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.BinaryComparisonOpType;
import net.opengis.fes.x20.ComparisonOpsType;
import net.opengis.fes.x20.FilterType;
import net.opengis.fes.x20.LiteralType;
import net.opengis.swe.x101.CountDocument.Count;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.ses.api.common.CustomStatementEvent;
import org.n52.ses.eml.v001.Constants;
import org.n52.ses.eml.v001.filter.IFilterElement;


public class ViewCountGuard extends CustomGuardFilter {
	
	private static final QName EQUAL_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyIsEqualTo");
	private static List<CustomStatementEvent> customEvents = new ArrayList<CustomStatementEvent>();
	private FilterType guard;
	
	static {
		ServiceLoader<CustomStatementEvent> loader = ServiceLoader.load(CustomStatementEvent.class);
		
		for (CustomStatementEvent customStatementEvent : loader) {
			if (customStatementEvent.bindsToEvent(CustomStatementEvent.REMOVE_VIEW_COUNT_EVENT)) {
				customEvents.add(customStatementEvent);
			}
		}
	}

	public ViewCountGuard(FilterType guard) {
		this.guard = guard;
		
	}
	
	@Override
	public String createExpressionString(boolean complexPatternGuard) {
		ComparisonOpsType ops = this.guard.getComparisonOps();
		
		StringBuilder sb = new StringBuilder();
		sb.append(" count(*) = ");
		
		sb.append(findCount(convertToBinaryComparisonOp(ops).getExpressionArray()));
		
		return sb.toString();
	}
	
	protected static BinaryComparisonOpType convertToBinaryComparisonOp(ComparisonOpsType ops) {
		QName coQName = ops.newCursor().getName();
		
		if (EQUAL_QNAME.equals(coQName)) {
			BinaryComparisonOpType bcop = (BinaryComparisonOpType) ops;
			return bcop;
		}
		
		return null;
	}

	private String findCount(XmlObject[] expressionArray) {
		for (XmlObject xo : expressionArray) {
			if (xo instanceof LiteralType) {
				XmlCursor cur = xo.newCursor();
				cur.toFirstChild();
				if (cur.getObject() instanceof Count) {
					return Integer.toString(((Count) cur.getObject()).getValue().intValue());
				}
			}
		}
		return null;
	}

	@Override
	public void setUsedProperty(String nodeValue) {
	}

	@Override
	public String getEPLClauseOperator() {
		return Constants.EPL_HAVING;
	}

	@Override
	public List<CustomStatementEvent> getCustomStatementEvents() {
		return customEvents;
	}
	
	public static class Factory implements CustomGuardFactory {

		private static final String VIEW_COUNT = "VIEW_COUNT";

		@Override
		public boolean supports(FilterType filter, Set<Object> propertyNames) {
			if (!filter.isSetComparisonOps()) return false;
			
			BinaryComparisonOpType bcops = convertToBinaryComparisonOp(filter.getComparisonOps());
			if (bcops == null) return false;
			
			return findMagicValueReference(bcops.getExpressionArray()) != null;
		}
		
		private String findMagicValueReference(XmlObject[] expressionArray) {
			for (XmlObject xo : expressionArray) {
				if (xo instanceof XmlString) {
					String val = ((XmlString) xo).getStringValue().trim();
					if (val.equals(VIEW_COUNT)) return val;
				}
			}
			return null;
		}

		@Override
		public IFilterElement createInstance(FilterType filter,
				Set<Object> propertyNames) {
			return new ViewCountGuard(filter);
		}
		
	}


}
