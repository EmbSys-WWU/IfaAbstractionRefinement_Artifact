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

package de.tub.pes.syscir.sc_model.variables;

import java.util.ArrayList;
import java.util.List;

import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;

/**
 * this class represents a simpletype, such as int, unsign int, ... all possible
 * simpletypes are found in the config/simpletypes.properties
 * 
 * @author Florian
 * 
 */

public class SCSimpleType extends SCVariable {

	private static final long serialVersionUID = -5056967513486882322L;

	/**
	 * constructor inherits from superclass
	 * 
	 * @param nam
	 *            name of the variable
	 */
	public SCSimpleType(String nam) {
		super(nam);
		otherModifiers = new ArrayList<String>();
	}

	/**
	 * constructor inherits from superclass
	 * 
	 * @param nam
	 *            name of the variable
	 * @param t
	 *            type of the variable
	 * @param other_mods
	 * @param cons
	 * @param stat
	 */
	public SCSimpleType(String nam, String t, boolean stat, boolean cons,
			List<String> other_mods) {
		super(nam);
		type = t;
		_static = stat;
		_const = cons;
		otherModifiers = other_mods;
	}

	/**
	 * creates new SimpleType-Variable which is initialized with a value
	 * 
	 * @param nam
	 *            name of the variable
	 * @param t
	 *            type of the variable
	 * @param val
	 *            value of the variable
	 */
	public SCSimpleType(String nam, String t, Expression val, boolean stat,
			boolean cons, List<String> other_mods) {
		super(nam);
		type = t;
//		if (val != null) {
//			this.setFirstInitialExpression(val);
//		}
		_static = stat;
		_const = cons;
		otherModifiers = other_mods;
	}

	public SCSimpleType(String name, String type, Expression val, boolean cons) {
		super(name);
		this.type = type;
//		if (val != null) {
//			this.setFirstInitialExpression(val);
//		}
		this._const = cons;
		_static = false;
		otherModifiers = new ArrayList<String>();
	}

	public SCSimpleType(String name, String type, Expression val) {
		super(name);
		this.type = type;
//		if (val != null) {
//			this.setFirstInitialExpression(val);
//		}
		//added by ammar for initializing simulationStopped and simulationTime
		if(val!=null){
			this.setDeclaration(new SCVariableDeclarationExpression(null,
					new SCVariableExpression(null, new SCSimpleType(name,
							type)), val));
		}
		//ends here
		_static = false;
		_const = false;
		otherModifiers = new ArrayList<String>();
	}

	public SCSimpleType(String name, String type) {
		super(name);
		this.type = type;

		_static = false;
		_const = false;
		otherModifiers = new ArrayList<String>();
	}

	/**
	 * Constructor to initiate a new SCSimpleType with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param old SCSimpleType which will be copied
	 * @param newName new name of the variable
	 */
	public SCSimpleType(SCSimpleType old, String newName) {
		super(old, newName);
	}

	/**
	 * initiates a new SCSimpleType with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param newName new name of the variable
	 * @return new SCSimpleType
	 */
	@Override
	public SCVariable flatCopyVariableWitNewName(String newName) {
		return new SCSimpleType(this, newName);
	}
}
