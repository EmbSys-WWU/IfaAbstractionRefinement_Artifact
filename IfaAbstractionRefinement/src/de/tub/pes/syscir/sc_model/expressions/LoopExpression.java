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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.util.Pair;

/**
 * this Expression represent the basic loop, containing a condition and a body.
 * For a specific loop please use the implementations like ForLoop, WhileLoop
 * and DoWhileLoop.
 * 
 * @author Florian
 * 
 */
public abstract class LoopExpression extends Expression {

	private static final long serialVersionUID = -1775301091944386408L;
	private static final transient Logger logger = LogManager
			.getLogger(LoopExpression.class.getName());

	private Expression condition = null;
	private List<Expression> loopBody = null;

	/** User may annotate maximum loop iterations. -1 means unknown, 0 = infinity. */
	private int maxCount = -1;

	public LoopExpression(Node n, String l, Expression cond,
			List<Expression> body) {
		super(n, l);
		setCondition(cond);
		setLoopBody(body);
	}

	public LoopExpression(Node n, String l, Expression cond,
			List<Expression> body, int maxCount) {
		super(n, l);
		setCondition(cond);
		setLoopBody(body);
		setMaxCount(maxCount);
	}

	public Expression getCondition() {
		return condition;
	}


	public void setCondition(Expression condition) {
		this.condition = condition;
		this.condition.setParent(this);
	}

	public List<Expression> getLoopBody() {
		return loopBody;
	}

	public void setLoopBody(List<Expression> loopBody) {
		this.loopBody = loopBody;
		for (Expression exp : this.loopBody) {
			if (exp == null) {
				logger.error("expression in loop body is null: {}", loopBody);
			} else {
				exp.setParent(this);
			}
		}
	}

	/**
	 * Get the max iteration frequency. 
	 * -1 means unknown, 0 means infinite (e.g. while(1))
	 * @return The maximum frequency this loop is iterated.
	 */
	public int getMaxCount() {
		return maxCount;
	}


	/**
	 * Set the max iteration frequency. 
	 * -1 means unknown, 0 means infinite (e.g. while(1))
	 */
	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public String toString() {
		if(this.label != null && !this.label.equals(""))
				return this.label + ": ";
		else
			return "";
	}

	public void addExpression(Expression exp) {
		exp.setParent(this);
		this.loopBody.add(exp);
	}

	/**
	 * Return everything in between the '()' of the loop.
	 * @return
	 */
	public List<Expression> getHeader() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.add(condition);
		return ret;
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		exps.add(condition);
		exps.addAll(condition.getInnerExpressions());

		for (Expression exp : loopBody) {
			exps.add(exp);
			exps.addAll(exp.getInnerExpressions());
		}
		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.add(condition);
		ret.addAll(loopBody);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.condition;
            default -> this.loopBody.get(index - 1);
        };
    }

    @Override
    public int getNumOfChildren() {
        return this.loopBody.size() + 1;
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		condition = replaceSingleExpression(condition, replacements);
		replaceExpressionList(loopBody, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((condition == null) ? 0 : condition.hashCode());
		result = prime * result
				+ ((loopBody == null) ? 0 : loopBody.hashCode());
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
		LoopExpression other = (LoopExpression) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (loopBody == null) {
			if (other.loopBody != null)
				return false;
		} else if (!loopBody.equals(other.loopBody))
			return false;
		return true;
	}

}
