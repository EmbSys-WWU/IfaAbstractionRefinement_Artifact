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

package de.tub.pes.syscir.engine.modeltransformer;

import java.util.LinkedList;
import java.util.Map;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCConnectionInterface;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCParameter;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;

/**
 * This transformer changes the name of specified types. Use
 * {@code setReplacement(Map<String, String>)} for providing the list of types
 * which have to be replaced. For example this transformator can change all
 * occurences "unsigned int" to "int" or "unsigned_int".
 * 
 * @author pockrandt
 * 
 */
public class TypeNameTransformer implements ModelTransformer {

	private Map<String, String> replacement = null;

	@Override
	public SCSystem transformModel(SCSystem model) {
		if (replacement != null) {
			
			//For all instances of KnownType classes
			
			

			
			// type names can occur in X different positions:
			// 1.) as classes (as names, fields, method parameters, method
			// variables and in inner classes)
			LinkedList<SCClass> allClasses = new LinkedList<SCClass>();
			findAllClasses(model, allClasses);
			for (SCClass cl : allClasses) {
				replaceTypesInClass(cl, replacement);
			}

			// 2.) as global variables
			for (SCVariable var : model.getGlobalVariables()) {
				replaceTypesInVariable(var, replacement);
			}

			// 3.) as parameters or variables of global methods
			for (SCFunction fun : model.getGlobalFunctions()) {
				replaceTypesInFunction(fun, replacement);
			}
			
		}
		return model;
	}

	/**
	 * Sets the replacements for the typenames. The key of each entry should be
	 * the old type name and the value should be the new type name. This method
	 * have to be called BEFORE {@code transformModel(SCSystem)}.
	 * 
	 * @param replacement
	 */
	public void setReplacement(Map<String, String> replacement) {
		this.replacement = replacement;
	}

	private void findAllClasses(SCSystem model, LinkedList<SCClass> allClasses) {
		for (SCClass cl : model.getClasses()) {
			if (!allClasses.contains(cl)) {
				allClasses.add(cl);
				searchClass(cl, allClasses);
			}
		}
		for (SCClassInstance instance : model.getInstances()) {
			SCClass cl = instance.getSCClass();
			if (!allClasses.contains(cl)) {
				allClasses.add(cl);
				searchClass(cl, allClasses);
			}
		}
	}
	
	private void searchClass(SCClass cl, LinkedList<SCClass> allClasses) {
		for (SCFunction function : cl.getMemberFunctions()) {
			for (Expression exp : function.getBody()) {
				for (Expression iExp : exp.getInnerExpressions()) {	
					if (iExp instanceof FunctionCallExpression) {
						FunctionCallExpression fce = (FunctionCallExpression) iExp;
						if (fce.getFunction().getSCClass() != null &&
							!allClasses.contains(fce.getFunction().getSCClass())
						) {
							allClasses.add(fce.getFunction().getSCClass());
							searchClass(fce.getFunction().getSCClass(), allClasses);
						}
					}
				}
			}
		}
		
		
	}
	
	private void replaceTypesInClass(SCClass cl, Map<String, String> replacement) {
		// check class name
		if (replacement.containsKey(cl.getName())) {
			cl.setName(replacement.get(cl.getName()));
		}

		// check class variables
		for (SCVariable var : cl.getMembers()) {
			replaceTypesInVariable(var, replacement);
		}

		// check all functions
		for (SCFunction fun : cl.getMemberFunctions()) {
			replaceTypesInFunction(fun, replacement);
		}

		// check all ports and sockets
		for (SCPort port : cl.getPortsSockets()) {
			replaceTypesOnPort(port, replacement);
		}
	}

	private void replaceTypesInVariable(SCVariable var,
			Map<String, String> replacement) {
		if (replacement.containsKey(var.getType())) {
			var.setType(replacement.get(var.getType()));
		}
	}

	private void replaceTypesInFunction(SCFunction fun,
			Map<String, String> replacement) {
		// replace all parameters
		for (SCParameter param : fun.getParameters()) {
			SCVariable var = param.getVar();
			replaceTypesInVariable(var, replacement);
		}

		// replace return type
		if (replacement.containsKey(fun.getReturnType())) {
			fun.setReturnType(replacement.get(fun.getReturnType()));
		}

		// replace local variables
		for (SCVariable var : fun.getLocalVariables()) {
			replaceTypesInVariable(var, replacement);
		}
	}

	private void replaceTypesOnPort(SCPort port, Map<String, String> replacement) {
		if (replacement.containsKey(port.getType())) {
			port.setType(replacement.get(port.getType()));
		}
	}

}
