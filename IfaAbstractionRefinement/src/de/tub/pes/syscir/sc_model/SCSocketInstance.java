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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;

/**
 * this class represents an instance of a port or socket an instance is
 * symbolized by the port and an Instance of a Module it also has lists of
 * connected Channelsm in case of a port, or a list of other portsocketinstances
 * in case of a socket
 * 
 * @author Florian
 * 
 */
public class SCSocketInstance implements SCConnectionInterface, Serializable {

	private static final long serialVersionUID = -3854123608062278089L;

	/**
	 * name of the socket instance
	 */
	protected String name;
	/**
	 * reference to the socket
	 */
	protected SCSocket socket;
	/**
	 * owner of the socket, an instance of the module where the socket was
	 * declared
	 */
	protected SCClassInstance owner;

	/**
	 * ModuleInstance where the socket functions are found, usual the owner
	 */
	protected SCClass socketFunctionLocation;
	/**
	 * List of functions called from this socket;
	 */
	protected List<FunctionCallExpression> unresolvedFunctionCalls;

	/**
	 * list of socket instances where the socket is connected to
	 */
	protected List<SCSocketInstance> connectedSocketinstances;

	/**
	 * creates a new socket
	 * 
	 * @param nam
	 *            name of the socket
	 * @param p
	 *            original socket
	 */
	public SCSocketInstance(String nam, SCSocket p) {
		name = nam;
		owner = null;
		socket = p;
		socketFunctionLocation = null;
		unresolvedFunctionCalls = new ArrayList<FunctionCallExpression>();
		connectedSocketinstances = new ArrayList<SCSocketInstance>();
	}

	/**
	 * return the name of the socket instance
	 * 
	 * @return String
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * return the original socket
	 * 
	 * @return SCSocket
	 */
	@Override
	public SCPort getPortSocket() {
		return socket;
	}

	/**
	 * returns the owner of the socket instance
	 * 
	 * @return SCClassInstance
	 */
	@Override
	public SCClassInstance getOwner() {
		return owner;
	}

	/**
	 * sets the owner of the socket instance
	 * 
	 * @param own
	 */
	@Override
	public void addOwner(SCClassInstance own) {
		owner = own;
	}

	/**
	 * Sets the submitted class instance as the location for all socket methods.
	 * A call to a socket method is always forwarded to this class instance.
	 * WARNING: once set the instance is immutable and the method returns false
	 * if the setting fails.
	 * 
	 * @param loc
	 * @return
	 */
	public boolean setSocketFunctionLocation(SCClass loc) {
		if (socketFunctionLocation != null) {
			return false;
		} else {
			socketFunctionLocation = loc;
			// resolve unresolved function calls
			if (!connectedSocketinstances.isEmpty()) {
				for (SCSocketInstance psi : connectedSocketinstances) {
					for (SCFunction supportedFunction : loc
							.getMemberFunctions()) {
						psi.resolveFunctionCall(supportedFunction);
					}
				}
			}
			return true;
		}
	}

	/**
	 * Adds a function call to the list of unresolved calls called from this
	 * socket instance and tries to resolve them.
	 * 
	 * @param name
	 * @param fce
	 */
	public void addCalledFunction(String name, FunctionCallExpression fce) {
		// lookup called function in connected socktes
		boolean resolved = false;
		if (!connectedSocketinstances.isEmpty()) {
			for (SCSocketInstance psi : connectedSocketinstances) {
				if (psi.getSocketFunctionLocation() != null) {
					for (SCFunction targetScf : psi.getSocketFunctionLocation()
							.getMemberFunctions()) {
						if (targetScf.getName().equals(name)) {
							fce.setFunction(targetScf);
							targetScf.setIsCalled(true);
							// we currently only support 1:1 connections
							resolved = true;
							break;
						}
					}
				}
			}
		}
		// else put in table
		if (!resolved) {
			unresolvedFunctionCalls.add(fce);
		}
	}

	/**
	 * Returns the list of unresolved function calls
	 * 
	 * @return
	 */
	public List<FunctionCallExpression> getUnresolvedFunctionCalls() {
		return unresolvedFunctionCalls;
	}

	/**
	 * Tries to resolve a function call by setting the called function in all
	 * matching unresolved function calls.
	 * 
	 * @param scf
	 */
	public void resolveFunctionCall(SCFunction scf) {
		List<FunctionCallExpression> resolved = new LinkedList<FunctionCallExpression>();
		for (FunctionCallExpression unresFce : unresolvedFunctionCalls) {
			if (unresFce.getFunction().getName().equals(scf.getName())) {
				unresFce.setFunction(scf);
				resolved.add(unresFce);
			}
		}
		unresolvedFunctionCalls.removeAll(resolved);
	}

	/**
	 * adds an SocketInstance to the List of connected SocketInstances, if their
	 * isn't one with the same name
	 * 
	 * @param siToAdd
	 *            the SocketInstance which should be added
	 * @return true if the SocketInstance have been added, false if not
	 */
	@Override
	public boolean addPortSocketInstance(SCConnectionInterface connIf) {
		if (connIf instanceof SCSocketInstance) {
			SCSocketInstance siToAdd = (SCSocketInstance) connIf;

			if (connectedSocketinstances.contains(siToAdd)) {
				return false;
			} else {
				connectedSocketinstances.add(siToAdd);
				// resolve function calls in newly connected psi
				if (socketFunctionLocation != null) {
					for (SCFunction supportedFunction : socketFunctionLocation
							.getMemberFunctions()) {
						siToAdd.resolveFunctionCall(supportedFunction);
					}
				}

				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * return the module-instance with the right name
	 * 
	 * @param mdl_nam
	 *            name of the module-instance
	 * @return SCModuleInstance
	 */
	public SCClass getSocketFunctionLocation() {
		return socketFunctionLocation;
	}

	@Override
	public SCSocketInstance getPortSocketInstance(String psi_nam) {
		for (SCSocketInstance p : connectedSocketinstances) {
			if (p.getName().equals(psi_nam)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * returns the list of connected PortSocketInstances
	 * 
	 * @return List<SCPortSCSocketInstance>
	 */
	public List<SCSocketInstance> getPortSocketInstances() {
		return connectedSocketinstances;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((owner == null) ? 0 : owner.toString().hashCode());
		result = prime * result + ((socket == null) ? 0 : socket.hashCode());
		result = prime
				* result
				+ ((socketFunctionLocation == null) ? 0
						: socketFunctionLocation.toString().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SCSocketInstance other = (SCSocketInstance) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.toString().equals(other.owner.toString()))
			return false;
		if (socket == null) {
			if (other.socket != null)
				return false;
		} else if (!socket.equals(other.socket))
			return false;
		if (socketFunctionLocation == null) {
			if (other.socketFunctionLocation != null)
				return false;
		} else if (!socketFunctionLocation.toString().equals(
				other.socketFunctionLocation.toString()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name + ": " + socket.toString();
	}

}
