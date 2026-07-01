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
 * this expression represents the goto-construct. Currently, it contains only the jump label.
 * TODO mp: as an enhancement it would be nice to resolve the goto.
 * 
 * @author Bettina
 * 
 */
public class GoToExpression extends Expression {

	private static final long serialVersionUID = -1993807086866347443L;
	private String jumpLabel;

	public GoToExpression(Node n, String jumpLabel) {
		super(n);
		this.jumpLabel = jumpLabel;
	}

	@Override
	public String toString() {
		return (super.toString() + "goto " + this.jumpLabel + ";");
	}

	public String getJumpLabel() {
		return jumpLabel;
	}

	public void setJumpLabel(String jumpLabel) {
		this.jumpLabel = jumpLabel;
	}
	
	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		return ret;
	}
	
	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((jumpLabel == null) ? 0 : jumpLabel.hashCode());
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
		GoToExpression other = (GoToExpression) obj;
		if (jumpLabel == null) {
			if (other.jumpLabel != null)
				return false;
		} else if (!jumpLabel.equals(other.jumpLabel))
			return false;
		return true;
	}

}
