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

package de.tub.pes.syscir.sc_model;

import java.io.Serializable;

import de.tub.pes.syscir.sc_model.variables.SCPointer;

/**
 * Encapsules variables used as parameters in the head of a function. A
 * Parameter consists of a variable and a reference type (either by value or by
 * reference).
 * 
 * @author pockrandt
 * 
 */
public class SCParameter implements Serializable {

	private static final long serialVersionUID = -8632218974613314166L;

	private SCVariable var;
	private SCREFERENCETYPE refType;
	/**
	 * The scfunction this parameter is a parameter of.
	 */
	private SCFunction function;

	/**
	 * Constructs a parameter with the name and type specified in var and the
	 * reference type refType
	 * 
	 * @param var
	 * @param refType
	 */
	public SCParameter(SCVariable var, SCREFERENCETYPE refType) {
		assert var != null;
		this.var = var;
		this.refType = refType;
		// usually, the parameter is created before the function is created.
		// Therefore we set function to null here. It is set when a parameter is
		// added to a function in SCFunction.
		this.function = null;
	}

	@Override
	public String toString() {
		String ret = (var.isConst() ? "const " : "") + var.getType();
		if (var instanceof SCPointer) {
			return ret + " *" + var.getName();
		} else {
			return ret + " " + refType.getSymbol() + var.name;
		}
	}

	public SCVariable getVar() {
		return var;
	}

	public void setVar(SCVariable var) {
		this.var = var;
	}

	public SCREFERENCETYPE getRefType() {
		return refType;
	}

	public void setRefType(SCREFERENCETYPE refType) {
		this.refType = refType;
	}

	/**
	 * Returns the function this parameter belongs to or null, if the function
	 * is not set.
	 * 
	 * @return
	 */
	public SCFunction getFunction() {
		return function;
	}

	public void setFunction(SCFunction function) {
		this.function = function;
	}

	public boolean isConst() {
		return var.isConst();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((refType == null) ? 0 : refType.hashCode());
		result = prime * result + ((var == null) ? 0 : var.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SCParameter other = (SCParameter) obj;
		if (refType != other.refType)
			return false;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}

}
