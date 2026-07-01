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

package de.tub.pes.syscir.engine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.w3c.dom.Node;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCMODIFIER;
import de.tub.pes.syscir.sc_model.SCPORTSCSOCKETTYPE;
import de.tub.pes.syscir.sc_model.SCParameter;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCSystem;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.variables.SCEvent;

public class Environment {

	/**
	 * this is the system, with it's ModuleInstances, global Variables and
	 * global Functions //it contains also the Portbindings between the
	 * ModuleInstances
	 **/
	private SCSystem system;

	private final TransformerFactory transformerFactory = new TransformerFactory();
	
	/** this String is used for the detection of a constructor or the SCMain **/
	private String location = "";

	/**
	 * this Map contains all classes which are used in the System
	 */
	private HashMap<String, SCClass> classList = null;
	/**
	 * this Map contains all KnownTypes which are used in the System
	 */
	private HashMap<String, SCClass> knownTypes = null;

	/**
	 * refers to the class currently under transformation.
	 */
	private SCClass currentClass = null;

	/**
	 * refers to the current Function whose XML-Tags will be transform
	 */
	private SCFunction currentFunction = null;

	/**
	 * refers to the current port or socket we encountered in the xml document.
	 * This is needed to determine the port/socket on which a function is
	 * invoked.
	 */
	private SCPort currentPortSocket = null;

	/**
	 * save the Type of the current Variable, if its an empty String its a
	 * Variable otherwise its a port or socket
	 */
	private String foundMemberType = "";
	/**
	 * saves the last Type of a Port or a socket
	 */
	private SCPORTSCSOCKETTYPE lastPortSocketType = null;

	/**
	 * contains the types which have been found, should be empty if we are
	 * outside a MemberDeclaration or an InitDeclaration
	 */
	private Stack<String> lastType = null;
	/**
	 * contains the found Modifiers of the current Variable, such as static or
	 * const
	 */
	private Stack<String> foundTypeModifiers = null;
	/**
	 * contains the arguments of the current Variable
	 */
	private List<String> lastType_TemplateArguments = null;
	/**
	 * contains the last Id, this can be nearly everything, for example: a
	 * Variable, a Module, a Struct, ...
	 */
	private Stack<String> lastQualifiedId = null;
	/**
	 * this contains the Parameter of the currently found function
	 */
	private List<SCParameter> lastParameterList = null;
	/**
	 * this contains the Call-Arguments of the current Fucntioncall
	 */
	private List<Expression> lastArgumentList = null;

	/**
	 * during phase 1, in this map, all functionbodys will be saved, after phase
	 * 6, this Map should be empty
	 */
	private HashMap<String, HashMap<String, List<Node>>> functionBodys = null;
	/**
	 * this contains the FunctionBody of the current Function
	 */
	private List<Node> lastFunctionBody = null;
	/**
	 * this saves the actual AccessKey, public or private
	 */
	private String CurrentAccessKey = "";
	/**
	 * during the functionparsing the found Expressions will be saved in this
	 * stack, if we reach the end of the highest BlockStatementTag we put the
	 * Expressions from this stack in the current function
	 */
	private Stack<Expression> ExpressionStack = null;
	/**
	 * this refers to the Function which will be marked as a Process in the
	 * corresponding XML-Tag
	 */
	private SCFunction lastProcessFunction = null;
	/**
	 * this is the Name of the Process but nearly everytime its the functionname
	 */
	private String lastProcessName = "";
	/**
	 * this list contains the Events where the Process is sensitive for
	 */
	private List<SCEvent> sensitivityList;
	/**
	 * this contains the modifiers of the process, for example dont_initialize
	 */
	private EnumSet<SCMODIFIER> lastProcessModifier = null;

	/**
	 * this marks weather we are in the highest BlockStatement(false) or we are
	 * in a BlockStatement in a Blockstatement etc. (true)
	 */
	private boolean rekursiveBlockStatement = false;
	/**
	 * this marks weather we are in a functionblock or not
	 */
	private boolean FunctionBlock = true;
	/**
	 * this marks if we build the system or not
	 */
	private boolean systemBuilding = false;
	/**
	 * this contains the last Initializer which was found
	 */
	private Expression lastInitializer = null;
	/**
	 * this contains the last scope-overwrite
	 */
	private String lastScopeOverwrite = "";
	/**
	 * this marks weather we are in a constructor or not
	 */
	private boolean inConstructor = false;

	public Environment() {
		lastType = new Stack<String>();
		lastQualifiedId = new Stack<String>();
		ExpressionStack = new Stack<Expression>();
		functionBodys = new HashMap<String, HashMap<String, List<Node>>>();
		classList = new HashMap<String, SCClass>();
		lastParameterList = new ArrayList<SCParameter>();
		lastArgumentList = new ArrayList<Expression>();
		sensitivityList = new ArrayList<SCEvent>();
		knownTypes = new HashMap<String, SCClass>();
		foundTypeModifiers = new Stack<String>();
		lastType_TemplateArguments = new ArrayList<String>();
		system = new SCSystem();
		try {
			transformerFactory.initialize();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SCSystem getSystem() {
		return system;
	}

	public void setSystem(SCSystem system) {
		this.system = system;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Returns a map of class names, mapped to their corresponding
	 * implementations.
	 * 
	 * @return
	 */
	public HashMap<String, SCClass> getClassList() {
		return classList;
	}

	public void setClassList(HashMap<String, SCClass> structList) {
		this.classList = structList;
	}

	public HashMap<String, SCClass> getKnownTypes() {
		return knownTypes;
	}

	public void setKnownTypes(HashMap<String, SCClass> knownTypes) {
		this.knownTypes = knownTypes;
	}

	public SCClass getCurrentClass() {
		return currentClass;
	}

	public void setCurrentClass(SCClass scclass) {
		this.currentClass = scclass;
	}

	public SCFunction getCurrentFunction() {
		return currentFunction;
	}

	public void setCurrentFunction(SCFunction currentFunction) {
		this.currentFunction = currentFunction;
	}
	
	public SCPort getCurrentPortSocket() {
		return currentPortSocket;
	}
	
	public void setCurrentPortSocket(SCPort currentPortSocket) {
		this.currentPortSocket = currentPortSocket;
	}

	public String getFoundMemberType() {
		return foundMemberType;
	}

	public void setFoundMemberType(String foundMemberType) {
		this.foundMemberType = foundMemberType;
	}

	public SCPORTSCSOCKETTYPE getLastPortSocketType() {
		return lastPortSocketType;
	}

	public void setLastPortSocketType(SCPORTSCSOCKETTYPE lastPortSocketType) {
		this.lastPortSocketType = lastPortSocketType;
	}

	public Stack<String> getLastType() {
		return lastType;
	}

	public void setLastType(Stack<String> lastType) {
		this.lastType = lastType;
	}

	public Stack<String> getFoundTypeModifiers() {
		return foundTypeModifiers;
	}

	public void setFoundTypeModifiers(Stack<String> foundTypeModifiers) {
		this.foundTypeModifiers = foundTypeModifiers;
	}

	public List<String> getLastType_TemplateArguments() {
		return lastType_TemplateArguments;
	}

	public void setLastType_TemplateArguments(
			List<String> lastType_TemplateArguments) {
		this.lastType_TemplateArguments = lastType_TemplateArguments;
	}

	public Stack<String> getLastQualifiedId() {
		return lastQualifiedId;
	}

	public void setLastQualifiedId(Stack<String> lastQualifiedId) {
		this.lastQualifiedId = lastQualifiedId;
	}

	public List<SCParameter> getLastParameterList() {
		return lastParameterList;
	}

	public void setLastParameterList(List<SCParameter> lastParameterList) {
		this.lastParameterList = lastParameterList;
	}

	public List<Expression> getLastArgumentList() {
		return lastArgumentList;
	}

	public void setLastArgumentList(List<Expression> lastArgumentList) {
		this.lastArgumentList = lastArgumentList;
	}

	public HashMap<String, HashMap<String, List<Node>>> getFunctionBodys() {
		return functionBodys;
	}

	public void setFunctionBodys(
			HashMap<String, HashMap<String, List<Node>>> functionBodys) {
		this.functionBodys = functionBodys;
	}

	public List<Node> getLastFunctionBody() {
		return lastFunctionBody;
	}

	public void setLastFunctionBody(List<Node> lastFunctionBody) {
		this.lastFunctionBody = lastFunctionBody;
	}

	public String getCurrentAccessKey() {
		return CurrentAccessKey;
	}

	public void setCurrentAccessKey(String currentAccessKey) {
		CurrentAccessKey = currentAccessKey;
	}

	public Stack<Expression> getExpressionStack() {
		return ExpressionStack;
	}

	public void setExpressionStack(Stack<Expression> expressionStack) {
		ExpressionStack = expressionStack;
	}

	public SCFunction getLastProcessFunction() {
		return lastProcessFunction;
	}

	public void setLastProcessFunction(SCFunction lastProcessFunction) {
		this.lastProcessFunction = lastProcessFunction;
	}

	public String getLastProcessName() {
		return lastProcessName;
	}

	public void setLastProcessName(String lastProcessName) {
		this.lastProcessName = lastProcessName;
	}

	public List<SCEvent> getSensitivityList() {
		return sensitivityList;
	}

	public void setSensitivityList(List<SCEvent> sensitivityList) {
		this.sensitivityList = sensitivityList;
	}

	public EnumSet<SCMODIFIER> getLastProcessModifier() {
		return lastProcessModifier;
	}

	public void setLastProcessModifier(EnumSet<SCMODIFIER> lastProcessModifier) {
		this.lastProcessModifier = lastProcessModifier;
	}

	public boolean isRekursiveBlockStatement() {
		return rekursiveBlockStatement;
	}

	public void setRekursiveBlockStatement(boolean rekursiveBlockStatement) {
		this.rekursiveBlockStatement = rekursiveBlockStatement;
	}

	public boolean isFunctionBlock() {
		return FunctionBlock;
	}

	public void setFunctionBlock(boolean functionBlock) {
		FunctionBlock = functionBlock;
	}

	public boolean isSystemBuilding() {
		return systemBuilding;
	}

	public void setSystemBuilding(boolean systemBuilding) {
		this.systemBuilding = systemBuilding;
	}

	public Expression getLastInitializer() {
		return lastInitializer;
	}

	public void setLastInitializer(Expression lastInitializer) {
		this.lastInitializer = lastInitializer;
	}

	public String getLastScopeOverwrite() {
		return lastScopeOverwrite;
	}

	public void setLastScopeOverwrite(String lastScopeOverwrite) {
		this.lastScopeOverwrite = lastScopeOverwrite;
	}

	public boolean isInConstructor() {
		return inConstructor;
	}

	public void setInConstructor(boolean inConstructor) {
		this.inConstructor = inConstructor;
	}

	/**
	 * Integrates the submitted environment into this environment. This method
	 * can be used to conveniently add the results of the parsing of an
	 * implementation file into the main environment used the whole system. All
	 * modules and structs in e are also added to the known types of this
	 * environment.
	 * 
	 * @param e
	 */
	public void integrate(Environment e) {
		// we have to set all modules and structs we integrate into the
		// environment to external as they are already finished.
		for (SCClass struct : e.classList.values()) {
			struct.setExternal(true);
			if (!classList.containsKey(struct.getName())) {
				this.classList.put(struct.getName(), struct);
			}
			if (!knownTypes.containsKey(struct.getName())) {
				this.knownTypes.put(struct.getName(), struct);
			}
		}
	}

	
	public TransformerFactory getTransformerFactory() {
		return transformerFactory;
	}

	
	
}
