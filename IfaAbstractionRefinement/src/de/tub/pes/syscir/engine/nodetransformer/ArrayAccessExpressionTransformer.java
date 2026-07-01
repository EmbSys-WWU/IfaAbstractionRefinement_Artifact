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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.ArrayAccessExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.MultiSocketAccessExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.variables.SCArray;
import de.tub.pes.syscir.sc_model.variables.SCPointer;

/**
 * every time an Array is accessed, we need to know the indices first we need
 * all primary-expressions in the first, the Array is specified the other ones
 * specifies the indices we handle the childenodes of every primary-tag after we
 * handle the first primary-tag we have an variable-expression on the stack, we
 * check if we have an array if its true, we keep it, and set a boolean true so
 * all other primary-expressions contain the index-descriptions we put them into
 * a list then we build a new ArrayAccessExpression and push it on the stack
 * 
 * @author Florian
 * 
 */
public class ArrayAccessExpressionTransformer extends AbstractNodeTransformer {
	
	private static Logger logger = LogManager.getLogger(ArrayAccessExpressionTransformer.class.getName());

	public void transformNode(Node node, Environment e) {
		// the first primary expression inside the access node is the array itself
		Node arrayNode = findChildNode(node, "primary_expression");
		handleChildNodes(arrayNode, e);
		Expression exp = e.getExpressionStack().pop();
		SCVariable arrOrPtr = null;
		SCPort ps = null;
		if (exp instanceof SCVariableExpression) {
			SCVariableExpression ve = (SCVariableExpression) exp;
			if (ve.getVar() instanceof SCArray || ve.getVar() instanceof SCPointer) {
				arrOrPtr =  ve.getVar();
			} else {
				logger.error("{}: Only Arrays or Pointers can be accessed in a ArrayAccessExpressionTransformer"
						,NodeUtil.getFixedAttributes(node));
			}
		} else if (exp instanceof SCPortSCSocketExpression) {
			SCPortSCSocketExpression pse = (SCPortSCSocketExpression) exp;
			ps = pse.getSCPortSCSocket();
		}
		
		// now, handle the access inside
		NodeList accessNodes = node.getChildNodes();
		List<Expression> exp_list = new ArrayList<Expression>();
		int start = 1; // first child is array itself
		if (isIgnorableWhitespaceNode(accessNodes.item(0))) {
			start++; // skip twice
		}
		for (int i = start; i < accessNodes.getLength(); i++) {
			Node n = accessNodes.item(i);
			if(!isIgnorableWhitespaceNode(n)) {
				handleNode(n, e);
				exp_list.add(e.getExpressionStack().pop());
			}
		}

		// put everything together
		Expression ret = null;
		if (arrOrPtr != null) {
			ret = new ArrayAccessExpression(node, arrOrPtr,	exp_list);
		} else {
			ret = new MultiSocketAccessExpression(node, ps, exp_list);
		}
		e.getExpressionStack().push(ret);
	}
}
