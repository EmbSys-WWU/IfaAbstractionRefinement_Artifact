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

package de.tub.pes.syscir.engine.typetransformer;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;

/**
 * 
 * @author Timm Liebrenz
 *
 */
public class EnumTypeTransformer extends AbstractTypeTransformer {

	private static Logger logger = LogManager
			.getLogger(EnumTypeTransformer.class.getName());

	public EnumTypeTransformer(String enumName) {
		super();
		this.name = enumName;
	}

	@Override
	public SCKnownType createInstance(String instName, Environment e,
			boolean stat, boolean cons, List<String> other_mods) {
		// return super.createInstance(instName, e, stat, cons, other_mods);
		SCClass type = e.getKnownTypes().get(name);
		SCKnownType kt = null;
		if (type != null) {
			kt = new SCKnownType(instName, type, e.getCurrentClass(), null,
					stat, cons, other_mods, e.getLastInitializer());
		} else {
			logger.error("Configuration error: type {} is not available", name);
		}
		return kt;
	}
}
