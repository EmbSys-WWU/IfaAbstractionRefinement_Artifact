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
import de.tub.pes.syscir.sc_model.expressions.DoWhileLoopExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.LoopExpression;

/**
 * we have a do-while-loop we extract the block which represents the loop-body
 * then we search for the condition to parse it and save it afterwards we handle
 * the loop-body at the end we create a new loopExpression and add it to the
 * stack
 * 
 * @author Florian
 * 
 */
public class DoStatementTransformer extends AbstractNodeTransformer {
	public void transformNode(Node node, Environment e) {
		int s = e.getExpressionStack().size();
		// handle Do-Block
		handleNode(findChildNode(node, "block"), e);
		// handle condition
		handleNode(findFirstChildNot(node, "block"), e);
		Expression cond = e.getExpressionStack().pop();
		List<Expression> exp_list = new ArrayList<Expression>();
		while (s != e.getExpressionStack().size()) {
			exp_list.add(e.getExpressionStack().get(s));
			e.getExpressionStack().remove(s);
		}

		LoopExpression le = new DoWhileLoopExpression(node, "", cond, exp_list);

		e.getExpressionStack().add(le);
	}
}
