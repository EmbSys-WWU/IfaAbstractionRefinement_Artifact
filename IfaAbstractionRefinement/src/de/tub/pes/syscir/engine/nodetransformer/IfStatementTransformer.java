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
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.IfElseExpression;

/**
 * first we handle the first real childnode, thats the condition afterwards we
 * get all childnodes named "block" we handle the first one, cause this is the
 * then-case then we look if we have an other block in the list, thats the
 * else-case, and handle it then we build a new IfElseExpression and puch it to
 * the stack
 * 
 * @author Florian
 * 
 */
public class IfStatementTransformer extends AbstractNodeTransformer {

	public void transformNode(Node node, Environment e) {
		handleNode(findFirstRealChild(node), e);
		Expression condition = e.getExpressionStack().pop();

		List<Node> blocks = findChildNodes(node, "block");

		int size = e.getExpressionStack().size();

		handleNode(blocks.get(0), e);

		List<Expression> then = new ArrayList<Expression>();
		// put the Expressions in the right order in the then-block
		for (int i = size; i < e.getExpressionStack().size(); i++) {
			then.add(e.getExpressionStack().get(i));
		}
		// remove these Expressions from the stack
		while (e.getExpressionStack().size() > size) {
			e.getExpressionStack().pop();
		}
		List<Expression> Else = new ArrayList<Expression>();
		if (blocks.size() == 2) {
			handleNode(blocks.get(1), e);

			Else = new ArrayList<Expression>();
			// put the Expressions in the right order in the then-block
			for (int i = size; i < e.getExpressionStack().size(); i++) {
				Else.add(e.getExpressionStack().get(i));
			}
			// remove these Expressions from the stack
			while (e.getExpressionStack().size() > size) {
				e.getExpressionStack().pop();
			}

		} else {
			// big problem impossible
		}

		IfElseExpression iee = new IfElseExpression(node, condition, then, Else);
		e.getExpressionStack().push(iee);

	}
}
