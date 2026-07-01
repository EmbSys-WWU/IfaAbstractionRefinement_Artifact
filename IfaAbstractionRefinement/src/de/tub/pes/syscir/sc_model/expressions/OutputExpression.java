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
 * This class represents the C++ output cout. All elements of a statement
 * "cout << x << y << z;" are hold in the expressions-List.
 * 
 * @author pockrandt
 * 
 */
public class OutputExpression extends Expression {

	private static final long serialVersionUID = -1804029659922367022L;
	private LinkedList<Expression> expressions;

	public OutputExpression(Node n, String label,
			LinkedList<Expression> expressions) {
		super(n, label);
		setExpressions(expressions);
	}

	@Override
	public String toString() {
		String out = "cout";
		for (Expression exp : expressions) {
			out += " << " + exp.toString().replaceAll(";", "");
		}
		out += ";";
		return out;
	}

	public LinkedList<Expression> getExpressions() {
		return expressions;
	}

	public void setExpressions(LinkedList<Expression> expressions) {
		this.expressions = expressions;
		for (Expression exp : this.expressions) {
			exp.setParent(this);
		}
	}

	/**
	 * Adds the expression exp to the end of the expressionlist.
	 * 
	 * @param exp
	 *            expression to add.
	 */
	public void addExpression(Expression exp) {
		expressions.add(exp);
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();

		for (Expression exp : expressions) {
			exps.add(exp);
			exps.addAll(exp.getInnerExpressions());
		}
		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.addAll(expressions);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return this.expressions.get(index);
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		replaceExpressionList(expressions, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((expressions == null) ? 0 : expressions.hashCode());
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
		OutputExpression other = (OutputExpression) obj;
		if (expressions == null) {
			if (other.expressions != null)
				return false;
		} else if (!expressions.equals(other.expressions))
			return false;
		return true;
	}

}
