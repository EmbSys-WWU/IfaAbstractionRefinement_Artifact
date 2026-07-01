/*****************************************************************************
 *
 * Copyright (c) 2008-17, Joachim Fellmuth, Holger Gross, Florian Greiner, 
 * Bettina Hünnemeyer, Paula Herber, Verena Klös, Timm Liebrenz, 
 * Tobias Pfeffer, Marcel Pockrandt, Rolf Schröder, Simon Schwan
 * Technische Universitaet Berlin, Software and Embedded Systems
 * Engineering Group, Ernst-Reuter-Platz 7, 10587 Berlin, Germany.
 * All rights reserved.
 * 
 * This file is part of STATE (SystemC to Timed Automata Transformation Engine).
 * 
 * STATE is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * STATE is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with STATE.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  Please report any problems or bugs to: state@pes.tu-berlin.de
 *
 ****************************************************************************/

package de.tub.pes.syscir.sc_model.expressions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * this expression represents the Questionmark-Construct it contains one
 * expression as Condition and two Expression which represent the if- and the
 * else-branch
 * 
 * @author Florian
 * 
 */
public class QuestionmarkExpression extends IfElseExpression {
	private static Logger logger = LogManager.getLogger(QuestionmarkExpression.class.getName());

	private static final long serialVersionUID = -8688318767339929612L;

	public QuestionmarkExpression(Node n, Expression cond, Expression t,
			Expression e) {
		super(n, cond, t, e);
	}

	public Expression getThen() {
		if (!getThenBlock().isEmpty()) {
			return super.getThenBlock().get(0);
		} else {
			return null;
		}
	}

	public Expression getElse() {
		if (!getElseBlock().isEmpty()) {
			return super.getElseBlock().get(0);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		String ret = "";
		String ifElseString = super.toString();
		
		if (ifElseString.indexOf("if")!=0){
			// add label declaration to return string
			ret += ifElseString.substring(0, ifElseString.indexOf("if"));
		}
		
		ret += getCondition().toString().replace(";", "") + " ? "
				+ getThen().toString().replace(";", "") + " : "
				+ getElse().toString().replace(";", "");

		return ret + ";";
	}

	public IfElseExpression toIfElseExpression() {
		logger.warn("Converting conditional operator :? to if/else control structure. The result is probably wrong");
		return new IfElseExpression(this.getNode(), this.getCondition(), this.getThen(), this.getElse());
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof QuestionmarkExpression && super.equals(obj);
	}

}
