/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.n52.ses.api.event.MapEvent;
import org.n52.ses.eml.v002.filter.spatial.methods.ICreateBuffer;
import org.n52.ses.eml.v002.filter.spatial.methods.PostGisCreateBuffer;
import org.n52.ses.eml.v002.filterlogic.esper.customFunctions.SpatialMethods;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.expression.ExprConstantNode;
import com.espertech.esper.epl.expression.ExprIdentNode;
import com.espertech.esper.event.map.MapEventBean;
import com.espertech.esper.view.ViewSupport;
import com.espertech.esper.view.Viewable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * This is a custom view for esper to create a dynamic spatial buffer.
 * The dynamic buffer gets updated by position information, thus
 * enabling trajectory processing.
 * 
 * @author matthes rieke
 *
 */
/**
 * @author matthes
 *
 */
public class DynamicSpatialBufferView extends ViewSupport {

	private static final Logger logger = LoggerFactory
			.getLogger(DynamicSpatialBufferView.class);

	public static final String SELECTFUNCTION_NAME = "SelectGeometryInDynamicBuffer";
	public static final boolean ALLOW_ORIGINAL_MESSAGE = true;
	public static final String SELECT_STRING = "*";
	
	private StatementContext context;
	private EventType eventType;
	private Double bufferDistance;
	private Geometry route;
	Geometry buffer;
	Object bufferMutex = new Object();
	private String distanceUOM;
	private String aircraftProperty;
	private String notifyProperty;
	private Point currentPosition;
	private Point previousPosition;
	private ICreateBuffer buffering;
	private String crs;
	private EventBean lastInsertStreamEvent;
	private int currentSegmentPoint = 1;
	private Geometry dynamicRoute;
	private GeometryFactory geomFactory = new GeometryFactory();
	

	/**
	 * @param ctx the statement context
	 * @param et the eventType from the factory
	 * @param geometry the route
	 * @param distance the buffer distance
	 * @param uom the distance uom
	 * @param crs the coordinate reference system for this buffer
	 * @param aircraftPos the aircraft updates
	 * @param notifyGeom the actual geometry
	 */
	public DynamicSpatialBufferView(AgentInstanceViewFactoryChainContext ctx,
			EventType et, Object geometry, Object distance, Object uom, Object crs,
			Object aircraftPos, Object notifyGeom) {
		this.context = ctx.getStatementContext();
		this.eventType = et;
		this.bufferDistance = (Double) ((ExprConstantNode) distance).getValue();
		this.distanceUOM = (String) ((ExprConstantNode) uom).getValue();;
		this.aircraftProperty = (String) ((ExprIdentNode) aircraftPos).getFullUnresolvedName();
		this.notifyProperty = (String) ((ExprIdentNode) notifyGeom).getFullUnresolvedName();
		this.buffering = new PostGisCreateBuffer();
		this.crs = (String) ((ExprConstantNode) crs).getValue();;

		WKTReader wkt = new WKTReader();
		String geomString = (String) ((ExprIdentNode) geometry).getUnresolvedPropertyName();
		try {
			this.route = wkt.read(geomString.substring(9, geomString.length() - 2));
			this.dynamicRoute = this.route;
		} catch (ParseException e) {
			logger.warn(e.getMessage(), e);
		}

		if (this.route != null) {
			this.buffer = this.buffering.buffer(this.route, this.bufferDistance, this.distanceUOM, this.crs);
		}
	}

	@Override
	public void update(EventBean[] newData, EventBean[] oldData) {
		// The remove stream most post the same exact object references of events that were posted as the insert stream
		EventBean[] removeStreamToPost;
		if (this.lastInsertStreamEvent != null) {
			removeStreamToPost = new EventBean[] {this.lastInsertStreamEvent};
		} 
		else {
			removeStreamToPost = new EventBean[] {populateMap(null)};
		}


		if (newData.length > 0) {
			for (EventBean eventBean : newData) {
				@SuppressWarnings("unchecked")
				Map<String, Object> underlying = (Map<String, Object>) eventBean.getUnderlying();

				MapEventBean positionEvent = null;
				MapEventBean notifyEvent = null;
				for (String type : underlying.keySet()) {
					if (type.equals(this.aircraftProperty.substring(0, this.aircraftProperty.indexOf(".")))) {
						/*
						 * we have a position update!
						 */
						positionEvent = (MapEventBean) underlying.get(type);

						this.previousPosition = this.currentPosition;
						this.currentPosition = (Point) positionEvent.getProperties().get(this.aircraftProperty.substring(
								this.aircraftProperty.indexOf(".")+1, this.aircraftProperty.length()));

						//check if our buffer needs an update
						checkBufferUpdate();
					}	
					else if (type.equals(this.notifyProperty.substring(0, this.notifyProperty.indexOf(".")))) {
						/*
						 * we have a notification geometry
						 */
						notifyEvent = (MapEventBean) underlying.get(type);
					}
				}

				if (notifyEvent != null && !areEqual(notifyEvent, positionEvent)) {
					/*
					 * this is a different event, treat it as notification update
					 */
					if (withinBuffer(notifyEvent.getProperties())) {
						/*
						 * it matched the dynamic buffer!
						 * populate to parent views -> this starts
						 * provision to the StatementListener
						 */
						if (this.hasViews()) {
							EventBean newDataPost = populateMap(notifyEvent);
							this.lastInsertStreamEvent = newDataPost; 
							updateChildren(new EventBean[] {newDataPost}, removeStreamToPost);
						}
						
						/*
						 * kann wieder raus, demokram
						 */
						if (Boolean.parseBoolean(ConfigurationRegistry.getInstance().getPropertyForKey("DEMO_MODE"))) {
							final Geometry geom = (Geometry) notifyEvent.getProperties().get(MapEvent.GEOMETRY_KEY);
							
							new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										HttpURLConnection conn = (HttpURLConnection) (new URL("http://localhost:8082")).openConnection();
										conn.setDoOutput(true);
										conn.setDoInput(true);
										conn.connect();
										OutputStream writer = conn.getOutputStream();
										
										String msg = "Notification: "+ geom.toText();
										writer.write(msg.getBytes());
										writer.flush();
										writer.close();
										conn.getResponseCode();
										conn.disconnect();
									} catch (MalformedURLException e) {
									} catch (IOException e) {
										logger.warn(e.getMessage(), e);
									}
								}
							}).start();
						}
						
					}
				}

			}
		}

	}

	private boolean withinBuffer(Map<String, Object> properties) {
		if (properties.get(MapEvent.GEOMETRY_KEY) == null) {
			return false;
		}

		Geometry geom = (Geometry) properties.get(MapEvent.GEOMETRY_KEY);
		if (this.buffer != null) {
			synchronized (this.bufferMutex) {
				return SpatialMethods.intersects(geom, this.buffer);	
			}
		}
		return false;
	}

	private boolean areEqual(MapEventBean notifyEvent,
			MapEventBean positionEvent) {
		//simplest case
		if (notifyEvent != null && positionEvent == null) return false;

		return notifyEvent.getProperties().get(MapEvent.ORIGNIAL_MESSAGE_KEY).equals(
				positionEvent.getProperties().get(MapEvent.ORIGNIAL_MESSAGE_KEY));
	}

	/**
	 * This methods implements the algorithm to check
	 * if the buffer has to be recalculated.
	 */
	private synchronized void checkBufferUpdate() {
		/*
		 * we follow a simple principle.
		 * - check the distance to the next route segement point.
		 * - compare previous distance to current distance.
		 * - if the distance is greater than the previous then we assume that
		 * 		the segment point is passed, since the distance is growing
		 * 
		 * TODO Future work for dynamic algorithm:
		 * - take angles into account
		 * - in the case the aircraft is too fast (i.e. it gets beyond the
		 * 		current active coordinate's successor during position updates)
		 * 		check for "left-out" segment points
		 * - to ensure the order of position updates, consider the xml timestamp
		 * 		element
		 */
		
		if (this.previousPosition == null) {
			return;
		}
		
		//only calculate an update if we have more then 1 segment.
		if (this.route.getCoordinates().length > 2 && this.route.getCoordinates().length - 1 != this.currentSegmentPoint) {
			Coordinate activePoint = this.route.getCoordinates()[this.currentSegmentPoint];
		
			double preDist = activePoint.distance(this.previousPosition.getCoordinate());
			double curDist = activePoint.distance(this.currentPosition.getCoordinate());
			
			//check if the position moves away from the active coordinate
			if (curDist > preDist) {
				Coordinate[] origCoords = this.route.getCoordinates();
				Coordinate[] points = new Coordinate[origCoords.length - this.currentSegmentPoint];
				
				for (int i = 0; i < points.length; i++) {
					points[i] = origCoords[i+this.currentSegmentPoint];
				}
				
				//increment the active coordinate
				this.currentSegmentPoint++;
				
				//create the new dynamic route
				this.dynamicRoute = this.geomFactory.createLineString(points);
				
				//calculate the actual buffer
				synchronized (this.bufferMutex) {
					this.buffer = this.buffering.buffer(this.dynamicRoute, this.bufferDistance, this.distanceUOM, this.crs);	
				}
				
				//demo stuff
				if (Boolean.parseBoolean(ConfigurationRegistry.getInstance().getPropertyForKey("DEMO_MODE"))) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								HttpURLConnection conn = (HttpURLConnection) (new URL("http://localhost:8082")).openConnection();
								conn.setDoOutput(true);
								conn.setDoInput(true);
								conn.connect();
								OutputStream writer = conn.getOutputStream();
								String msg = null;
								synchronized (DynamicSpatialBufferView.this.bufferMutex) {
									msg = "BufferUpdate="+ DynamicSpatialBufferView.this.buffer.toText();
								}
								
								if (msg != null) {
									writer.write(msg.getBytes());
									writer.flush();
									writer.close();
									conn.getResponseCode();
								}
								
								conn.disconnect();
							} catch (MalformedURLException e) {
							} catch (IOException e) {
							}
						}
					}).start();
				}
			}
		}
		
	}

	private EventBean populateMap(MapEventBean notifyEvent) {
		EventBean result;
		
		if (notifyEvent == null) {
			result = new MapEventBean(this.eventType);
		} else {
			result = new MapEventBean(notifyEvent.getProperties(), notifyEvent.getEventType());
		}
		
		return result;
	}

	@Override
	public EventType getEventType() {
		return this.eventType;
	}

	@Override
	public Iterator<EventBean> iterator() {
		this.context.getDynamicReferenceEventTypes();
		return null;
	}

	@Override
	public void setParent(Viewable parent) {
		super.setParent(parent);
		if (parent != null)  {
			//TODO do what?
		}
	}


}
