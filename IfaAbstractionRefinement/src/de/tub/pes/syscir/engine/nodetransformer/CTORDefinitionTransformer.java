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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCParameter;

/**
 * with this tag we found the constructor of the current object we create a
 * dummyfunction and parse the parameters of the function we also save the body
 * we create a new SCFunction add the parameter and put it to the functionlist
 * of the current object we also that the constructor-variable of the current
 * object
 * 
 * @author Florian
 * 
 */
public class CTORDefinitionTransformer extends AbstractNodeTransformer {
	
	private static Logger logger = LogManager.getLogger(CTORDefinitionTransformer.class.getName());
	
	public void transformNode(Node node, Environment e) {

		if (e.getCurrentClass() != null) {
			generateConstructor(node, e, e.getCurrentClass());
		} else {
			logger.error("{}: Encountered a constructor without any enclosing struct or module.",NodeUtil.getFixedAttributes(node));
		}

	}

	/**
	 * Generates the constructor for the given class.
	 * 
	 * @param node
	 * @param e
	 * @param cl
	 */
	private void generateConstructor(Node node, Environment e, SCClass cl) {
		e.getLastQualifiedId().clear();
		Node id = findChildNode(node, "qualified_id");
		handleNode(id, e);

		String name = (e.getLastQualifiedId().empty()) ? cl.getName() : e
				.getLastQualifiedId().pop();
		String type = cl.getName();

		SCFunction placeholder = new SCFunction(name, type);
		e.setCurrentFunction(placeholder);

		Node parameterList = findChildNode(node, "parameter_list");
		handleNode(parameterList, e);

		List<SCParameter> clone = new ArrayList<SCParameter>();
		for (SCParameter param : e.getLastParameterList()) {
			clone.add(param);
		}
		e.getLastParameterList().clear();

		SCFunction constr = new SCFunction(name, type, clone);
		e.setCurrentFunction(constr);

		Node initializer = findChildNode(node, "ctor_initializer");
		handleNode(initializer, e);

		Node block = findChildNode(node, "block");
		handleNode(block, e);
		
		if (cl.getConstructor() != null && !cl.getConstructor().equals(constr)) {
			logger.error("{}: Encountered multiple constructors which are not identically.",NodeUtil.getFixedAttributes(node));
		} else {
			cl.addMemberFunction(constr);
			cl.setConstructor(constr);

			e.setLocation("constructor");
			HashMap<String, List<Node>> existingFunctions = e.getFunctionBodys()
					.get(e.getCurrentClass().getName());
			if (existingFunctions == null) {
				existingFunctions = new HashMap<String, List<Node>>();
			}

			existingFunctions.put(constr.getName(), e.getLastFunctionBody());
			e.getFunctionBodys().put(e.getCurrentClass().getName(),
					existingFunctions);
		}		

		e.setLocation("");
		e.setCurrentFunction(null);
	}

}
