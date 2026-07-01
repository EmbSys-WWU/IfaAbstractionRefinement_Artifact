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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;


import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.TransformerFactory;
import de.tub.pes.syscir.engine.typetransformer.KnownTypeTransformer;
import de.tub.pes.syscir.engine.util.Constants;
import de.tub.pes.syscir.engine.util.NodeUtil;

/**
 * In this class we handle the BuiltinTypeSpecifier-Tag we extract the name of
 * the type and if some exist, the suptype and the length
 * 
 * @author Florian
 * 
 */
public class BuiltinTypeSpecifierTransformer extends AbstractNodeTransformer {

	private static Logger logger = LogManager
			.getLogger(BuiltinTypeSpecifierTransformer.class.getName());

	public void transformNode(Node node, Environment e) {
		List<Node> templates = findChildNodes(node, "declaration_specifiers");

		String type = NodeUtil.getAttributeValueByName(node, "name");
		String length = NodeUtil.getAttributeValueByName(node, "length");

		e.getLastType_TemplateArguments().clear();
		for (Node n : templates) {
			handleNode(n, e);
			String subType = (String) e.getLastType().pop();
			e.getLastType_TemplateArguments().add(subType);
		}

		if (e.getTransformerFactory().isSimpleType(type)) {
			// nothing
		} else if (type.equals("void")) {
			// nothing
		} else if (type.equals("sc_event")) {
			// nothing
		} else if (type.equals("sc_module_name")) {
			// ignore
		} else if (type.equals("peq_with_cb_and_phase")) {
			/* // nothing to do here */
		} else if (type.equals("sc_time")) {
			// nothing
		} else if (type.equals("tlm_dmi")) {
			logger.debug("Creating dummy tlm_dmi type.");
		} else {

			if (e.getKnownTypes().containsKey(type)) {

			} else {
				KnownTypeTransformer tpTrans = e.getTransformerFactory()
						.getTypeTransformer(type, e);
				if (tpTrans != null) {
					tpTrans.createType(e);
				} else {
					logger.error("{}: Builtin type '{}' is not supported.",
							NodeUtil.getFixedAttributes(node), type);
					type = null;
				}
			}
		}
		if (length != null && !length.equals("0")) {
			type = type + "<" + length + ">";
		}

		e.getLastType().push(type);

	}

}
