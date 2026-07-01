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
import de.tub.pes.syscir.sc_model.SCFunction;

/**
 * This Expression is a container for a sequence of expressions.
 * 
 * @author rschroeder
 */
public class ExpressionBlock extends Expression {

	private static final long serialVersionUID = -3875422736888189260L;

	protected List<Expression> block;
	
	/**
	 * Reference to the function that owns this expression block.
	 * May be null if this block is not part of a function.
	 */
	protected transient SCFunction owningFunction;

	public ExpressionBlock(Node n, List<Expression> block) {
		super(n);
		setBlock(block);
	}

	public ExpressionBlock(Node n) {
		super(n);
		setBlock(null);
	}

	public List<Expression> getBlock() {
		return block;
	}

	public void setBlock(List<Expression> blk) {
		if (blk == null) {
			block = new LinkedList<Expression>();
		} else {
			block = blk;
		}
	}

	public boolean addExpression(Expression exp) {
		if (exp == null) {
			return false;
		}
		if (block == null) {
			setBlock(null);
		}
		block.add(exp);
		exp.setParent(this);
		return true;
	}

	public boolean addAll(List<Expression> ls) {
		for (Expression expr : ls) {
			if (!addExpression(expr)) {
				return false;
			}
		}
		return true;
	}

	public void emptyBlock() {
		setBlock(null);
	}

	/**
	 * Sets the function that owns this expression block.
	 * 
	 * @param function the owning SCFunction, or null
	 */
	public void setOwningFunction(SCFunction function) {
		this.owningFunction = function;
	}

	/**
	 * Gets the function that owns this expression block.
	 * 
	 * @return the owning SCFunction, or null if not set
	 */
	public SCFunction getOwningFunction() {
		return this.owningFunction;
	}

	@Override
	public SCFunction getContainingFunction() {
		if (this.owningFunction != null) {
			return this.owningFunction;
		}
		return super.getContainingFunction();
	}

	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		if (block != null) {
			for (Expression exp : block) {
				if (exp != null) {
					out.append(exp.toString()).append("\n");
				}
			}
			out.deleteCharAt(out.length() - 1);
		}
		return out.toString();
	}

	@Override
	public List<Expression> getInnerExpressions() {
		LinkedList<Expression> ls = new LinkedList<Expression>();
		for (Expression expr : block) {
			ls.add(expr);
			ls.addAll(expr.getInnerExpressions());
		}
		return ls;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ls = new LinkedList<Expression>();
		ls.addAll(block);
		return ls;
	}

    @Override
    public Expression getChild(int index) {
        return this.block.get(index);
    }

    @Override
    public int getNumOfChildren() {
        return this.block.size();
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		replaceExpressionList(block, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * block.hashCode();
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
		ExpressionBlock other = (ExpressionBlock) obj;
		if (block == null) {
			if (other.block != null)
				return false;
		} else if (!block.equals(other.block))
			return false;
		return true;
	}
}
