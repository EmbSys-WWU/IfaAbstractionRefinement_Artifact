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
 * This expression models the basic assert() statement.
 * 
 * @author pockrandt
 * 
 */
public class AssertionExpression extends Expression {

	private static final long serialVersionUID = -2536174919926485707L;

	/**
	 * Represents the condition of the assertion.
	 */
	private Expression condition;

	public AssertionExpression(Node n, Expression cond) {
		super(n);
		setCondition(cond);
	}
	
	public Expression getCondition() {
		return condition;
	}

	public void setCondition(Expression cond) {
		this.condition = cond;
		if (cond != null) {
			cond.setParent(this);
		}
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> list = new LinkedList<Expression>();
		if (condition != null) {
			list.add(condition);
		}
		return list;
	}

    @Override
    public Expression getChild(int index) {
        if (index != 0 || this.condition == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return this.condition;
    }

	@Override
	public LinkedList<Expression> getInnerExpressions() {
		LinkedList<Expression> exps = new LinkedList<Expression>();
		if (condition != null) {
			exps.add(condition);
			exps.addAll(condition.getInnerExpressions());
		}
		return exps;
	}

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		condition = replaceSingleExpression(condition, replacements);
	}
	
	public String toString() {
		String ret = super.toString() + "assert("
				+ condition.toString().replace(";", "") + ");";
		return ret;
	}
		

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((condition == null) ? 0 : condition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof AssertionExpression)) {
			return false;
		}
		AssertionExpression other = (AssertionExpression) obj;
		if (condition == null) {
			if (other.condition != null) {
				return false;
			}
		} else if (!condition.equals(other.condition)) {
			return false;
		}
		return true;
	}

}
