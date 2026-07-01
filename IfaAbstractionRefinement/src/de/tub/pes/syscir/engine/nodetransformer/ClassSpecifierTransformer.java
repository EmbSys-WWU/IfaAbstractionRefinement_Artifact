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

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCClass;

/**
 * Handles the class specifier node. This node always occures when a class
 * (module or struct) is declared. As it is possible to declare a class inside
 * of another one, we have to make sure that we store the surrounding class and
 * restore it afterwards.
 * 
 * @author Florian
 * 
 */
public class ClassSpecifierTransformer extends AbstractNodeTransformer {

	public void transformNode(Node node, Environment e) {
		String className = NodeUtil.getAttributeValueByName(node, "name");
		String keyword = NodeUtil.getAttributeValueByName(node, "keyword");
//		boolean extSCModule = Boolean.parseBoolean(NodeUtil
//				.getAttributeValueByName(node, "extSCModule"));

		// seems as if KaSCPar can't differantiate between structs and classes
		// (both get the class-keyword), so we might not need this check.
		if (keyword.equals("class")) {
			e.setCurrentAccessKey("private");
		} else if (keyword.equals("struct")) {
			e.setCurrentAccessKey("public");
		}

		//save the current class (the surrounding class)
		SCClass temp = e.getCurrentClass();
		SCClass cl = null;
		if (e.getClassList().containsKey(className)) {
			// this module was found as Father of an other module,
			// so it exists already in the list of modules in the
			// environment
			cl = e.getClassList().get(className);

		} else {
			// we found a new class
			cl = new SCClass(className);
			e.getClassList().put(className, cl);
			e.getSystem().addClass(cl);
		}

		//parse the class
		e.setCurrentClass(cl);
		e.getFunctionBodys().put(cl.getName(), null);
		handleChildNodes(node, e);
		
		//restore surrounding class
		e.setCurrentClass(temp);
	}

}
