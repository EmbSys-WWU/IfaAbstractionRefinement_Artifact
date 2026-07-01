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
 * this expression represents a Binary-Construct out of the systemC-Code it
 * contains the left-hand expression, the operator and the right-hand expression
 * a BinaryExpressions can be everything where to exressions ar conected their
 * are, Arithmetic-Operators, Logic-Operators, relational-Operators, but also
 * the "." for PostFix-Constructs and the "" All Operators are in the
 * <link>OPERATOR</link>-Class
 * 
 * @author Florian
 * 
 */
public class BinaryExpression extends Expression {

	private static final long serialVersionUID = -2396981160240523208L;
	private Expression left;
	private String op;
	private Expression right;

	public BinaryExpression(Node n, Expression left, String o, Expression right) {
		super(n);
		setLeft(left);
		this.op = o;
		setRight(right);
	}

	@Override
	public String toString() {
		return super.toString() + left.toString().replace(";", "") + " " + op
				+ " " + right.toString().replace(";", "") + ";";
	}

	public Expression getLeft() {
		return left;
	}

	public void setLeft(Expression left) {
		this.left = left;
		this.left.setParent(this);
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public Expression getRight() {
		return right;
	}

	public void setRight(Expression right) {
		this.right = right;
		this.right.setParent(this);
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		exps.add(left);
		exps.addAll(left.getInnerExpressions());
		exps.add(right);
		exps.addAll(right.getInnerExpressions());

		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.add(left);
		ret.add(right);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.left;
            case 1 -> this.right;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int getNumOfChildren() {
        return 2;
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		left = replaceSingleExpression(left, replacements);
		right = replaceSingleExpression(right, replacements);

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		BinaryExpression other = (BinaryExpression) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (op == null) {
			if (other.op != null)
				return false;
		} else if (!op.equals(other.op))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}
}
