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
package org.n52.ses.io.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.BoundedByDocument;
import net.opengis.gml.x32.EnvelopeDocument;
import net.opengis.gml.x32.LineStringDocument;
import net.opengis.gml.x32.PointDocument;
import net.opengis.gml.x32.PolygonDocument;

import org.apache.muse.ws.notification.NotificationMessage;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.oxf.xmlbeans.parser.XMLBeansParser;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.n52.ses.api.AbstractParser;
import org.n52.ses.api.event.MapEvent;
import org.n52.ses.api.exception.GMLParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/**
 * 
 * Class for parsing WXXM data.
 * 
 * @author Matthes Rieke <m.rieke@uni-muenster.de>
 *
 */
public class WXXMParser extends AbstractParser {

	private static final Logger logger = LoggerFactory
			.getLogger(WXXMParser.class);

	/**
	 * namespace used for WXXM data
	 */
	public static final String WXXM_NAMESPACE = "http://www.eurocontrol.int/wx/1.1";

	/**
	 * namespace used for AVWX data
	 */
	public static final String AVWX_NAMESPACE = "http://www.eurocontrol.int/avwx/1.1";

	/*
	 * WX and AVWX qnames
	 */
	private static final QName AERODROME_FC_NAME = new QName(AVWX_NAMESPACE, "aerodromeWxForecast");
	private static final QName AIRSPACE_AREA_FC_NAME = new QName(AVWX_NAMESPACE, "airspaceAreaForecast");
	private static final QName AERODROME_OBS_NAME = new QName(AVWX_NAMESPACE, "aerodromeWxObservation");
	private static final QName AIRSPACE_OBS_NAME = new QName(AVWX_NAMESPACE, "airspaceWxObservation");
	private static final QName FORECAST_NAME = new QName(WXXM_NAMESPACE, "Forecast");
	private static final QName APPLIES_TO_NAME = new QName(AVWX_NAMESPACE, "appliesTo");
	private static final QName ISSUED_FOR_NAME = new QName(AVWX_NAMESPACE, "issuedFor");
	private static final QName AERODROME_NAME = new QName(AVWX_NAMESPACE, "Aerodrome");
	private static final QName AIRSPACE_NAME = new QName(AVWX_NAMESPACE, "Airspace");
	private static final QName VALID_TIME_NAME = new QName(WXXM_NAMESPACE, "validTime");
	private static final QName ISSUE_TIME_NAME = new QName(AVWX_NAMESPACE, "issueTime");
	private static final QName FORECAST_ANALYSIS_TIME_NAME = new QName(WXXM_NAMESPACE,
	"forecastAnalysisTime");
	private static final QName FEATURE_COLLECTION_NAME = new QName(WXXM_NAMESPACE, "FeatureCollection");
	private static final QName FEATURE_MEMBER_NAME = new QName(WXXM_NAMESPACE, "featureMember");
	private static final QName WX_OBSERVATION_NAME = new QName(WXXM_NAMESPACE, "Observation");

	/*
	 * OM qnames
	 */
	private static final QName OM_FOI_NAME = new QName(OMParser.OM_GML32_NAMESPACE, "featureOfInterest");

	/*
	 * GML qnames
	 */
	private static final QName LOCATION_NAME = new QName(GML32Parser.GML32_NAMESPACE, "location");
	private static final QName BOUNDED_BY_NAME = new QName(GML32Parser.GML32_NAMESPACE, "boundedBy");
	private static final QName IDENTIFIER_NAME = new QName(GML32Parser.GML32_NAMESPACE, "identifier");


	/*
	 * XPath expressions
	 */
	private static final String SELECT_TAF_XPATH = "declare namespace avwx='" +
	AVWX_NAMESPACE + "'; .//avwx:TAF";
	private static final String SELECT_SIGMET_XPATH = "declare namespace avwx='" +
	AVWX_NAMESPACE + "'; .//avwx:SIGMET";
	private static final String SELECT_METAR_XPATH = "declare namespace avwx='" +
	AVWX_NAMESPACE + "'; .//avwx:METAR";
	private static final String SELECT_PIREP_XPATH = "declare namespace avwx='" +
	AVWX_NAMESPACE + "'; .//avwx:PIREP";
	private static final String SELECT_EXTENT_OF_XPATH = "declare namespace wx='" +
	WXXM_NAMESPACE + "'; .//wx:extentOf";
	private static final String SELECT_ICAO_CODE_XPATH = "declare namespace avwx='" +
	AVWX_NAMESPACE + "'; .//avwx:icaoCode";
	private static final String SELECT_SYSTEMS_XPATH = "declare namespace avwx='" +
	AVWX_NAMESPACE + "'; " + "declare namespace wx='" +
	WXXM_NAMESPACE + "'; " + "declare namespace gml='" +
	GML32Parser.GML32_NAMESPACE + "'; .//avwx:process//wx:System/gml:name";

	private static final String AVWX_TYPE_KEY = "avwxType";


	
	/**
	 * default constructor
	 */
	public WXXMParser() {
		//nothing to do
	}

	/**
	 * @param collection the collection of WXXM data
	 * @return List of data elements
	 */
	public List<MapEvent> parseWXXM(XmlObject collection) {
		ArrayList<MapEvent> results = new ArrayList<MapEvent>();

		/*
		 * parse TAFs
		 */
		parseTAFs(results, collection);

		/*
		 * parse SIGMETs
		 */
		parseSIGMETs(results, collection);

		/*
		 * parse METARs
		 */
		parseMETARs(results, collection);

		/*
		 * parse PIREPs
		 */
		parsePIREPs(results, collection);

		/*
		 * check if we do not have any results.
		 * then try to parse other stuff (e.g., severe weather model)
		 */
		if (results.size() == 0) {
			parseOther(results, collection);
		}

		return results;
	}



	private void parseTAFs(ArrayList<MapEvent> results, XmlObject collection) {
		XmlObject[] tafs = XmlUtil.selectPath(SELECT_TAF_XPATH, collection);

		if (tafs != null && tafs.length > 0) {
			for (XmlObject taf : tafs) {
				Geometry tafGeometry = null;
				DateTime tafTime = null;
				String tafAeroFeature = null;

				/*
				 * 
				 * get the TAF-wide geometry from the appliesTo element.
				 * 
				 */

				XmlObject[] appTo = taf.selectChildren(APPLIES_TO_NAME);

				if (appTo.length > 0) {
					XmlObject[] aero = appTo[0].selectChildren(AERODROME_NAME);

					if (aero.length > 0) {
						try {
							tafGeometry = getGeometryFromLocationOrBoundedBy(aero[0]);
						}
						catch (Exception e) {
							logger.warn(e.getMessage(), e);
						}

						tafAeroFeature = XmlUtil.stripText(aero[0].selectChildren(IDENTIFIER_NAME));
					}
				}

				/*
				 * could also be child of TAF-element
				 */
				if (tafGeometry == null) {
					try {
						tafGeometry = getGeometryFromLocationOrBoundedBy(taf);
					}
					catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}

				/*
				 * get the issue time, used if time from forecast could
				 * not be parsed.
				 */

				XmlObject[] issue = taf.selectChildren(ISSUE_TIME_NAME);
				if (issue.length > 0) {
					tafTime = new DateTime(issue[0].newCursor().getTextValue());
				}


				/*
				 * 
				 * get the Forecast objects
				 * 
				 */
				XmlObject[] aeroFCs = taf.selectChildren(AERODROME_FC_NAME);

				/*
				 * multiple forecasts possible.
				 * generate one MapEvent for each
				 */

				for (XmlObject aefc : aeroFCs) {
					DateTime[] dateTimes = null;
					Geometry geometry = null;
					String aeroFeature = tafAeroFeature;

					/*
					 * select the Forecast
					 */
					XmlObject[] fc = aefc.selectChildren(FORECAST_NAME);
					if (fc.length > 0) {
						/*
						 * select time
						 */
						dateTimes = getTimeFromForecast(fc[0]);

						/*
						 * select the geometry
						 */
						geometry = getGeometryFromForecast(fc[0]);
					}

					if (dateTimes == null || dateTimes.length == 0) {
						// time could not be parsed, try taf-wide
						// issueTime
						if (tafTime != null) {
							dateTimes = new DateTime[] {tafTime};
						}
						else {
							logger.warn("Time for current TAF could not be parsed. skipping");
							continue;
						}
					}

					/*
					 * generate the new MapEvent
					 */
					if (geometry == null && tafGeometry != null) {
						geometry = tafGeometry;
					}

					MapEvent newEvent = createMapEvent(dateTimes, geometry);

					if (newEvent == null) continue;

					if (aeroFeature != null) {
						newEvent.put(AIXMParser.AERO_FEATURE_KEY, aeroFeature);
					}

					newEvent.put(AVWX_TYPE_KEY, "TAF");

					results.add(newEvent);

				}


			}
		}		
	}


	private void parseSIGMETs(ArrayList<MapEvent> results, XmlObject collection) {
		XmlObject[] sigmets = XmlUtil.selectPath(SELECT_SIGMET_XPATH, collection);

		if (sigmets != null && sigmets.length > 0) {
			for (XmlObject sigmet : sigmets) {
				Geometry sigmetGeometry = null;
				DateTime sigmetTime = null;

				/*
				 * get SIGMET-wide geometry
				 * issuedFor
				 */
				XmlObject[] issuedFor = sigmet.selectChildren(ISSUED_FOR_NAME);
				if (issuedFor.length > 0) {
					/*
					 * Airspace
					 */
					XmlObject[] airspace = issuedFor[0].selectChildren(AIRSPACE_NAME);
					if (airspace.length > 0) {
						try {
							sigmetGeometry = getGeometryFromLocationOrBoundedBy(airspace[0]);
						} catch (Exception e) {
							logger.warn(e.getMessage(), e);
						}
					}
				}

				//try the child of sigmet
				if (sigmetGeometry == null) {
					try {
						sigmetGeometry = getGeometryFromLocationOrBoundedBy(sigmet);
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}


				/*
				 * get the issue time, used if time from forecast could
				 * not be parsed.
				 */

				XmlObject[] issue = sigmet.selectChildren(ISSUE_TIME_NAME);
				if (issue.length > 0) {
					sigmetTime = new DateTime(issue[0].newCursor().getTextValue());
				}


				XmlObject[] aaFCs = sigmet.selectChildren(AIRSPACE_AREA_FC_NAME);

				/*
				 * multiple AIRSPACE_AREA_Forecast possible
				 */
				for (XmlObject aafc : aaFCs) {
					Geometry geometry = null;
					DateTime[] dateTimes = null;
					String aeroFeature = null;

					XmlObject[] fc = aafc.selectChildren(FORECAST_NAME);

					if (fc.length > 0) {
						/*
						 * parse the geometry
						 */
						geometry = getGeometryFromForecast(fc[0]);

						/*
						 * parse the time
						 */
						dateTimes = getTimeFromForecast(fc[0]);

						XmlObject[] foi = fc[0].selectChildren(OM_FOI_NAME);

						if (foi.length > 0) {
							aeroFeature = XmlUtil.stripText(XmlUtil.selectPath(SELECT_ICAO_CODE_XPATH, foi[0]));
						}

					}

					if (dateTimes == null || dateTimes.length == 0) {
						// time could not be parsed, try TAF-wide
						// issueTime
//						if (sigmet != null) {
						dateTimes = new DateTime[] {sigmetTime};
//						}
					}

//					if (dateTimes != null) {
					/*
					 * generate the new MapEvent
					 */
					if (geometry == null && sigmetGeometry != null) {
						geometry = sigmetGeometry;
					}

					MapEvent newEvent = createMapEvent(dateTimes, geometry);

					if (newEvent == null) continue;

					if (aeroFeature != null) {
						newEvent.put(AIXMParser.AERO_FEATURE_KEY, aeroFeature);
					}

					newEvent.put(AVWX_TYPE_KEY, "SIGMET");

					results.add(newEvent);
//					}
//					else {
//						log.warning("Time for current SIGMET could not be parsed. skipping");
//					}

				}
			}
		}
	}


	private void parseMETARs(ArrayList<MapEvent> results, XmlObject collection) {
		XmlObject[] metars = XmlUtil.selectPath(SELECT_METAR_XPATH, collection);

		if (metars != null && metars.length > 0) {
			for (XmlObject metar : metars) {
				Geometry metarGeometry = null;
				DateTime metarTime = null;
				String metarAeroFeature = null;

				/*
				 * 
				 * get the TAF-wide geometry from the appliesTo element.
				 * 
				 */

				XmlObject[] appTo = metar.selectChildren(APPLIES_TO_NAME);

				if (appTo.length > 0) {
					XmlObject[] aero = appTo[0].selectChildren(AERODROME_NAME);

					if (aero.length > 0) {
						try {
							metarGeometry = getGeometryFromLocationOrBoundedBy(aero[0]);
						}
						catch (Exception e) {
							logger.warn(e.getMessage(), e);
						}

						metarAeroFeature = XmlUtil.stripText(aero[0].selectChildren(IDENTIFIER_NAME));
					}
				}

				/*
				 * could also be child of TAF-element
				 */
				if (metarGeometry == null) {
					try {
						metarGeometry = getGeometryFromLocationOrBoundedBy(metar);
					}
					catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}

				/*
				 * get the issue time, used if time from forecast could
				 * not be parsed.
				 */

				XmlObject[] issue = metar.selectChildren(ISSUE_TIME_NAME);
				if (issue.length > 0) {
					metarTime = new DateTime(issue[0].newCursor().getTextValue());
				}


				/*
				 * 
				 * get the Observation objects
				 * 
				 */
				XmlObject[] aeroOBs = metar.selectChildren(AERODROME_OBS_NAME);

				/*
				 * multiple Observations possible.
				 * generate one MapEvent for each
				 */

				for (XmlObject aeob : aeroOBs) {
					DateTime[] dateTimes = null;
					Geometry geometry = null;
					String aeroFeature = metarAeroFeature;

					/*
					 * try forecast
					 */
					XmlObject[] fc = aeob.selectChildren(FORECAST_NAME);

					if (fc.length > 0) {
						dateTimes = getTimeFromForecast(fc[0]);

						geometry = getGeometryFromForecast(fc[0]);

					}
					else {

						/*
						 * try observation
						 */
						XmlObject[] obs = aeob.selectChildren(WX_OBSERVATION_NAME);

						if (obs.length > 0) {
							dateTimes = getTimeFromObservation(obs[0]);

							geometry = getGeometryFromObservation(obs[0]);

						}
					}

					if ((dateTimes == null || dateTimes.length == 0) && metarTime != null) {
						dateTimes = new DateTime[] {metarTime};
					}

					if (geometry == null && metarGeometry != null) {
						geometry = metarGeometry;
					}

					/*
					 * create mapevent
					 */
					MapEvent newEvent = createMapEvent(dateTimes, geometry);

					if (newEvent == null) {
						continue;
					}

					if (aeroFeature != null) {
						newEvent.put(AIXMParser.AERO_FEATURE_KEY, aeroFeature);
					}

					newEvent.put(AVWX_TYPE_KEY, "METAR");

					results.add(newEvent);
				}
			}
		}
	}


	private void parsePIREPs(ArrayList<MapEvent> results, XmlObject collection) {
		XmlObject[] pireps = XmlUtil.selectPath(SELECT_PIREP_XPATH, collection);

		if (pireps != null && pireps.length > 0) {
			for (XmlObject pirep : pireps) {
				Geometry pirepGeometry = null;
				String pirepAeroFeature = null;


				/*
				 * could also be child of PIREP-element
				 */
				try {
					pirepGeometry = getGeometryFromLocationOrBoundedBy(pirep);
				}
				catch (Exception e) {
					logger.warn(e.getMessage(), e);
				}
				
				/*
				 * get the Systems
				 */
				XmlObject[] systems = XmlUtil.selectPath(SELECT_SYSTEMS_XPATH, pirep);
				String[] systemNames = new String[systems.length];
				for (int i = 0; i < systems.length; i++) {
					systemNames[i] = XmlUtil.stripText(systems[i]);
				}
				

				/*
				 * 
				 * get the Observation objects
				 * 
				 */
				XmlObject[] aeroOBs = pirep.selectChildren(AIRSPACE_OBS_NAME);

				/*
				 * multiple Observations possible.
				 * generate one MapEvent for each
				 */

				for (XmlObject aeob : aeroOBs) {
					DateTime[] dateTimes = null;
					Geometry geometry = null;
					String aeroFeature = pirepAeroFeature;


					/*
					 * try observation
					 */
					XmlObject[] obs = aeob.selectChildren(WX_OBSERVATION_NAME);

					if (obs.length > 0) {
						dateTimes = getTimeFromObservation(obs[0]);

						geometry = getGeometryFromObservation(obs[0]);

					}


					if ((dateTimes == null || dateTimes.length == 0)) {
						logger.info("Could not parse time of current PIREP. skipping.");
						continue;
					}

					if (geometry == null && pirepGeometry != null) {
						geometry = pirepGeometry;
					}

					/*
					 * create mapevent
					 */
					MapEvent newEvent = createMapEvent(dateTimes, geometry);

					if (newEvent == null) {
						continue;
					}

					if (aeroFeature != null) {
						newEvent.put(AIXMParser.AERO_FEATURE_KEY, aeroFeature);
					}

					newEvent.put(AVWX_TYPE_KEY, "PIREP");
					
					/*
					 * make a duplicate for each affected system
					 */
					for (String sys : systemNames) {
						MapEvent dupl = new MapEvent(newEvent);
						dupl.put(AIXMParser.AERO_FEATURE_KEY, sys);
						results.add(dupl);
					}
					
				}
			}
		}
	}


	private void parseOther(ArrayList<MapEvent> results, XmlObject collection) {
		XmlObject[] coll = collection.selectChildren(FEATURE_COLLECTION_NAME);
		XmlObject[] members = null;

		if (coll.length > 0) {
			members = coll[0].selectChildren(FEATURE_MEMBER_NAME);		
		}
		else {
			return;
		}

		/*
		 * go through all featureMembers
		 */
		for (XmlObject member : members) {
			DateTime[] dateTimes = null;
			Geometry geometry = null;

			XmlObject[] obs = member.selectChildren(WX_OBSERVATION_NAME);

			/*
			 * parse time
			 */
			if (obs.length > 0) {
				dateTimes = getTimeFromObservation(obs[0]);

				/*
				 * parse geometry
				 */
				geometry = getGeometryFromObservation(obs[0]);
			}

			if (dateTimes != null) {
				MapEvent newEvent = createMapEvent(dateTimes, geometry);

				if (newEvent == null) continue;

				newEvent.put(AVWX_TYPE_KEY, "WXXM");

				results.add(newEvent);
			}
			else {
				logger.warn("Time for current WXXM element could not be parsed. skipping");
				continue;
			}

		}
	}


	private MapEvent createMapEvent(DateTime[] dateTimes, Geometry geometry) {
		MapEvent newEvent = null;
		if (dateTimes.length == 1) {
			newEvent = new MapEvent(dateTimes[0].getMillis(), dateTimes[0].getMillis());
		}
		else if (dateTimes.length == 2) {
			newEvent = new MapEvent(dateTimes[0].getMillis(), dateTimes[1].getMillis());
		}
		else {
			logger.warn("Time for current WXXM element could not be parsed. skipping");
			return null;
		}

		if (geometry != null) {
			newEvent.put(MapEvent.GEOMETRY_KEY, geometry);
		}
		else {
			String time = "";
			for (DateTime dateTime : dateTimes) {
				time += dateTime +", ";
			}
			logger.warn("Current WXXM element has no (parseable) geometry: "+ time);
		}

		return newEvent;
	}



	private Geometry getGeometryFromObservation(XmlObject obs) {
		XmlObject[] foi = obs.selectChildren(OM_FOI_NAME);
		if (foi.length > 0) {
			XmlCursor cursor = foi[0].newCursor();
			cursor.toFirstChild();
			return getGeometryFromLocationOrBoundedBy(cursor.getObject());
		}

		return null;
	}

	private DateTime[] getTimeFromObservation(XmlObject obs) {
		XmlObject[] times = obs.selectChildren(new QName(OMParser.OM_GML32_NAMESPACE,
		"samplingTime"));

		if (times.length == 0) {
			/*
			 * probably O&M/GML32 NS used
			 */
			times = obs.selectChildren(new QName(OMParser.OM_NAMESPACE,
			"samplingTime"));
		}

		if (times.length > 0) {
			return extractTime(times);
		}

		return null;
	}

	private Geometry getGeometryFromLocationOrBoundedBy(XmlObject xobj) {
		XmlObject geomObc = null;
		XmlObject[] geomXML = xobj.selectChildren(LOCATION_NAME);

		/*
		 * try location
		 */
		if (geomXML.length > 0) {
			try {
				geomObc = PointDocument.Factory.parse(geomXML[0].toString());
			} catch (XmlException e) {
				/*
				 * nothing
				 */
			}

			if (geomObc == null) {
				try {
					geomObc = LineStringDocument.Factory.parse(geomXML[0].toString());
				} catch (XmlException e) {
					/*
					 * 
					 */
				}
			}
		}

		/*
		 * try boundedBy
		 */
		else {
			geomXML = xobj.selectChildren(BOUNDED_BY_NAME);

			if (geomXML.length > 0) {
				try {
					geomObc = BoundedByDocument.Factory.parse(
							geomXML[0].toString()).getBoundedBy().getEnvelope();
				} catch (Exception e) {
					/*
					 * nothing
					 */
				}
			}
		}

		if (geomObc != null) {
			try {
				return GML32Parser.parseGeometry(geomObc);
			} catch (ParseException e) {
				logger.warn(e.getMessage(), e);
			} catch (GMLParseException e) {
				logger.warn(e.getMessage(), e);
			}
		}


		return null;
	}





	private Geometry getGeometryFromForecast(XmlObject forecast) {
		/*
		 * get the om:result
		 */
		XmlObject[] omResult = forecast.selectChildren(new QName(OMParser.OM_GML32_NAMESPACE,
		"result"));

		/*
		 * om1.0/gml31 is used
		 */
		if (omResult.length == 0) {
			omResult = forecast.selectChildren(new QName(OMParser.OM_NAMESPACE,
			"result"));
		}

		if (omResult.length > 0) {
			XmlObject[] extents = XmlUtil.selectPath(SELECT_EXTENT_OF_XPATH, omResult[0]);

			if (extents.length > 0) {
				XmlObject geomElem = null;
				try {
					//Polygon
					geomElem = PolygonDocument.Factory.parse(extents[0].toString());

					//Envelope
					if (geomElem == null) {
						EnvelopeDocument.Factory.parse(extents[0].toString());
					}

					//try parsing
					if (geomElem != null) {
						return GML32Parser.parseGeometry(geomElem);
					}
				} catch (XmlException e) {
					logger.warn(e.getMessage(), e);
				} catch (ParseException e) {
					logger.warn(e.getMessage(), e);
				} catch (GMLParseException e) {
					logger.warn(e.getMessage(), e);
				}
			}

			/*
			 * TODO other elements in om:result where geometry could be set
			 */
		}

		/*
		 * TODO parse om:featureOfInterest? many possibilities
		 */

		/*
		 * try location or boundedBy
		 */
		return getGeometryFromLocationOrBoundedBy(forecast);
	}

	private DateTime[] getTimeFromForecast(XmlObject forecast) {
		DateTime[] result = null;

		/*
		 * first try the wx:validTime
		 * it should be more precise
		 */
		XmlObject[] times = forecast.selectChildren(VALID_TIME_NAME);

		// om:samplingTime is mandatory, hence always there
		if (times.length == 0) {
			times = forecast.selectChildren(new QName(OMParser.OM_GML32_NAMESPACE,
			"samplingTime"));
		}

		if (times.length == 0) {
			/*
			 * probably O&M/GML31 NS used
			 */
			times = forecast.selectChildren(new QName(OMParser.OM_NAMESPACE,
			"samplingTime"));
		}

		/*
		 * try FORECAST_ANALYSIS_TIME_NAME
		 */
		if (times.length == 0) {
			times = forecast.selectChildren(FORECAST_ANALYSIS_TIME_NAME);
		}

		result = extractTime(times);

		return result;
	}



	private DateTime[] extractTime(XmlObject[] times) {
		if (times.length > 0) {
			try {
				//create doc
				XmlObject time = XmlObject.Factory.parse(
						times[0].toString());

				//try parsing
				if (time != null) {
					return GML32Parser.parseTime(time);
				}

			} catch (XmlException e) {
				logger.warn(e.getMessage(), e);
			}
		}

		return null;
	}




	/**
	 * @param args not used
	 */
	public static void main(String[] args) {
		File f = new File("D:/pirep1.xml");
		try {
			logger.info("parse METAR");
			List<MapEvent> evs = new WXXMParser().parseWXXM(XmlObject.Factory.parse(f));
			for (MapEvent mapEvent : evs) {
				System.err.println(mapEvent);
			}

						logger.info("parse tafs");
						f = new File("D:/metar1.xml");
						evs = new WXXMParser().parseWXXM(XmlObject.Factory.parse(f));
						for (MapEvent mapEvent : evs) {
							System.err.println(mapEvent);
						}
			//
			//			log.info("parse sigmet");
			//			f = new File("D:/sigmet1.xml");
			//			evs = new WXXMParser(XmlObject.Factory.parse(f)).parseWXXM();
			//			for (MapEvent mapEvent : evs) {
			//				System.err.println(mapEvent);
			//			}
		} catch (XmlException e) {
			logger.warn(e.getMessage(), e);
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	@Override
	public boolean accept(NotificationMessage message) {
		@SuppressWarnings("unchecked")
		Collection<QName> cnames = message.getMessageContentNames();

		for (QName qn : cnames) {
			if (hasWXXMContent(qn, message)) return true;
		}

		return false;
	}
	
	private boolean hasWXXMContent(QName qn, NotificationMessage message) {
		/*
		 * check if root is one of the types
		 */
		if (qn.equals(new QName(WXXMParser.AVWX_NAMESPACE, "TAF"))) {
			return true;
		}
		else if (qn.equals(new QName(WXXMParser.AVWX_NAMESPACE, "SIGMET"))) {
			return true;
		}
		else if (qn.equals(new QName(WXXMParser.AVWX_NAMESPACE, "METAR"))) {
			return true;
		}
		else if (qn.equals(new QName(WXXMParser.AVWX_NAMESPACE, "PIREP"))) {
			return true;
		}
		
		Element content = message.getMessageContent(qn);
		
		/*
		 * else check for subnodes
		 */
		if (content.getElementsByTagNameNS(WXXMParser.AVWX_NAMESPACE, "TAF").getLength() > 0) {
			return true;
		}
		else if (content.getElementsByTagNameNS(WXXMParser.AVWX_NAMESPACE, "SIGMET").getLength() > 0) {
			return true;
		}
		else if (content.getElementsByTagNameNS(WXXMParser.AVWX_NAMESPACE, "METAR").getLength() > 0) {
			return true;
		}
		else if (content.getElementsByTagNameNS(WXXMParser.AVWX_NAMESPACE, "PIREP").getLength() > 0) {
			return true;
		}
		else if (content.getElementsByTagNameNS(WXXMParser.WXXM_NAMESPACE, "featureMember").getLength() > 0) {
			return true;
		}
		
		return false;
	}

	@Override
	public List<MapEvent> parse(NotificationMessage message) throws Exception {
		@SuppressWarnings("unchecked")
		Collection<QName> cnames = message.getMessageContentNames();

		for (QName qn : cnames) {
			if (hasWXXMContent(qn, message)) {
				return parseWXXM(XMLBeansParser.parse(message.getMessageContent(qn), false));
			}
		}
		
		return null;
	}

	@Override
	protected String getName() {
		return "WXXMParser [for TAF, SIGMET, METAR, PIREP]";
	}

}
