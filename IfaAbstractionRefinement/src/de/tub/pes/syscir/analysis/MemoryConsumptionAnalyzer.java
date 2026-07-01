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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCParameter;
import de.tub.pes.syscir.sc_model.SCREFERENCETYPE;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.NewArrayExpression;
import de.tub.pes.syscir.sc_model.expressions.NewExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.variables.SCArray;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCPeq;
import de.tub.pes.syscir.sc_model.variables.SCPointer;

/**
 * Estimates the worst case memory consumption of a SystemC model. WARNING: This
 * approximation is not safe if non-constant values are used for array
 * instantiation.
 * 
 * @author rschroeder
 */
public class MemoryConsumptionAnalyzer implements Analyzer {

	private static Logger logger = LogManager
			.getLogger(MemoryConsumptionAnalyzer.class.getName());

	private HashMap<String, Integer> totalMemConsumption;
	private HashMap<String, Integer> memConsumptionOnlyRefVars;
	private VariableUsageAnalyzer vua;
	private List<SCVariable> allVars;
	private Set<SCPeq> allPeqs;

	@Override
	public void analyze(SCSystem scs) {
		TimeConsumptionAnalyzer tca = new TimeConsumptionAnalyzer();
		totalMemConsumption = new HashMap<String, Integer>();
		memConsumptionOnlyRefVars = new HashMap<String, Integer>();
		allVars = new LinkedList<SCVariable>();
		allPeqs = new HashSet<SCPeq>();
		vua = new VariableUsageAnalyzer();
		vua.analyze(scs);
		for (SCVariable scvar : scs.getGlobalVariables()) {
			update(totalMemConsumption, analyzeSCVar(scvar, false));
			update(memConsumptionOnlyRefVars, analyzeSCVar(scvar, true));
			addToAllVars(scvar);
		}
		for (SCFunction scfct : scs.getGlobalFunctions()) {
			update(totalMemConsumption, analyzeFunction(scfct, false));
			update(memConsumptionOnlyRefVars, analyzeFunction(scfct, true));
		}
		for (SCClassInstance scci : scs.getInstances()) {
			if (scci.isSCModule() || scci.isSCKnownType() || scci.isChannel()) {
				SCClass scc = scci.getSCClass();
				int nbrProcs = scc.getProcesses().size();
				nbrProcs = (nbrProcs > 0 ? nbrProcs : 1); // channels dont have
															// procs
				for (SCVariable scvar : scc.getMembers()) {
					update(totalMemConsumption, analyzeSCVar(scvar, false));
					update(memConsumptionOnlyRefVars, analyzeSCVar(scvar, true));
					addToAllVars(scvar);
				}
				for (SCFunction scfct : scc.getMemberFunctions()) {
					int amount = (tca.determineTimeConsumption(scfct) ? nbrProcs
							: 1);
					Map<String, Integer> updates = analyzeFunction(scfct, false);
					Map<String, Integer> updatesOnlyRefs = analyzeFunction(
							scfct, true);
					for (int i = 1; i <= amount; i++) {
						update(totalMemConsumption, updates);
						update(memConsumptionOnlyRefVars, updatesOnlyRefs);
					}
				}
			}
		}
	}

	private Map<String, Integer> analyzeFunction(SCFunction scfct,
			boolean addOnlyIfReferenced) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (SCParameter param : scfct.getParameters()) {
			if (param.getRefType() == SCREFERENCETYPE.BYVALUE) {
				update(ret, analyzeSCVar(param.getVar(), addOnlyIfReferenced));
			}
			addToAllVars(param.getVar());
		}
		for (SCVariable scvar : scfct.getLocalVariables()) {
			update(ret, analyzeSCVar(scvar, addOnlyIfReferenced));
			addToAllVars(scvar);
		}
		for (Expression expr : scfct.getAllExpressions()) {
			update(ret, analyzeExpression(expr));
		}
		return ret;
	}

	private Map<String, Integer> analyzeExpression(Expression expr) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		if (expr instanceof NewExpression) {
			NewExpression ne = (NewExpression) expr;
			ret.put(ne.getObjType(), 1);
		} else if (expr instanceof NewArrayExpression) {
			NewArrayExpression nae = (NewArrayExpression) expr;
			int amount = exprToInt(nae.getSize());
			ret.put(nae.getObjType(), amount);
		} 
		return ret;
	}

	private Map<String, Integer> analyzeSCVar(SCVariable var,
			boolean addOnlyIfReferenced) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		if (var.isSCClassInstance()) {
			if (((SCClassInstance) var).isSCModule()) {
				return ret; // put structs, but not scmodules
			}
		} // if necessary, test var.isArrayOfSCClassInstances(), too
		int amount = 1;
		if (addOnlyIfReferenced && !vua.getRefvars().contains(var)) {
			amount = 0;
		}
		// TODO: so far, we count all types of varying sizes together. should be separated
		String type = var.getType();
		String ptrType = type + "*";
		if (var instanceof SCArray) {
			SCArray arr = (SCArray) var;
			Expression size = arr.getDerivedSize();
			amount = exprToInt(size);
			// TODO: should we increment the ptr type for arrays, too?
		} else if (var instanceof SCPointer) {
			ret.put(ptrType, 1);
			amount = 0;
		} else if (var instanceof SCPeq) {
			allPeqs.add((SCPeq) var);
		}
		ret.put(type, amount);
		return ret;
	}

	private void update(Map<String, Integer> storage,
			Map<String, Integer> updates) {
		for (Map.Entry<String, Integer> entry : updates.entrySet()) {
			String type = entry.getKey();
			int amount = entry.getValue();
			if (storage.containsKey(type)) {
				amount += storage.get(type);
			}
			storage.put(type, amount);
		}
	}

	private int exprToInt(Expression size) {
		int i = 0;
		if (size != null) {
			try {
				i = Integer.parseInt(size.toStringNoSem());
			} catch (NumberFormatException e) {
			}
			if (size instanceof SCVariableExpression) {
				// to get the value of 'int size = 2; int buf[size];'
				// this is wrong, in case of 'int size = 2; size = ...;
				// int buf[size];'
				if (((SCVariableExpression) size).getVar().hasInitialValue()) {
					SCVariableDeclarationExpression d = ((SCVariableExpression) size)
							.getVar().getDeclaration();
					return exprToInt(d.getFirstInitialValue());
				}
			}
		}
		return i;
	}

	private boolean addToAllVars(SCVariable scvar) {
		if (scvar.isNotSCModule()) {
			return allVars.add(scvar);
		}
		return false;
	}

	public int getCount(String type, boolean onlyRefVars) {
		if (totalMemConsumption == null) {
			logger.warn("Called getCount() before analyzing the system");
			return 0;
		}
		HashMap<String, Integer> consumption = totalMemConsumption;
		if (onlyRefVars) {
			consumption = memConsumptionOnlyRefVars;
		}
		if (consumption.containsKey(type)) {
			return consumption.get(type);
		}
		return 0;
	}

	public int getTotalCountForAllSizes(String type, boolean onlyRefVars) {
		if (totalMemConsumption == null) {
			logger.warn("Called getCount() before analyzing the system");
			return 0;
		}
		HashMap<String, Integer> consumption = totalMemConsumption;
		if (onlyRefVars) {
			consumption = memConsumptionOnlyRefVars;
		}
		//LinkedList<String> all = new LinkedList<String>();
		//allNames = consumption.keySet();
		int amount = 0;
		
		for (String tmp : consumption.keySet()) {
			if (tmp.startsWith(type+"<") || tmp.equals(type))
				amount += consumption.get(tmp);
		}
		
		return amount;
	}
	public int getCount(String type) {
		return getCount(type, false);
	}

	public List<SCVariable> getVars() {
		return allVars;
	}

	public Set<SCPeq> getPeqs() {
		return allPeqs;
	}

}
