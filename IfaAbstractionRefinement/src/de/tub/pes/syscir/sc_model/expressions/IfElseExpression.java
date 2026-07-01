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
 * this expression represents the If-Then-Else-Construct it contains one
 * Expression which represent the condition anf two lists of Expressions, one
 * represents the then-case and one the else-case
 * 
 * @author Florian
 * 
 */
public class IfElseExpression extends Expression {

	private static final long serialVersionUID = -5645500451034258989L;
	private Expression condition;
	private List<Expression> thenBlock;
	private List<Expression> elseBlock;

	public IfElseExpression(Node n, Expression cond, List<Expression> t,
			List<Expression> e) {
		super(n);
		setCondition(cond);
		setThenBlock(t);
		setElseBlock(e);
	}

	public IfElseExpression(Node n, Expression cond, Expression t, Expression e) {
		super(n);
		setCondition(cond);
		addThenExpression(t);
		addElseExpression(e);
	}

	@Override
	public String toString() {
		String ret = super.toString() + "if ("
				+ condition.toString().replace(";", "") + ") {";
		for (Expression e : thenBlock) {
			ret += "\n" + e.toString()/* .replace(";", "") + ";" */;
		}
		if (!elseBlock.isEmpty()) {
			ret += "\n} else {";
			for (Expression e : elseBlock) {
				ret += "\n" + e.toString()/* .replace(";", "") + ";" */;
			}

		}
		return ret + "\n}";
	}

	public void addThenExpression(Expression exp) {
		if (thenBlock == null) {
			thenBlock = new LinkedList<Expression>();
		}
		exp.setParent(this);
		thenBlock.add(exp);

	}

	public void addElseExpression(Expression exp) {
		if (elseBlock == null) {
			elseBlock = new LinkedList<Expression>();
		}
		exp.setParent(this);
		elseBlock.add(exp);
	}

	public List<Expression> getThenBlock() {
		return this.thenBlock;
	}

	public void setThenBlock(List<Expression> thenBlock) {
		this.thenBlock = thenBlock;
		for (Expression exp : this.thenBlock) {
			exp.setParent(this);
		}
	}

	public List<Expression> getElseBlock() {
		return this.elseBlock;
	}

	public void setElseBlock(List<Expression> elseBlock) {
		this.elseBlock = elseBlock;
		for (Expression exp : this.elseBlock) {
			exp.setParent(this);
		}
	}

	public Expression getCondition() {
		return this.condition;
	}

	public void setCondition(Expression cond) {
		this.condition = cond;
		this.condition.setParent(this);
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		exps.add(condition);
		exps.addAll(condition.getInnerExpressions());

		for (Expression exp : thenBlock) {
			exps.add(exp);
			exps.addAll(exp.getInnerExpressions());
		}

		for (Expression exp : elseBlock) {
			exps.add(exp);
			exps.addAll(exp.getInnerExpressions());
		}
		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.add(condition);
		ret.addAll(thenBlock);
		ret.addAll(elseBlock);
		return ret;
	}

    @Override
    public int getNumOfChildren() {
        return this.thenBlock.size() + this.elseBlock.size() + 1;
    }

    @Override
    public Expression getChild(int index) {
        if (index == 0) {
            return this.condition;
        }
        index--;
        if (index < this.thenBlock.size()) {
            return this.thenBlock.get(index);
        }
        index -= this.thenBlock.size();
        return this.elseBlock.get(index);
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {

		condition = replaceSingleExpression(condition, replacements);
		replaceExpressionList(thenBlock, replacements);
		replaceExpressionList(elseBlock, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((condition == null) ? 0 : condition.hashCode());
		result = prime * result
				+ ((elseBlock == null) ? 0 : elseBlock.hashCode());
		result = prime * result
				+ ((thenBlock == null) ? 0 : thenBlock.hashCode());
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
		IfElseExpression other = (IfElseExpression) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (elseBlock == null) {
			if (other.elseBlock != null)
				return false;
		} else if (!elseBlock.equals(other.elseBlock))
			return false;
		if (thenBlock == null) {
			if (other.thenBlock != null)
				return false;
		} else if (!thenBlock.equals(other.thenBlock))
			return false;
		return true;
	}
}
