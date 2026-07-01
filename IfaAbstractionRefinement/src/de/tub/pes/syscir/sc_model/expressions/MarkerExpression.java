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
 * This abstract class is used as a base class for all marker expressions. A
 * marker expression is an expression which contains no information but the
 * expression itself. An example for a markerExpression is the "break"-statement
 * in loops or switch-case-blocks.
 * 
 * @author pockrandt
 * 
 */
public abstract class MarkerExpression extends Expression {

	private static final long serialVersionUID = -6603730384359041400L;

	public MarkerExpression(Node n, String label) {
		super(n, label);
	}

	public MarkerExpression(Node n) {
		super(n);
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		return new LinkedList<Expression>();
	}

    @Override
    public Expression getChild(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int getNumOfChildren() {
        return 0;
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
	}
}
