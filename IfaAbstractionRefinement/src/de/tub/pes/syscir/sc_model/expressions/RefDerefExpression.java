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
 * This expression represents all referencing and dereferencing expressions like
 * referencing of variables (&x) and dereferencing of pointers (*ptr).
 * 
 * @author pockrandt
 * 
 */
public class RefDerefExpression extends Expression {

	private static final long serialVersionUID = 2397706482279356874L;

	/**
	 * The referencing operation (&)
	 */
	public static final boolean REFERENCING = true;
	/**
	 * The dereferencing operation (*)
	 */
	public static final boolean DEREFERENCING = false;

	private Expression expression;
	private boolean isReferencing;

	public RefDerefExpression(Node n, Expression exp, boolean isReferencing) {
		super(n);
		setExpression(exp);
		this.isReferencing = isReferencing;
	}

	public Expression getExpression() {
		return this.expression;
	}

	public void setExpression(Expression exp) {
		this.expression = exp;
		this.expression.setParent(this);
	}

	public boolean isReferencing() {
		return isReferencing;
	}

	public boolean isDerefencing() {
		return !isReferencing();
	}

	@Override
	public String toString() {
		return super.toString() + (isReferencing ? "&" : "*")
				+ expression.toString();
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		exps.add(expression);
		exps.addAll(expression.getInnerExpressions());
		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.add(expression);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.expression;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		expression = replaceSingleExpression(expression, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + (isReferencing ? 1231 : 1237);
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
		RefDerefExpression other = (RefDerefExpression) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (isReferencing != other.isReferencing)
			return false;
		return true;
	}
}
