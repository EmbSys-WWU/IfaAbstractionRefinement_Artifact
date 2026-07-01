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
 * this Expression is only for better reading after a print
 * 
 * @author Florian
 * 
 */
public class BracketExpression extends Expression {

	private static final long serialVersionUID = 5690296452803522658L;

	private Expression inBrackets;

	public BracketExpression(Node n, Expression exp) {
		super(n);
		setInBrackets(exp);
	}

	public Expression getInBrackets() {
		return inBrackets;
	}

	public void setInBrackets(Expression exp) {
		this.inBrackets = exp;
		this.inBrackets.setParent(this);
	}

	@Override
	public String toString() {
		return super.toString() + "(" + inBrackets.toString().replace(";", "")
				+ ");";
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		exps.add(inBrackets);
		exps.addAll(inBrackets.getInnerExpressions());

		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.add(inBrackets);
		return ret;
	}

    @Override
    public int getNumOfChildren() {
        return 1;
    }

    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.inBrackets;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		inBrackets = replaceSingleExpression(inBrackets, replacements);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((inBrackets == null) ? 0 : inBrackets.hashCode());
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
		BracketExpression other = (BracketExpression) obj;
		if (inBrackets == null) {
			if (other.inBrackets != null)
				return false;
		} else if (!inBrackets.equals(other.inBrackets))
			return false;
		return true;
	}

}
