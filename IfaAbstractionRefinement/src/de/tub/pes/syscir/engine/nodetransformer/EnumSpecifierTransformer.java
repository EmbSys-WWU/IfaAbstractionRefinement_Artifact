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
import org.w3c.dom.NodeList;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.TransformerFactory;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCEnumType;
import de.tub.pes.syscir.sc_model.expressions.Expression;

/**
 * This class handles EnumSpecifier nodes. It is used when an enum types is
 * defined in the model and creates a new enumType in the system. Since the
 * child nodes of an EnumSpecifier node are known, the "enumerator_list" and the
 * "enumerator child" nodes are also handled in this transformer.
 * 
 * @author Timm Liebrenz
 * 
 */
public class EnumSpecifierTransformer extends AbstractNodeTransformer {

	private static Logger logger = LogManager
			.getLogger(EnumSpecifierTransformer.class.getName());

	@Override
	public void transformNode(Node node, Environment e) {
		String enumName = NodeUtil.getAttributeValueByName(node, "name");

		if (e.getKnownTypes().isEmpty()
				|| !e.getKnownTypes().containsKey(enumName)) {
			SCEnumType enumType = new SCEnumType(enumName);
			e.getTransformerFactory().addEnumType(enumName);

			Node enumListNode = null; // "enumerator_list"
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				if (node.getChildNodes().item(i).getNodeName()
						.equals("enumerator_list")) {
					enumListNode = node.getChildNodes().item(i);
					break;
				}
			}

			NodeList enumerators = enumListNode.getChildNodes();

			// handle all child nodes and create a new enum element for each
			// "enumerator" node
			for (int i = 0; i < enumerators.getLength(); i++) {

				Node enumNode = enumerators.item(i);
				if (!enumNode.getNodeName().equals("enumerator")) {
					continue;
				}

				String expressionName = NodeUtil.getAttributeValueByName(
						enumNode, "name");

				// A defined value is given as child node of the enumerator
				// node. It can be handled like an expression.
				int currentStackSize = e.getExpressionStack().size();
				handleChildNodes(enumNode, e);
				// if the child node handling does not put an expression on the
				// stack, there is no defined value given for this enum element
				if (currentStackSize < e.getExpressionStack().size()) {
					// defined value is given
					Expression ie = e.getExpressionStack().pop();

					// we only support Integer values as defined values for enum
					// elements
					try {
						int iv = Integer.parseInt(ie.toStringNoSem());

						enumType.addElement(expressionName, iv);
					} catch (NumberFormatException nfe) {
						logger.error(
								"Cannot parse the defined value for enum element {} in enum type {}, only Integer values are supported",
								expressionName, enumName);
					}
				} else {
					// no defined value is given
					enumType.addElement(expressionName);
				}
			}
			e.getSystem().addEnumType(enumType);
		}
	}

}
