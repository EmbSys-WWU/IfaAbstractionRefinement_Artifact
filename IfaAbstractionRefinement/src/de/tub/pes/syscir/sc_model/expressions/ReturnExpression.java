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
 * this expression represents the return-construct it contains an expression
 * where the returned Expression is saved.
 * 
 * @author Florian
 * 
 */
public class ReturnExpression extends Expression {

	private static transient final Logger logger = LogManager
			.getLogger(ReturnExpression.class.getName());

	private static final long serialVersionUID = -8348637675858553260L;
	private Expression returnStatement;

	public ReturnExpression(Node n, Expression returnStatement) {
		super(n);
		setReturnStatement(returnStatement);
	}

	@Override
	public String toString() {
		if (returnStatement != null) {
			return super.toString() + "return "
					+ returnStatement.toString().replace(";", "") + ";";
		} else {
			return super.toString() + "return;";
		}
	}

	public Expression getReturnStatement() {
		return returnStatement;
	}

	public void setReturnStatement(Expression returnStatement) {
		this.returnStatement = returnStatement;
		if (returnStatement != null) {
			this.returnStatement.setParent(this);
		}
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		if (returnStatement != null) {
			exps.add(returnStatement);
			exps.addAll(returnStatement.getInnerExpressions());
		}
		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		if (returnStatement != null)
			ret.add(returnStatement);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        if (index != 0 || this.returnStatement == null) {
            throw new IndexOutOfBoundsException(index);
        }
        return this.returnStatement;
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		if (!replacements.isEmpty() && returnStatement != null)
			returnStatement = replaceSingleExpression(returnStatement,
					replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((returnStatement == null) ? 0 : returnStatement.hashCode());
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
		ReturnExpression other = (ReturnExpression) obj;
		if (returnStatement == null) {
			if (other.returnStatement != null)
				return false;
		} else if (!returnStatement.equals(other.returnStatement))
			return false;
		return true;
	}

}
