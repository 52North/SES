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
package org.n52.ses.eml.v002.filter.spatial;

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
	
	/**
	 * the DWithin QName
	 */
	public static final QName DWITHIN_QNAME = new QName(FES_NAMESPACE, "DWithin");
	
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
