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

package de.tub.pes.syscir.analysis;

import java.util.HashMap;
import java.util.Map;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;

/**
 * Checks for each function, whether and if so, how often, it was called.
 * @author rschroeder
 *
 */
public class FunctionCallAnalyzer implements Analyzer {
	private static Map<SCFunction, Integer> funcCalls = new HashMap<SCFunction, Integer>();

	/**
	 * analyze() deletes previous cache. Use only once.
	 */
	@Override
	public void analyze(SCSystem scs) {
		funcCalls = new HashMap<SCFunction, Integer>();
		for (SCClass mod : scs.getClasses()) {
			for (SCFunction func : mod.getMemberFunctions()) {
				analyzeFunction(func);
			}
			for (SCProcess proc: mod.getProcesses()) {
				SCFunction func = proc.getFunction();
				incFuncCall(func);
			}
		}
		for (SCFunction func : scs.getGlobalFunctions()) {
			analyzeFunction(func);
		}
	}

	private void analyzeFunction(SCFunction func) {
		for (Expression expr : func.getAllExpressions()) {
			analyzeExpr(expr);
		}
	}

	private void analyzeExpr(Expression expr) {
		if (expr == null) {
			return;
		}
		if (expr instanceof FunctionCallExpression) {
			FunctionCallExpression fce = (FunctionCallExpression) expr;
			incFuncCall(fce.getFunction());
		}
	}

	private void incFuncCall(SCFunction func) {
		int value = 1;
		if (funcGetsCalled(func)) {
			value = funcCalls.get(func) + 1;
		}
		funcCalls.put(func, value);
	}

	public int nbrFuncCalls(SCFunction func) {
		if (funcGetsCalled(func)) {
			return funcCalls.get(func);
		} else {
			return 0;
		}
	}

	public boolean funcGetsCalled(SCFunction func) {
		return (funcCalls.containsKey(func));
	}
}
