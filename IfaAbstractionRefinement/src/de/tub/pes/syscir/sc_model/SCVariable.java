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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.variables.SCArray;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;

public abstract class SCVariable implements Serializable {

	private static final long serialVersionUID = -7016058794777825908L;

	private static Logger logger = LogManager.getLogger(SCVariable.class
			.getName());

	protected String name;
	protected String type;
	/**
	 * saves the EnumType, null otherwise
	 * NOTE: At the moment only used by the abstraction engine. 
	 */
	protected SCEnumType enumInformation;
	//@Kannan 
	protected int processC;
	protected int sensC;
	//
	protected Boolean _static = false; // static
	protected Boolean _const = false; // const
	protected List<String> otherModifiers;
	/**
	 * The variableDeclarationExpression for this variable. This field is always
	 * null if the variable is no locale variable of a method. Might contain an
	 * initial value, if the variable is initialized during declaration.
	 */
	protected SCVariableDeclarationExpression declaration;

	public SCVariable(String nam) {
		name = nam;
		otherModifiers = new LinkedList<String>();
		declaration = null;
	}
	
	/**
	 * Constructor to initiate a new SCVariable with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param var variable to be replaced
	 * @param newName old name will be replaced by this
	 */
	protected SCVariable(SCVariable old, String newName) {
		this.name = newName;
		this.type = old.type;
		this.processC = old.processC;
		this.sensC = old.sensC;
		this._static = old._static;
		this._const = old._const;
		this.otherModifiers = null;
		this.declaration = null;
	}


	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getTypeWithoutSize() {
		String tmp = type;
		if (tmp.contains("<")) {
			int start = tmp.indexOf("<");
			int end = tmp.indexOf(">") + 1;
			tmp = tmp.substring(0, start) + tmp.substring(end);
		}
		return tmp;
	}

	public boolean isStatic() {
		return _static;
	}

	public boolean isConst() {
		return _const;
	}

	/**
	 * Returns the declaration of the variable. A declaration can contain an
	 * initial value. Might return null if the variable is a module variable.
	 *
	 * @return
	 */
	public SCVariableDeclarationExpression getDeclaration() {
		return declaration;
	}

	public void setDeclaration(SCVariableDeclarationExpression declaration) {
		this.declaration = declaration;
	}

	public List<String> otherMods() {
		return otherModifiers;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer(getDeclarationString());
		ret.append(getInitializationString());
		//@Kannan To create 2D array declaration for stat and dyn sensitivity
		String decString = getDeclarationString();
		
		if(decString.compareTo("int stat_sensitivity")==0 || decString.compareTo("int dyn_sensitivity")==0)
		{
			
			ret.append("[" + processC + "]" + "[" + sensC + "]");
		}
		//@Kannan : Ends here
		ret.append(";");
		
		return ret.toString();
	}
	
	/**
	 * Returns a string representation of the variable usable for the
	 * declaration of a variable. The declaration of a Variable always contains
	 * its type, its modifiers and its name but might be enhanced with [] for
	 * arrays (and other type specific constructs if necessary). The declaration
	 * string NEVER contains the initial values of a variable and is NEVER
	 * terminated with an ";".
	 *
	 * @return
	 */
	public String getDeclarationString() {
		StringBuffer out = new StringBuffer();

		if (_static) {
			out.append("static ");
		}

		if (_const) {
			out.append("const ");
		}

		out.append(getType() + " " + name);

		return out.toString();
	}

	/**
	 * Returns a string which represents the initialization if a variable.
	 * Initialization for basic data types like int always starts with an " = ",
	 * followed by the initial values. If multiple initial values are provided
	 * the basic method uses a constructor call. Other variables might use
	 * special handling like constructor calls, or array initialization with '{'
	 * and '}'. The initialization string NEVER ends with an ";". If the
	 * Variable has no initial value this method returns an EMPTY String.
	 *
	 * @return
	 */
	public String getInitializationString() {
		if (hasInitialValue()) {
			StringBuffer out = new StringBuffer(" = ");

			if (getInitialValueCount() == 1) {
				out.append(declaration.getInitialValues().get(0));
			} else {
				out.append(getType() + "(");
				for (Expression exp : declaration.getInitialValues()) {
					out.append(exp.toString() + ", ");
				}
				out.setLength(out.length() - 2);
				out.append(")");
			}
			return out.toString();
		} else {
			return "";
		}

	}

	public void print() {
	}

	/**
	 * initiates a new SCVariable with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @return new SCVariable
	 */
	abstract public SCVariable flatCopyVariableWitNewName(String newName);

	/**
	 * Sets the _const flag for the variable, marking whether it is a constant
	 * variable (= true) or not (=false).
	 *
	 * @param value
	 */
	public void setConst(boolean value) {
		this._const = value;
	}

	public void setType(String type) {
		this.type = type;
		if(enumInformation != null)
			enumInformation = null;
	}
	
	public void setEnumType(SCEnumType enumType){
		enumInformation = enumType;
		this.type = enumType.getName();
	}
	
	/**
	 * @return null or the SCEnum this var is a type of
	 * NOTE: only used for abstraction engine at the moment. 
	 */
	public SCEnumType getEnumType(){
		return enumInformation;
		}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_const == null) ? 0 : _const.hashCode());
		result = prime * result + ((_static == null) ? 0 : _static.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((otherModifiers == null) ? 0 : otherModifiers.hashCode());
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
		SCVariable other = (SCVariable) obj;
		if (_const == null) {
			if (other._const != null)
				return false;
		} else if (!_const.equals(other._const))
			return false;
		if (_static == null) {
			if (other._static != null)
				return false;
		} else if (!_static.equals(other._static))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (otherModifiers == null) {
			if (other.otherModifiers != null)
				return false;
		} else if (!otherModifiers.equals(other.otherModifiers))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * Returns true if the variable has at least one initial value and false if
	 * not.
	 *
	 * @return
	 */
	public boolean hasInitialValue() {
		return this.declaration != null
				&& (this.declaration.getInitialValues().size() > 0);
	}

	/**
	 * Returns the number of initial values of this variable. A variable without
	 * initial values (or without any declaration) returns 0.
	 * 
	 * @return
	 */
	public int getInitialValueCount() {
		if (hasInitialValue()) {
			return this.declaration.getInitialValues().size();
		} else {
			return 0;
		}
	}

	public boolean hasDeclaration() {
		return declaration != null;
	}

	/**
	 * We sometimes check whether a variable is a SClassInstance. Most of the
	 * time, whatever it is we want to do also applies for arrays of
	 * SClassInstances. Therefore, we add some convenient helper functions here
	 * to get what we need in those cases.
	 * 
	 * @return
	 */
	public boolean isSCClassInstance() {
		return (this instanceof SCClassInstance);
	}
	public boolean isArrayOfSCClassInstances() {
		return (this instanceof SCArray && ((SCArray) this)
				.isArrayOfSCClassInstances());
	}

	public boolean isSCClassInstanceOrArrayOfSCClassInstances() {
		return (isSCClassInstance() || this.isArrayOfSCClassInstances());
	}

	public SCClass getSClassIfPossible() {
		if (this instanceof SCClassInstance) {
			return ((SCClassInstance) this).getSCClass();
		} else if (this.isArrayOfSCClassInstances()) {
			return ((SCArray) this).getSCClass();
		}
		return null;
	}

	public boolean isSCModule() {
		if (isSCClassInstance()) {
			return ((SCClassInstance) this).isSCModule();
		}
		return false;
	}

	public boolean isNotSCModule() {
		return !isSCModule();
	}

	public boolean isChannel() {
		if (isSCClassInstance()) {
			return ((SCClassInstance) this).isChannel();
		}
		return false;
	}

	public boolean isNotChannel() {
		return !isChannel();
	}
}
