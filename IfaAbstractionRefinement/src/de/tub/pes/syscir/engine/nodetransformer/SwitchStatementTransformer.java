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

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.sc_model.expressions.CaseExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SwitchExpression;

/**
 * Handles switch-case blocks.
 */
public class SwitchStatementTransformer extends AbstractNodeTransformer {
	@Override
	public void transformNode(Node node, Environment e) {

		// handle the switch
		handleNode(findFirstRealChild(node), e);
		Expression condition = e.getExpressionStack().pop();
		SwitchExpression se = new SwitchExpression(node, condition);

		NodeList nodes = node.getChildNodes();
		// handle all cases
		for (int i = 0; i < nodes.getLength(); i++) {
			Node curNode = nodes.item(i);
			// every case (even a default case) has a switch label, followed by
			// a block statement
			if (curNode.getNodeName().equals("switch_label")) {
				// parsing the case expression
				CaseExpression ce = new CaseExpression(curNode,
						new LinkedList<Expression>());
				int currentStackSize = e.getExpressionStack().size();
				handleChildNodes(curNode, e);
				if (currentStackSize < e.getExpressionStack().size()) {
					// a real case
					ce.setCondition(e.getExpressionStack().pop());
				}

				// in most cases there is a block statement after a switch label
				// but it that's not necessary.
				// as the block_statement don't have to appear directly after
				// the switch_label, we have to iterate through all from this
				// point on:
				// we have to stop when we get above the number of child nodes
				// or when we reach another switch_label (marking an empty
				// case).
				List<Expression> blocks = new LinkedList<Expression>();
				for (int j = i + 1; j < nodes.getLength(); j++) {
					if (nodes.item(j).getNodeName().equals("switch_label")) {
						break;
					} else if (nodes.item(j).getNodeName()
							.equals("block_statement")) {
						curNode = nodes.item(j);
						// parse the body
						int size = e.getExpressionStack().size();
						handleChildNodes(curNode, e);
						for (int k = size; k < e.getExpressionStack().size(); k++) {
							blocks.add(e.getExpressionStack().get(k));
						}
						while (size != e.getExpressionStack().size()) {
							e.getExpressionStack().pop();
						}

					}
				}
				ce.setBody(blocks);
				se.addCase(ce);
			}
		}
		e.getExpressionStack().add(se);
	}

}
