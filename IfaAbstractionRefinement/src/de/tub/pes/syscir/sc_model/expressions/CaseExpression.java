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
 * Represents a single case in a switch case statement.
 * 
 * @author pockrandt
 * 
 */
public class CaseExpression extends Expression {

	private static final long serialVersionUID = 7916149870446969571L;

	private boolean isDefaultCase;
	private Expression condition;
	private List<Expression> body;

	public CaseExpression(Node n, Expression condition, List<Expression> body) {
		super(n);
		this.isDefaultCase = false;
		setCondition(condition);
		setBody(body);
	}

	/**
	 * Constructor for a default case expression.
	 * 
	 * @param n
	 * @param body
	 */
	public CaseExpression(Node n, List<Expression> body) {
		this(n, null, body);
	}

	@Override
	public String toString() {
		String ret = "";
		if (isDefaultCase) {
			ret += "default: \n";
		} else {
			ret += "case " + condition + ": \n";
		}
		for (Expression exp : body) {
			ret += exp + "\n";
		}
		return ret;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		if (!isDefaultCase) {
			ret.add(condition);
		}
		ret.addAll(body);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        if (index == 0 && !this.isDefaultCase) {
            return this.condition;
        }
        index = this.isDefaultCase? index : index - 1;
        return this.body.get(index);
    }

    @Override
    public int getNumOfChildren() {
        return this.body.size() + (this.isDefaultCase? 0 : 1);
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {

		if (!isDefaultCase) {
			condition = replaceSingleExpression(condition, replacements);
		}

		replaceExpressionList(body, replacements);

	}

	public boolean isDefaultCase() {
		return isDefaultCase;
	}

	public Expression getCondition() {
		return condition;
	}

	public void setCondition(Expression condition) {
		this.condition = condition;
		if (condition != null) {
			this.condition.setParent(this);
			this.isDefaultCase = false;
		} else {
			this.isDefaultCase = true;
		}
	}

	public List<Expression> getBody() {
		return body;
	}

	public void setBody(List<Expression> body) {
		this.body = body;
		for (Expression exp : body) {
			exp.setParent(this);
		}
	}

}
