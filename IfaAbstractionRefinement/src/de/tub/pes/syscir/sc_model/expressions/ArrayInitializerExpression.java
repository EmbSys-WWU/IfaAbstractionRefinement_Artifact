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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.util.Pair;

/**
 * this expression represents an Initializeation of an array out of the
 * SystemC-Code it contains an array of Expressions which represents the
 * dimension-values
 * 
 * @author Florian
 * 
 */
public class ArrayInitializerExpression extends Expression {

	private static final long serialVersionUID = -4430516385046264470L;

	protected Expression[] values = null;

	public ArrayInitializerExpression(Node n, int dim) {
		super(n);
		this.values = new Expression[dim];
	}

	public void initAtPosition(int pos, Expression val) {
		this.values[pos] = val;
		if (val != null) {
			this.values[pos].setParent(this);
		}
	}

	public Expression expAtPosition(int pos) {
		return values[pos];
	}

	/**
	 * Returns the number of elements in this ArrayInitializerExpression.
	 * 
	 * @return
	 */
	public int getArrayElementCount() {
		return values.length;
	}

	@Override
	public String toString() {
		String ret = super.toString() + "{";
		for (Expression exp : values) {
			ret = ret + exp.toString().replace(";", "") + ", ";
		}
		if (values.length > 0) {
			ret = ret.substring(0, ret.length() - 2);
		}
		ret = ret + "}";
		return ret + ";";
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		for (Expression exp : values) {
			exps.add(exp);
			exps.addAll(exp.getInnerExpressions());
		}
		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.addAll(Arrays.asList(values));
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return this.values[index];
    }

    @Override
    public int getNumOfChildren() {
        return this.values.length;
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		replaceExpressionArray(values, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(values);
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
		ArrayInitializerExpression other = (ArrayInitializerExpression) obj;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}

}
