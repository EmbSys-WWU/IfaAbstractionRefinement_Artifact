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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.EventNotificationExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.variables.SCEvent;

/**
 * Handles the SCNotify node. These nodes represent the notification of an
 * event.
 * 
 * @author pockrandt
 * 
 */
public class SCNotifyTransformer extends AbstractNodeTransformer {

	private static Logger logger = LogManager
			.getLogger(SCNotifyTransformer.class.getName());

	@Override
	public void transformNode(Node node, Environment e) {
		String ev = NodeUtil.getAttributeValueByName(node, "name");
		if (e.getCurrentClass() != null) {
			SCClass mod = e.getCurrentClass();
			SCEvent evt = mod.getEventByName(ev);
			SCVariableExpression evt_exp;
			if (evt != null) {
				evt_exp = new SCVariableExpression(node, evt);
			} else {
				SCVariable var = mod.getMemberByName(ev);
				evt_exp = new SCVariableExpression(node, var);
			}

			Node arguments = findChildNode(node, "arguments_list");
			ArrayList<Expression> args = new ArrayList<Expression>();
			if (arguments != null) {
				int s = e.getExpressionStack().size();
				handleChildNodes(arguments, e);

				while (s != e.getExpressionStack().size()) {
					args.add(e.getExpressionStack().get(s));
					e.getExpressionStack().remove(s);
				}
			}

			EventNotificationExpression ene = new EventNotificationExpression(
					node, evt_exp, args);
			e.getExpressionStack().add(ene);

			e.getCurrentFunction().addEventNotification(ene);
		} else {
			logger.error(
					"{}: Encountered notification of event outside of a class. Eventname: {}",
					NodeUtil.getFixedAttributes(node), ev);
		}

	}
}
