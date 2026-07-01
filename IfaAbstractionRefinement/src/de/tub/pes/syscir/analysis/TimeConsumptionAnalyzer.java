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

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;

/**
 * Determines for every function in the system whether it consumes time and
 * which function a function calls. Time consumption is transitive: a function
 * consumes time if it either uses the wait() statement of SystemC or if it
 * calls a function that consumes time.
 * 
 * @author pockrandt
 * 
 */
public class TimeConsumptionAnalyzer implements Analyzer {

	private boolean completeAnalsis;
	
	public TimeConsumptionAnalyzer(){
		completeAnalsis = false;
	}
	
	/**
	 * 
	 * @param complete The analysis does not stop after time behaivour is defined
	 * but is going on and sets all functionCall informations. 
	 */
	public TimeConsumptionAnalyzer(boolean complete){
		completeAnalsis = complete;
	}
	
	public void analyze(SCSystem scs) {
		for (SCClass mod : scs.getClasses())
			for (SCFunction scfct : mod.getMemberFunctions()) {
				determineTimeConsumption(scfct);
			}

		for (SCFunction scfct : scs.getGlobalFunctions()) {
			determineTimeConsumption(scfct);
		}
	}

	/**
	 * Checks whether a given function consumes time. A function consumes time
	 * if it contains a wait-function OR if it calls a function that consumes
	 * time. module or over a port or socket
	 * 
	 * @param f
	 *            the function to check
	 * @return true if the given function consumes time
	 */
	public boolean determineTimeConsumption(SCFunction f) {

		if (f.getName().equals("wait")) {
			f.setConsumesTime(true);
			f.setTimingAnalyzed(true);
			return true;
		}

		if (f.getTimingAnalyzed())
			return f.getConsumesTime();
		else
			f.setTimingAnalyzed(true);

		if (f.getConsumesTime())
			return true;

		boolean ct = false;

		for (Expression expr : f.getAllExpressions()) {

			if (expr instanceof FunctionCallExpression) {
				FunctionCallExpression fc_expr = (FunctionCallExpression) expr;
				f.addFunctionCall(fc_expr);
				fc_expr.getFunction().setIsCalled(true);
				if(completeAnalsis == false)
					ct = ct || determineTimeConsumption(fc_expr.getFunction());
				else
					ct = ct | determineTimeConsumption(fc_expr.getFunction());
			}
		}

		// Might be useful for debugging
		// if (ct)
		// System.out.println("Function " + f.getName() + " consumes time.");
		// else
		// System.out.println("Function " + f.getName()
		// + " does not consume time.");

		f.setConsumesTime(ct);

		// it doesn't consume time if there are no function calls
		return ct;
	}

}
