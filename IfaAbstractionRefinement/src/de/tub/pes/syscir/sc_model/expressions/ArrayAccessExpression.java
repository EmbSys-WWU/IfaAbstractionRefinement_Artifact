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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.util.Pair;
import de.tub.pes.syscir.sc_model.SCVariable;

/**
 * This Expression represents the access to an array (e.g., arr[x]). An array
 * can either be a SCArray or a SCPointer. It contains the array (e.g. the scvar
 * from SCVarExpr) and the expression used to access the array.
 * 
 * @author Marcel, Florian, rschroeder
 * 
 */
public class ArrayAccessExpression extends SCVariableExpression {

	private static final long serialVersionUID = -5917543828733388700L;

	/**
	 * An array can either be represented by a SCArray or by a SCPointer.
	 */
	protected List<Expression> access;

	public ArrayAccessExpression(Node n, SCVariable array,
			List<Expression> access) {
		super(n, array);
		setAccess(access);
	}

	public ArrayAccessExpression(Node n, SCVariable array, Expression accessExpr) {
		this(n, array, new ArrayList<Expression>());
		access.add(accessExpr);
	}

	@Override
	public String toString() {
		String ret = !label.equals("") ? label + ": " + var.getName() : var
				.getName();
		for (Expression e : access) {
			ret = ret + "[" + e.toStringNoSem() + "]";
		}
		return ret + ";";
	}

	public List<Expression> getAccess() {
		return access;
	}

	public void setAccess(List<Expression> access) {
		this.access = access;
		for (Expression exp : this.access) {
			exp.setParent(this);
		}
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		for (Expression exp : access) {
			exps.add(exp);
			exps.addAll(exp.getInnerExpressions());
		}
		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		return new LinkedList<Expression>(access);
	}

    @Override
    public Expression getChild(int index) {
        return this.access.get(index);
    }

    @Override
    public int getNumOfChildren() {
        return access.size();
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		replaceExpressionList(access, replacements);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((access == null) ? 0 : access.hashCode());
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
		ArrayAccessExpression other = (ArrayAccessExpression) obj;
		if (access == null) {
			if (other.access != null)
				return false;
		} else if (!access.equals(other.access))
			return false;
		return true;
	}

}
