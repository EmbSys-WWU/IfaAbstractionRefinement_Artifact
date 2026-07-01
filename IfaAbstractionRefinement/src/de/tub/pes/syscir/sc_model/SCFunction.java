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

package de.tub.pes.syscir.sc_model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.engine.util.Pair;
import de.tub.pes.syscir.sc_model.expressions.EventNotificationExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.ExpressionBlock;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.variables.SCSimpleType;

/**
 * Represents a SystemC (C++) function. Functions consists of a name,
 * parameters, a return type and a body.
 * 
 * @author Florian
 * 
 */
public class SCFunction implements Serializable , Comparable<SCFunction> {
	private static Logger logger = LogManager.getLogger(SCFunction.class
			.getName());

	private static final long serialVersionUID = -5059654048644387505L;
	private static final String VOID_RETURN_TYPE = "void";
	/**
	 * name of the function
	 */
	protected String name;
	/**
	 * list of SCVariable which represent the parameters
	 */
	protected List<SCParameter> parameters;
	/**
	 * the return type
	 */
	protected String returnType;
	/**
	 * the list of expressions, which represent the body
	 */
	protected List<Expression> body;
	

	protected ExpressionBlock bodyBlock;
	/**
	 * the list of the local variables, this contains also the parameters
	 */
	protected List<SCVariable> localVariables;

	/**
	 * A flag that determines whether this function is time consuming.
	 * Initially, we have assume that all functions are not time-consuming.
	 */
	protected boolean consumesTime;

	/**
	 * A flag that determines whether the timing of this function was already
	 * analyzed. Used to avoid that two functions recursively calling each other
	 * are analyzed multiple times.
	 */
	protected boolean timingAnalyzed;
	

	/**
	 * A flag that determines whether the timing of this function was already
	 * analyzed by the Timing_Analyzer package. Used to avoid recursive analysis .
	 * If this flag is set exitTAB should be set as well.
	 */
	protected boolean extendedTimingAnalyzed;

	/**
	 * A flag that determines whether this function is called by another
	 * function. Set to true in the ArgumentsTransformer if a call to this
	 * function is found.
	 */
	protected boolean isCalled;

	/**
	 * The class which contains the function. Might be null if the function is a
	 * global function.
	 */
	protected SCClass scclass;

	/**
	 * A list of other functions which are called by this function.
	 */
	protected LinkedList<FunctionCallExpression> functionCalls;

	protected LinkedList<EventNotificationExpression> eventNotifications;
	
	/**
	 * For splitting an SCSystem, functions can be annotated in which partition this function is called. 
	 */
	protected HashSet<Integer> partitionNumbers;



	/**
	 * creates a dummy function
	 * 
	 * @param name
	 *            name of the function
	 */
	public SCFunction(String name) {
		this(name, null);

	}

	/**
	 * creates a function without parameters
	 * 
	 * @param name
	 *            name of the function
	 * @param returnType
	 *            returntype of the function, null if there is no return-type
	 *            (void)
	 */
	public SCFunction(String name, String returnType) {
		this(name, returnType, new ArrayList<SCParameter>());
	}

	/**
	 * creates a function with parameters
	 * 
	 * @param name
	 *            name of the function
	 * @param ret_typ
	 *            returntype of the function, null if there is no return-type
	 *            (void)
	 * @param vars
	 *            list of variables which represent the parameters
	 * @param scclass
	 *            the class containing the function.
	 */
	public SCFunction(String name, String returnType, List<SCParameter> vars) {
		this(name, returnType, vars, new ArrayList<Expression>(),
				new ArrayList<SCVariable>());
	}

	/**
	 * creates a function with parameters and body
	 * 
	 * @param name
	 *            name of the function
	 * @param retType
	 *            returntype of the function, null if there is no return-type
	 *            (void)
	 * @param parameters
	 *            list of parameters which represent the parameters
	 * @param body
	 *            list of Expressions which represent the function_s body
	 * @param lovalVariables
	 *            list of local variables.
	 */
	public SCFunction(String name, String retType,
			List<SCParameter> parameters, List<Expression> body,
			List<SCVariable> localVariables) {
		this.name = name;
		setReturnType(retType);
		for (SCParameter par : parameters) {
			par.setFunction(this);
		}
		this.parameters = parameters;
		this.body = body;
		this.bodyBlock = new ExpressionBlock(null, body);
		this.bodyBlock.setOwningFunction(this);
		this.localVariables = localVariables;
		this.consumesTime = false;
		this.isCalled = false;
		this.timingAnalyzed = false;
		this.functionCalls = new LinkedList<FunctionCallExpression>();
		this.eventNotifications = new LinkedList<EventNotificationExpression>();
		partitionNumbers = new HashSet<Integer>();
	}
		

	public List<SCVariable> getLocalVariablesAndParametersAsSCVars() {
		List<SCVariable> ls = new ArrayList<SCVariable>(parameters.size()
				+ localVariables.size());
		ls.addAll(localVariables);

		for (SCParameter param : parameters) {
			ls.add(param.getVar());
		}
		return ls;
	}

	public List<SCVariable> getLocalVariables() {
		return localVariables;
	}
	
	public void setLocalVariables(List<SCVariable> localVariables) {
		this.localVariables = localVariables;
	}

	/**
	 * returns the name of the function
	 * 
	 * @return name Name of the function
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * returns the list of expressions which represent the body
	 * 
	 * @return List<Expression>
	 */
	public List<Expression> getBody() {
		return body;
	}

	public void setBody(List<Expression> body) {
		this.body = body;
	}

	/**
	 * returns the list of variables which represent the parameters of the
	 * function
	 * 
	 * @return List<SCVariable>
	 */
	public List<SCParameter> getParameters() {
		return parameters;
	}

	public SCParameter getParameter(String name) {
		for (SCParameter par : parameters) {
			if (par.getVar().getName().equals(name)) {
				return par;
			}
		}
		return null;
	}

	/**
	 * adds a parameter to the function. This parameter is added as the last
	 * parameter of the function.
	 * 
	 * @param param
	 */
	public void addParameter(SCParameter param) {
		this.parameters.add(param);
		param.setFunction(this);
	}

	/**
	 * returns the returntype of the function.
	 * 
	 * @return SCTYPES
	 */
	public String getReturnType() {
		return returnType;
	}

	public String getReturnTypeWithoutSize() {
		String tmp = returnType;
		if (tmp.contains("<")) {
			int start = tmp.indexOf("<");
			int end = tmp.indexOf(">") + 1;
			tmp = tmp.substring(0, start) + tmp.substring(end);
		}
		return tmp;
	}

	/**
	 * adds an expression to the end of the body of the function
	 * 
	 * @param exp
	 */
	public void addExpressionAtEnd(Expression exp) {
		exp.setParent(bodyBlock);
		body.add(exp);
	}

	/**
	 * adds an expression to begin of the body of the function
	 * 
	 * @param exp
	 */
	public void addExpressionAtFrond(Expression exp) {
		exp.setParent(bodyBlock);
		body.add(0, exp);
	}

	/**
	 * Adds a variable to the list of variables which are created in the
	 * function
	 * 
	 * @param varToAdd
	 * @return true if the variable already exists; false if it is a new
	 *         variable
	 */
	public boolean addLocalVariable(SCVariable varToAdd) {
		if (existVarWith(varToAdd)) {
			logger.error("Variable with the name {} already exists",
					varToAdd.getName());
			return false;
		} else {
			localVariables.add(varToAdd);
			return true;
		}
	}

	/**
	 * return the variable with the same name
	 * 
	 * @param var_nam
	 * @return the variable or null
	 */
	public SCVariable getLocalVariable(String var_nam) {
		if (existVarWith(new SCSimpleType(var_nam))) {
			for (SCVariable v : localVariables) {
				if (v != null && var_nam != null && v.getName().equals(var_nam)) {
					return v;
				}
			}
		}
		return null;
	}

	public SCVariable getLocalVariableOrParameterAsSCVar(String var_name) {
		for (SCVariable var : localVariables) {
			if (var.getName().equals(var_name)) {
				return var;
			}
		}
		for (SCParameter param : parameters) {
			if (param.getVar() != null) {
				if (param.getVar().getName().equals(var_name)) {
					return param.getVar();
				}
			}
		}
		return null;
	}

	/**
	 * check whether a variable is already in the List or not
	 * 
	 * @param var_nam
	 * @return true if their is a variable in the list with the same name; false
	 *         if not
	 */
	public boolean existVarWith(SCVariable var) {
		for (SCVariable v : localVariables) {
			if (v != null && var != null && v.getName().equals(var.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String ret = "";
		ret += returnType + " " + name + "(";
		for (SCParameter param : parameters) {
			ret += param + ", ";
		}
		if (parameters.size() > 0) {
			ret = ret.substring(0, ret.lastIndexOf(", "));
		}
		ret = ret + "){";
		if (!body.isEmpty()) {
			for (Expression exp : body) {
				// TODO mp: this is just a quickfix, it should never happen but
				// currenty do :-(
				if (exp != null) {
					ret += "\n" + exp.toString();
				}
			}
		}

		ret = ret + "\n}";

		return ret;
	}

	/**
	 * Returns a List of all expressions in the function. NOTE: unlike the
	 * getBody() method this method also returns all subexpressions of
	 * expressions. This method should mainly be used for debug-Issues and for a
	 * fast way to scan all expressions without manipulating them.
	 * 
	 * @return
	 */
	public List<Expression> getAllExpressions() {
		List<Expression> exps = new LinkedList<Expression>();
		for (Expression exp : body) {
			exps.add(exp);
			exps.addAll(exp.getInnerExpressions());
		}
		return exps;
	}

	/**
	 * Replaces all expressions in the method body equal to a first element of a
	 * pair of the list with the second element of the pair.
	 * 
	 * @param replacements
	 */
	public void replaceExpressions(
			List<Pair<Expression, Expression>> replacements) {
		boolean replaced = false;
		for (int i = 0; i < body.size(); i++) {
			replaced = false;
			for (Pair<Expression, Expression> pair : replacements) {
				// yes, we really mean ==
				if (body.get(i) == pair.getFirst()) {
					body.set(i, pair.getSecond());
					pair.getSecond().setParent(bodyBlock);
					replaced = true;
					break;
				}
			}
			if (!replaced) {
				body.get(i).replaceInnerExpressions(replacements);
			}
		}

	}

	public void setReturnType(String returnType) {
		if (returnType == null) {
			this.returnType = VOID_RETURN_TYPE;
		} else {
			this.returnType = returnType;
		}
	}

	public boolean hasReturnType() {
		return (!VOID_RETURN_TYPE.equals(returnType));
	}

	/**
	 * Returns the SCClass this method is contained in. Returns null if the
	 * method is not a memberfunction of an SCClass.
	 * 
	 * @return
	 */
	public SCClass getSCClass() {
		return this.scclass;
	}

	public void setSCClass(SCClass scclass) {
		this.scclass = scclass;
	}

	/**
	 * Sets the consumeTime flag
	 * 
	 * @param flag
	 */
	public void setConsumesTime(boolean flag) {
		this.consumesTime = flag;
	}

	/**
	 * Returns the consumeTime flag
	 * 
	 * @return consumesTime
	 */
	public boolean getConsumesTime() {
		return this.consumesTime;
	}

	/**
	 * Sets the timingAnalyzed flag
	 * 
	 * @param flag
	 */
	public void setTimingAnalyzed(boolean flag) {
		this.timingAnalyzed = flag;
	}

	/**
	 * Returns the timingAnalyzed flag
	 * 
	 * @return timingAnalyzed
	 */
	public boolean getTimingAnalyzed() {
		return this.timingAnalyzed;
	}

	/**
	 * Returns if this function was already analyzed by the TA package.
	 * @return extendedTimingAnalyzed
	 */
	public boolean isExtendedTimingAnalyzed() {
		return extendedTimingAnalyzed;
	}

	/**
	 * Set if this function was handled by the TA package 
	 * @param extendedTimingAnalyzed
	 */
	public void setExtendedTimingAnalyzed(boolean extendedTimingAnalyzed) {
		this.extendedTimingAnalyzed = extendedTimingAnalyzed;
	}

	/**
	 * Sets the isCalled flag
	 * 
	 * @param flag
	 */
	public void setIsCalled(boolean flag) {
		this.isCalled = flag;
	}

	/**
	 * Returns the isCalled flag
	 * 
	 * @return isCalled
	 */
	public boolean getIsCalled() {
		return this.isCalled;
	}
	/** 
	 * Check if the function is calling a function which calls it or itself.
	 * @return Whether a recursive call occurs
	 */
	public boolean hasRecursions() {
		return this.isCalledBy(this);
	}
	
	/**
	 * Check deeply all function calls of the argument function and return
	 * true if it contains a call to the this function.
	 * 
	 * @param callerFunction The Function which might call this
	 * @return whether this is (indirectly) called by the caller
	 */
	public boolean isCalledBy(SCFunction callerFunction) {
		List<FunctionCallExpression> callExprList = callerFunction.getFunctionCalls();
		for (FunctionCallExpression callExpr : callExprList) {
			SCFunction callie = callExpr.getFunction();
			// captain obvious
			if (this.equals(callie)) return true;
			// X calls
			return this.isCalledBy(callie);
		}
		return false;
	}

	/**
	 * Returns the function calls of this Function
	 * 
	 * @return functionCalls
	 */
	public LinkedList<FunctionCallExpression> getFunctionCalls() {
		return this.functionCalls;
	}

	/**
	 * adds a function call
	 * 
	 * @param name
	 *            of the function call, including dereferencing operators (e.g.
	 *            socket.b_transport)
	 */
	public void addFunctionCall(FunctionCallExpression functioncall) {
		this.functionCalls.add(functioncall);
	}

	/**
	 * sets a new list of function calls
	 * 
	 * @param list
	 *            of function calls
	 */
	public void setFunctionCalls(
			LinkedList<FunctionCallExpression> functionCalls) {
		this.functionCalls = functionCalls;
	}
	
	/**
	 * 
	 * @return The partition of the SCSystem the function is called in. 
	 */
	public Set<Integer> getPartitionNumbers() {
		return partitionNumbers;
	}

	/**
	 * Sets the  partition of the SCSystem the function is in
	 * @param i the partition.
	 */
	public void addPartitionNumber(int partition) {
		this.partitionNumbers.add(partition);
	}
	

	public void addEventNotification(EventNotificationExpression evntNotify) {
		this.eventNotifications.add(evntNotify);
	}

	public List<EventNotificationExpression> getEventNotifications() {
		return eventNotifications;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + (consumesTime ? 1231 : 1237);
		result = prime * result
				+ ((functionCalls == null) ? 0 : functionCalls.hashCode());
		result = prime * result + (isCalled ? 1231 : 1237);
		result = prime * result
				+ ((localVariables == null) ? 0 : localVariables.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result
				+ ((returnType == null) ? 0 : returnType.hashCode());
		result = prime * result + (timingAnalyzed ? 1231 : 1237);
		return result;
	}

	@Override
	/**
	 * Note: Does NOT check if partition is the same!
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SCFunction other = (SCFunction) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (consumesTime != other.consumesTime)
			return false;
		if (functionCalls == null) {
			if (other.functionCalls != null)
				return false;
		} else if (!functionCalls.equals(other.functionCalls))
			return false;
		if (isCalled != other.isCalled)
			return false;
		if (localVariables == null) {
			if (other.localVariables != null)
				return false;
		} else if (!localVariables.equals(other.localVariables))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		if (timingAnalyzed != other.timingAnalyzed)
			return false;
		return true;
	}
	
	@Override
	public int compareTo(final SCFunction otherFunc) {
		if (this.getName().equals("update")) {
			return -1;
		} else if (this.isCalledBy(otherFunc)) {
			return -1;
		} else if (otherFunc.isCalledBy(this)) {
			return 1;
		} else {
			return 0;
		}
	}
}
