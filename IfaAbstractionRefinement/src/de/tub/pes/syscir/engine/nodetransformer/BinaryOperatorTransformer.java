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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;

/**
 * if we found a binary-operator-tag we need the right and the leftside of the
 * operator and the operator itself we handle the child-nodes, if the stack is
 * to small or if one of the two expressions is null we report an error
 * otherwise we extract the right operator and build a new binaryexpression and
 * push it to the stack
 * 
 * @author Florian
 * 
 */
public class BinaryOperatorTransformer extends AbstractNodeTransformer {
	
	private static Logger logger = LogManager.getLogger(BinaryOperatorTransformer.class.getName());
	
	public void transformNode(Node node, Environment e) {

		handleChildNodes(node, e);

		if (e.getExpressionStack().size() >= 2) {
			Expression right = e.getExpressionStack().pop();
			Expression left = e.getExpressionStack().pop();

			if (right == null || left == null) {
				logger.error("{}: Missing argument for binary operation.",NodeUtil.getFixedAttributes(node));
			}

			String op = getOperatorString(node.getNodeName());
			if (op == null) {
				logger.error("{}: Unknown binary operator",NodeUtil.getFixedAttributes(node));
			}

			BinaryExpression be = new BinaryExpression(node, left,
					op, right);
			e.getExpressionStack().push(be);
		} else {
			logger.error("{}: Missing argument for binary operation.",NodeUtil.getFixedAttributes(node));
		}

	}

	/**
	 * Returns an operator string for the node name. For example, if 'add_node'
	 * is the given string, it returns '+'.
	 * 
	 * @param nodeName
	 * @return Operator
	 */
	private static String getOperatorString(String nodeName) {
		if (nodeName.equals("add_node"))
			return "+";
		else if (nodeName.equals("subtract_node"))
			return "-";
		else if (nodeName.equals("mul_node"))
			return "*";
		else if (nodeName.equals("div_node"))
			return "/";
		else if (nodeName.equals("mod_node"))
			return "%";
		else if (nodeName.equals("bitwise_and_node"))
			return "&";
		else if (nodeName.equals("bitwise_or_node"))
			return "|";
		else if (nodeName.equals("bitwise_xor_node"))
			return "^";
		else if (nodeName.equals("and_node"))
			return "&&";
		else if (nodeName.equals("or_node"))
			return "||";
		else if (nodeName.equals("eq_node"))
			return "==";
		else if (nodeName.equals("lt_node"))
			return "<";
		else if (nodeName.equals("gt_node"))
			return ">";
		else if (nodeName.equals("le_node"))
			return "<=";
		else if (nodeName.equals("ge_node"))
			return ">=";
		else if (nodeName.equals("neq_node"))
			return "!=";
		else if (nodeName.equals("neq_node"))
			return "!=";
		else if (nodeName.equals("left_shift_node"))
			return "<<";
		else if (nodeName.equals("right_shift_node"))
			return ">>";

		return null;
	}
}
