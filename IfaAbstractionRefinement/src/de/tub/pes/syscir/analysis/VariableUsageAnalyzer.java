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

import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCParameter;
import de.tub.pes.syscir.sc_model.SCREFERENCETYPE;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.DeleteExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.RefDerefExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.variables.SCArray;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCPointer;

/**
 * Determines for each variable if it is a pointer (pointers), a variable whose
 * reference is used (refvars) or a normal variable (vars).
 * 
 * @author rschroeder
 */
public class VariableUsageAnalyzer implements Analyzer {

	private static Logger logger = LogManager
			.getLogger(VariableUsageAnalyzer.class.getName());

	private final HashSet<SCVariable> pointers = new HashSet<SCVariable>();
	private final HashSet<SCVariable> vars = new HashSet<SCVariable>();
	private final HashSet<SCVariable> refvars = new HashSet<SCVariable>();

	@Override
	public void analyze(SCSystem scs) {
		for (SCVariable scvar : scs.getGlobalVariables()) {
			putDeclaredVariable(scvar);
		}
		for (SCFunction scfct : scs.getGlobalFunctions()) {
			putFunction(scfct);
		}
		for (SCClassInstance scci : scs.getInstances()) {
			if (scci.isSCModule() || scci.isSCKnownType() || scci.isChannel()) {
				SCClass scc = scci.getSCClass();
				for (SCVariable var : scc.getMembers()) {
					putDeclaredVariable(var);
				}
				for (SCFunction scfct : scc.getMemberFunctions()) {
					putFunction(scfct);
				}
			}
		}
		// make refvars and vars disjunct
		vars.removeAll(refvars);

		logger.debug("pointers: {} ", pointers);
		logger.debug("refvars: {}", refvars);
		logger.debug("vars: {}", vars);
	}

	private void toPointers(SCVariable var) {
		pointers.add(var);
	}

	private void toRefvars(SCVariable var) {
		refvars.add(var);
	}

	private void toVars(SCVariable var) {
		vars.add(var);
	}

	private void putDeclaredVariable(SCVariable var) {
		if (var instanceof SCPointer) {
			toPointers(var);
		} else if (var instanceof SCArray) {
			toRefvars(var);
		} else {
			toVars(var);
		}
	}

	private void putParameter(SCParameter p) {
		// TODO: is this correct?
		if (p.getRefType() == SCREFERENCETYPE.BYREFERENCE) {
			toRefvars(p.getVar());
		} else {
			putDeclaredVariable(p.getVar());
		}
	}

	private void putFunction(SCFunction scfct) {
		for (SCParameter param : scfct.getParameters()) {
			putParameter(param);
		}
		for (SCVariable var : scfct.getLocalVariables()) {
			putDeclaredVariable(var);
		}
		for (Expression expr : scfct.getAllExpressions()) {
			putExpression(expr);
		}
	}

	private void putExpression(Expression expr) {
		if (expr instanceof DeleteExpression) {
			putExpression(((DeleteExpression) expr).getVarToDeleteExpr());
			return;
		}
		
		if (expr instanceof RefDerefExpression
				&& ((RefDerefExpression) expr).isReferencing()) {
			for (Expression innerExpr : expr.crawlDeeper()) {
				if (innerExpr instanceof SCVariableExpression) {
					SCVariable var = ((SCVariableExpression) innerExpr)
							.getVar();
					toRefvars(var);
				} else {
					logger.warn(
							"{}: Encountered unexpected by-reference in expression {} for parameter {}.",
							NodeUtil.getFixedAttributes(innerExpr.getNode()),
							innerExpr);
				}
			}
		} else if (expr instanceof FunctionCallExpression) {
			FunctionCallExpression fce = (FunctionCallExpression) expr;
			SCFunction fun = fce.getFunction();
			for (int i = 0; i < fun.getParameters().size(); i++) {
				if (fun.getParameters().get(i).getRefType() == SCREFERENCETYPE.BYREFERENCE) {
					Expression param = fce.getParameters().get(i);
					if (param instanceof SCVariableExpression) {
						SCVariableExpression varExp = (SCVariableExpression) param;
						toRefvars(varExp.getVar());
					} else {
						logger.warn(
								"{}: Encountered unexpected by-reference parameter for functionCall {} for parameter {}.",
								NodeUtil.getFixedAttributes(fce.getNode()),
								fce, i);
					}
				}
			}
		}
	}

	/**
	 * @return the pointers
	 */
	public HashSet<SCVariable> getPointers() {
		return pointers;
	}

	/**
	 * @return the vars
	 */
	public HashSet<SCVariable> getVars() {
		return vars;
	}

	/**
	 * @return the refvars
	 */
	public HashSet<SCVariable> getRefvars() {
		return refvars;
	}

	public HashSet<SCVariable> getEverything() {
		HashSet<SCVariable> all = new HashSet<SCVariable>();
		all.addAll(getVars());
		all.addAll(getRefvars());
		all.addAll(getPointers());
		return all;
	}
}
