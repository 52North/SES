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
package org.n52.ses.util.concurrent;

/**
 * Class providing an estimation for future processing timeouts.
 * It stores ARRAY_SIZE processing periods and calculates on that basis
 * an IDW interpolated estimated timeout. This implementation could
 * in the worst case lead to loss of messages but provides adequate performance.
 * 
 * @author matthes rieke <m.rieke@52north.org> 
 *
 */
public abstract class PredictedTimeoutEstimation implements ITimeoutEstimation {

	protected static final int ARRAY_SIZE = 50;
	protected static final double IDW_POWER = 1;
	protected int[] timeouts = new int[ARRAY_SIZE];
	protected int currentPos = 0;
	protected int fixedMinimum;
	protected int initialTimeout;

	public PredictedTimeoutEstimation() {
	}

	@Override
	public void setMaximumTimeout(int timeout) {
		this.initialTimeout = timeout;
		for (int i = 0; i < this.timeouts.length; i++) {
			this.timeouts[i] = timeout;
		}
	}
	
	@Override
	public void setMinimumTimeout(int l) {
		this.fixedMinimum = l;		
	}

	@Override
	public void updateTimeout(long l) {
		updateTimeout(l, false);
	}

	@Override
	public void updateTimeout(long l, boolean onFailure) {
		if (onFailure) {
			/*
			 * weight failure deltas heavier than normal
			 */
			l = Math.min(this.initialTimeout, l);
		}
		this.timeouts[currentPos] = (int) l;
		this.currentPos = (this.currentPos+1) % ARRAY_SIZE;
	}

	@Override
	public abstract int getCurrenTimeout();


	public static void main(String[] args) {
		PredictedTimeoutEstimation t = new IDWTimeoutEstimation();
		t.setMinimumTimeout(500);
		t.setMaximumTimeout(5000);
		for (int i = 0; i < ARRAY_SIZE *1.5; i++) {
			t.updateTimeout(10*i);
		}
		System.out.println(t.getCurrenTimeout());
	}

	public static class IDWTimeoutEstimation extends PredictedTimeoutEstimation {

		
		@Override
		public int getCurrenTimeout() {
			int result = 0;
			/*
			 * IDW
			 */
			double sum = 0;
			for (int i = 0; i < this.timeouts.length; i++) {
				double distance = (i-currentPos);
				if (distance > 0) {
					distance = ARRAY_SIZE - distance;
				} else if (distance < 0 ){
					distance = Math.abs(distance);
				} else {
					distance = ARRAY_SIZE;
				}

				distance = 1.0 / Math.pow(distance, IDW_POWER);

				sum += distance;
			}

			for (int i = 0; i < this.timeouts.length; i++) {
				/*
				 * weight the values regarding distance to current position.
				 * normalized by ARRAY_SIZE
				 */
				double distance = (i-currentPos);
				if (distance > 0) {
					distance = ARRAY_SIZE - distance;
				} else if (distance < 0 ){
					distance = Math.abs(distance);
				} else {
					distance = ARRAY_SIZE;
				}

				distance = 1.0 / Math.pow(distance, IDW_POWER);

				double weighted = this.timeouts[i] * (distance / sum);
				//						System.out.println( ((i == currentPos) ? "CURRENT!!" : "") + distance+"; orig="+this.timeouts[i]+"; weighting: "+weighted);
				result += weighted;
			}
			return Math.max(fixedMinimum, result);
		}

	}
	
	
	public static class AverageTimeoutEstimation extends PredictedTimeoutEstimation {
		
		
		@Override
		public int getCurrenTimeout() {
			int result = 0;
			for (int i = 0; i < this.timeouts.length; i++) {
				result += this.timeouts[i];
			}
			return Math.max(fixedMinimum, result / this.timeouts.length);
		}
	}
}
