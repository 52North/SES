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