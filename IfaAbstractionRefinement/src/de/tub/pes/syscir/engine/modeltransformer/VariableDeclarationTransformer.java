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

import java.util.LinkedList;
import java.util.List;

import de.tub.pes.syscir.engine.util.Pair;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.EmptyExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;

/**
 * Replaces SCVariableDeclarationExpressions (e.g., int i = 42;) in all
 * functions with either binaryExpressions (i, "=" 42) or empty expressions.
 * Useful if the declaration of veriables have to be done statically and
 * therefore must be removed from the body of functions.
 * 
 * @author pockrandt
 * 
 */
public class VariableDeclarationTransformer implements ModelTransformer {

	@Override
	public SCSystem transformModel(SCSystem model) {
		List<SCFunction> globfunc = model.getGlobalFunctions();
		List<SCClass> classes = model.getClasses();

		for (SCFunction func : globfunc) {
			List<Pair<Expression, Expression>> replacements = getVDReplaceList(func
					.getAllExpressions());
			func.replaceExpressions(replacements);
		}

		for (SCClass mod : classes) {
			List<SCFunction> memFunc = mod.getMemberFunctions();
			List<SCProcess> processes = mod.getProcesses();

			for (SCFunction func : memFunc) {
				List<Pair<Expression, Expression>> replacements = getVDReplaceList(func
						.getAllExpressions());
				func.replaceExpressions(replacements);
			}
			for (SCProcess proc : processes) {
				SCFunction func = proc.getFunction();
				List<Pair<Expression, Expression>> replacements = getVDReplaceList(func
						.getAllExpressions());
				func.replaceExpressions(replacements);
			}

		}

		return model;
	}

	private List<Pair<Expression, Expression>> getVDReplaceList(
			List<Expression> exprlist) {
		List<Pair<Expression, Expression>> replacements = new LinkedList<Pair<Expression, Expression>>();
		for (Expression expr : exprlist) {
			if (expr instanceof SCVariableDeclarationExpression) {
				SCVariableDeclarationExpression vdexpr = (SCVariableDeclarationExpression) expr;
				Expression replacement;
				if (vdexpr.getInitialValues().size() != 0) {
					replacement = new BinaryExpression(vdexpr.getNode(),
							vdexpr.getVariable(), "=",
							vdexpr.getFirstInitialValue());
				} else {
					replacement = new EmptyExpression(vdexpr.getNode());
				}

				replacements.add(new Pair<Expression, Expression>(vdexpr,
						replacement));
			}
		}

		return replacements;
	}
}
