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
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.BracketExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.QuestionmarkExpression;

/**
 * Handles the primary_expression node. Usually this node can just be ignored
 * and we only have to handle the child nodes. However as the statement (x * y)
 * % z can only be differentiated from x * y % z by the order of the operation
 * nodes AND by an extra primary expression where the brackets are in the first
 * example, we have to insert a bracketExpression if the child node returns a
 * binary expression.
 * 
 * @author pockrandt
 * 
 */
public class PrimaryExpressionTransformer extends AbstractNodeTransformer {

	public void transformNode(Node node, Environment e) {
		// saving the stacksize
		int previousSize = e.getExpressionStack().size();
		// handling the child nodes
		handleChildNodes(node, e);

		// check if there was only one child node
		if (e.getExpressionStack().size() == previousSize + 1) {
			// check if the last expression on the stack is a binary expression
			Expression exp = e.getExpressionStack().pop();
			//TODO is this really necessary or can we just add brackets to all expressions?
			if (exp instanceof AccessExpression || exp instanceof BinaryExpression || exp instanceof QuestionmarkExpression) {
				// encapsulate the binary expression in a bracketExpression
				exp = new BracketExpression(exp.getNode(), exp);
			}
		
			// put the expression back on the stack
			e.getExpressionStack().add(exp);
		}
	}
}
