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

package de.tub.pes.syscir.engine.nodetransformer;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.TransformerFactory;
import de.tub.pes.syscir.engine.typetransformer.KnownTypeTransformer;
import de.tub.pes.syscir.engine.typetransformer.SCFifoTypeTransformer;
import de.tub.pes.syscir.engine.typetransformer.SCSignalTypeTransformer;
import de.tub.pes.syscir.engine.util.Constants;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCConnectionInterface;
import de.tub.pes.syscir.sc_model.SCEnumElement;
import de.tub.pes.syscir.sc_model.SCEnumType;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCPORTSCSOCKETTYPE;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCPortInstance;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.AssertionExpression;
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.EndlineExpression;
import de.tub.pes.syscir.sc_model.expressions.EnumElementExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.NameExpression;
import de.tub.pes.syscir.sc_model.expressions.SCClassInstanceExpression;
import de.tub.pes.syscir.sc_model.expressions.SCDeltaCountExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCStopExpression;
import de.tub.pes.syscir.sc_model.expressions.SCTimeStampExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;

/**
 * we found a name, but we didn't know if we have a variable, a event, a port or
 * sockets, a module instance or a function we check it in this order, on trying
 * to find it with the getters of the current functions or the current object if
 * we found something with this name, we create a new Expression, corresponding
 * to the case if we didn't found something, we assume its a new function and
 * create one and build a new expression
 * 
 * @author Florian, Timm Liebrenz
 * 
 */
public class IDExpressionTransformer extends AbstractNodeTransformer {
	
	private static Logger logger = LogManager
			.getLogger(IDExpressionTransformer.class.getName());
	
	@Override
	public void transformNode(Node node, Environment e) {
		String name = NodeUtil.getAttributeValueByName(node, "name");

		// it's a special construct like null, a special systemC function
		// (sc_time_stamp, name)
		// or maybe an endl?
		Expression exp = null;
		if (name.equals("NULL")) {
			exp = new ConstantExpression(node, "NULL");
		} else if (name.equals("sc_time_stamp")) {
			exp = new SCTimeStampExpression(node, "");
		} else if (name.equals("sc_stop")) {
			exp = new SCStopExpression(node, "");
		} else if (name.equals("sc_delta_count") || name.equals("delta_count")) {
			exp = new SCDeltaCountExpression(node, "");
		} else if (name.equals("name")) {
			exp = new NameExpression(node, "");
		} else if (name.equals("endl")) {
			exp = new EndlineExpression(node, "");
		} else if (name.equals("assert")) {
			exp = new AssertionExpression(node, null);
		}

		if (exp != null) {
			e.getExpressionStack().add(exp);
			return;
		}

		if (node.getPreviousSibling() != null) {
			Node scope_overwrite = node.getPreviousSibling()
					.getPreviousSibling();
			if (scope_overwrite != null
					&& scope_overwrite.getNodeName().equals("scope_override")) {
				String scope = NodeUtil.getAttributeValueByName(
						scope_overwrite, "name");
				if (scope.equals("tlm")) {
					String type = "tlm_sync_enum";
					if (!e.getKnownTypes().containsKey(type)) {
						KnownTypeTransformer typeTrans = e.getTransformerFactory()
								.getTypeTransformer(type, e);
						if (typeTrans != null) {
							typeTrans.createType(e);
						}

					}

					SCVariable tlm_enum = e.getKnownTypes().get(type)
							.getMemberByName(name);
					if (tlm_enum != null) {
						SCVariableExpression ve = new SCVariableExpression(
								node, tlm_enum);
						e.getExpressionStack().push(ve);
						return;
					}
				}
			}
		}

		SCVariable var = null;
		if (e.getCurrentFunction() != null) {
			var = e.getCurrentFunction().getLocalVariableOrParameterAsSCVar(
					name);
		}
		if (var == null) {
			if (e.getCurrentClass() != null) {
				var = e.getCurrentClass().getMemberByName(name);
			}
		}
		if (var == null) {
			var = e.getSystem().getGlobalVariables(name);
		}
		SCVariableExpression var_exp = null;
		if (var != null) {
			var_exp = new SCVariableExpression(node, var);
			e.getExpressionStack().push(var_exp);
			return;
		} else {
			// we havn't a Variable
			// what else?
			// enum Element
			for (SCEnumType et : e.getSystem().getEnumTypes()) {
				SCEnumElement ee = et.getElementByName(name);
				if (ee != null) {
					EnumElementExpression ce = new EnumElementExpression(node, ee);
					e.getExpressionStack().push(ce);
					return;
				}
			}

			// event
			SCEvent ev = e.getCurrentClass().getEventByName(name);
			if (ev != null) {
				SCVariableExpression ev_exp = new SCVariableExpression(node, ev);
				e.getExpressionStack().push(ev_exp);
				return;
			}
			// port/Socket
			SCPort ps = null;
			ps = e.getCurrentClass().getPortSocketByName(name);

			if (ps != null) {
				SCPortSCSocketExpression ps_exp = new SCPortSCSocketExpression(
						node, ps);
				e.getExpressionStack().push(ps_exp);
				return;
			}
			// moduleInstance
			SCClassInstance clI = e.getCurrentClass().getInstanceByName(name);

			if (clI != null) {
				SCClassInstanceExpression classInstExp = new SCClassInstanceExpression(
						node, clI);
				e.getExpressionStack().push(classInstExp);
				return;
			}
			// function
			SCFunction fct = null;
			// first we look if it's a class method
			if (e.getCurrentClass() != null) {
				fct = e.getCurrentClass().getMemberFunctionByName(name);
			}
			// if it's not a class method, it might be the method of a port or
			// socket
			if (fct == null && e.getCurrentPortSocket() != null) {
				fct = findFunctionFromLastPortSocket(e, name);
			}
			// if it's neither a class method nor a port method it might be a
			// global function
			if (fct == null && e.getSystem().getGlobalFunction(name) != null) {
				fct = e.getSystem().getGlobalFunction(name);
			}

			// we have a function we can't identify. It's propably a c++
			// or systemc built in function. We have the name
			// and generate a new function.
			if (fct == null) {
				// We don't know where this function comes from so we use NULL
				// as the containing class
				fct = new SCFunction(name, null);
			}
			FunctionCallExpression fce = new FunctionCallExpression(node, fct,
					null);
			e.getExpressionStack().push(fce);

		}

	}
	
	private SCFunction findFunctionFromLastPortSocket(Environment e, String fctName) {
		
		SCFunction function = null;
		
		String className = e.getCurrentPortSocket().getType();
		if (e.getClassList().containsKey(className)) {
			function = e.getClassList().get(className)
					.getMemberFunctionByName(fctName);
		} else if (SCPORTSCSOCKETTYPE.SC_FIFO_ALL.contains(e
				.getCurrentPortSocket().getConType())
		) {
			String fifoType = e.getCurrentPortSocket().getType();
			
			String scFifoName = SCFifoTypeTransformer.createName(fifoType);
			if (e.getClassList().containsKey(scFifoName)) {
				function = e.getClassList().get(scFifoName)
						.getMemberFunctionByName(fctName);
			}
		} else if (SCPORTSCSOCKETTYPE.SC_SIGNAL_ALL.contains(e
				.getCurrentPortSocket().getConType())
		) {
			String type = e.getCurrentPortSocket().getType();
			String scSignalName = SCSignalTypeTransformer.createName(type);
			if (e.getClassList().containsKey(scSignalName)) {
				function = e.getClassList().get(scSignalName)
						.getMemberFunctionByName(fctName);
			}
		} else if (
			( e.getCurrentPortSocket().getConType() == SCPORTSCSOCKETTYPE.SC_PORT ) &&
			( e.getCurrentPortSocket().getType().equals("sc_signal") )
				
		) {
			String instanceName = e.getCurrentPortSocket().getName();
			List<SCConnectionInterface> possibleTypes = e.getSystem().getPortSocketInstances(e.getCurrentPortSocket());
			if (!possibleTypes.isEmpty()) {
				SCPortInstance instance = (SCPortInstance) possibleTypes.get(0);
				SCKnownType type = instance.getChannel(instanceName);
				function = type.getSCClass().getMemberFunctionByName(fctName);
			}
		}
		
		return function;
	}
	
}
