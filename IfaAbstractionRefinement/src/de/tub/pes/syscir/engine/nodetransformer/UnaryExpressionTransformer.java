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

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.RefDerefExpression;
import de.tub.pes.syscir.sc_model.expressions.UnaryExpression;

/**
 * Transforms all nodes of the unary_operator type. For convenience we create
 * two different expressions here. The general unary expression and the more
 * specialized refderefexpression for all pointer dereferencing and variable
 * referencing operations. This differentiation eases the handling of pointers
 * and referenced variables later on.
 * 
 * @author Florian
 * 
 */
public class UnaryExpressionTransformer extends AbstractNodeTransformer {
	public void transformNode(Node node, Environment e) {
		// usually a unary expression contains an operator (e.g., not)
		Node operator = findChildNode(node, "unary_operator");
		String op = NodeUtil.getAttributeValueByName(operator, "operator");

		// and a primary expression (e.g. (x))
		Node expression = findChildNode(node, "primary_expression");
		if (expression == null) {
			// however sometimes the expression is a postfix expression (e.g.,
			// port.read()).
			expression = findChildNode(node, "postfix_expression");
		}
		handleNode(expression, e);

		Expression exp = e.getExpressionStack().pop();
		if (op.equals("pointer")) {
			RefDerefExpression rde = new RefDerefExpression(node, exp,
					RefDerefExpression.DEREFERENCING);
			e.getExpressionStack().push(rde);

		} else if (op.equals("ref")) {
			RefDerefExpression rde = new RefDerefExpression(node, exp,
					RefDerefExpression.REFERENCING);
			e.getExpressionStack().push(rde);

		} else {
			if (op.equals("add")) {
				exp = new UnaryExpression(node, true, "+", exp);
			} else if (op.equals("minnus")) { // !
				exp = new UnaryExpression(node, true, "-", exp);
			} else if (op.equals("tilde")) {
				exp = new UnaryExpression(node, true, "~", exp);
			} else if (op.equals("not")) {
				exp = new UnaryExpression(node, true, "!", exp);
			}
			e.getExpressionStack().push(exp);
		}

	}
}
