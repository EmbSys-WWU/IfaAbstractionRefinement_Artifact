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

/**
 * This transformer handles the right part in a binary
 * expression like 'int x = 3;' (e.g. '3' is the expression).
 *
 * @author Florian
 * 
 */
public class InitializerTransformer extends AbstractNodeTransformer {
	
	private static Logger logger = LogManager.getLogger(InitializerTransformer.class.getName());
	
	public void transformNode(Node node, Environment e) {
		e.setLastInitializer(null);
		int size = e.getExpressionStack().size();
		handleChildNodes(node, e);
		if (e.getExpressionStack().size() == size + 1) {
			e.setLastInitializer(e.getExpressionStack().pop());
		} else if (e.getExpressionStack().size() == size) {
			e.setLastInitializer(null);
		} else {
			logger.error("{}: more then one Expression as Initializer",NodeUtil.getFixedAttributes(node));
		}
	}
}
