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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.TransformerFactory;
import de.tub.pes.syscir.engine.typetransformer.KnownTypeTransformer;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCPORTSCSOCKETTYPE;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCSocket;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.SCClassInstanceExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.expressions.TimeUnitExpression;
import de.tub.pes.syscir.sc_model.variables.SCArray;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCEvent;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;
import de.tub.pes.syscir.sc_model.variables.SCPeq;
import de.tub.pes.syscir.sc_model.variables.SCPointer;
import de.tub.pes.syscir.sc_model.variables.SCSimpleType;
import de.tub.pes.syscir.sc_model.variables.SCTime;

/**
 * 
 * @author rschroeder
 * 
 */
public class DeclaratorTransformer extends AbstractNodeTransformer {
	private static Logger logger = LogManager
			.getLogger(DeclaratorTransformer.class.getName());

	private Node n_declarator;
	private Node n_qualified_id;
	private Node n_declarator_suffixes;
	private String varName;
	private String varType;
	boolean isTypedef;
	boolean isStatic;
	boolean isConstant;
	List<String> otherTypeModifiers;
	List<String> templateArguments;

	private enum SCOPE {
		FUNCTION, CLASS, SYSTEM
	};

	SCOPE scope;

	@Override
	public void transformNode(Node node, Environment e) {
		n_declarator = findChildNode(node, "declarator");
		n_qualified_id = findChildNode(n_declarator, "qualified_id");
		n_declarator_suffixes = findChildNode(n_declarator,
				"declarator_suffixes");

		if (e.getCurrentFunction() != null) {
			scope = SCOPE.FUNCTION;
		} else if (e.getClass() != null) {
			scope = SCOPE.CLASS;
		} else {
			scope = SCOPE.SYSTEM;
		}

		handleInitialization(node, e);

		setTypeModifiers(node, e);
		setTemplateArguments(node, e);

		if (e.getLastType().size() == 0) {
			logger.error("{}: Can't get variable type",
					NodeUtil.getFixedAttributes(node));
			return;
		}
		varType = e.getLastType().lastElement();

		if (n_qualified_id == null) {
			handlePointer(node, e);
		} else {
			varName = NodeUtil.getAttributeValueByName(n_qualified_id, "name");
			if (varName == null) {
				logger.error("{}: Can't get variable name",
						NodeUtil.getFixedAttributes(node));
				return;
			} else if (isTypedef) {
				handleTypedef(node, e);
			} else if (e.getFoundMemberType().equals("PortSocket")) {
				handlePortSocket(node, e);
			} else if (e.getTransformerFactory().isSimpleType(varType)) {
				handleSimpleType(node, e);
			} else if (varType.equals("sc_event")) {
				handleSCEvent(node, e);
			} else if (varType.equals("sc_time")) {
				handleSCTime(node, e);
			} else if (varType.equals("peq_with_cb_and_phase")) {
				handleSCPeq(node, e);
			} else if (!e.getKnownTypes().isEmpty()
					&& e.getKnownTypes().containsKey(varType)) {
				handleKnownType(node, e);
			} else if (!e.getClassList().isEmpty()
					&& e.getClassList().containsKey(varType)) {
				handleClass(node, e);
			} else if (e.getTransformerFactory().isKnownType(varType)) {
				handleUnititializedKnownType(node, e);
			} else {
				handleUnknownClass(node, e);
			}
		}
	}
	
	private void addVariableDeclarationExprWithParameters(Node node, Environment e, SCVariable var, List<Expression> parameters) {
		SCVariableDeclarationExpression vde = null;
		Expression classInstanceOrVariableExpr = null;
		if (var instanceof SCClassInstance) {
			SCClassInstance classInstance = (SCClassInstance) var;
			classInstance.setInstanceLabel(checkLabelExistence(parameters));
			classInstanceOrVariableExpr = new SCClassInstanceExpression(node,
					classInstance);
			e.getSystem().addInstance((SCClassInstance) var);
		} else {
			classInstanceOrVariableExpr = new SCVariableExpression(node, var);
		}
		if (parameters == null || parameters.size() == 0) {
			vde = new SCVariableDeclarationExpression(node, classInstanceOrVariableExpr);
		} else {
			vde = new SCVariableDeclarationExpression(node, classInstanceOrVariableExpr, parameters);
		}
		
		switch (scope) {
		case FUNCTION:
			e.getExpressionStack().add(vde);
			e.getCurrentFunction().addLocalVariable(var);
			break;
		case CLASS:
			e.getCurrentClass().addMember(var);
			break;
		case SYSTEM:
			e.getSystem().addGlobalVariable(var);
			break;
		}
	}

	private void addVariableAndDeclarationExprIfNeeded(Node node,
			Environment e, SCVariable var) {

		if (e.getLastInitializer() != null) {
			List<Expression> parameters = new ArrayList<Expression>();
			parameters.add(e.getLastInitializer());
			addVariableDeclarationExprWithParameters(node, e, var, parameters);
			e.setLastInitializer(null);
		} else if (e.getLastArgumentList() != null
				&& !e.getLastArgumentList().isEmpty()) {
			addVariableDeclarationExprWithParameters(node, e, var, e.getLastArgumentList());
			e.setLastArgumentList(new LinkedList<Expression>());
		} else {
			addVariableDeclarationExprWithParameters(node, e, var, null);
		}
	}

	private SCVariable getSCArray(Node node, Environment e) {
		int dimCount = Integer.valueOf(NodeUtil.getAttributeValueByName(
				n_declarator_suffixes, "arrayCounter"));
		if (dimCount != 1) {
			logger.error("{}: Multidimensional arrays are not supported",
					NodeUtil.getFixedAttributes(node));
			return null;
		}
		if (n_declarator_suffixes.getChildNodes().getLength() != 0) {
			// 'int arr[expr];'
			handleChildNodes(n_declarator_suffixes, e);
			if (e.getExpressionStack().size() == 0) {
				logger.error("{}: Can't get array size",
						NodeUtil.getFixedAttributes(node));
			} else {
				return new SCArray(varName, varType, e.getExpressionStack()
						.pop());
			}
		} else {
			// 'int arr[] = {...};
			// or even 'int arr[];' :(
			return new SCArray(varName, varType);
		}
		return null;
	}

	private void setTypeModifiers(Node n, Environment e) {
		isTypedef = false;
		isStatic = false;
		isConstant = false;
		otherTypeModifiers = new ArrayList<String>(0);

		for (String modifier : e.getFoundTypeModifiers()) {
			if (modifier.equals("typedef")) {
				// TODO: differenciate between func/class/global?
				isTypedef = true;
			} else if (modifier.equals("static")) {
				isStatic = true;
			} else if (modifier.equals("const")) {
				isConstant = true;
			} else {
				otherTypeModifiers.add(modifier);
			}
		}
		e.getFoundTypeModifiers().clear();
	}

	private void setTemplateArguments(Node node, Environment e) {
		templateArguments = new ArrayList<String>(0);
		for (String subtype : e.getLastType_TemplateArguments()) {
			templateArguments.add(subtype);
		}
	}

	// Checks whether the current parameter list contains any label in the context of class instantiation
	private String checkLabelExistence(List<Expression> parameters) {
		if(parameters != null && !(parameters.isEmpty()) && parameters.get(0) instanceof ConstantExpression) {
			ConstantExpression val = (ConstantExpression) parameters.get(0);
			if(val.getValue().length() >= 2) {
				if(val.getValue().charAt(0) == ('\"') && val.getValue().charAt(val.getValue().length()-1) == ('\"')) {
					if(val.getValue().length() == 2) {
						return "";
					} else {
						return val.getValue().substring(1, val.getValue().length()-1);
					}
				}
			}
		}
		return null;
	}
	
	private void handleTypedef(Node node, Environment e) {
		// TODO: handle typedefs
		logger.warn("Typedefs are currently ignored: {}",
				NodeUtil.getFixedAttributes(node));
	}

	private void handlePointer(Node node, Environment e) {
		Node ptrOp = this.findChildNode(n_declarator, "ptr_operator");
		if (NodeUtil.getAttributeValueByName(ptrOp, "name").equals("*")) {
			Node n_declarator_declarator = this.findChildNode(n_declarator,
					"declarator");
			Node n_decl_decl_qualified_id = this.findChildNode(
					n_declarator_declarator, "qualified_id");
			varName = NodeUtil.getAttributeValueByName(
					n_decl_decl_qualified_id, "name");
			SCPointer ptr = new SCPointer(varName, varType, isStatic,
					isConstant, otherTypeModifiers, e.getLastInitializer());
			addVariableAndDeclarationExprIfNeeded(node, e, ptr);
		} else {
			logger.error(
					"{}: Node does not have an attribute name with the value * and is therefore no Pointer.",
					NodeUtil.getFixedAttributes(node));
		}
	}

	private void handlePortSocket(Node node, Environment e) {
		switch (scope) {
		case CLASS:
			SCPORTSCSOCKETTYPE portType = e.getLastPortSocketType();
			SCPort ps = (portType == SCPORTSCSOCKETTYPE.SC_SOCKET) ? new SCSocket(
					varName, varType, portType) : new SCPort(varName, varType,
					portType);
			e.getCurrentClass().addPortSocket(ps);
			break;
		case FUNCTION:
		case SYSTEM:
			logger.error("{}: SCPortSocket declared outside class",
					NodeUtil.getFixedAttributes(node));
			break;
		}
	}

	private void handleSimpleType(Node node, Environment e) {
		SCVariable var = null;
		if (n_declarator_suffixes != null) { // we have an array
			var = getSCArray(node, e);
		} else {
			var = new SCSimpleType(varName, varType, e.getLastInitializer(),
					isStatic, isConstant, otherTypeModifiers);
		}
		if (var != null) {
			addVariableAndDeclarationExprIfNeeded(node, e, var);
		} else {
			logger.error("{}: Couldn't get variable",
					NodeUtil.getFixedAttributes(node));
		}
	}

	private void handleSCEvent(Node node, Environment e) {
		switch (scope) {
		case CLASS:
			SCEvent se = new SCEvent(varName, isStatic, isConstant,
					otherTypeModifiers);
			e.getCurrentClass().addEvent(se);
			break;
		case FUNCTION:
		case SYSTEM:
			logger.error("{}: SCEvent declared outside class",
					NodeUtil.getFixedAttributes(node));
			break;
		}
	}

	private void handleSCTime(Node node, Environment e) {
		// possible SC_TIME declarations
		// sc_time t;
		// sc_time t(1, SC_SEC);
		// sc_time t = sc_time(1, SC_SEC); // 'initialized'
		SCTime st = null;

		List<Expression> parameters = e.getLastArgumentList();

		boolean useFuncCall = false;
		if (e.getLastInitializer() != null
				&& e.getLastInitializer() instanceof FunctionCallExpression) {
			FunctionCallExpression fce = (FunctionCallExpression) e
					.getLastInitializer();
			parameters = fce.getParameters();
			useFuncCall = true;
		}
		
		st = new SCTime(varName, isStatic, isConstant, otherTypeModifiers,
				useFuncCall);
		
		if (parameters.size() == 2) {
			// the sc_time variable is initialized with a value and a timeunit
			// (e.g., 1, SC_NS).
			Expression timeunit = parameters.get(1);
			if (!(timeunit instanceof TimeUnitExpression)) {
				logger.error(
						"{}: Encountered unexpexted second parameter for sc_time initilization. Expected was a systemc time unit (e.g. SC_SEC or SC_NS) but got {}",
						NodeUtil.getFixedAttributes(node), timeunit.toString());
			} else {
				addVariableDeclarationExprWithParameters(node, e, st, parameters);
			}
		} else if (parameters.size() == 0) {
			// the variable is not initialized
			addVariableAndDeclarationExprIfNeeded(node, e, st);
		} else {
			logger.error(
					"{}: Encountered unexpected argument size for initialization of sc_time. Expected was either none or two parameters but got {}",
					NodeUtil.getFixedAttributes(node), parameters.size());
		}
	}

	private void handleSCPeq(Node node, Environment e) {
		switch (scope) {
		case CLASS:
			e.getLastType_TemplateArguments().clear();
			SCPeq peq = new SCPeq(varName, e.getCurrentClass(),
					templateArguments, isStatic, isConstant, otherTypeModifiers);
			e.getCurrentClass().addMember(peq);
			break;
		case FUNCTION:
		case SYSTEM:
			logger.error("{}: SCPeq declared outside class",
					NodeUtil.getFixedAttributes(node));
			break;
		}
	}

	private void handleKnownType(Node node, Environment e) {
		KnownTypeTransformer typeTrans = e.getTransformerFactory().getTypeTransformer(
				varType, e);
		if (typeTrans != null) {
			SCKnownType kt = null;
			switch (scope) {
			case FUNCTION:
				kt = typeTrans.initiateInstance(varName,
						e.getLastArgumentList(), e, isStatic, isConstant,
						otherTypeModifiers);
				break;
			case CLASS:
				kt = typeTrans.createInstance(varName, e, isStatic, isConstant,
						otherTypeModifiers);
				break;
			case SYSTEM:
				logger.error(
						"{}: unimplemented handling of global var of known type",
						NodeUtil.getFixedAttributes(node));
				break;
			}
			if (kt != null) {
				addVariableAndDeclarationExprIfNeeded(node, e, kt);
			} else {
				logger.error("{}: Could not create known type",
						NodeUtil.getFixedAttributes(node));
			}
		} else {
			logger.error("{}: Could not get known type transformer",
					NodeUtil.getFixedAttributes(node));
		}
	}

	private void handleClass(Node node, Environment e) {
		SCVariable var = null;
		switch (scope) {
		case CLASS:
		case FUNCTION:
			if (n_declarator_suffixes == null) {
				var = new SCClassInstance(varName, e.getClassList()
						.get(varType), e.getCurrentClass());
			} else if (NodeUtil.containsAttribute(n_declarator_suffixes,
					"arrayCounter")) {
				// we have a struct array here
				var = getSCArray(node, e);
				((SCArray) var).setIsArrayOfSCClassInstances(e.getClassList()
						.get(varType));
			} else {
				logger.error(
						"{}: Encountered a module variable with name {} which seems to be something like a struct but is neither a normal struct nor a struct array.",
						NodeUtil.getFixedAttributes(n_declarator_suffixes),
						varName);
			}
			break;
		case SYSTEM:
			var = new SCClassInstance(varName, e.getClassList().get(varType),
					e.getCurrentClass());
			break;
		}
		if (var != null) {
			addVariableAndDeclarationExprIfNeeded(node, e, var);
		} else {
			logger.error("{}: Can't handle class correctly",
					NodeUtil.getFixedAttributes(node));
		}
	}

	/**
	 * Handles all possible kinds of initialization which can occur during
	 * declaration. There are three cases: <br>
	 * (1) by initializer, e.g., int x = 43; sc_time t = sc_time(2, SC_NS); <br>
	 * (2) by argument list, e.g., MyModule mod("module", ...) <br>
	 * (3) no initialization, e.g., int j;
	 * 
	 * @param node
	 * @param e
	 */
	void handleInitialization(Node node, Environment e) {
		// case 1 and two are mutual exclusive, so we don't care for order of
		// the check
		if (findChildNode(node, "initializer") != null) {
			// case 1
			e.setLastInitializer(null);
			// also sets e.lastInitializer to the found initializer
			handleNode(findChildNode(node, "initializer"), e);
		}
		if (findChildNode(node, "arguments_list") != null) {
			e.setLastArgumentList(null);
			// also sets e.lastArgumentList to the found argument list
			handleNode(findChildNode(node, "arguments_list"), e);
		}
		// do nothing in case 3.
	}

	private void handleUnititializedKnownType(Node node, Environment e) {
		handleKnownType(node, e);
	}

	private void handleUnknownClass(Node node, Environment e) {
		SCClass s = new SCClass(varType);
		e.getClassList().put(varType, s);
		SCClassInstance si = new SCClassInstance(varName, s,
				e.getCurrentClass());
		addVariableAndDeclarationExprIfNeeded(node, e, si);
	}

}
