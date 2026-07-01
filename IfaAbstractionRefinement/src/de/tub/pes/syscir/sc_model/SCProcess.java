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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCPortEvent;

/**
 * this class represents a SCProcess it has a Link to a SCFunction, a List of
 * Events on which this process is sensitive for and en enumSet of Modifiers
 * theirs also a name, which is usually equal to the function-name an a Type, for
 * example SCThread, SCMethod
 * 
 * @author Florian
 * 
 */

public class SCProcess implements Serializable {

	private static final long serialVersionUID = 4827343065412103535L;

	/**
	 * the name of the Process
	 */
	private String name;
	/**
	 * the List of events, on which the Process is sensitive on
	 */
	private List<SCEvent> sensitive_on;
	/**
	 * the modifier of the process, like dontinitialize
	 */
	private EnumSet<SCMODIFIER> modifier;
	/**
	 * the Function to which this process refer
	 */
	private SCFunction function;
	/**
	 * the type of the process
	 */
	private SCPROCESSTYPE type;

	/**
	 * creates a dummy process
	 * 
	 * @param nam
	 *            name of the process
	 */
	public SCProcess(String nam) {
		name = nam;
		sensitive_on = new ArrayList<SCEvent>();
	}

	/**
	 * creates a new process with a function and a processtype
	 * 
	 * @param nam
	 *            name of the new process
	 * @param t
	 *            type of the process
	 * @param fct
	 *            function of the process
	 */
	public SCProcess(String nam, SCPROCESSTYPE t, SCFunction fct) {
		name = nam;
		function = fct;
		type = t;
		modifier = EnumSet.of(SCMODIFIER.NONE);
		sensitive_on = new ArrayList<SCEvent>();
	}

	/**
	 * creates a new process with a function, a processtype and a list of events
	 * where this process is sensitive for
	 * 
	 * @param nam
	 *            name of the process
	 * @param t
	 *            type of the process
	 * @param fct
	 *            function of the process
	 * @param sens_lst
	 *            sensitivity-list
	 */
	public SCProcess(String nam, SCPROCESSTYPE t, SCFunction fct,
			List<SCEvent> sens_lst) {
		name = nam;
		sensitive_on = sens_lst;
		function = fct;
		type = t;
		modifier = EnumSet.of(SCMODIFIER.NONE);
	}

	/**
	 * creates a new process with a function, a processtype and a Set of
	 * modifiers
	 * 
	 * @param nam
	 *            name of the process
	 * @param t
	 *            type of the process
	 * @param fct
	 *            function of the process
	 * @param mod
	 *            set of modifiers
	 */
	public SCProcess(String nam, SCPROCESSTYPE t, SCFunction fct,
			EnumSet<SCMODIFIER> mod) {
		name = nam;
		function = fct;
		type = t;
		if (mod != null) {		
			modifier = mod;
		} else {
			modifier = EnumSet.of(SCMODIFIER.NONE);
		}
		sensitive_on = new ArrayList<SCEvent>();
	}

	/**
	 * creates a new process with a function, a processtype, a list of events
	 * where this process is sensitive for and a set of modifiers
	 * 
	 * @param nam
	 *            name of the process
	 * @param t
	 *            type of the process
	 * @param fct
	 *            function of the process
	 * @param sens_lst
	 *            sensitivity-list
	 * @param mod
	 *            set of modifiers
	 */
	public SCProcess(String nam, SCPROCESSTYPE t, SCFunction fct,
			List<SCEvent> sens_lst, EnumSet<SCMODIFIER> mod) {
		name = nam;
		sensitive_on = sens_lst;
		function = fct;
		type = t;
		if (mod != null) {		
			modifier = mod;
		} else {
			modifier = EnumSet.of(SCMODIFIER.NONE);
		}
	}

	/**
	 * return name of the process
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * return the list of the Events on which the process is sensitive
	 * 
	 * @return List<SCEvent>
	 */
	public List<SCEvent> getSensitivity() {
		return sensitive_on;
	}

	/**
	 * returns the function of the process
	 * 
	 * @return SCFunction
	 */
	public SCFunction getFunction() {
		return function;
	}

	/**
	 * returns the function of the process
	 * 
	 * @return SCFunction
	 */
	public void setFunction(SCFunction f) {
		function=f;
	}

	/**
	 * returns the type of the process
	 * 
	 * @return SCPROCESSTYPE
	 */
	public SCPROCESSTYPE getType() {
		return type;
	}

	/**
	 * returns the set of modifiers
	 * 
	 * @return EnumSet<SCMODIFIER>
	 */
	public EnumSet<SCMODIFIER> getModifier() {
		return modifier;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		String ret = type.toString() + "(" + function.getName() + ");\n";
		
		if (modifier != null) {
			for (SCMODIFIER mod : modifier) {
				if (mod != SCMODIFIER.NONE) {
					ret += mod.toString() + ";\n";
				}
			}
		}

		if (sensitive_on != null && !sensitive_on.isEmpty()) {
			ret += "sensitive";
			for (SCEvent e : sensitive_on) {
				ret += " << " + e.getName();
				if (e instanceof SCPortEvent) {
					SCPortEvent scpe = (SCPortEvent) e;
					ret += "." + scpe.getEventType();
				}
			}
			ret += ";";
		}
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((function == null) ? 0 : function.hashCode());
		result = prime * result
				+ ((modifier == null) ? 0 : modifier.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((sensitive_on == null) ? 0 : sensitive_on.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		SCProcess other = (SCProcess) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (modifier == null) {
			if (other.modifier != null)
				return false;
		} else if (!modifier.equals(other.modifier))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (sensitive_on == null) {
			if (other.sensitive_on != null)
				return false;
		} else if (!sensitive_on.equals(other.sensitive_on))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
