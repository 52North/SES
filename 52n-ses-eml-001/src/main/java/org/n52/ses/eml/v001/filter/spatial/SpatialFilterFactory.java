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
package org.n52.ses.eml.v001.filter.spatial;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.BBOXType;
import net.opengis.fes.x20.BinarySpatialOpType;
import net.opengis.fes.x20.DistanceBufferType;
import net.opengis.fes.x20.SpatialOpsType;


/**
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class SpatialFilterFactory {
	
	private static final String FES_NAMESPACE = "http://www.opengis.net/fes/2.0";
	/*
	 * BBox QName
	 */
	private static final QName BBOX_QNAME = new QName(FES_NAMESPACE, "BBOX");
	
	/*
	 * Distance Buffer Ops QNames
	 */
	private static final QName BEYOND_QNAME = new QName(FES_NAMESPACE, "Beyond");
	private static final QName DWITHIN_QNAME = new QName(FES_NAMESPACE, "DWithin");
	
	/*
	 * Binary Spatial Ops QNames
	 */
	private static final QName CONTAINS_QNAME = new QName(FES_NAMESPACE, "Contains");
	private static final QName CROSSES_QNAME = new QName(FES_NAMESPACE, "Crosses");
	private static final QName DISJOINT_QNAME = new QName(FES_NAMESPACE, "Disjoint");
	private static final QName EQUALS_QNAME = new QName(FES_NAMESPACE, "Equals");
	private static final QName INTERSECTS_QNAME = new QName(FES_NAMESPACE, "Intersects");
	private static final QName OVERLAPS_QNAME = new QName(FES_NAMESPACE, "Overlaps");
	private static final QName TOUCHES_QNAME = new QName(FES_NAMESPACE, "Touches");
	private static final QName WITHIN_QNAME = new QName(FES_NAMESPACE, "Within");
	
	
	/**
	 * Builds the necessary spatial filter
	 * 
	 * @param sot FES spatial operator
	 * 
	 * @return a spatial filter object
	 */
	//TODO: property names not necessary (compare to ALogicalFilter.FACTORY)?
	public ASpatialFilter buildSpatialFilter(SpatialOpsType sot) { 
		QName sotQName = sot.newCursor().getName();
		
		//bbox ops types
		if (sotQName.equals(BBOX_QNAME)) {
			BBOXType bboxOp = (BBOXType) sot;
			return new BBOXFilter(bboxOp);
		}
		
		//Distance Buffer Ops types
		if (sotQName.equals(BEYOND_QNAME) || sotQName.equals(DWITHIN_QNAME)) {
			DistanceBufferType dbOp = (DistanceBufferType) sot;
		
			if (BEYOND_QNAME.equals(sotQName)) {
				return new BeyondFilter(dbOp);
			}
			
			else if (DWITHIN_QNAME.equals(sotQName)) {
				return new DWithinFilter(dbOp);
			}
		}
		
		//binary spatial ops types
		BinarySpatialOpType bsOp = (BinarySpatialOpType) sot;
		
		if (CONTAINS_QNAME.equals(sotQName)) {
			return new ContainsFilter(bsOp);
		}
		
		else if (CROSSES_QNAME.equals(sotQName)) {
			return new CrossesFilter(bsOp);
		}
		
		else if (DISJOINT_QNAME.equals(sotQName)) {
			return new DisjointFilter(bsOp);
		}
		
		else if (EQUALS_QNAME.equals(sotQName)) {
			return new EqualsFilter(bsOp);
		}
		
		else if (INTERSECTS_QNAME.equals(sotQName)) {
			return new IntersectsFilter(bsOp);
		}
		
		else if (OVERLAPS_QNAME.equals(sotQName)) {
			return new OverlapsFilter(bsOp);
		}
		
		else if (TOUCHES_QNAME.equals(sotQName)) {
			return new TouchesFilter(bsOp);
		}
		
		else if (WITHIN_QNAME.equals(sotQName)) {
			return new WithinFilter(bsOp);
		}
		
		return null;
	}

}
