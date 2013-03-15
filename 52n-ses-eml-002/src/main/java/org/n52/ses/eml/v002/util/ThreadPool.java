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

package org.n52.ses.eml.v002.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.n52.ses.util.concurrent.NamedThreadFactory;

/**
 * ThreadPool for the execution of {@link Runnable}s.
 * Implemented as Singleton. 
 * 
 * @author Thomas Everding
 *
 */
public class ThreadPool {
	
	private static ThreadPool instance = null;
	
	private ExecutorService executor;
	
	/**
	 * 
	 * Private Constructor
	 *
	 */
	private ThreadPool() {
		this.executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("EML002-UpdateHanderPool"));
	}
	
	
	/**
	 * 
	 * @return the only instance of this class
	 */
	public static synchronized ThreadPool getInstance() {
		if (instance == null) {
			instance = new ThreadPool();
		}
		
		return instance;
	}
	
	
	/**
	 * Executes a class implementing {@link Runnable}.
	 * Does not block.
	 * 
	 * @param runnable the runnable
	 */
	public synchronized void execute(Runnable runnable) {
		this.executor.submit(runnable);
	}
}
