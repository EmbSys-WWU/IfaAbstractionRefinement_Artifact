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
import de.tub.pes.syscir.sc_model.SCPort;

public class MultiSocketAccessExpression extends Expression {

	private static final long serialVersionUID = 1882082603088570197L;
	private SCPort portSocket;
	private List<Expression> access;

	public MultiSocketAccessExpression(Node n, SCPort ps,
			List<Expression> a) {
		super(n);
		this.portSocket = ps;
		setAccess(a);
	}

	public String toString() {
		String ret = super.toString() + portSocket.getName();
		for (Expression e : access) {
			ret = ret + "[" + e.toString().replace(";", "") + "]";
		}

		return ret + ";";
	}

	public SCPort getPortSocket() {
		return portSocket;
	}

	public void setPortSocket(SCPort portSocket) {
		this.portSocket = portSocket;
	}

	public List<Expression> getAccess() {
		return access;
	}

	public void setAccess(List<Expression> access) {
		this.access = access;
		for (Expression exp : this.access) {
			exp.setParent(this);
		}
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();

		for (Expression exp : access) {
			exps.add(exp);
			exps.addAll(exp.getInnerExpressions());
		}

		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.addAll(access);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return access.get(index);
    }

    @Override
    public int getNumOfChildren() {
        return this.access.size();
    }

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		replaceExpressionList(access, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((access == null) ? 0 : access.hashCode());
		result = prime * result
				+ ((portSocket == null) ? 0 : portSocket.hashCode());
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
		MultiSocketAccessExpression other = (MultiSocketAccessExpression) obj;
		if (access == null) {
			if (other.access != null)
				return false;
		} else if (!access.equals(other.access))
			return false;
		if (portSocket == null) {
			if (other.portSocket != null)
				return false;
		} else if (!portSocket.equals(other.portSocket))
			return false;
		return true;
	}
}
