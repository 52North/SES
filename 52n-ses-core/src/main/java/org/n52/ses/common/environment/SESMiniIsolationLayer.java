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
package org.n52.ses.common.environment;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.muse.core.Environment;
import org.apache.muse.core.platform.mini.MiniIsolationLayer;

/**
 * Replacement of default {@link MiniIsolationLayer} ot provide
 * a different Environment (which itself provides a different SoapClient).
 * 
 * @author matthes rieke
 *
 */
public class SESMiniIsolationLayer extends MiniIsolationLayer {

	public SESMiniIsolationLayer(HttpServletRequest initialRequest,
			ServletContext initialContext) {
		super(initialRequest, initialContext);
	}

	@Override
	protected Environment createEnvironment() {
		 return new SESMiniEnvironment(getInitialRequest(), getInitialContext());
	}

	
	
}
