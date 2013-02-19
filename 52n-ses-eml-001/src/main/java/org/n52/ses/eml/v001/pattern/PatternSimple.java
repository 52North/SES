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

import java.util.Vector;

import org.n52.ses.api.eml.ILogicController;
import org.n52.ses.api.eml.IPatternSimple;
import org.n52.ses.eml.v001.Constants;
import org.n52.ses.eml.v001.filterlogic.EMLParser;



/**
 * representation of simple patterns
 * 
 * @author Thomas Everding
 * 
 */
public class PatternSimple extends AGuardedViewPattern implements IPatternSimple {
	
	private String inputName;
	
	private Vector<PropRestriction> propertyRestrictions = new Vector<PropRestriction>();
	
	private Statement[] statements = null;
	
	private ILogicController controller;
	
	
	/**
	 * 
	 * Constructor
	 * 
	 * @param logicController controller of this process
	 */
	public PatternSimple(ILogicController logicController) {
		this.controller = logicController;
	}
	

	/**
	 * @return the inputName
	 */
	public String getInputName() {
		return this.inputName;
	}
	

	/**
	 * @param inputName the inputName to set
	 */
	public void setInputName(String inputName) {
		this.inputName = inputName;
	}
	

	/**
	 * @return the propertyRestrictions
	 */
	public Vector<PropRestriction> getPropertyRestrictions() {
		return this.propertyRestrictions;
	}
	

	/**
	 * adds a property restriction
	 * 
	 * @param restriction the new property restriction
	 */
	public void addPropertyRestriction(PropRestriction restriction) {
//		logger.info("property restriction: " + restriction.getName() + " = " + restriction.getValue());
		this.propertyRestrictions.add(restriction);
	}
	

	@Override
	public Statement[] createEsperStatements() {
		//lazy load
		if (this.statements != null) {
			return this.statements;
		}
		
		/*
		 * statements look like: 
		 * 
		 * select <selectFunction> 
		 * from <inputevents with restrictions> 
		 * where <guard>
		 * 
		 * one statement per select function is built
		 */

		if (this.selectFunctions.size() <= 0) {
			//no select function specified
			String statement = Constants.EPL_SELECT 
							   + " * " 
							   + this.createFromClause() 
							   + " "
							   + this.createWhereClause(false);
			Statement s = new Statement();
			s.setStatement(statement);
			s.setView(this.view);
			
			SelFunction sel = new SelFunction(this.controller);
			sel.setFunctionName(Constants.FUNC_SELECT_EVENT_NAME);
			sel.setStatement(statement);
			
			s.setSelectFunction(sel);
			
			return new Statement[] { s };
		}
		
		this.statements = new Statement[this.selectFunctions.size()];
		
		//get from and where clause first, they do not change
		String fromClause = this.createFromClause();
		String whereClause = this.createWhereClause(false);
		
		//get insert and select clause for every select function
		String statement;
		Statement s;
		SelFunction sel;
		for (int i = 0; i < this.statements.length; i++) {
			sel = this.selectFunctions.get(i);
			
			//			//insert into
			//			statement = this.createInsertClause(sel);
			
			//select
			statement = this.createSelectClause(sel);
			
			//from
			statement += " " + fromClause;
			
			//where
			statement += " " + whereClause;
			
			//add to list
			s = new Statement();
			s.setSelectFunction(sel);
			s.setStatement(statement);
			s.setView(this.view);
			this.statements[i] = s;
		}
		
		return this.statements;
	}
	

	@Override
	public Statement[] createEsperStatements(EMLParser parser) {
		return createEsperStatements();
	}
	
	//	/**
	//	 * creates the insert into clause for a given select function
	//	 * 
	//	 * @param selectFunction the select function
	//	 * 
	//	 * @return the insert into clause
	//	 */
	//	private String createInsertClause(SelFunction selectFunction) {
	//		return Constants.EPL_INSERT
	//			   + " "
	//			   + Constants.EPL_INTO
	//			   + " "
	//			   + selectFunction.getNewEventName();
	//	}
	
	/**
	 * creates the select clause for a select functions
	 * 
	 * @param sel the select function
	 * 
	 * @return the select clause
	 */
	private String createSelectClause(SelFunction sel) {
		String result = Constants.EPL_SELECT + " ";
		
		//		if (sel.getFunctionName().equals(Constants.FUNC_SELECT_EVENT_NAME)) {
		//			//select event on simple patterns always: "select *"
		//			result += "*";
		//		}
		//		else {
		result += sel.getSelectString(true);
		//		}
		return result;
	}
	

	/**
	 * creates the from clause for this pattern
	 * 
	 * @return the from clause
	 */
	private String createFromClause() {
		String clause = Constants.EPL_FROM + " ";
		
		/*
		 * from input
		 */
		clause += this.inputName;
		
		/*
		 * restrictions
		 */
		if (this.propertyRestrictions.size() > 0) {
			boolean first = true;
			clause += "(";
			
			for (PropRestriction res : this.propertyRestrictions) {
				if (!first) {
					clause += ", ";
				}
				
				//add restriction
				clause += res.getName() + "=" + res.getValue();
				first = false;
			}
			
			clause += ")";
		}
		
		/*
		 * view
		 */
		clause += this.view.getViewString();
		
		return clause;
	}
	

	/**
	 * creates the where clause for this pattern
	 * @param fullNames if <code>true</code> the property names are used with the event names, else only the
	 * property names are used
	 * @return the where clause
	 */
	private String createWhereClause(boolean fullNames) {
		if (this.guard != null) {
			//TODO hier nur lvl. 2
//			String pName = null;
//			if (selectFunctions.size() != 0) {
//				Object o = this.selectFunctions.get(0).getFunctionParameters().get(Constants.FUNC_SELECT_PROPERTY_NAME); 
//				if (o != null) {
//					pName = o.toString();
//				}
//			}
			return this.guard.createStatement(fullNames);
		}
		return "";
	}
}