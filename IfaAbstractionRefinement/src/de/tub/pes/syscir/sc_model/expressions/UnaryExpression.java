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
 * This expression represents all unary expressions (expressions with one
 * Operator in front or at the end). This is only an abstract class and is
 * specified further by UnaryPreExpression and UnaryPostExpression.
 * 
 * @author Florian
 * 
 */
public class UnaryExpression extends Expression {

	public static final boolean PRE = true;
	public static final boolean POST = false;

	private static final long serialVersionUID = -4161776944237991064L;
	private Expression expression;
	private String operator;
	private boolean prepost;

	public UnaryExpression(Node n, boolean prepost, String op, Expression exp) {
		super(n);
		this.prepost = prepost;
		this.operator = op;
		setExpression(exp);
	}

	@Override
	public String toString() {
		if (prepost == PRE) {
			if (operator.equals("return") && expression == null) {
				return super.toString() + operator + ";";
			} else {
				return super.toString() + operator
						+ expression.toString().replace(";", "") + ";";
			}
		} else {
			return super.toString() + expression.toString().replace(";", "")
					+ operator + ";";
		}
	}

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
		this.expression.setParent(this);
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public boolean isPrepost() {
		return prepost;
	}

	public void setPrepost(boolean prepost) {
		this.prepost = prepost;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
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
        if (index == 0) {
            return expression;
        }
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int getNumOfChildren() {
        return 1;
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
		result = prime * result
				+ ((operator == null) ? 0 : operator.hashCode());
		result = prime * result + (prepost ? 1231 : 1237);
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
		UnaryExpression other = (UnaryExpression) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		if (operator == null) {
			if (other.operator != null)
				return false;
		} else if (!operator.equals(other.operator))
			return false;
		if (prepost != other.prepost)
			return false;
		return true;
	}
}
