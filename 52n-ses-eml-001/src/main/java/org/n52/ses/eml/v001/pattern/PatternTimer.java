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

package org.n52.ses.eml.v001.pattern;

import org.n52.ses.eml.v001.Constants;
import org.n52.ses.eml.v001.filterlogic.EMLParser;

/**
 * representation of a timer pattern
 * 
 * @author Thomas Everding
 *
 */
public class PatternTimer extends AViewPattern{
	
	/**
	 * <code>true</code> for interval patterns
	 */
	private boolean interval;
	
	/**
	 * interval in ms
	 */
	private long duration = -1;
	
	private int second = -1;
	
	private int minute = -1;
	
	private int hour = -1;
	
	private int dayOfWeek = -1;
	
	private int dayOfMonth = -1;
	
	private int month = -1;


	/**
	 * @return the interval boolean (<code>true</code> for interval)
	 */
	public boolean isInterval() {
		return this.interval;
	}

	/**
	 * sets the interval boolean (<code>true</code> for interval)
	 * 
	 * @param interval the interval to set
	 */
	public void setInterval(boolean interval) {
		this.interval = interval;
	}

	/**
	 * @return the duration in ms
	 */
	public long getDuration() {
		return this.duration;
	}

	/**
	 * @param duration the duration to set in ms
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * @return the second
	 */
	public int getSecond() {
		return this.second;
	}

	/**
	 * @param second the second to set
	 */
	public void setSecond(int second) {
		this.second = second;
	}

	/**
	 * @return the minute
	 */
	public int getMinute() {
		return this.minute;
	}

	/**
	 * @param minute the minute to set
	 */
	public void setMinute(int minute) {
		this.minute = minute;
	}

	/**
	 * @return the hour
	 */
	public int getHour() {
		return this.hour;
	}

	/**
	 * @param hour the hour to set
	 */
	public void setHour(int hour) {
		this.hour = hour;
	}

	/**
	 * @return the dayOfWeek (1 = Monday, 7 = Sunday)
	 */
	public int getDayOfWeek() {
		return this.dayOfWeek;
	}

	/**
	 * @param dayOfWeek the dayOfWeek to set (1 = Monday, 7 = Sunday)
	 */
	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	/**
	 * @return the dayOfMonth
	 */
	public int getDayOfMonth() {
		return this.dayOfMonth;
	}

	/**
	 * @param dayOfMonth the dayOfMonth to set
	 */
	public void setDayOfMonth(int dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	/**
	 * @return the month
	 */
	public int getMonth() {
		return this.month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(int month) {
		this.month = month;
	}

	@Override
	public Statement[] createEsperStatements() {
		Statement[] statements = new Statement[this.selectFunctions.size()];
		
		/*
		 * build clauses
		 */
		SelFunction sel;
		Statement stat;
		String selectClause;
		String fromClause;
		String viewString;
		
		//build from clause
		fromClause = this.buildFromClause();
		
		for (int i = 0; i < statements.length; i++) {
			//build select clauses
			sel = this.selectFunctions.get(i);
			selectClause = this.buildSelectClause(sel);
			
			//build view
			viewString = this.view.getViewString();
			
			//build statement
			stat = new Statement();
			stat.setSelectFunction(sel);
			stat.setStatement(selectClause + " " + fromClause + viewString);
			stat.setView(this.view);
			
			//add the new statement to the result list
			statements[i] = stat;
		}
		
		return statements;
	}
	
	@Override
	public Statement[] createEsperStatements(EMLParser parser) {
		return createEsperStatements();
	}


	/**
	 * 
	 * @return the from clause for this pattern
	 */
	private String buildFromClause() {
		//timers are patterns in esper EPL
		String result = Constants.EPL_FROM
						+ " "
						+ Constants.EPL_PATTERN 
						+  " [every (";
		
		if (this.interval) {
			//interval timer
			result += this.buildIntervalString();
		}
		else {
			//at timer
			result += this.buildAtString();
		}
		
		//close brackets
		result += ")]";
		
		return result;
	}
	
	
	/**
	 * 
	 * @return the string for an at timer
	 */
	private String buildAtString() {
		String result = Constants.TIMER_AT + "(";
		
		/*
		 * timer:at syntax:
		 * 
		 * timer:at(<minute>, <hour>, <day of month>, <month>, <day of week>, <second>)
		 */
		
		//add minute
		if (this.minute == -1) {
			//minute not set
			result += "*";
		}
		else {
			result += this.minute;
		}
		
		result += ", ";
		
		//add hour
		if (this.hour == -1) {
			//hour not set
			result += "*";
		}
		else {
			result += this.hour;
		}
		
		result += ", ";
		
		//add day of month
		if (this.dayOfMonth == -1) {
			//day of month not set
			result += "*";
		}
		else {
			result += this.dayOfMonth;
		}
		
		result += ", ";
		
		//add month
		if (this.month == -1) {
			//month not set
			result += "*";
		}
		else {
			result += this.month;
		}
		
		result += ", ";
		
		//add day of week
		if (this.dayOfWeek == -1) {
			//day of week not set
			result += "*";
		}
		else {
			/*
			 * convert day of week from EML to esper EPL
			 * 
			 * EML: Sunday = 7
			 * EPL: Sunday = 0
			 */
			result += (this.dayOfWeek % 7);
		}
		
		//add second
		if (this.second == -1) {
			//second not set, optional in esper EPL
			result += ")";
		}
		else {
			result += ", " + this.second + ")";
		}
		
		return result;
	}

	
	/**
	 * 
	 * @return the string for an interval timer
	 */
	private String buildIntervalString() {
		//build string
		String result = Constants.TIMER_INTERVAL 
						+ "("
						+ this.duration
						+ " msec)";
		
		return result;
	}

	/**
	 * 
	 * @param selFunction the select function object
	 * @return the select clause for the given function
	 */
	private String buildSelectClause(SelFunction selFunction) {
		String result = Constants.EPL_SELECT + " ";
		
		if (selFunction.getFunctionName().equals(Constants.FUNC_SELECT_EVENT_NAME)) {
			result += "\"" + Constants.TIMER_EVENT_VALUE + "\" as value";
			
		}
		else {
			result += selFunction.getSelectString(false);
		}
		
		return result;
	}
}
