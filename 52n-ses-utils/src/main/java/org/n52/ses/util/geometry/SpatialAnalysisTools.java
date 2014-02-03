/**
 * Copyright (C) 2008-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.ses.util.geometry;

import org.n52.ses.util.common.ConfigurationRegistry;

import com.vividsolutions.jts.geom.Geometry;

public class SpatialAnalysisTools {
	

	private static ICreateBuffer bufferAnalysis;

	public static Geometry buffer(Geometry geom, double distance, String ucumUom, String crs) {
		synchronized (SpatialAnalysisTools.class) {
			if (bufferAnalysis == null) {
				bufferAnalysis = (ICreateBuffer) initializeImplementation(ICreateBuffer.class);
			}	
		}
		
		return bufferAnalysis.buffer(geom, distance, ucumUom, crs);
	}

	private static Object initializeImplementation(Class<ICreateBuffer> clazz) {
		String impl = ConfigurationRegistry.getInstance().getPropertyForKey(clazz.getName());
		try {
			Class<?> implClazz = Class.forName(impl);
			if (clazz.isAssignableFrom(implClazz)) {
				return implClazz.newInstance();
			} else {
				throw new IllegalStateException(impl + " is not implementing "+clazz.getName());
			}
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
	
}
