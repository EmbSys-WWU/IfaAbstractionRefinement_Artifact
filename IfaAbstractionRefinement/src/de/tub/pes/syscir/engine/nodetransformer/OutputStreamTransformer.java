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
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.OutputExpression;

/**
 * Transforms outputstream-nodes. These nodes are generated out of
 * cout-Statements in SystemC. We generate an OutputExpression from these
 * statements.
 * 
 * @author pockrandt
 * 
 */
public class OutputStreamTransformer extends AbstractNodeTransformer {

	private static Logger logger = LogManager.getLogger(OutputStreamTransformer.class.getName());
	
	public void transformNode(Node node, Environment e) {
		String access = NodeUtil.getAttributeValueByName(node, "name");
		if (access.equals("cout")) {
			Stack<Expression> oldExpressionStack = e.getExpressionStack();
			Stack<Expression> tempStack = new Stack<Expression>();
			e.setExpressionStack(tempStack);

			LinkedList<Expression> exps = new LinkedList<Expression>();
			handleChildNodes(node, e);
			while (!e.getExpressionStack().isEmpty()) {
				exps.addFirst(e.getExpressionStack().pop());
			}

			OutputExpression oe = new OutputExpression(node, "", exps);

			oldExpressionStack.add(oe);
			e.setExpressionStack(oldExpressionStack);
		} else {
			// we have an outputstream we can not handle
			logger.error("{}: Encountered an outputstream node with name {} which cannot be handled (yet).",
					NodeUtil.getFixedAttributes(node),access);
		}
	}
}
