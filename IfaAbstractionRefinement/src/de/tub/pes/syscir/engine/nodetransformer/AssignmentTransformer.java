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
 * an assignment-tag has 2 childnodes, the first childnode is the left-element 
 * where the right-element, the second childnode, is assigned to
 * we save an assignment as binary-expression
 * after the childnodehandling the topelement of the stack is the right-element and the next element is the left-element
 * if we have a childnode named assignment_expression_operator we take the assignment-operator from him, otherwise its the "="
 * @author Florian
 *
 */
public class AssignmentTransformer extends AbstractNodeTransformer {

	private static Logger logger = LogManager.getLogger(AssignmentTransformer.class.getName());
	
	public void transformNode(Node node, Environment e){
		handleChildNodes(node, e);
		Node opNode = findChildNode(node, "assignment_expression_operator");
	      String op = "=";
	      if(opNode != null) {
	        op = NodeUtil.getAttributeValueByName(opNode, "name");
	      }
	      
	      if(e.getExpressionStack().size() < 2) { 
	        logger.error("{}: Invalid assignment",NodeUtil.getFixedAttributes(node));
	        return;
	      }
		Expression exp_rgt = e.getExpressionStack().pop();
		Expression exp_lft = e.getExpressionStack().pop();
		
		BinaryExpression bin_exp = new BinaryExpression(node, exp_lft, op, exp_rgt);
		
		e.getExpressionStack().push(bin_exp);
		
	}
}
