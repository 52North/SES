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

package org.n52.ses.eml.v001.filterlogic.esper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.*;
import com.espertech.esper.client.EventBean;

/**
 * Statement aware update listener to debug the plugin
 * 
 * @author Thomas Everding
 *
 */
public class DebugListener implements StatementAwareUpdateListener{
	
	private static final Logger logger = LoggerFactory
			.getLogger(DebugListener.class);

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement epStatement, EPServiceProvider serviceProvider) {
		DebugListener.logger.debug("");
		DebugListener.logger.debug("-------------------------------");
		DebugListener.logger.debug("Update received for statement:");
		DebugListener.logger.debug("\tname: " + epStatement.getName());
		DebugListener.logger.debug("\ttext: " + epStatement.getText());
		
		if (newEvents != null) {
			DebugListener.logger.debug("new events:");
			DebugListener.logger.debug("\tsize: " + newEvents.length);
			
			for (int i = 0; i < newEvents.length; i++) {
				DebugListener.logger.debug("\tnumber " + i);
				DebugListener.logger.debug("\t\tbean:  " + newEvents[i]);
				
				try{
					Object obj = newEvents[i].get("value");
					DebugListener.logger.debug("\t\tvalue: " + obj);
				}
				catch (Throwable t) {/*empty*/}
			}
		}
		else {
			DebugListener.logger.debug("new events are null");
		}
		
		if (oldEvents != null) {
			DebugListener.logger.debug("old events:");
			DebugListener.logger.debug("\tsize: " + oldEvents.length);
			
			for (int i = 0; i < oldEvents.length; i++) {
				DebugListener.logger.debug("\tnumber " + i);
				DebugListener.logger.debug("\t\tbean: " + oldEvents[i]);
			}
		}
		else {
			DebugListener.logger.debug("old events are null");
		}
		
		DebugListener.logger.debug("service provider: ");
		DebugListener.logger.debug("\t" + serviceProvider);
		
		DebugListener.logger.debug("-------------------------------");
	}
	
}
