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
 * Represents a 'new $type' or 'new $type(init)' expression.
 * @author rschroeder
 *
 */
public class NewExpression extends Expression {
	private static Logger logger = LogManager.getLogger(NewExpression.class.getName());

	public NewExpression(Node n) {
		super(n);
		arguments = new ArrayList<Expression>(0);
	}

	private static final long serialVersionUID = -1605241181529469122L;
	private String objType;
	private List<Expression> arguments;

	@Override
	public LinkedList<Expression> crawlDeeper() {
		return new LinkedList<Expression>(arguments);
	}

    @Override
    public Expression getChild(int index) {
        return this.arguments.get(index);
    }

    @Override
    public int getNumOfChildren() {
        return this.arguments.size();
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		replaceExpressionList(arguments, replacements);
	}

	@Override
	public String toString() {
		StringBuffer out = new StringBuffer("new " + objType);
		if (arguments.size() > 0) {
			String sep = ", ";
			out.append("(");
			for (Expression e : arguments) {
				out.append(e.toString().replace(";", "") + sep);
			}
			out.setLength(out.length() - sep.length());
			out.append(")");
		}
		//out.append(";"); // right?
		return out.toString();
	}

	/**
	 * @return the objType
	 */
	public String getObjType() {
		return objType;
	}

	/**
	 * @param objType the objType to set
	 */
	public void setObjType(String objType) {
		this.objType = objType;
	}

	/**
	 * @return the arguments
	 */
	public List<Expression> getArguments() {
		return arguments;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(List<Expression> arguments) {
		for(Expression arg : arguments) {
			arg.setParent(this);
		}
		this.arguments = arguments;
	}

}
