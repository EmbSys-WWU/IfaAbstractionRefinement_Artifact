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
package de.tub.pes.syscir.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.util.Pair;




/**
 * This class is used to signal, that the containig variable  
 * should be set over a non deterministic choice by the Transformer.
 * 
 * 
 * @author Jan Phillip Kasper
 *
 */
public class SCVariableNonDetSet extends Expression {

	private static final long serialVersionUID = 1L;

	/**
	 * The variableEx containing the var we have to set to a non deterministic value
	 */
	protected SCVariableExpression var;
	
	public SCVariableNonDetSet(Node n, SCVariableExpression v) {
		super(n);
		this.var = v;
	}
	
	/**
	 * @return the varEx containing the var that has to be set to a non deterministic value
	 */
	public SCVariableExpression getVar() {
		return var;
	}
	

	@Override
	public String toString() {
		return super.toString() + " non deterministic "+ var.toString();
	}
	
	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int getNumOfChildren() {
        return 0;
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((var == null) ? 0 : var.hashCode());
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
		SCVariableNonDetSet other = (SCVariableNonDetSet) obj;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}

}
