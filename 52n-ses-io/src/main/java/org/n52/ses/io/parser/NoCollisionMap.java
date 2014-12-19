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
package org.n52.ses.io.parser;

import java.util.HashMap;
import java.util.Vector;

/**
 * A HashMap which handles key-collisions itself.
 * 
 * In case of a collision it adds a new {@link Vector}
 * at the key and adds the values in the sub-vector.
 * 
 * @author Thomas Everding
 *
 */
public class NoCollisionMap extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	
	@SuppressWarnings("unchecked")
	@Override
	public Object put(String key, Object value) {
		//check if key already in use
		if (this.containsKey(key)) {
			//handle collision
			Vector<Object> inner;
			if (this.get(key) instanceof Vector<?>) {
				//collision happened earlier, get inner vector
				inner = (Vector<Object>) this.get(key);
				
				//add value
				inner.add(value);
				
				return null;
			}
			//else: first collision
			inner = new Vector<Object>();
			
			//add old value at 0
			inner.add(this.get(key));
			
			//add new value at 1
			inner.add(value);
			
			//place inner map at the key
			return super.put(key, inner);
		}
		//else: just insert
		return super.put(key, value);
	}
}
