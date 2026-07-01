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

/**
 * this class represents an event in SystemC the only important is it's name
 * 
 * @author Florian
 * 
 */
public class SCEvent extends SCVariable {

	private static final long serialVersionUID = -8291494536976028138L;

	/**
	 * Creats a new Event
	 * 
	 * @param nam
	 *            Name of the Event
	 * @param other_mods
	 * @param cons
	 * @param stat
	 */
	public SCEvent(String nam, boolean stat, boolean cons,
			List<String> other_mods) {
		super(nam);
		type = "SCEvent";
		_static = stat;
		_const = cons;
		otherModifiers = other_mods;
	}

	/**
	 * Constructor to initiate a new SCEvent with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param old SCEvent which will be copied
	 * @param newName new name of the variable
	 */
	protected SCEvent(SCEvent old, String newName) {
		super(old, newName);
	}

	/**
	 * initiates a new SCEvent with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param newName new name of the variable
	 * @return new SCEvent
	 */
	@Override
	public SCVariable flatCopyVariableWitNewName(String newName) {
		return new SCEvent(this, newName);
	}
	
	
	
}
