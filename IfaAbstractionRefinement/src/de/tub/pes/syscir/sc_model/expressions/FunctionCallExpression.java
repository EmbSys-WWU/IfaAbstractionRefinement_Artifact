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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.util.Pair;
import de.tub.pes.syscir.sc_model.SCFunction;

/**
 * this expression represent a FunctionCall it contains a reference to the
 * function and a list of Expressions which represent the Call-Parameter
 * 
 * @author Florian
 * 
 */
public class FunctionCallExpression extends Expression {

	private static final long serialVersionUID = -6937911626522812L;
	private SCFunction function;
	private List<Expression> parameters;

	public FunctionCallExpression(Node n, SCFunction function,
			List<Expression> params) {
		super(n);
		//this.function = function;
		setFunction(function);
		setParameters(params);
		
	}

	public void setFunction(SCFunction function) {
		this.function = function;
	}

	public SCFunction getFunction() {
		return function;
	}

	public void setParameters(List<Expression> params) {
		if (params == null) {
			params = new LinkedList<Expression>();
		}
		this.parameters = params;
		for (Expression exp : this.parameters) {
			exp.setParent(this);
		}

	}

	public void addParameters(List<Expression> params) {
		for (Expression exp : params) {
			addSingleParameter(exp);
		}
	}

	public void addSingleParameter(Expression param) {
		param.setParent(this);
		this.parameters.add(param);
	}

	public List<Expression> getParameters() {
		return this.parameters;
	}

	@Override
	public String toString() {
		String ret = super.toString() + function.getName() + "(";
		if (parameters != null) {
			for (Expression e : parameters) {
				ret = ret + e.toString().replace(";", "") + ", ";
			}
			if (parameters.size() > 0) {
				ret = ret.substring(0, ret.length() - 2);
			}
		}
		ret = ret + ");";
		return ret;
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		for (Expression exp : parameters) {
			exps.add(exp);
			exps.addAll(exp.getInnerExpressions());
		}
		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.addAll(parameters);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return this.parameters.get(index);
    }

    @Override
    public int getNumOfChildren() {
        return this.parameters.size();
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		replaceExpressionList(parameters, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((function == null) ? 0 : function.toString().hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
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
		FunctionCallExpression other = (FunctionCallExpression) obj;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.toString().equals(other.function.toString()))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}
}
