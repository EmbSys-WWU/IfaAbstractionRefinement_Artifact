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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * Represents the while loop.
 * 
 * @author pockrandt
 * 
 */
public class WhileLoopExpression extends LoopExpression {

	private static final long serialVersionUID = -3973267367960454253L;
	private static final transient Logger logger = LogManager
			.getLogger(WhileLoopExpression.class.getName());

	public WhileLoopExpression(Node n, String l, Expression cond,
			List<Expression> body) {
		super(n, l, cond, body);
	}

	public WhileLoopExpression(Node n, String l, Expression cond,
			List<Expression> body, int maxCount) {
		super(n, l, cond, body, maxCount);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof WhileLoopExpression && super.equals(obj);
	}

	public String toString() {
		String ret = super.toString() + "while ("
				+ getCondition().toString().replace(";", "") + ") {";
		List<Expression> loopBody = getLoopBody();
		for (Expression e : loopBody) {
			if (e == null) {
				logger.error("expression in loop body is null: {}", loopBody);
			} else {
				ret = ret + "\n" + e.toString()/* .replace(";", "") + ";" */;
			}
		}
		return ret + "\n}";
	}
}
