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
import de.tub.pes.syscir.sc_model.SCClass;

/**
 * Handles the baseSpecifier tag, which represents the inheritance of modules
 * and structs. Adds the classes to the father objects of the current module or
 * class and sets flags if we found interfaces used by primitive channels or
 * hierarchical channels.
 * 
 * @author Florian
 * 
 */
public class BaseSpecifierTransformer extends AbstractNodeTransformer {

	private static Logger logger = LogManager.getLogger(BaseSpecifierTransformer.class.getName());
	
	public void transformNode(Node node, Environment e) {
		SCClass currentClass = e.getCurrentClass();
		if (currentClass != null) {
			handleInheritance(node, e, currentClass);
		} else {
			logger.error("{}: Encountered an inheritance modifier without any enclosing class.",NodeUtil.getFixedAttributes(node));
		}
	}

	private void handleInheritance(Node node, Environment e, SCClass cl) {
		String name = NodeUtil.getAttributeValueByName(node, "name");
		if (name.equals("sc_module")) {
			// standard inheritation
			cl.addInheritFrom(new SCClass(name));
		} else if (name.equals("sc_channel") || name.equals("sc_interface")) {
			cl.addInheritFrom(new SCClass(name));
			cl.setHierarchicalChannel();
		} else if (name.equals("sc_prim_channel")) {
			cl.setPrimitiveChannel();
		} else {
			if (e.getClassList().containsKey(name)) {
				cl.addInheritFrom(e.getClassList().get(name));
			} else {
				// we found a new Module and hope that later in the XML-File
				// this module is specified
				// TODO: is this really necessary with our multi-phase approach?
				SCClass mod = new SCClass(name);
				e.getClassList().put(name, mod);
				cl.addInheritFrom(mod);
			}
		}
	}
}
