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

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.sc_model.expressions.ArrayInitializerExpression;
/**
 * at the arrayinitializer-tag the Array is already declared
 * now we need to handle the childnodes
 * every element is descripted as a expression
 * every time we handle a childnode the lastInitializer-Variable of the environment is set
 * we take it and put it in the ArrayInitializerExpression
 * After we handled all childnodes, we push this expression in the stack
 * a multidimensional Array is initialized as array of array-initalizers
 * @author Florian
 *
 */
public class ArrayInitializerTransformer extends AbstractNodeTransformer {
	public void transformNode(Node node, Environment e){
		List<Node> nodeList = findChildNodes(node, "initializer");
		ArrayInitializerExpression arr = new ArrayInitializerExpression(node, nodeList.size());
		int i = 0;
		for(Node n : nodeList){
			handleNode(n, e);
			
				arr.initAtPosition(i, e.getLastInitializer());
				i++;
			
		}
		e.getExpressionStack().push(arr);
	}
}
