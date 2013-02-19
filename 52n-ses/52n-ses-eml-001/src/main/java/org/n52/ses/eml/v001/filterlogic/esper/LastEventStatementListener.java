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
package org.n52.ses.eml.v001.filterlogic.esper;

import org.n52.ses.api.ws.ISubscriptionManager;
import org.n52.ses.eml.v001.pattern.Statement;

import com.espertech.esper.client.EventBean;

/**
 * This class extends the StatementListener. It only processes
 * the last event of an incoming event-array. This functionality
 * is close to impossible to create within esper using EPL.
 * Only constructor and the {@link StatementListener#update(EventBean[], EventBean[])}
 * method are needed.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class LastEventStatementListener extends StatementListener {

	
	/**
	 * see {@link StatementListener#StatementListener(Statement, EsperController, SubscriptionManager)}
	 */
	public LastEventStatementListener(Statement statement,
			EsperController controller, ISubscriptionManager sub) {
		super(statement, controller, sub);
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if (newEvents != null && newEvents.length > 0) {
			this.handleMatch(newEvents[newEvents.length - 1]);
		}
	}

	
	
}
