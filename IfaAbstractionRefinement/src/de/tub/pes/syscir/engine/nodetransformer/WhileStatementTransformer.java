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
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.LoopExpression;
import de.tub.pes.syscir.sc_model.expressions.WhileLoopExpression;

/**
 * first we handle the first real childnode, this is the condition afterwards we
 * handle the block-child, which is the loop-body. 
 * Then we build a new expression and add it to the stack.
 * 
 * The user may annotate the maximum iteration frequency for timing analysis. 
 * The annotation has to be the first comment inside the loop containing the
 * fixed string "// MAX_ITERATIONS = X " (case and space sensitive), where
 * X is an positive integer value.
 * If there is a loop annotation inside the block we store the value. 
 * 
 * @author Florian
 * 
 */
public class WhileStatementTransformer extends AbstractNodeTransformer {
	public void transformNode(Node node, Environment e) {
		handleNode(findFirstRealChild(node), e);

		Expression condition = e.getExpressionStack().pop();

		List<Node> blocks = findChildNodes(node, "block");
		int size = e.getExpressionStack().size();

		handleNode(blocks.get(0), e);

		int maxIterations = -1; // -1 means unknown
		// handle simple infinite loop
		if (condition instanceof ConstantExpression) {
			ConstantExpression constCond = (ConstantExpression) condition;
			if (constCond.getValue() == "1" || constCond.getValue().equals("true") ) {
				maxIterations = 0; 
			}
		}
		// but overwrite if annotated max iterations
		Node comment = findChildNode(blocks.get(0), "comment");
		if (comment != null) {
			Node nameNode = comment.getAttributes().getNamedItem("name");
			String name = nameNode.getNodeValue();
			if (name.startsWith("// MAX_ITERATIONS = ")) {
				maxIterations = Integer.valueOf(name.substring("// MAX_ITERATIONS = ".length()));
			}
		} 


		List<Expression> body = new ArrayList<Expression>();
		// put the Expressions in the right order in the then-block
		for (int i = size; i < e.getExpressionStack().size(); i++) {
			body.add(e.getExpressionStack().get(i));
		}
		// remove these Expressions from the stack
		while (e.getExpressionStack().size() > size) {
			e.getExpressionStack().pop();
		}

		LoopExpression le = new WhileLoopExpression(node, "", condition,
				body, maxIterations);
		e.getExpressionStack().add(le);
	}
}
