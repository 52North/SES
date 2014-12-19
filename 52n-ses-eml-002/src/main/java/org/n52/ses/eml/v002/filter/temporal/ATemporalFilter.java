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
package org.n52.ses.eml.v002.filter.temporal;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.BinaryTemporalOpType;
import net.opengis.fes.x20.TemporalOpsType;

import org.apache.muse.util.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.n52.ses.api.exception.FESParseException;
import org.n52.ses.eml.v002.filter.IFilterElement;
import org.w3c.dom.Node;

/**
 * 
 * @author Thomas Everding
 * 
 */
public abstract class ATemporalFilter implements IFilterElement {

	/**
	 * Factory to build new comparison filters.
	 */
	public static final TemporalFilterFactory	FACTORY					= new TemporalFilterFactory();

	private static final String					FES_NAMESPACE			= "http://www.opengis.net/fes/2.0";
	private static final String					GML_NAMESPACE			= "http://www.opengis.net/gml/3.2";

	/**
	 * qualified name of FES ValueReference
	 */
	protected static final QName				VALUE_REFERENCE_QNAME	= new QName(FES_NAMESPACE, "ValueReference");

	/**
	 * qualified name of GML identifier
	 */
	protected static final QName				GML_IDENTIFIER			= new QName(GML_NAMESPACE, "identifier");

	/**
	 * qualified name of GML validTime
	 */
	protected static final QName				GML_VALID_TIME			= new QName(GML_NAMESPACE, "validTime");

	/**
	 * qualified name of GML TimePeriod
	 */
	protected static final QName				GML_TIME_PERIOD			= new QName(GML_NAMESPACE, "TimePeriod");

	/**
	 * qualified name of GML TimeInstant
	 */
	protected static final QName				GML_TIME_INSTANT		= new QName(GML_NAMESPACE, "TimeInstant");

	/**
	 * qualified name of GML timePosition
	 */
	protected static final QName				GML_TIME_POSITION		= new QName(GML_NAMESPACE, "timePosition");

	/**
	 * qualified name of GML beginPosition
	 */
	protected static final QName				GML_BEGIN_POSITION		= new QName(GML_NAMESPACE, "beginPosition");

	/**
	 * qualified name of GML endPosition
	 */
	protected static final QName				GML_END_POSITION		= new QName(GML_NAMESPACE, "endPosition");

	/**
	 * xbeans {@link TemporalOpsType}
	 */
	protected TemporalOpsType					temporalOp;


	/**
	 * 
	 * Constructor
	 * 
	 * @param temporalOp
	 *            XML representation of a temporal filter
	 */
	public ATemporalFilter(TemporalOpsType temporalOp) {
		this.temporalOp = temporalOp;
	}


	/**
	 * Parses the gml:validTime from a BinaryTemporalOpType
	 * 
	 * @param binaryOp
	 *            {@link BinaryTemporalOpType}
	 * @return jodatime {@link Interval}
	 */
	protected Interval parseGMLTimePeriodFromBinaryTemporalOp(BinaryTemporalOpType binaryOp) {
		XmlObject period = binaryOp.selectChildren(GML_TIME_PERIOD)[0];

		String beginPos = "";
		XmlObject hasIndeterminate = period.selectChildren(GML_BEGIN_POSITION)[0].selectAttribute(new QName("", "indeterminatePosition"));
		if (hasIndeterminate == null) {
			beginPos = stripText(period.selectChildren(GML_BEGIN_POSITION));
		}
		else {
			beginPos = stripText(hasIndeterminate);
		}

		String endPos = "";
		hasIndeterminate = period.selectChildren(GML_END_POSITION)[0].selectAttribute(new QName("", "indeterminatePosition"));
		if (hasIndeterminate == null) {
			endPos = stripText(period.selectChildren(GML_END_POSITION));
		}
		else {
			endPos = stripText(hasIndeterminate);
		}

		/*
		 * construct time in millis. for "unknown" set the MAX_VALUE
		 */
		long beginPosMs, endPosMs = 0L;
		if (beginPos.equals("unknown")) {
			beginPosMs = Long.MAX_VALUE;
		}
		else {
			beginPosMs = new DateTime(beginPos).getMillis();
		}

		if (endPos.equals("unknown")) {
			endPosMs = Long.MAX_VALUE;
		}
		else {
			endPosMs = new DateTime(endPos).getMillis();
		}
		return new Interval(beginPosMs, endPosMs);
	}


	/**
	 * @param valRef
	 *            xbeans value reference
	 * @return jodatime {@link Interval}
	 * @throws FESParseException
	 *             if unexpected error occurs
	 */
	protected Interval getTimeFromValueReference(XmlObject valRef) throws FESParseException {
		String valRefString = XmlUtils.toString(valRef.getDomNode().getFirstChild()).trim();

		if (valRefString.endsWith("validTime")) {
			return parseValidTime();
		}
		throw new FESParseException("Only gml:validTime supported at the current developement state");
	}


	private Interval parseValidTime() {
		XmlObject[] valRef = this.temporalOp.selectChildren(VALUE_REFERENCE_QNAME);

		if (valRef.length == 2) {
			// another valRef, referenced time
			// TODO how to parse the time with a ValueReference?
		}

		else {
			XmlObject[] period = this.temporalOp.selectChildren(GML_TIME_PERIOD);
			if (period != null && period.length > 0) {

				String beginPos = "";
				XmlObject hasIndeterminate = period[0].selectChildren(GML_BEGIN_POSITION)[0].selectAttribute(new QName("", "indeterminatePosition"));
				if (hasIndeterminate == null) {
					beginPos = stripText(period[0].selectChildren(GML_BEGIN_POSITION));
				}
				else {
					beginPos = stripText(hasIndeterminate);
				}

				String endPos = "";
				hasIndeterminate = period[0].selectChildren(GML_END_POSITION)[0].selectAttribute(new QName("", "indeterminatePosition"));
				if (hasIndeterminate == null) {
					endPos = stripText(period[0].selectChildren(GML_END_POSITION));
				}
				else {
					endPos = stripText(hasIndeterminate);
				}

				/*
				 * construct time in millis. for "unknown" set the MAX_VALUE
				 */
				long beginPosMs, endPosMs = 0L;
				if (beginPos.equals("unknown")) {
					beginPosMs = Long.MAX_VALUE;
				}
				else {
					beginPosMs = new DateTime(beginPos).getMillis();
				}

				if (endPos.equals("unknown")) {
					endPosMs = Long.MAX_VALUE;
				}
				else {
					endPosMs = new DateTime(endPos).getMillis();
				}
				return new Interval(beginPosMs, endPosMs);

			}
			XmlObject[] instant = this.temporalOp.selectChildren(GML_TIME_INSTANT);
			if (instant != null && instant.length > 0) {
				String timepos = stripText(instant[0].selectChildren(GML_TIME_POSITION));

				long beginPosMs = new DateTime(timepos).getMillis();
				return new Interval(beginPosMs, beginPosMs);
			}
		}
		return null;
	}


	/**
	 * Strips out the text of an xml-element and returns as a String.
	 */
	private String stripText(XmlObject[] elems) {
		if (elems != null && elems.length > 0) {
			return stripText(elems[0]);
		}
		return null;
	}


	private String stripText(XmlObject elem) {
		if (elem != null) {
			Node child = elem.getDomNode().getFirstChild();
			if (child != null) {
				return XmlUtils.toString(child).trim();
			}
		}
		return null;
	}

}
