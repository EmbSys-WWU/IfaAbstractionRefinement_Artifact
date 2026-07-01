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

package de.tub.pes.syscir.engine.modeltransformer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;

/**
 * Convert 'int a = foo();' to 'int a; a = foo();'
 *
 * @author rschroeder
 */
public class VarDeclWithFuncCallTransformer implements ModelTransformer {

	@Override
	public SCSystem transformModel(SCSystem model) {
		List<SCFunction> globfunc = model.getGlobalFunctions();
		List<SCClass> classes = model.getClasses();

		for (SCFunction func : globfunc) {
			modifyBody(func.getBody());
		}

		for (SCClass mod : classes) {
			List<SCFunction> memFunc = mod.getMemberFunctions();
			List<SCProcess> processes = mod.getProcesses();

			for (SCFunction func : memFunc) {
				modifyBody(func.getBody());
			}
			for (SCProcess proc : processes) {
				SCFunction func = proc.getFunction();
				modifyBody(func.getBody());
			}

		}

		return model;
	}

	private void modifyBody(List<Expression> body) {
		HashMap<Integer, Expression> inserts = new HashMap<Integer, Expression>();
		for (int i = 0; i < body.size(); i++) {
			Expression expr = body.get(i);
			if (expr instanceof SCVariableDeclarationExpression) {
				SCVariableDeclarationExpression vdexpr = (SCVariableDeclarationExpression) expr;
				if (vdexpr.getFirstInitialValue() instanceof FunctionCallExpression) {
					Expression funccall = new BinaryExpression(null, vdexpr.getVariable(), "=", vdexpr.getFirstInitialValue());
					inserts.put(i + 1, funccall);
					vdexpr.setInitialValues(new LinkedList<Expression>());
				}
			}
		}
		for (Map.Entry<Integer, Expression> entry : inserts.entrySet()) {
		    body.add(entry.getKey(), entry.getValue());
		}
	}
}
