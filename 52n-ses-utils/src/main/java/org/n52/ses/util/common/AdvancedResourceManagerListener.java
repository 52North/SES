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
package org.n52.ses.util.common;
import org.apache.muse.core.ResourceManagerListener;
import org.apache.muse.ws.addressing.EndpointReference;
import org.apache.muse.ws.addressing.soap.SoapFault;

/**
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 *
 * Extension of the {@link ResourceManagerListener}.
 * @param <T> type of the resource
 */
public interface AdvancedResourceManagerListener<T> {

	/**
	 * A new Resource has been added
	 * @param epr the reference to the endpoint of the resource
	 * @param resource the new resource
	 * @throws SoapFault if an error occurred on adding
	 */
	public void resourceAdded(EndpointReference epr, T resource)  throws SoapFault;
	
	/**
	 * A Resource has been removed
	 * @param epr the endpoint reference of the removed resource
	 * @param resource the removed resource
	 * @throws SoapFault if an error occurred while removing
	 */
	public void resourceRemoved(EndpointReference epr, T resource)  throws SoapFault;
	
}
