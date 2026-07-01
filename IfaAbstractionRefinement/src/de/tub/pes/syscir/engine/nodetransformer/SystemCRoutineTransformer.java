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

package de.tub.pes.syscir.engine.nodetransformer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCMODIFIER;
import de.tub.pes.syscir.sc_model.SCParameter;
import de.tub.pes.syscir.sc_model.SCREFERENCETYPE;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.SCDeltaCountExpression;
import de.tub.pes.syscir.sc_model.expressions.SCStopExpression;
import de.tub.pes.syscir.sc_model.expressions.SCTimeStampExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.expressions.UnaryExpression;
import de.tub.pes.syscir.sc_model.variables.SCSimpleType;

/**
 * Handles all SystemC routines. A SystemC routine is a function of the SystemC
 * Standard (e.g. name()). In this transformer also special handling for
 * specific constructs like "dont_initialize" is done.
 * 
 */

public class SystemCRoutineTransformer extends AbstractNodeTransformer {
	public void transformNode(Node node, Environment e) {
		String val = NodeUtil.getAttributeValueByName(node, "name");
		if (val.equals("dont_initialize")) {
			e.setLastProcessModifier(EnumSet.of(SCMODIFIER.DONTINITIALIZE));
		} else {
			// an other routine
			e.setLastType(new Stack<String>());
			e.setLastArgumentList(new LinkedList<Expression>());
			e.setLastParameterList(new LinkedList<SCParameter>());
			handleChildNodes(node, e);
			SCFunction fct = null;

			SCVariable var = null;
			if (e.getLastArgumentList().size() > 0) {
				for (Expression exp : e.getLastArgumentList()) {
					if (exp instanceof ConstantExpression) {
						ConstantExpression ce = (ConstantExpression) exp;
						var = new SCSimpleType("function_parameter",
								"not specified", ce, false, false,
								new ArrayList<String>());
					} else if (exp instanceof SCVariableExpression) {
						SCVariableExpression ve = (SCVariableExpression) exp;
						var = ve.getVar();
					} else if (exp instanceof FunctionCallExpression) {
						FunctionCallExpression fe = (FunctionCallExpression) exp;
						String retType = fe.getFunction().getReturnType();
						var = new SCSimpleType("functionCall", retType, false,
								false, new ArrayList<String>());
					} else if (exp instanceof BinaryExpression) {
						BinaryExpression be = (BinaryExpression) exp;
						// TODO: find out exact Type
						var = new SCSimpleType("function_parameter",
								"not specified", be, false, false,
								new ArrayList<String>());
					} else if (exp instanceof UnaryExpression) {
						UnaryExpression ue = (UnaryExpression) exp;
						// TODO: find out exact Type
						var = new SCSimpleType("function_parameter",
								"not specified", ue, false, false,
								new ArrayList<String>());
					}
				}

				if (var != null) {
					SCParameter param = new SCParameter(var,
							SCREFERENCETYPE.BYVALUE);
					e.getLastParameterList().add(param);
				}

				List<SCParameter> clone = new ArrayList<SCParameter>();
				for (SCParameter param : e.getLastParameterList()) {
					clone.add(param);
				}
				e.getLastParameterList().clear();

				if (e.getLastType().isEmpty()) {
					fct = new SCFunction(val, "void", clone);
				} else {
					fct = new SCFunction(val, e.getLastType().pop(), clone);
				}
			} else {
				if (e.getLastType().isEmpty()) {
					fct = new SCFunction(val, "void",
							new ArrayList<SCParameter>());
				} else {
					fct = new SCFunction(val, e.getLastType().pop(),
							new ArrayList<SCParameter>());
				}
			}
			List<Expression> clone2 = new ArrayList<Expression>();
			for (Expression exp : e.getLastArgumentList()) {
				clone2.add(exp);
			}
			e.getLastArgumentList().clear();

			if (val.equals("sc_time_stamp")) { // special handling for
												// sc_time_stamp
				SCTimeStampExpression time = new SCTimeStampExpression(node, "");
				e.getExpressionStack().push(time);
			} else if (val.equals("sc_stop")) { // special handling for
												// sc_stop
				SCStopExpression stop = new SCStopExpression(node, "");
				e.getExpressionStack().push(stop);
			} else if (val.equals("sc_delta_count")
					|| val.equals("delta_count")) { // special handling for
				// sc_delta_count
				SCDeltaCountExpression delta = new SCDeltaCountExpression(node,
						"");
				e.getExpressionStack().push(delta);
			} else {
				FunctionCallExpression fct_cal_exp = new FunctionCallExpression(
						node, fct, clone2);
				e.getExpressionStack().push(fct_cal_exp);
			}

		}
	}
}
