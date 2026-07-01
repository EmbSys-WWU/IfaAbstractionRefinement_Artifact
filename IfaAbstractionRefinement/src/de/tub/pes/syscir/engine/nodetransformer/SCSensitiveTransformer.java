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
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCPortEvent;
import de.tub.pes.syscir.sc_model.variables.SCVariableEvent;

public class SCSensitiveTransformer extends AbstractNodeTransformer {
	
	private static Logger logger = LogManager.getLogger(SCSensitiveTransformer.class.getName());
	
	public void transformNode(Node node, Environment e) {
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node child = nodes.item(i);
			// default events
			String nodeName = child.getNodeName();
			if (nodeName.equals("qualified_id")) {
				String id = NodeUtil.getAttributeValueByName(child, "name");
				SCClass cl = e.getCurrentClass();
				if (cl.getEventByName(id) != null) {
					e.getSensitivityList().add(cl.getEventByName(id));
				} else if (cl.getPortSocketByName(id) != null) {
					e.getSensitivityList().add(
							new SCPortEvent(id, cl.getPortSocketByName(id)));
				} else if (cl.getMemberByName(id) != null) {
					e.getSensitivityList().add(
							new SCVariableEvent(id, new SCVariableEvent(id, cl
									.getMemberByName(id))));
				} else {
					logger.debug(NodeUtil.getFixedAttributes(child));
					logger.error("{}: Encountered sensitivity to {}, which is neither a port or socket, nor an event or a variable."
							,NodeUtil.getFixedAttributes(node),id);
					return;
				}

				// other event types (e.g., clock.pos())
			} else if (nodeName.equals("postfix_expression")) {
				handleNode(child, e);
				Expression exp = e.getExpressionStack().pop();
				if (exp instanceof AccessExpression) {
					AccessExpression ae = (AccessExpression) exp;
					if (ae.getLeft() instanceof SCPortSCSocketExpression
							&& ae.getRight() instanceof FunctionCallExpression) {
						SCPortSCSocketExpression scps = (SCPortSCSocketExpression) ae
								.getLeft();
						FunctionCallExpression fce = (FunctionCallExpression) ae
								.getRight();
						SCEvent ev = new SCPortEvent(scps.getSCPortSCSocket()
								.getName(), scps.getSCPortSCSocket(), fce
								.getFunction().getName());
						e.getSensitivityList().add(ev);
					} else {
						logger.error("{}: Encountered an unknown event type.",NodeUtil.getFixedAttributes(node));
					}
				}
			}
		}
	}
}
