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
package org.n52.ses.eml.v001.filter.custom;

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
import org.n52.ses.eml.v001.Constants;
import org.n52.ses.eml.v001.filter.IFilterElement;

public class ViewCountGuard extends CustomGuardFilter {
	
	private static final QName EQUAL_QNAME = new QName("http://www.opengis.net/fes/2.0", "PropertyIsEqualTo");
	private FilterType guard;

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
