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

package de.tub.pes.syscir.sc_model.variables;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.expressions.Expression;

/**
 * this class represents a KnownType
 * 
 * @author Florian
 * 
 */
public class SCKnownType extends SCClassInstance {

	private static final long serialVersionUID = -1550114392150953966L;

	private static transient Logger logger = LogManager
			.getLogger(SCKnownType.class.getName());

	public SCKnownType(String name, SCClass type, SCClass outerClass, List<Expression> para,
			boolean stat, boolean cons, List<String> other_mods, Expression init) {
		super(name, type, outerClass);
		initialize(para);
		this._static = stat;
		this._const = cons;
		this.otherModifiers = other_mods;
	}

	public void initialize(List<Expression> params) {
//		if (!this.initialized) {
//			if (!params.isEmpty()) {
//				this.initialExpressions = params;
//				this.initialized = true;
//			}
//		} else {
//			logger.warn("SCKnownType.initialize: Variable is already initialized");
//		}
	}

}
