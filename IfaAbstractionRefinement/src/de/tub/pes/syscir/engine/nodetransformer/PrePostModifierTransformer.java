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
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.UnaryExpression;

/**
 * first we handle the childnode then we get the top-expression from the stack
 * next step is, get the real type of the node and create the right
 * unary-expression and add it to the stack
 * 
 * @author Florian
 * 
 */
public class PrePostModifierTransformer extends AbstractNodeTransformer {

	private static Logger logger = LogManager.getLogger(PrePostModifierTransformer.class.getName());
	
	public static final String PRE_DEC = "predecrement_node";
	public static final String PRE_INC = "preincrement_node";
	public static final String POST_INC = "postincrement_node";
	public static final String POST_DEC = "postdecrement_node";

	public void transformNode(Node node, Environment e) {

		handleChildNodes(node, e);
		String op = node.getNodeName();

		Expression exp = e.getExpressionStack().pop();

		if (op.equals(PRE_DEC)) {
			UnaryExpression ue = new UnaryExpression(node, true, "--", exp);
			e.getExpressionStack().push(ue);
		} else if (op.equals(PRE_INC)) { // !
			UnaryExpression ue = new UnaryExpression(node, true, "++", exp);
			e.getExpressionStack().push(ue);
		} else if (op.equals(POST_DEC)) {
			UnaryExpression ue = new UnaryExpression(node, false, "--", exp);
			e.getExpressionStack().push(ue);
		} else if (op.equals(POST_INC)) {
			UnaryExpression ue = new UnaryExpression(node, false, "++", exp);
			e.getExpressionStack().push(ue);
		} else {
			logger.error("{}: Configuration Error: PrePostModifierTransformer should only be used for post/pre in/decrement nodes."
					,NodeUtil.getFixedAttributes(node));
		}

	}

}
