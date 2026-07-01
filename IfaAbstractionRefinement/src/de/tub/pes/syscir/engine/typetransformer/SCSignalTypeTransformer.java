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

package de.tub.pes.syscir.engine.typetransformer;

import java.util.List;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.TransformerFactory;
import de.tub.pes.syscir.engine.util.Constants;
import de.tub.pes.syscir.engine.util.Pair;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;

public class SCSignalTypeTransformer extends AbstractTypeTransformer {

	/**
	 * creates the scClass of the sc_signal and applies the
	 * correct data type to the channel
	 */
	public void createType(Environment e) {
		
		if (!e.getLastType().isEmpty() &&
			!e.getLastType().peek().startsWith("sc_signal") &&
			!e.getLastType().peek().equals("void")
		) {
			String type = e.getLastType().peek();
			this.name = createName(type);
			Environment temp = createEnvironment(e, type);

			temp.getCurrentClass().setName(this.name);
			e.integrate(temp);
		} else if (!e.getLastType_TemplateArguments().isEmpty()) {
			String type = e.getLastType_TemplateArguments().get(0);
			this.name = createName(type);
		}
	
	}
	
	/**
	 * creates the correct SCKnownType for the sc_signal when
	 * the channel is instantiated
	 */
	@Override
	public SCKnownType initiateInstance(String instName, List<Expression> params, Environment e, boolean stat,
			boolean cons, List<String> other_mods) {
		
		if (!e.getLastType_TemplateArguments().isEmpty()) {
			String type = e.getLastType_TemplateArguments().get(0);
			this.name = createName(type);
		}
		SCKnownType knownType = super.initiateInstance(instName, params, e, stat, cons, other_mods);
		return knownType;
		
	}
	
	/**
	 * creates an Environment-Object of the sc_signal implementation
	 * therefore it uses
	 * - sc_signal_generic.ast.xml for data type with generic parameters (i.e. sc_uint<XX>)
	 * - sc_signal.ast.xml for simple data typen (i.e. bool, int, ...)
	 * 
	 * @param e
	 * @param type
	 * @return
	 */
	private Environment createEnvironment(Environment e, String type) {
		Pair<String, String>[] replacements;
		if (type.contains("<")) {
			replacements = new Pair[3];
			
			this.impl = TransformerFactory.getImplementation("sc_signal", "sc_signal_generic.ast.xml");
			String typeIdentifier = type.substring(0, type.indexOf("<"));
			String length = type.substring(type.indexOf("<")+1, type.length()-1);
			replacements[0] = new Pair<String, String>(Constants.GENERIC_TYPE, typeIdentifier);
			replacements[1] = new Pair<String, String>(Constants.GENERIC_TYPE_LENGTH, length);
			replacements[2] = new Pair<String, String>("sc_signalx", "sc_signal_" + typeIdentifier);
		} else {
			replacements = new Pair[2];
			replacements[0] = new Pair<String, String>(Constants.GENERIC_TYPE, type);
			replacements[1] = new Pair<String, String>("sc_signalx", "sc_signal_" + type);
		}
		
		return super.createGenericType(e, replacements);
	}
	
	/**
	 * removes special characters from the type String so that
	 * valid SCClass names will be created
	 * @param type
	 * @return
	 */
	private static String typeForTemplate(String type) {
		return type
				.replace(" ", "")
				.replace("<", "")
				.replace(">", "")
				.replace("_", "");
	}
	
	/**
	 * creates the name string for sc_signal
	 * -> sc_signal_TYPE
	 * @param type
	 * @return
	 */
 	public static String createName(String type) {
 		return "sc_signal" + Constants.GENERIC_TYPE_DELIMITER + typeForTemplate(type);
 	}
	
}
