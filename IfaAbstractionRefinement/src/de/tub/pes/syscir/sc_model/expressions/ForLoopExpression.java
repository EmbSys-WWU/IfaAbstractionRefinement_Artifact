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
 * this expression represents a For-Loop it contains 3 Expressions which
 * represent the initial-construct, the final-construct and the
 * iterator-construct it also contains a List of Expressions, which represent
 * the Body
 * 
 * @author Florian
 * 
 */

public class ForLoopExpression extends LoopExpression {

	private static final long serialVersionUID = -5415940107730537267L;
	private Expression initializer;
	private Expression iterator;

	public ForLoopExpression(Node n, String l, Expression initializer,
			Expression condition, Expression iterator, List<Expression> body) {
		super(n, l, condition, body);
		setInitializer(initializer);
		setIterator(iterator);
	}

	public ForLoopExpression(Node n, String l, Expression initializer,
			Expression condition, Expression iterator, List<Expression> body, 
			int maxCount) {
		super(n, l, condition, body, maxCount);
		setInitializer(initializer);
		setIterator(iterator);
	}

	@Override
	public String toString() {

		String ret = super.toString() + "for("
				+ initializer.toString().replace(";", "") + "; "
				+ getCondition().toString().replace(";", "") + "; "
				+ iterator.toString().replace(";", "") + "){";
		for (Expression e : getLoopBody()) {
			// System.out.println(e);
			ret = ret + "\n\t" + e.toString()/* .replace(";", "") + ";" */;
		}
		return ret + "\n}";
	}

	public Expression getInitializer() {
		return initializer;
	}

	public void setInitializer(Expression initializer) {
		this.initializer = initializer;
		this.initializer.setParent(this);
	}

	public Expression getIterator() {
		return iterator;
	}

	public void setIterator(Expression iterator) {
		this.iterator = iterator;
		this.iterator.setParent(this);
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		exps.add(initializer);
		exps.addAll(initializer.getInnerExpressions());
		exps.add(iterator);
		exps.addAll(iterator.getInnerExpressions());

		exps.addAll(super.getInnerExpressions());

		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = super.crawlDeeper();
		ret.add(initializer);
		ret.add(iterator);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        if (index == 0) {
            return getCondition();
        }
        index--;
        int bodyLength = getLoopBody().size();
        if (index < bodyLength) {
            return getLoopBody().get(index);
        }
        index -= bodyLength;
        return switch (index) {
            case 0 -> this.initializer;
            case 1 -> this.iterator;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int getNumOfChildren() {
        return super.getNumOfChildren() + 2;
    }

	@Override
	public List<Expression> getHeader() {
		List<Expression> ret = super.getHeader();
		ret.add(0, initializer); // add at the begin
		ret.add(iterator);
		return ret;
	}

	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		super.replaceInnerExpressions(replacements);
		initializer = replaceSingleExpression(initializer, replacements);
		iterator = replaceSingleExpression(iterator, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((initializer == null) ? 0 : initializer.hashCode());
		result = prime * result
				+ ((iterator == null) ? 0 : iterator.hashCode());
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
		ForLoopExpression other = (ForLoopExpression) obj;
		if (initializer == null) {
			if (other.initializer != null)
				return false;
		} else if (!initializer.equals(other.initializer))
			return false;
		if (iterator == null) {
			if (other.iterator != null)
				return false;
		} else if (!iterator.equals(other.iterator))
			return false;
		return true;
	}
}
