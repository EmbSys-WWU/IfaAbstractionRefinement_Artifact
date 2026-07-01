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
import java.util.List;

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.TimeUnitExpression;
import de.tub.pes.syscir.sc_model.variables.SCTIMEUNIT;

/**
 * if the tag has a value-attribute, we extract it, look weather there are
 * subinformations and create a new constant-expression if we found out, thats a
 * Timeunit, we create a new Variable and a new variable-expression no matter
 * what it is, we push the expression on the stack
 * 
 * if it hasn't a value-attribute, we only handle the childnodes
 */
public class ConstantTransformer extends AbstractNodeTransformer {

	public void transformNode(Node node, Environment e) {
		String value = NodeUtil.getAttributeValueByName(node, "value");
		if (value != null) {
			long intVal = 0;
			try {
				if (value.indexOf("0x") == 0) {
					intVal = Long.parseLong(value.substring(2), 16);
					value = new Long(intVal).toString();
				} else if (value.indexOf("-0x") == 0) {
					intVal = -Long.parseLong(value.substring(3), 16);
					value = new Long(intVal).toString();
				}
			} catch (Exception exc) {
			}
			
			Expression exp;

			if (SCTIMEUNIT.containsValue(value)) {
				exp = new TimeUnitExpression(node, "", SCTIMEUNIT.valueOf(value));
			} else {
				exp = new ConstantExpression(node, value);
			}

			// e.operandStack.push(value);
			e.getExpressionStack().push(exp);
		} else {
			handleChildNodes(node, e);
		}
	}

}
