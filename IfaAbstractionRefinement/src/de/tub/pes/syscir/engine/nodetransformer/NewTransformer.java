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
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.NewArrayExpression;
import de.tub.pes.syscir.sc_model.expressions.NewExpression;

/**
 * Transforms 'new $type', 'new $type(init)' and 'new $type[size]' expressions.
 *
 * @author rschroeder
 *
 */
public class NewTransformer extends AbstractNodeTransformer {
	private static Logger logger = LogManager.getLogger(NewTransformer.class
			.getName());

	@Override
	public void transformNode(Node node, Environment e) {
		Node new_type_id = findChildNode(node, "new_type_id");
		// the object type (myclassname/mystructname/int/...) name:
		Node typeNode = recursiveFindChildNode(new_type_id, "qualified_id");
		if (typeNode == null) {
			typeNode = recursiveFindChildNode(new_type_id,
					"builtin_type_specifier"); // for 'int', 'bool'
		}
		String type = NodeUtil.getAttributeValueByName(typeNode, "name");
		Node new_declarator = recursiveFindChildNode(node, "new_declarator");
		Expression ret = null;
		if (new_declarator == null) { // normal object
			NewExpression ne = new NewExpression(node);
			ne.setObjType(type);

			Node new_initializer = findChildNode(node, "new_initializer");
			if (new_initializer != null) { // the contructor is called
				Node arguments_list = findChildNode(new_initializer,
						"arguments_list");
				if (arguments_list != null) { // arguments are provided
					NodeList args = arguments_list.getChildNodes();
					int len = args.getLength();
					List<Expression> arguments = new ArrayList<Expression>(len);
					int prevStacksize = e.getExpressionStack().size();
					int currStacksize;
					Expression argExpr;
					for (int i = 0; i < len; i++) {
						handleNode(args.item(i), e);
						currStacksize = e.getExpressionStack().size();
						if (currStacksize != prevStacksize) { // something
																// happened
							if (currStacksize == prevStacksize + 1) {
								argExpr = e.getExpressionStack().pop();
								arguments.add(argExpr);
							} else {
								logger.info("1 argument != 1 expression while reading new initilizer list");
							}
						}
					}
					ne.setArguments(arguments);
				}
			}
			ret = ne;
		} else { // array declaration
			Node direct_new_declarator = recursiveFindChildNode(new_declarator,
					"direct_new_declarator");
			if (direct_new_declarator != null) {
				handleChildNodes(direct_new_declarator, e);
				Expression size = e.getExpressionStack().pop();
				NewArrayExpression nae = new NewArrayExpression(node);
				nae.setObjType(type);
				nae.setSize(size);
				ret = nae;
			} else {
				logger.error(
						"{}: cant handle dyn array creation without direct declararation",
						NodeUtil.getFixedAttributes(new_declarator));
			}
		}
		if (ret != null) {
			e.getExpressionStack().add(ret);
		} else {
			logger.error("{}: Couldn't transform 'new' expression",
					NodeUtil.getFixedAttributes(node));
		}
	}
}
