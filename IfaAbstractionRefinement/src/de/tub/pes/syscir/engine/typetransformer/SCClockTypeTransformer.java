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
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.TimeUnitExpression;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;
import de.tub.pes.syscir.sc_model.variables.SCTIMEUNIT;

public class SCClockTypeTransformer extends AbstractTypeTransformer {

	private static Logger logger = LogManager.getLogger(SCClockTypeTransformer.class
			.getName());

	public void createType(Environment e) {
		super.createType(e);
		this.name = "sc_clock";
	}

	@Override
	public SCKnownType createInstance(String instName, Environment e,
			boolean stat, boolean cons, List<String> other_mods) {
		SCClass type = e.getKnownTypes().get(name);
		SCKnownType kt = null;
		if (type != null) {
			kt = new SCKnownType(instName, type, e.getCurrentClass(), null,
					stat, cons, other_mods, e.getLastInitializer());
		} else {
			logger.error("Configuration error: type sc_clock not available");
		}
		return kt;
	}

	@Override
	public SCKnownType initiateInstance(String instName,
			List<Expression> params, Environment e, boolean stat, boolean cons,
			List<String> other_mods) {
		SCClass type = e.getKnownTypes().get(name);
		SCKnownType kt = null;
		if (type != null) {
			if (e.getKnownTypes().get(name).getConstructor().getParameters()
					.size() != params.size() - 1)
			// Constructor in implementation-file has only 2 params, because the
			// implementation
			// only handle SC_NS, so its default, but the
			// systemC-Constructor-call has 3 parameters

			{
				
				logger.error(
						"{} not the right number of parameters to initiate {}",
						this, instName);
				return null;
			} else {
				if(params.size() == 3 && params.get(1) instanceof ConstantExpression && params.get(2) instanceof TimeUnitExpression){
					int period = Integer.parseInt(((ConstantExpression)params.get(1)).getValue());
					TimeUnitExpression unit = (TimeUnitExpression)params.get(2);
					period = SCTIMEUNIT.convert(period,unit.getTimeUnit(),SCTIMEUNIT.valueOf("SC_NS"));
					unit.setTimeUnit(SCTIMEUNIT.valueOf("SC_NS"));
					ConstantExpression periodExpr = (ConstantExpression)params.get(1);
					periodExpr.setValue("" + period);
					params.set(2, unit);
					params.set(1, periodExpr);
					
				}
				kt = new SCKnownType(instName, type, e.getCurrentClass(),
						params, stat, cons, other_mods, e.getLastInitializer());
			}
		} else {
			logger.error("Configuration error: type sc_clock not available");
		}
		return kt;

	}

}
