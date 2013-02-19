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
package org.n52.ses.api.ws;
import org.w3c.dom.Element;

/**
 * Interface for the destroy registration method.
 * 
 * @author Jan Torben Heuer <jan.heuer@uni-muenster.de>
 *
 * the destroyRegistration method is in another namespace thus we
 * have to externalize at least the interface.
 */
public interface IDestroyRegistration {
	
	/**
	 * standard prefix for XML QNames
	 */
    String PREFIX = "tns";

    /**
     * namespace for the destroy operation name
     */
    String NAMESPACE_URI = "http://docs.oasis-open.org/wsn/brw-2";

    /**
     * destroys a registration
     * @param something method parameters in XML
     * @return XML response
     * @throws Exception if an error occurred on destroying
     */
    public Element destroyRegistration(Element something) throws Exception;

}