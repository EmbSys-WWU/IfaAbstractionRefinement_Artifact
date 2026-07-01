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
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCParameter;

/**
 * we have found the main-function so we serach for the parameters, handle them,
 * get the return-type and the name of this function and build a new function,
 * which we add to the global-systemfucntions we also get the block-node, where
 * the function-body is described we saved it for later
 * 
 * @author Florian
 * 
 */
public class SCMainDeclarationTransformer extends AbstractNodeTransformer {
	public void transformNode(Node node, Environment e) {
		e.setLocation("MAIN");
		e.getLastQualifiedId().clear();
		handleChildNodes(node, e);
		String name = e.getLastQualifiedId().pop();
		String type = e.getLastType().pop();

		List<SCParameter> clone = new ArrayList<SCParameter>();
		for (SCParameter param : e.getLastParameterList()) {
			clone.add(param);
		}
		e.getLastParameterList().clear();

		SCFunction main = new SCFunction(name, type, clone);

		e.getSystem().addGlobalFunction(main);

		HashMap<String, List<Node>> existingFunctions = e
				.getFunctionBodys().get("system");
		if (existingFunctions == null) {
			existingFunctions = new HashMap<String, List<Node>>();
		}
		existingFunctions.put(main.getName(), e.getLastFunctionBody());

		e.getFunctionBodys().put("system", existingFunctions);
		e.setLocation("");
	}

}
