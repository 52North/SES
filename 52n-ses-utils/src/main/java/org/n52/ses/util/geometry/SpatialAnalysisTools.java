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
