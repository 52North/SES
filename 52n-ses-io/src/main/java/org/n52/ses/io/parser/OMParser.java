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
package org.n52.ses.io.parser;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractFeatureCollectionType;
import net.opengis.gml.AbstractTimeObjectType;
import net.opengis.gml.BoundingShapeType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.FeatureCollectionDocument;
import net.opengis.gml.FeatureCollectionDocument2;
import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.PointType;
import net.opengis.gml.TimeInstantType;
import net.opengis.gml.TimePeriodType;
import net.opengis.gml.TimePositionType;
import net.opengis.om.x10.ObservationDocument;
import net.opengis.om.x10.ObservationType;
import net.opengis.om.x10.ProcessPropertyType;
import net.opengis.sampling.x10.SamplingPointDocument;
import net.opengis.sampling.x10.SamplingPointType;
import net.opengis.swe.x101.AbstractDataRecordType;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.CompositePhenomenonType;
import net.opengis.swe.x101.DataArrayDocument;
import net.opengis.swe.x101.DataArrayType;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordDocument;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.PhenomenonPropertyType;
import net.opengis.swe.x101.PhenomenonType;
import net.opengis.swe.x101.SimpleDataRecordType;
import net.opengis.swe.x101.TimeObjectPropertyType;
import net.opengis.swe.x101.UomPropertyType;
import net.opengis.swe.x101.CategoryDocument.Category;
import net.opengis.swe.x101.CountDocument.Count;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.TextBlockDocument.TextBlock;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.TimeDocument.Time;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.muse.ws.addressing.soap.SoapFault;
import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.joda.time.DateTime;
import org.n52.oxf.conversion.unit.CustomUnitConverter;
import org.n52.oxf.conversion.unit.NumberWithUOM;
import org.n52.oxf.conversion.unit.UOMTools;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.util.common.ConfigurationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Parses OGC O&M documents as events
 *
 */
public class OMParser extends AbstractParser {

	private static final Logger logger = LoggerFactory
			.getLogger(OMParser.class);

	/**
	 * the namespace of the used O&M version
	 */
	public static final String OM_NAMESPACE = "http://www.opengis.net/om/1.0";

	/**
	 * the namespace for O&M 1.0 with GML 3.2
	 */
	public static final String OM_GML32_NAMESPACE = "http://www.opengis.net/om/1.0/gml32";

	private static final String PHENOMENON_STRING_KEY = "phenomenon";
	private static final String UOM_STRING_KEY = "uom";
	private static final String IS_UCUM_BOOLEAN_KEY = "isUcum";
	private static final String IS_TIME_BOOLEAN_KEY = "isTime";
	private static final String POSITION_INT_KEY = "position";

	
	public OMParser() {
		UOMTools.addCustomUnitConverter(new CustomUnitConverter() {
			@Override
			public String getUnitString() {
				return "m+NN";
			}
			
			@Override
			public String getBaseUnit() {
				return "m";
			}
			
			@Override
			public NumberWithUOM convert(double doubleValue) {
				return new NumberWithUOM(doubleValue, "m");
			}
		});
	}

	/**
	 * @param oDoc O&M Observation document to be parsed 
	 * @return a {@link List} of parsed {@link MapEvent}
	 * @throws Exception exception
	 */
	public List<MapEvent> parseOM(ObservationDocument oDoc) throws Exception {

		//the resulting new events
		ArrayList<MapEvent> events = new ArrayList<MapEvent>();

		ObservationType observation = null;
		if (oDoc != null) {
			observation = oDoc.getObservation();
		}

		if (observation == null) throw new SoapFault("No " +
		"Observation found!");

		//the phenomenon/uom list
		List<Map<String, Object>> pums = new LinkedList<Map<String, Object>>();

		boolean hasTimestamp = false;


		/*
		 * Parse om:result. Use this phenomenons (and timestamps)
		 */
		XmlObject result = observation.getResult();

		if (result instanceof XmlAnyTypeImpl) {
			XmlAnyTypeImpl anyTypeDoc = (XmlAnyTypeImpl) result;

			XmlObject xobj = null;

			xobj = XMLBeansParser.parse(anyTypeDoc.toString(), false);

			if (xobj != null && xobj instanceof DataArrayDocument) {
				/*
				 * parse data array
				 */
				parseDataArray((DataArrayDocument) xobj, pums, events);
			} 
			else {
				//parse anything else than data arrays

				//check if content is of Group swe:AnyScalar and try parsing
				if (!parseAnyScalarTypes(anyTypeDoc, events)){

					/*
					 * use swe:DataRecords
					 */
					if (!parseDataRecord(xobj, events)) {
						/*
						 * parse as double or string, was not an AnyScalar type
						 */
						parseAsSingleValue(anyTypeDoc, events);	
					}
				}

				/*
				 * parsing for OWS-7
				 * 
				 * will use swe:DataRecords
				 */
				if (xobj != null && xobj instanceof DataRecordDocument) {
					//result does not need to be parsed. TODO: why?
					//one event will be sent per notification so one event has to be created
				}
			}
		}



		/*
		 * check for a timestamp
		 */
		if (events.size() > 0) {
			Object tempTime = events.get(0).get(MapEvent.START_KEY);
			if (tempTime != null) {
				if ((Long) tempTime != 0) {
					hasTimestamp = true;
				}
			}
		}

		/*
		 * parse time from samplingTime
		 */
		if (!hasTimestamp) {

			TimeObjectPropertyType topt = observation.getSamplingTime();
			AbstractTimeObjectType to = topt.getTimeObject();

			if (to instanceof TimeInstantType) {
				TimeInstantType tit = (TimeInstantType) to;
				TimePositionType tp = tit.getTimePosition();
				long timeStamp = getTimestamp(tp.getStringValue(), null);

				//put time
				for (MapEvent me : events) {
					me.put(MapEvent.START_KEY, timeStamp);
					me.put(MapEvent.END_KEY, timeStamp);
				}
			}

			else if (to instanceof TimePeriodType) {
				TimePeriodType tpt = (TimePeriodType) to;
				TimePositionType bp = tpt.getBeginPosition();
				TimePositionType ep = tpt.getEndPosition();

				long begin = getTimestamp(bp.getStringValue(), null);
				long end = getTimestamp(ep.getStringValue(), null);

				//put time
				for (MapEvent me : events) {
					me.put(MapEvent.START_KEY, begin);
					me.put(MapEvent.END_KEY, end);
				}
			}
		}

		/*
		 * parse the sensor-id from procedure
		 */
		ProcessPropertyType proc = observation.getProcedure();
		String sensorId = "";
		if (proc.isSetHref()) {
			sensorId = proc.getHref();
		}

		/*
		 * put sensor-id and procedure
		 */
		for (MapEvent me : events) {
			me.put(MapEvent.SENSORID_KEY, sensorId);
			me.put("procedure", sensorId);
//
//			if (observation.getProcedure().isSetHref()) {
//				me.put("procedure", observation.getProcedure().getHref());
//			}
		}

		/*
		 * parse observed property
		 */
		PhenomenonPropertyType observedProperty = observation.getObservedProperty();
		if (observedProperty.isSetHref()) {
			OMParser.logger.debug("########################## parsing observed property");
			String obProp = observedProperty.getHref();

			//put observed property
			for (MapEvent me : events) {
				me.put(MapEvent.OBSERVED_PROPERTY_KEY, obProp);
			}
		} else {
			/*
			 * Special treatment of observations from pegel-online SOS 
			 * holding composite phenomena
			 * BAW_MODE is required
			 */
			OMParser.logger.debug("#############################\n\n");
			ConfigurationRegistry config = ConfigurationRegistry.getInstance();
			boolean bawMode = false;
			Object gm = config.getPropertyForKey(ConfigurationRegistry.USE_FOR_BAW);
			if (gm != null) {
				bawMode = Boolean.parseBoolean(gm.toString());
			}
			if(bawMode){
				OMParser.logger.debug("BAW is ON");
				PhenomenonType phen = observedProperty.getPhenomenon();
				if (phen instanceof CompositePhenomenonType) {
					OMParser.logger.debug("found composite phenomenon");
					CompositePhenomenonType compPhen = (CompositePhenomenonType) phen;
					int length = compPhen.getComponentArray().length;
					PhenomenonPropertyType comp = compPhen.getComponentArray(length-1);
					if(comp.isSetHref()){
						String obProp = comp.getHref();
						OMParser.logger.debug("using last component of composite phenomenon with href:\"" +
								obProp + "\"");

						//put observed property
						for (MapEvent me : events) {
							me.put(MapEvent.OBSERVED_PROPERTY_KEY, obProp);
						}
					}
				}

			} else {
				OMParser.logger.debug("BAW is OFF");
			}
		}


		/*
		 * parse featureOfInterest -> Geometry
		 */
		FeaturePropertyType foi = observation.getFeatureOfInterest();
		XmlObject feature = XmlObject.Factory.parse(foi.toString());

		parseFeature(feature, events);

		return events;
	}





	private void parseFeature(XmlObject feature, List<MapEvent> events) throws com.vividsolutions.jts.io.ParseException, XmlException {
		Geometry geom = null;
		String foiID = "";
		if (feature instanceof FeatureCollectionDocument2 || feature instanceof FeatureCollectionDocument) {
			AbstractFeatureCollectionType fct;
			if (feature instanceof FeatureCollectionDocument2) {
				fct = ((FeatureCollectionDocument2) feature).getFeatureCollection();	
			} else {
				fct = ((FeatureCollectionDocument) feature).getFeatureCollection();
			}
			
			FeaturePropertyType[] fmarray = fct.getFeatureMemberArray();

			for (FeaturePropertyType fpt : fmarray) {

				XmlObject member = XmlObject.Factory.parse(fpt.toString());

				/*
				 * recursive call
				 */
				parseFeature(member, events);
				return;
			}
		}

		/*
		 * no feature collection
		 */
		else if (feature instanceof SamplingPointDocument) {
			SamplingPointType spt = ((SamplingPointDocument) feature).getSamplingPoint();
			geom = getPositionFromSamplingPoint(spt);

			if (spt.isSetId()) {
				foiID = spt.getId();
			}
		}

		/*
		 * put geometry
		 */
		if (geom != null) {
			for (MapEvent me : events) {
				me.put(MapEvent.GEOMETRY_KEY, geom);
			}
		}

		/*
		 * put foi id
		 */
		if (!foiID.equals("")) {
			for (MapEvent me : events) {
				me.put(MapEvent.FOI_ID_KEY, foiID);
			}
		}		
	}


	private boolean parseDataRecord(XmlObject xobj,	ArrayList<MapEvent> events) {
		DataRecordType dr = null;
		if (xobj instanceof DataRecordDocument) {
			dr = ((DataRecordDocument) xobj).getDataRecord();
		}
		else if (xobj instanceof DataRecordType) {
			dr = (DataRecordType) xobj;
		}
		
		if (dr != null) {
			DataComponentPropertyType[] fields = dr.getFieldArray();
			
			/*
			 * new event with dummy time, later changed
			 */
			MapEvent newEvent = new MapEvent(0, 0);

			for (DataComponentPropertyType field : fields) {
				if (field.isSetTime()) {
					/*
					 * change the timestamp
					 */
					Time time = field.getTime();
					DateTime dateTime = new DateTime(time.getValue());
					newEvent.put(MapEvent.START_KEY, dateTime.getMillis());
					newEvent.put(MapEvent.END_KEY, dateTime.getMillis());
				}
				else if (field.isSetQuantity()) {
					/*
					 * parse quantity
					 */
					Quantity quan = field.getQuantity();
					double value = quan.getValue();
					String name = field.getName();
					if (quan.isSetUom()) {
						if (quan.getUom().isSetCode()) {
							NumberWithUOM temp = this.unitConverter.convert(quan.getUom().getCode(), value);
							newEvent.put(name, temp.getValue());
						}
						else newEvent.put(name, value);
					}
					else newEvent.put(name, value);
				}
				else if (field.isSetCount()) {
					/*
					 * parse count
					 */
				}
			}
			
			events.add(newEvent);
			
			/*
			 * we succeeded
			 */
			return true;
		}
		
		/*
		 * we failed
		 */
		return false;
	}




	/**
	 * Parses a DataArray inside the om:result
	 * 
	 * @param dad the {@link DataArrayDocument}
	 * @param pums List of phenomenon/uom models
	 * @param events new Events are added to this list
	 * @throws Exception
	 */
	private void parseDataArray(DataArrayDocument dad, List<Map<String, Object>> pums,
			ArrayList<MapEvent> events) throws Exception {

		//TODO: add parsing of doubleValue and stringValue

		DataArrayType array = dad.getDataArray1();
		BigInteger elemCount = array.getElementCount().getCount().getValue();

		//get the type
		DataComponentPropertyType elemType = array.getElementType();

		/*
		 * Build event structure.
		 */
		if (elemType.isSetAbstractDataRecord()) {
			AbstractDataRecordType adr = elemType.getAbstractDataRecord();

			//SimpleDataRecord
			if (adr instanceof SimpleDataRecordType) {
				SimpleDataRecordType sdr = (SimpleDataRecordType) adr;
				Map<String, Object> pum;
				for (int i = 0; i < sdr.getFieldArray().length; i++) {
					pum = getPhenomenonAndUomFromScalar(sdr.getFieldArray(i));
					pum.put(POSITION_INT_KEY, i);
					pums.add(pum);
				}
			}

			else if (adr instanceof DataRecordType) {
				DataRecordType sdr = (DataRecordType) adr;
				for (int i = 0; i < sdr.getFieldArray().length; i++) {
					Map<String, Object> pum = getPhenomenonAndUomFromComponent(sdr.getFieldArray(i));
					pum.put(POSITION_INT_KEY, i);
					pums.add(pum);
				}
			}
		}


		/*
		 * Fill MapEvent with data.
		 */
		final TextBlock textblock = array.getEncoding().getTextBlock();
		final String blockS = textblock.getBlockSeparator();
		final String tokenS = textblock.getTokenSeparator();
		final String decimalS = textblock.getDecimalSeparator();

		Element elem = (Element) array.getValues().getDomNode();
		String values = XmlUtils.toString(elem.getFirstChild()).trim();

		StringTokenizer line = new StringTokenizer(values, blockS);

		String current = null;
		MapEvent newEvent;
		StringTokenizer fields;
		boolean firstQuantity;
		Object finalValue = null;
		int count = 0;
		int i = 0;
		while (line.hasMoreTokens()) {
			i = 0;
			newEvent = new MapEvent(0, 0);
			firstQuantity = true;
			current = line.nextToken();
			fields = new StringTokenizer(current, tokenS);

			while (fields.hasMoreTokens()) {

				Map<String, Object> pum = pums.get(i);

				if (i != ((Integer) pum.get(POSITION_INT_KEY)) ) {
					OMParser.logger.warn("Probably using wrong phenomenon for values! Recheck code.");
				}
				current = fields.nextToken();

				if ((Boolean) pum.get(IS_UCUM_BOOLEAN_KEY)) {
					//got value for conversion
					current.replaceAll(decimalS, ".");
					Double value = Double.NaN;
					try {
						value = Double.parseDouble(current);
					} catch (NumberFormatException e) {
						OMParser.logger.warn(e.getMessage(), e);
					}

					if (value != Double.NaN) {
						//						Object[] conv = this.converter.convert((String) pum.get(UOM_STRING_KEY),
						//								(String) pum.get(PHENOMENON_STRING_KEY), value);
						//no usage of registered phenomena as their registration seems not to work (and is also not intended / needed)
						String uom = (String) pum.get(UOM_STRING_KEY);
						double converted = UOMTools.convertToBaseUnit(value, uom);
						newEvent.put((String) pum.get(PHENOMENON_STRING_KEY), converted);

						if (firstQuantity) {
							//it is the first quantity, set as final value
							finalValue = converted;
							firstQuantity = false;
						}
					}
					//					else {
					//						newEvent.put((String) pum.get(PHENOMENON_STRING_KEY), value);
					//					}
					//					no else!! would result in Double.NaN in the event.

				} 
				else if ((Boolean) pum.get(IS_TIME_BOOLEAN_KEY)) {
					long timeStamp = getTimestamp(current, pum);
					newEvent.put(MapEvent.START_KEY, timeStamp);
					newEvent.put(MapEvent.END_KEY, timeStamp);
				}
				else {
					newEvent.put((String) pum.get(PHENOMENON_STRING_KEY), current);
					if (finalValue != null) {
						//final value has not yet been set, use this one
						finalValue = current;
					}
				}

				i++;
			}

			//set the value field
			if (finalValue != null) {
				newEvent.put(MapEvent.VALUE_KEY, finalValue);
			}

			events.add(newEvent);
			count++;
		}

		if (count != elemCount.intValue()) {
			OMParser.logger.info("Unexpected element count: expected "+ elemCount +", got "+
					count +".");
		}		
	}


	/**
	 * Process and parse any type of swe:AnyScalar inside the om:result
	 * 
	 * @param omResultContent the anyScalar element
	 * @param events resulting events are added to this list
	 * @return true if an AnyScalar type was found and parsed.
	 */
	private boolean parseAnyScalarTypes(XmlObject omResultContent, ArrayList<MapEvent> events) {
		if (omResultContent == null) return false;

		/*
		 * select children that are of AnyScalar group
		 */
		String sweNS = "http://www.opengis.net/swe/1.0.1";
		QName[] qnArray = new QName[] {
				new QName(sweNS, "Count"),
				new QName(sweNS, "Quantity"),
				new QName(sweNS, "Time"),
				new QName(sweNS, "Boolean"),
				new QName(sweNS, "Category"),
				new QName(sweNS, "Text")
		};

		QNameSet qns = QNameSet.forArray(qnArray);
		qnArray = null;
		XmlObject[] anyScalars = omResultContent.selectChildren(qns);


		/*
		 * if found any, parse the results
		 */
		if (anyScalars.length > 0) {

			for (XmlObject as : anyScalars) {

				UomPropertyType uom = null;
				double doubleValue = Double.NaN;
				String stringValue = null;
				String definition = null;

				/*
				 * AnyNumerical
				 */
				if (as instanceof Count) {
					Count count = (Count) as;

					if (count.isSetValue()) {
						doubleValue = Double.parseDouble(""+ count.getValue());
					}

					if (count.isSetDefinition()) {
						definition = count.getDefinition();
					}
				}
				else if (as instanceof Quantity) {
					Quantity quant = (Quantity) as;

					if (quant.isSetUom()) {
						uom = quant.getUom();
					}

					if (quant.isSetValue()) {
						doubleValue = quant.getValue();
					}

					if (quant.isSetDefinition()) {
						definition = quant.getDefinition();
					}
				}
				else if (as instanceof Time) {
					//TODO parse
				}

				/*
				 * Other AnyScalars
				 */
				else if (as instanceof net.opengis.swe.x101.BooleanDocument.Boolean) {
					net.opengis.swe.x101.BooleanDocument.Boolean bool = 
						(net.opengis.swe.x101.BooleanDocument.Boolean) as;

					if (bool.isSetValue()) {
						stringValue = ""+ bool.getValue();
					}

					if (bool.isSetDefinition()) {
						definition = bool.getDefinition();
					}
				}
				else if (as instanceof Category) {
					Category cat = (Category) as;

					if (cat.isSetValue()) {
						stringValue = cat.getValue();
					}

					if (cat.isSetDefinition()) {
						definition = cat.getDefinition();
					}
				}
				else if (as instanceof Text) {
					Text text = (Text) as;

					if (text.isSetValue()) {
						stringValue = text.getValue();
					}

					if (text.isSetDefinition()) {
						definition = text.getDefinition();
					}
				}

				//convert if a uom was found
				if (uom != null && doubleValue != Double.NaN) {
					try {
						//convert
						doubleValue = (Double) this.unitConverter.convert(uom.getCode(), doubleValue).getValue();
					}
					catch (Throwable t) {
						OMParser.logger.info("could not convert uom '" + uom + "' to base unit, reason: " + t.getMessage());
					}
				}

				/*
				 * generate new event
				 */
				MapEvent newEvent = new MapEvent(0, 0);
				if (doubleValue != Double.NaN) {
					newEvent.put(MapEvent.VALUE_KEY, doubleValue);
					newEvent.put(MapEvent.RESULT_KEY, doubleValue);
					//add to new event with definition (observed property)
					if (definition != null) {
						newEvent.put(definition.replaceAll(":", "__").replaceAll("\\.", "_"),
								doubleValue);
					}
				} else if (stringValue != null) {
					newEvent.put(MapEvent.STRING_VALUE_KEY,  stringValue);
					newEvent.put(MapEvent.RESULT_KEY, stringValue);
					//add to new event with definition (observed property)
					if (definition != null) {
						newEvent.put(definition.replaceAll(":", "__").replaceAll("\\.", "_"),
								stringValue);

					}
				}

				events.add(newEvent);
			}

		}
		else {
			return false;
		}


		return true;
	}




	/**
	 * Nothing else worked. Parse as a single double or string value in the om:result here.
	 * @param anyTypeDoc the xml-fragement.
	 * @param events resulting events are added to this list.
	 */
	private void parseAsSingleValue(XmlAnyTypeImpl anyTypeDoc, ArrayList<MapEvent> events) {
		double value = Double.NaN;
		boolean isDouble = true;
		try {

			String s = anyTypeDoc.getStringValue().trim();

			value = Double.parseDouble(s);
		}
		catch (Exception ex) {
			//not a double? contine
			isDouble = false;
		}


		if (isDouble) {
			//result is a double value

			MapEvent newEvent = new MapEvent(0, 0);

			String uom = "";

			int attributeLenght = anyTypeDoc.getDomNode().getAttributes().getLength();

			String localName;

			//try to find a uom attribute
			for (int i = 0; i < attributeLenght; i++) {
				localName = anyTypeDoc.getDomNode().getAttributes().item(i).getLocalName();

				if (localName.equals("uom")) {
					uom = anyTypeDoc.getDomNode().getAttributes().item(i).getNodeValue();
				}
			}

			//if uom was found, convert the value
			if (!uom.equals("")) {
				try {
					//convert
					value = (Double) this.unitConverter.convert(uom, value).getValue();
				}
				catch (Throwable t) {
					OMParser.logger.info("could not convert uom '" + uom + "' to base unit, reason: " + t.getMessage());
				}
			}

			newEvent.put(MapEvent.VALUE_KEY, value);
			newEvent.put(MapEvent.RESULT_KEY, value);
			newEvent.put(MapEvent.DOUBLE_VALUE_KEY, value);
			newEvent.put(MapEvent.STRING_VALUE_KEY, "" + value);

			events.add(newEvent);
		}

		else {
			//parse as a simple string, nothing else worked
			MapEvent newEvent = new MapEvent(0, 0);

			String textValue = anyTypeDoc.getStringValue().trim();
			newEvent.put(MapEvent.VALUE_KEY, textValue);
			newEvent.put(MapEvent.RESULT_KEY, textValue);
			newEvent.put(MapEvent.STRING_VALUE_KEY, textValue);

			events.add(newEvent);
		}		
	}


	/**
	 * Parse the position from a given {@link SamplingPointType}
	 */
	private Geometry getPositionFromSamplingPoint(SamplingPointType spt) throws com.vividsolutions.jts.io.ParseException {
		if (spt.getPosition().isSetPoint()) {
			PointType point = spt.getPosition().getPoint();

			if (point.isSetPos()) {
				WKTReader reader = new WKTReader();
				return reader.read("POINT("+ point.getPos().getStringValue() + ")");
			}
		}

		else if (spt.isSetBoundedBy()) {
			BoundingShapeType bb = spt.getBoundedBy();
			EnvelopeType env = bb.getEnvelope();
			return getGeometryFromEnvelope(env);
		}

		return null;
	}

	/**
	 * Parse the position from a given {@link EnvelopeType}
	 */
	private Geometry getGeometryFromEnvelope(EnvelopeType et) throws com.vividsolutions.jts.io.ParseException {
		Element elem = (Element) et.getUpperCorner().getDomNode();
		String ucString = XmlUtils.toString(elem.getFirstChild()).trim();
		String[] uc = ucString.split(" ");

		int i = 0;
		for (String string : uc) {
			string = string.trim();
			uc[i] = string;
			i++;
		}

		//get lower corner and split at " "
		elem = (Element) et.getLowerCorner().getDomNode();
		String lcString = XmlUtils.toString(elem.getFirstChild()).trim();
		String[] lc = lcString.split(" ");

		i = 0;
		for (String string : lc) {
			string = string.trim();
			lc[i] = string;
			i++;
		}

		String wktString = "POLYGON(("+ lc[0] +" "+ lc[1] +", "+ lc[0] +" "+ uc[1] +", "+ uc[0] +" "+ uc[1] +
		", "+ uc[0] +" "+ lc[1] +", "+ lc[0] +" "+ lc[1]+ "))";

		WKTReader wktReader = new WKTReader();
		return wktReader.read(wktString);		
	}

	/**
	 * Parse the phenomenon and UOM from an {@link AnyScalarPropertyType}
	 */
	private Map<String, Object> getPhenomenonAndUomFromScalar(AnyScalarPropertyType scalar) throws Exception {

		Map<String, Object> result = new HashMap<String, Object>();
		result.put(IS_TIME_BOOLEAN_KEY, false);
		result.put(IS_UCUM_BOOLEAN_KEY, false);
		result.put(POSITION_INT_KEY, -1);

		String phen;
		String uom;

		if (scalar.isSetBoolean()) {
			phen = scalar.getBoolean().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
			uom = "";
		} else if (scalar.isSetQuantity()) {
			phen = scalar.getQuantity().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
			uom = getUom(scalar.getQuantity().getUom(), result);
		} else if (scalar.isSetCount()) {
			phen = scalar.getCount().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
			uom = "";
		} else if (scalar.isSetTime()) {
			phen = scalar.getTime().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
			uom = getUom(scalar.getTime().getUom(), result);
			result.put(IS_TIME_BOOLEAN_KEY, true);
		} else if (scalar.isSetText()) {
			uom = "";
			phen = scalar.getText().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
		} else if(scalar.isSetCategory()) {
			uom = "";
			phen = scalar.getCategory().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
		} else {
			return null;
		}

		result.put(PHENOMENON_STRING_KEY, phen);
		result.put(UOM_STRING_KEY, uom);

		return result;
	}



	/**
	 * Parse the phenomenon and UOM from an {@link DataComponentPropertyType}
	 */
	private Map<String, Object> getPhenomenonAndUomFromComponent(
			DataComponentPropertyType component) throws Exception {

		Map<String, Object> result = new HashMap<String, Object>();
		result.put(IS_TIME_BOOLEAN_KEY, false);
		result.put(IS_UCUM_BOOLEAN_KEY, false);
		result.put(POSITION_INT_KEY, -1);

		String phen;
		String uom;

		if (component.isSetBoolean()) {
			phen = component.getBoolean().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
			uom = "";
		} else if (component.isSetQuantity()) {
			phen = component.getQuantity().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
			uom = getUom(component.getQuantity().getUom(), result);
		} else if (component.isSetCount()) {
			phen = component.getCount().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
			uom = "";
		} else if (component.isSetTime()) {
			phen = component.getTime().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
			uom = getUom(component.getTime().getUom(), result);
			result.put(IS_TIME_BOOLEAN_KEY, true);
		} else if (component.isSetText()) {
			uom = "";
			phen = component.getText().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
		} else if(component.isSetCategory()) {
			uom = "";
			phen = component.getCategory().getDefinition().replaceAll(":", "__").replaceAll("\\.", "_");
		} else {
			throw new SoapFault("the " +
					"DataComponentPropertyType is an unsupported type:" + component.getType());
		}

		result.put(PHENOMENON_STRING_KEY, phen);
		result.put(UOM_STRING_KEY, uom);

		return result;
	}


	/**
	 * Get the uom of a {@link UomPropertyType}
	 */
	private String getUom(UomPropertyType uom, Map<String, Object> result) {
		if (uom != null) {
			if (uom.isSetHref()) {
				return uom.getHref();
			}

			else if (uom.isSetCode()) {
				result.put(IS_UCUM_BOOLEAN_KEY, true);
				return uom.getCode();
			}

			//TODO implement UnitDefinition
		}

		return "";
	}


	/**
	 * parses the time provided
	 * 
	 * @param time the time to parse
	 * @param pum uom helper
	 * 
	 * @return the time as long
	 * @throws ParseException exception
	 */
	private long getTimestamp(String time, Map<String, Object> pum) throws ParseException {
		if (pum != null && pum.get(UOM_STRING_KEY).equals("")) {
			//TODO: do something with the pum here...
			//treat as normal timestamp according ISO8601
			DateTime dt = new DateTime(time);
			return dt.getMillis();
		}
		//try also joda time
		DateTime dt = new DateTime(time);
		return dt.getMillis();
	}

	@Override
	public boolean accept(NotificationMessage message) {
		QName omQName = new QName(OM_NAMESPACE, "Observation");
		Element content = message.getMessageContent(omQName);

		if (content != null) {
			return true;
		}
		
		return false;
	}

	@Override
	public List<MapEvent> parse(NotificationMessage message) throws Exception {
		QName omQName = new QName(OM_NAMESPACE, "Observation");
		Element content = message.getMessageContent(omQName);

		if (content != null) {
			XmlObject oDoc = XMLBeansParser.parse(content, true);
			return this.parseOM((ObservationDocument) oDoc);
		}
		
		return null;
	}


	@Override
	protected String getName() {
		return "OMParser";
	}

}
