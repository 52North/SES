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
