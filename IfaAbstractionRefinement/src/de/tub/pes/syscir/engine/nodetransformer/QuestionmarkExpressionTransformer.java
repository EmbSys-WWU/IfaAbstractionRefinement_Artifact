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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.QuestionmarkExpression;

/**
 * a questionmark-expression is similar to an ifElseExpression first we get all
 * real childnodes, if we found less then 3 we have to few then we get the first
 * three nodes, the first is the condition the second the if-case the third the
 * else-case we handle all and remember the found expression with this three
 * expressions we build the new questionmarkexpression
 * 
 * @author Florian
 * 
 */
public class QuestionmarkExpressionTransformer extends AbstractNodeTransformer {
	
	private static Logger logger = LogManager.getLogger(QuestionmarkExpressionTransformer.class.getName());
	
	public void transformNode(Node node, Environment e) {
		List<Node> nodes = findRealChildNodes(node);
		if (nodes.size() < 3) {
			logger.error("{}: Questionmark expression is missing one or more child-expressions.",NodeUtil.getFixedAttributes(node));
			return;
		}
		Node cond = nodes.remove(0);
		Node ifTrue = nodes.remove(0);
		Node ifFalse = nodes.remove(0);

		handleNode(cond, e);
		Expression exp_cond = e.getExpressionStack().pop();

		handleNode(ifTrue, e);
		Expression exp_if = e.getExpressionStack().pop();

		handleNode(ifFalse, e);
		Expression exp_else = e.getExpressionStack().pop();

		QuestionmarkExpression qe = new QuestionmarkExpression(node, exp_cond,
				exp_if, exp_else);
		e.getExpressionStack().push(qe);
	}

}
