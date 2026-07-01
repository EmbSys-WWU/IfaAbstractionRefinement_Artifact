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
 * this expression is used for a FunctionCall of a socket
 * at the runtime of the transformation we don't know to which ModuleInstance a Port is connected
 * so we can't refer to a specified function, we only can save the functionname.
 * @author Florian
 *
 */
public class SocketFunctionCallExpression extends Expression {

	private static final long serialVersionUID = -1657072658518322361L;
	private String functionName;
	
	public SocketFunctionCallExpression(Node n, String funName){
		super(n);
		this.functionName = funName;
	}
	
	@Override
	public String toString() {
		return super.toString() + functionName + "();";
	}
	
	public String getFunction() {
		return functionName;
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
		result = prime * result
				+ ((functionName == null) ? 0 : functionName.hashCode());
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
		SocketFunctionCallExpression other = (SocketFunctionCallExpression) obj;
		if (functionName == null) {
			if (other.functionName != null)
				return false;
		} else if (!functionName.equals(other.functionName))
			return false;
		return true;
	}
}
