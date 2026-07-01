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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.ArrayInitializerExpression;
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;

/**
 * An array has either a size *or* initial expressions. An array is always on
 * the stack. Dynamic arrays are pointers.
 * 
 * @author rschroeder
 */
public class SCArray extends SCVariable {
	private static transient Logger logger = LogManager.getLogger(SCArray.class
			.getName());
	private static final long serialVersionUID = -1135916937921526291L;

	/**
	 * Contains the size of the array. Might be null if the if the array is a
	 * parameter of a function and therefore its size is not declared and the
	 * array has no initial expression in its declaration.
	 */
	private Expression size = null;

	/**
	 * If this array is an array of sc class instances, make sure we have a link
	 * to the belonging class. See set setIsArrayOfSCClassInstances() below.
	 */
	private SCClass scClass = null;

	public SCArray(String name, String type) {
		super(name);
		this.setType(type);
	}

	public SCArray(String name, String type, Expression size) {
		this(name, type);
		this.size = size;
	}

	/**
	 * Constructor to initiate a new SCArray with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param old SCArray which will be copied
	 * @param newName new name of the variable
	 */
	public SCArray(SCArray old, String newName) {
		super(old, newName);
		this.size = null;
		this.scClass = null;
	}
	
	/**
	 * Returns the size of the array as an expression or null if the array is
	 * used as a parameter and therefore does not have a fixed size. The
	 * resulting Expression might be derived by the
	 * SCVariableDeclarationExpression in the case where an array is declared
	 * like this "int arr[] = {0,1,2};" this function will return a
	 * ConstantExpression containing the String "3".
	 * 
	 * @return
	 */
	public Expression getSize() {
		return size;
	}
	
	/**
	 * Get the real size of the array. Either by the size expression
	 * or as a ConstantExpression derived from the declaration. This
	 * never returns null!
	 * @return
	 */
	public Expression getDerivedSize() {
		if (size != null) {
			return size;
		} else {
			String mySize = "0";
			if (declaration != null && declaration.getFirstInitialValue() != null) {
				mySize = Integer.toString(declaration.getFirstInitialValue().getInnerExpressions().size());
			}
			return new ConstantExpression(null, mySize);
		}
	}

	public void setSize(Expression size) {
		this.size = size;
	}

	@Override
	public String toString() {
		StringBuffer out = new StringBuffer(super.getDeclarationString());
		out.append("[");
		if (this.size != null) {
			out.append(size.toStringNoSem());
		}
		out.append("]");
		out.append(";");
		return out.toString();
	}

	@Override
	public void setDeclaration(SCVariableDeclarationExpression declaration) {
		Expression init = declaration.getFirstInitialValue();
		if (init != null && !(init instanceof ArrayInitializerExpression)) {
			logger.error("Setting an array declaration for {} which doesn't use an ArrayInitializerExpression", getName());
		}
		super.setDeclaration(declaration);
	}

	@Override
	public String getDeclarationString() {
		StringBuffer out = new StringBuffer(super.getDeclarationString());
		out.append("[");
		if (size != null) {
			out.append(size.toStringNoSem());
		}
		out.append("]");
		return out.toString();
	}

	@Override
	public String getInitializationString() {
		if (hasInitialValue()) {
			StringBuffer out = new StringBuffer(" = ");
			ArrayInitializerExpression arrInit = (ArrayInitializerExpression) declaration.getFirstInitialValue();
			out.append(arrInit.toStringNoSem());
			return out.toString();
		} else {
			return "";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		if (size != null) {
			result *= prime * size.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SCArray other = (SCArray) obj;
		if (size != null && (!size.equals(other.size))) {
			return false;
		}
		return true;
	}

	public void setIsArrayOfSCClassInstances(SCClass scc) {
		this.scClass = scc;
	}

	public boolean isArrayOfSCClassInstances() {
		return scClass != null;
	}

	public SCClass getSCClass() {
		return scClass;
	}

	/**
	 * initiates a new SCArray with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param newName new name of the variable
	 * @return new SCArray
	 */
	@Override
	public SCVariable flatCopyVariableWitNewName(String newName) {
		return new SCArray(this, newName);
	}
}
