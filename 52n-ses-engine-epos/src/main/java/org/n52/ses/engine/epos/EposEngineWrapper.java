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
package org.n52.ses.engine.epos;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.muse.ws.notification.Filter;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.muse.ws.notification.impl.FilterCollection;
import org.apache.muse.ws.notification.impl.FilterFactory;
import org.n52.epos.engine.EposEngine;
import org.n52.epos.engine.rules.RuleInstance;
import org.n52.epos.event.EposEvent;
import org.n52.epos.filter.ActiveFilter;
import org.n52.epos.filter.EposFilter;
import org.n52.epos.filter.PassiveFilter;
import org.n52.epos.rules.Rule;
import org.n52.epos.transform.TransformationException;
import org.n52.epos.transform.TransformationRepository;
import org.n52.ses.api.IFilterEngine;
import org.n52.ses.api.ws.EngineCoveredFilter;
import org.n52.ses.api.ws.INotificationMessage;
import org.n52.ses.api.ws.ISubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class EposEngineWrapper implements IFilterEngine {
	
	private static final Logger logger = LoggerFactory.getLogger(EposEngineWrapper.class);
	private Map<ISubscriptionManager, Rule> rules = new HashMap<ISubscriptionManager, Rule>();

	public EposEngineWrapper() {
		FilterFactory.getInstance().addHandler(new EposFilterFactory());
	}
	
	@Override
	public void filter(INotificationMessage message) {
		if (message.getNotificationMessage() instanceof NotificationMessage) {
			NotificationMessage notific = (NotificationMessage) message.getNotificationMessage();
			
			Iterator<?> it = notific.getMessageContentNames().iterator();
			while (it.hasNext()) {
				QName mcn = (QName) it.next();
				Element content = notific.getMessageContent(mcn);
				EposEvent event;
				try {
					event = (EposEvent) TransformationRepository.Instance.transform(content, EposEvent.class);
					EposEngine.getInstance().filterEvent(event);
				} catch (TransformationException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
		
		
	}

	@Override
	public boolean registerFilter(final ISubscriptionManager subMgr, FilterCollection engineFilters) throws Exception {
		List<EposFilter> filters = findEposFilters(engineFilters, new ArrayList<EposFilter>());
		
		if (filters.isEmpty()) {
			logger.info("No Epos filters found ({})", subMgr.getFilter());
			return false;
		}
		
		RuleInstance rule = new RuleInstance(subMgr);
		
		for (EposFilter eposFilter : filters) {
			if (eposFilter instanceof PassiveFilter) {
				rule.setPassiveFilter((PassiveFilter) eposFilter);
			}
			else if (eposFilter instanceof ActiveFilter) {
				rule.addActiveFilter((ActiveFilter) eposFilter);
			}
		}
		
		logger.info("Registering Rule: {}", rule);
		EposEngine.getInstance().registerRule(rule);
		
		synchronized (this) {
			this.rules.put(subMgr, rule);	
		}
		
		return true;
	}


	private List<EposFilter> findEposFilters(Filter filter, List<EposFilter> resultList) {
		if (filter instanceof EngineCoveredFilter) {
			EngineCoveredFilter ecf = (EngineCoveredFilter) filter;
			Object specific = ecf.getEngineSpecificFilter();
			if (specific instanceof EposFilter) {
				resultList.add((EposFilter) specific);	
			}
		}
		else if (filter instanceof FilterCollection) {
			for (Object f : ((FilterCollection) filter).getFilters()) {
				findEposFilters((Filter) f, resultList);
			}
		}
		return resultList;
	}

	@Override
	public void unregisterFilter(ISubscriptionManager subMgr) throws Exception {
		synchronized (this) {
			if (this.rules.containsKey(subMgr)) {
				EposEngine.getInstance().unregisterRule(this.rules.get(subMgr));
				this.rules.remove(subMgr);	
			}
		}		
	}

	@Override
	public void shutdown() {
		EposEngine.getInstance().shutdown();
	}

}
