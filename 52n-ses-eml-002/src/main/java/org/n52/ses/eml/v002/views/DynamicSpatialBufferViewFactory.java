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
package org.n52.ses.eml.v002.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.ses.api.event.MapEvent;

import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.ViewFactoryContext;
import com.espertech.esper.view.ViewFactorySupport;
import com.espertech.esper.view.ViewParameterException;
import com.vividsolutions.jts.geom.Geometry;

/**
 * The Factory for the {@link DynamicSpatialBufferView}.
 * 
 * @author matthes rieke
 *
 */
public class DynamicSpatialBufferViewFactory extends ViewFactorySupport {

	private EventType eventType;
	private Object geometry;
	private Object distance;
	private Object uom;
	private Object aircraftPos;
	private Object notifyGeom;
	private Object crs;


	@Override
	public EventType getEventType() {
		return this.eventType;
	}


	@Override
	public void setViewParameters(ViewFactoryContext context, List<ExprNode> params)
	throws ViewParameterException {
		String errorMessage = "'dynamicSpatialBuffer' needs this parameters: com.vividsolutions.jts.geom.Geometry travelRoute, java.lang.Double bufferDistance, java.lang.String distanceUOM, com.vividsolutions.jts.geom.Geometry positionUpdate, com.vividsolutions.jts.geom.Geometry notificationGeometry";
	    if (params.size() != 6) {
	      throw new ViewParameterException(errorMessage);
	    }

	    this.geometry = params.get(0);
	    this.distance = params.get(1);
	    this.uom = params.get(2);
	    this.crs = params.get(3);
	    this.aircraftPos = params.get(4);
	    this.notifyGeom = params.get(5);
	}

	@Override
	public void attach(EventType parentType, StatementContext context, ViewFactory optionalParentFactory,
			List<ViewFactory> partenViewFactories) throws ViewParameterException {
		// create new event type
		Map<String, Object> eventTypeMap = new HashMap<String, Object>();
		eventTypeMap.put(MapEvent.GEOMETRY_KEY, Geometry.class);
		this.eventType = context.getEventAdapterService().createAnonymousMapType(parentType.getName()+"-anonymous", eventTypeMap);		
	}

	@Override
	public View makeView(
			AgentInstanceViewFactoryChainContext context) {
		return new DynamicSpatialBufferView(context, this.eventType,
				this.geometry, this.distance, this.uom, this.crs, this.aircraftPos, this.notifyGeom);
	}

	
}