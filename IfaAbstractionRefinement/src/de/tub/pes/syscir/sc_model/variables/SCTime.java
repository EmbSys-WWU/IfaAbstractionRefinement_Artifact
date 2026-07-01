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

import java.util.List;

import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.Expression;

/**
 * This class represents a Time-Object in SystemC.
 * 
 * @author rschroeder
 * 
 */
public class SCTime extends SCVariable {

	private static final long serialVersionUID = -7735653162175762184L;

	private boolean useFuncCall = false;

	public SCTime(String nam, boolean stat, boolean cons,
			List<String> other_mods, boolean useFuncCall) {
		super(nam);
		this.type = "sc_time";
		this._static = stat;
		this._const = cons;
		this.otherModifiers = other_mods;
		this.useFuncCall = useFuncCall;
	}

	/**
	 * Constructor to initiate a new SCTime with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param old SCTime which will be copied
	 * @param newName new name of the variable
	 */
	public SCTime(SCTime old, String newName) {
		super(old, newName);
	}
	
	@Override
	public String getInitializationString() {
		if (useFuncCall && getInitialValueCount() > 0) {
			StringBuffer ret = new StringBuffer(" = sc_time");
			ret.append("(");
			for (Expression exp : declaration.getInitialValues()) {
				ret.append(exp.toStringNoSem() + ", ");
			}
			ret.setLength(ret.length() - 2);
			ret.append(")");
			return ret.toString();
		}
		return "";
	}

	/**
	 * initiates a new SCTime with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param newName new name of the variable
	 * @return new SCTime
	 */
	@Override
	public SCVariable flatCopyVariableWitNewName(String newName) {
		return new SCTime(this, newName);
	}

	
	
}
