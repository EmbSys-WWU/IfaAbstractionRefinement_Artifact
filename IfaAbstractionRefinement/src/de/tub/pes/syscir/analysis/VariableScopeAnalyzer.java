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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.IfElseExpression;
import de.tub.pes.syscir.sc_model.expressions.LoopExpression;
import de.tub.pes.syscir.sc_model.expressions.SCClassInstanceExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;

/**
 * Analyze the scope of variables (only inside functions).
 * ExclInnerScope only takes vars from the current scope that weren't defined
 * in inner scopes. You get those as well when using InclInnerScopes.
 * The loop headers are part of the outer scope (e.g. the loop var in for loop
 * is accessible outside the loop).
 * @author rschroeder
 *
 */
public class VariableScopeAnalyzer implements Analyzer {
	private static Logger logger = LogManager.getLogger(VariableScopeAnalyzer.class.getName());

	private static final Map<List<Expression>, List<SCVariable>> cacheInclInnerScopes = new HashMap<List<Expression>, List<SCVariable>>();
	private static final Map<List<Expression>, List<SCVariable>> cacheExclInnerScopes = new HashMap<List<Expression>, List<SCVariable>>();

	public VariableScopeAnalyzer() {
		super();
	}

	/**
	 * analyze() only fills up the cache ...
	 */
	@Override
	public void analyze(SCSystem scs) {
		for (SCClass mod : scs.getClasses()) {
			for (SCFunction scfct : mod.getMemberFunctions()) {
				fillCache(scfct.getBody());
			}
		}
		for (SCFunction scfct : scs.getGlobalFunctions()) {
			fillCache(scfct.getBody());
		}
	}

	private void fillCache(List<Expression> body) {
		getVarsInclInnerScopes(body);
		getVarsExclInnerScopes(body);
		for (Expression e : body) {
			if (e instanceof LoopExpression) {
				fillCache(((LoopExpression) e).getLoopBody());
			} else if (e instanceof IfElseExpression) {
				fillCache(((IfElseExpression) e).getThenBlock());
				fillCache(((IfElseExpression) e).getElseBlock());
			}
		}
	}

	/**
	 * Return all variables in the current scope. This includes variables
	 * inside inner scopes.
	 * @return
	 */
	public List<SCVariable> getVarsInclInnerScopes(List<Expression> body) {
		return getVars(body, true);
	}
	/**
	 * Return only variables in this scope which do not belong to inner
	 * scopes.
	 * @return
	 */
	public List<SCVariable> getVarsExclInnerScopes(List<Expression> body) {
		return getVars(body, false);
	}

	private List<SCVariable> getVars(List<Expression> body, boolean inclInnerScopes) {
		if (body == null || body.size() == 0) {
			return null;
		}
		Map<List<Expression>, List<SCVariable>> cache = cacheInclInnerScopes;
		if (!inclInnerScopes) {
			cache = cacheExclInnerScopes;
		}
		if (cache.containsKey(body)) {
				return cache.get(body);
		} else {
			List<SCVariable> ls = new LinkedList<SCVariable>();
			for (Expression e : body) {
				if (e instanceof SCVariableDeclarationExpression) {
					Expression tmp = ((SCVariableDeclarationExpression) e).getVariable();
					SCVariable scvar = null;
					try {
						scvar = ((SCVariableExpression) tmp).getVar();
					} catch (ClassCastException cce) {
						scvar = ((SCClassInstanceExpression) tmp).getInstance();
					}
					ls.add(scvar);
				} else if (e instanceof LoopExpression) {
					// the header of a loop actually belongs to the outer scope
					LoopExpression le = (LoopExpression) e;
					ls.addAll(getVars(le.getHeader(), inclInnerScopes));
				}
				if (inclInnerScopes) {
					// create shallow copy here to not change original lists
					List<Expression> innerLs = new LinkedList<Expression>();
					if (e instanceof LoopExpression) {
						innerLs.addAll(((LoopExpression) e).getLoopBody());
					} else if (e instanceof IfElseExpression) {
						innerLs.addAll(((IfElseExpression) e).getThenBlock());
						innerLs.addAll(((IfElseExpression) e).getElseBlock());
					}
					if (innerLs.size() > 0) {
						ls.addAll(getVars(innerLs, inclInnerScopes));
					}
				}
			}
			cache.put(body, ls);
			return ls;
		}
	}

	/**
	 * Get a variable from this scope. It may be from an inner scope.
	 * @return
	 */
	public SCVariable getVarInclInnerScopes(List<Expression> body, String name) {
		return getVar(body, name, true);
	}
	/**
	 * Get a variable from this scope. Don't get a variable from an inner
	 * scrope.
	 * @return
	 */
	public SCVariable getVarExclInnerScopes(List<Expression> body, String name) {
		return getVar(body, name, false);
	}

	private SCVariable getVar(List<Expression> body, String name, boolean inclInnerScopes) {
		List<SCVariable> ls = getVars(body, inclInnerScopes);
		for (SCVariable scvar : ls) {
			if (scvar.getName().equals(name)) {
				return scvar;
			}
		}
		return null;
	}

	/**
	 * Check wheter a variable exists in this scope. This includes inner
	 * scopes.
	 * @param scvar
	 * @return
	 */
	public boolean varExistsInclInnerScopes(List<Expression> body, SCVariable scvar) {
		return varExistsInclInnerScopes(body, scvar.getName());
	}

	public boolean varExistsInclInnerScopes(List<Expression> body, String name) {
		return varExists(body, name, true);
	}

	/**
	 * Check wheter a variable exists in this scope. Only return true for
	 * variables which aren't in an inner scope.
	 * @param scvar
	 * @return
	 */
	public boolean varExistsExclInnerScopes(List<Expression> body, SCVariable scvar) {
		return varExistsExclInnerScopes(body, scvar.getName());
	}
	public boolean varExistsExclInnerScopes(List<Expression> body, String name) {
		return varExists(body, name, false);
	}

	private boolean varExists(List<Expression> body, String name, boolean inclInnerScopes) {
		return (getVar(body, name, inclInnerScopes) != null);
	}

	// convenience
	public boolean varExistsExclInnerScopes(SCFunction func, SCVariable scvar) {
		return varExistsExclInnerScopes(func, scvar.getName());
	}
	public boolean varExistsExclInnerScopes(SCFunction func, String name) {
		return varExistsExclInnerScopes(func.getBody(), name);
	}
}
