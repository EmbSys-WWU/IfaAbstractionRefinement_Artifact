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
 * this expression represents a switch-case-construct. It contains an expression
 * for the initial switch-statement and one condition and one body per case
 * statement.
 * 
 * @author Florian
 * 
 */

public class SwitchExpression extends Expression {

	private static transient final Logger logger = LogManager
			.getLogger(SwitchExpression.class.getName());

	private static final long serialVersionUID = -3201626133965710498L;
	private Expression switchExpression;

	/**
	 * Contains the bodies of all cases, ordered by appearance.
	 */
	private List<Expression> cases;

	public SwitchExpression(Node n, Expression switchExp) {
		super(n);
		setSwitchExpression(switchExp);
		this.cases = new LinkedList<Expression>();
	}

	/**
	 * Adds a case, consisting of condition and a body to the end of the switch
	 * expression.
	 * 
	 * @param condition
	 *            - condition for the switch
	 * @param body
	 *            - body of the case
	 */
	public void addCase(CaseExpression ce) {
		ce.setParent(this);
		cases.add(ce);
	}

	public List<Expression> getCases() {
		return this.cases;
	}

	public void setCases(List<Expression> ces) {
		this.cases = ces;
		for (Expression exp : cases) {
			exp.setParent(this);
		}
	}

	@Override
	public String toString() {
		String ret = super.toString();
		ret = "switch(" + switchExpression.toString().replace(";", "")
				+ ") {\n";

		for (Expression ce : cases) {
			ret += ce.toString();
		}
		ret = ret + "}\n";

		return ret;
	}

	public Expression getSwitchExpression() {
		return switchExpression;
	}

	public void setSwitchExpression(Expression switchExpression) {
		this.switchExpression = switchExpression;
        this.switchExpression.setParent(this);
	}

	@Override
	public List<Expression> getInnerExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		exps.add(switchExpression);
		exps.addAll(switchExpression.getInnerExpressions());

		for (Expression ce : cases) {
			if (ce instanceof CaseExpression) {
				CaseExpression cexp = (CaseExpression) ce;
				if (!cexp.isDefaultCase()) {
					exps.add(cexp.getCondition());
				}

				for (Expression exp : cexp.getBody()) {
					exps.add(exp);
					exps.addAll(exp.getInnerExpressions());
				}
			} else {
				logger.error("Encounterd an expression in cases which is not a CaseExpression but {}.", ce.getClass().getName());
			}
		}
		return exps;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.add(switchExpression);
		ret.addAll(cases);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.switchExpression;
            default -> this.cases.get(index - 1);
        };
    }

    @Override
    public int getNumOfChildren() {
        return this.cases.size() + 1;
    }

	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		switchExpression = replaceSingleExpression(switchExpression, replacements);
		replaceExpressionList(cases, replacements);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cases == null) ? 0 : cases.hashCode());
		result = prime
				* result
				+ ((switchExpression == null) ? 0 : switchExpression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof SwitchExpression)) {
			return false;
		}
		SwitchExpression other = (SwitchExpression) obj;
		if (cases == null) {
			if (other.cases != null) {
				return false;
			}
		} else if (!cases.equals(other.cases)) {
			return false;
		}
		if (switchExpression == null) {
			if (other.switchExpression != null) {
				return false;
			}
		} else if (!switchExpression.equals(other.switchExpression)) {
			return false;
		}
		return true;
	}

}
