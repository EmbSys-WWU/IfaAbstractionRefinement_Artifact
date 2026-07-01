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
 * Represents a 'delete ptr' call.
 * 
 * @author rschroeder
 */
public class DeleteExpression extends Expression {
	private static Logger logger = LogManager.getLogger(DeleteExpression.class
			.getName());

	private Expression varToDeleteExpr;
	private static final long serialVersionUID = -81727930449111393L;
	protected static final String DELETE = "delete";

	public DeleteExpression(Node n) {
		super(n);
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ll = new LinkedList<Expression>();
		ll.add(varToDeleteExpr);
		return ll;
	}

    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.varToDeleteExpr;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int getNumOfChildren() {
        return 1;
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		varToDeleteExpr = replaceSingleExpression(varToDeleteExpr, replacements);
	}

	@Override
	public String toString() {
		return DELETE + " " + varToDeleteExpr.toStringNoSem();
	}

	/**
	 * @return the varToDeleteExpr
	 */
	public Expression getVarToDeleteExpr() {
		return varToDeleteExpr;
	}

	/**
	 * @param varToDeleteExpr
	 *            the varToDeleteExpr to set
	 */
	public void setVarToDeleteExpr(Expression varToDeleteExpr) {
		varToDeleteExpr.setParent(this);
		this.varToDeleteExpr = varToDeleteExpr;
	}
}
