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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.util.Pair;

/**
 * this Expression represents a Variable-Declaration inside of a function. it
 * contains a Expression which refer to the Variable and an expression which
 * represent the initial value
 * 
 * @author Florian
 * 
 */
public class SCVariableDeclarationExpression extends Expression {

	private static final long serialVersionUID = -8499943635883952860L;
	private static final transient Logger logger = LogManager
			.getLogger(SCVariableDeclarationExpression.class.getName());

	private Expression variable;
	private List<Expression> initialValues;

	public SCVariableDeclarationExpression(Node n, Expression v,
			List<Expression> ini) {
		this(n, v);
		setInitialValues(ini);
	}

	public SCVariableDeclarationExpression(Node n, Expression v,
			Expression firstInit) {
		this(n, v);
		setFirstInitialValue(firstInit);

	}

	public SCVariableDeclarationExpression(Node n, Expression v) {
		super(n);
		initialValues = new LinkedList<Expression>();
		setVariable(v);
		initialValues = new LinkedList<Expression>();
	}

	public Expression getVariable() {
		return this.variable;
	}

	public void setVariable(Expression variable) {
		this.variable = variable;
		if (variable != null) {
			this.variable.setParent(this);
		}
		if (variable instanceof SCVariableExpression) {
			((SCVariableExpression) variable).getVar().setDeclaration(this);
		} else if (variable instanceof SCClassInstanceExpression) {
			((SCClassInstanceExpression) variable).getInstance()
					.setDeclaration(this);
		}
	}

	public Expression getFirstInitialValue() {
		if (initialValues == null) {
			logger.error("initialValues is null, should be empty list");
			return null;
		} else if (!initialValues.isEmpty()) {
			return this.initialValues.get(0);
		} else {
			return null;
		}
	}

	public void setFirstInitialValue(Expression val) {
		if (val != null) {
			val.setParent(this);
			initialValues.add(val);
		}
	}

	public List<Expression> getInitialValues() {
		// defensive copying
		return new ArrayList<Expression>(this.initialValues);
	}

	public void setInitialValues(List<Expression> initialValues) {
		if (initialValues == null) {
			logger.warn("trying to set null as initialValues");
			return;
		}
		this.initialValues = initialValues;
		for (Expression exp : this.initialValues) {
			exp.setParent(this);
		}
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer(super.toString().replace(":", ":;"));
		if (variable instanceof SCVariableExpression) {
			SCVariableExpression v = (SCVariableExpression) variable;
			ret.append(v.getVar().getDeclarationString().replace(";", ""));
			ret.append(v.getVar().getInitializationString());
		} else if (variable instanceof SCClassInstanceExpression) {
			SCClassInstanceExpression m = (SCClassInstanceExpression) variable;
			ret.append(m.getInstance().getDeclarationString().replace(";", ""));
			ret.append(m.getInstance().getInitializationString());
		}

		ret.append(";");
		return ret.toString();

	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		exps.add(variable);
		exps.addAll(variable.getInnerExpressions());
		exps.addAll(initialValues);
		for (Expression exp : initialValues) {
			exps.addAll(exp.getInnerExpressions());
		}

		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.add(variable);
		ret.addAll(initialValues);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.variable;
            default -> this.initialValues.get(index - 1);
        };
    }

    @Override
    public int getNumOfChildren() {
        return this.initialValues.size() + 1;
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		variable = replaceSingleExpression(variable, replacements);
		replaceExpressionList(initialValues, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((initialValues == null) ? 0 : initialValues.hashCode());
		result = prime * result
				+ ((variable == null) ? 0 : variable.hashCode());
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
		SCVariableDeclarationExpression other = (SCVariableDeclarationExpression) obj;
		if (initialValues == null) {
			if (other.initialValues != null)
				return false;
		} else if (!initialValues.equals(other.initialValues))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.toString().equals(other.variable.toString()))
			return false;
		return true;
	}

}
