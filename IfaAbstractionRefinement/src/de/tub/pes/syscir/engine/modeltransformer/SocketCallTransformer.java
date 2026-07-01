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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.variables.SCPeq;

public class SocketCallTransformer implements ModelTransformer {

	private static final Logger logger = LogManager
			.getLogger(SocketCallTransformer.class.getName());

	public SocketCallTransformer() {
	}

	@Override
	public SCSystem transformModel(SCSystem model) {
		if (model == null) {
			logger.error("model is null");
			return null;
		}
		// get all classes with sockets
		List<SCClass> classesWithSocket = new ArrayList<SCClass>();
		for (SCClass scclass : model.getClasses()) {
			if (!scclass.getPortsSockets().isEmpty()) {
				// We are currently not using this functionality
				// socket calls are treated in:
				// sc2uppaal.ClassInstanceTransformer.instantiateAndBind
				// transformClass(scclass);
				classesWithSocket.add(scclass);
			}
			for (SCVariable var : scclass.getMembers()) {
				if (var instanceof SCPeq) {
					classesWithSocket.add(scclass);
					break;
				}
			}
		}
		removeSocketFromClasses(classesWithSocket);
		return null;
	}

	private void removeSocketFromClasses(List<SCClass> classesWithSocket) {
		assert classesWithSocket != null;
		for (SCClass scclass : classesWithSocket) {
			removeSocketFromConstructor(scclass.getConstructor());
		}
	}

	private void removeSocketFromConstructor(SCFunction constructor) {
		assert constructor != null;
		assert constructor.getBody() != null;
		assert constructor.getBody() == constructor.getBody();
		List<Expression> exprToRemove = new ArrayList<Expression>();
		for (Expression expr : constructor.getBody()) {
			if (expr instanceof BinaryExpression
					&& ((BinaryExpression) expr).getLeft() instanceof SCPortSCSocketExpression) {
				// socket = socket("..");
				exprToRemove.add(expr);
			} else if (expr instanceof AccessExpression) {
				AccessExpression accessExpr = (AccessExpression) expr;
				if (accessExpr.getLeft() instanceof SCPortSCSocketExpression
						&& accessExpr.getRight() instanceof FunctionCallExpression
						&& ((FunctionCallExpression) accessExpr.getRight())
								.getFunction().getName().equals("bind")) {
					// socket.bind("..");
					exprToRemove.add(expr);
				}
			} else if (expr instanceof BinaryExpression
					&& ((BinaryExpression) expr).getLeft() instanceof SCVariableExpression
					&& ((SCVariableExpression) ((BinaryExpression) expr)
							.getLeft()).getVar() instanceof SCPeq) {
				// peq = "peq_name";
				exprToRemove.add(expr);
			}
		}
		// getBody does no defensive copying (see assert)
		constructor.getBody().removeAll(exprToRemove);
	}
}
