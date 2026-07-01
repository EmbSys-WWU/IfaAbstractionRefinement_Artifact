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
 * Represents the event notification expression. An event notification consists
 * of a variableExpression containing the event and either 0 (immediate
 * notification) one or two parameters (delta delay or timed notification).
 * 
 * @author pockrandt
 * 
 */
public class EventNotificationExpression extends Expression {

	private static final long serialVersionUID = 1L;

	/**
	 * The event which is notified.
	 */
	private Expression event;

	/**
	 * The parameters for the event notification. This list may be empty if the
	 * notification is an immediate notification or contain an SC_ZERO_DELAY
	 * timing expression (delta delay notification), an sc_time variable or two
	 * parameters, one of them a timingExpression (timed notification).
	 */
	private List<Expression> parameters;

	public EventNotificationExpression(Node node, SCVariableExpression event,
			List<Expression> params) {
		super(node);
		this.event = event;
		this.event.setParent(this);
		setParameters(params);
	}

	@Override
	public String toString() {
		String ret = super.toString();
		ret += event.toString().replace(";", "") + ".notify(";
		// parameters can only have 3 different (valid) sizes
		if (parameters.size() == 1) {
			ret += parameters.get(0).toString();
		} else if (parameters.size() == 2) {
			ret += parameters.get(0).toString() + ", " + parameters.get(1);
		}

		ret += ");";
		return ret;
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		LinkedList<Expression> ret = new LinkedList<Expression>();
		ret.add(event);
		ret.addAll(parameters);
		return ret;
	}

    @Override
    public Expression getChild(int index) {
        return switch (index) {
            case 0 -> this.event;
            default -> this.parameters.get(index - 1);
        };
    }

    @Override
    public int getNumOfChildren() {
        return this.parameters.size() + 1; 
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
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		event = replaceSingleExpression(event, replacements);
		replaceExpressionList(parameters, replacements);
	}

	public Expression getEvent() {
		return event;
	}

	public void setEvent(SCVariableExpression event) {
		this.event = event;
		this.event.setParent(this);
	}

	public List<Expression> getParameters() {
		return parameters;
	}

	public void setParameters(List<Expression> parameters) {
		this.parameters = parameters;
		for (Expression exp : this.parameters) {
			exp.setParent(this);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((event == null) ? 0 : event.hashCode());
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
		EventNotificationExpression other = (EventNotificationExpression) obj;
		if (event == null) {
			if (other.event != null)
				return false;
		} else if (!event.equals(other.event))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}

}
