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
import java.util.List;

import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;

/**
 * this class represents an instance of a port or socket an instance is
 * symbolized by the port and an Instance of a Module it also has lists of
 * connected Channelsm in case of a port, or a list of other portsocketinstances
 * in case of a socket
 * 
 * @author Florian
 * 
 */
public class SCPortInstance implements SCConnectionInterface, Serializable {

	private static final long serialVersionUID = -3854123608062278089L;

	/**
	 * name of the port instance
	 */
	protected String name;
	/**
	 * reference to the port
	 */
	protected SCPort port;
	/**
	 * owner of the port, an instance of the module where the port was declared
	 */
	protected SCClassInstance owner;

	/**
	 * list of channels where the port or socket is connected to
	 */
	protected List<SCKnownType> connectedChannels;

	/**
	 * list of instances, the port is connected to. Contains all self defined
	 * channels
	 */
	protected List<SCClassInstance> connectedClassInstances;

	/**
	 * list of port instances where the port is connected to
	 */
	protected List<SCPortInstance> connectedPortinstances;

	/**
	 * creates a new port instance
	 * 
	 * @param nam
	 *            name of the port
	 * @param p
	 *            original port
	 */
	public SCPortInstance(String nam, SCPort p) {
		name = nam;
		owner = null;
		port = p;
		connectedChannels = new ArrayList<SCKnownType>();
		connectedPortinstances = new ArrayList<SCPortInstance>();
		connectedClassInstances = new ArrayList<SCClassInstance>();
	}

	/**
	 * return the name of the port instance
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * return the original port
	 * 
	 * @return SCPort
	 */
	public SCPort getPortSocket() {
		return port;
	}

	/**
	 * returns the owner of the port instance
	 * 
	 * @return SCClassInstance
	 */
	public SCClassInstance getOwner() {
		return owner;
	}

	/**
	 * sets the owner of the port instance
	 * 
	 * @param own
	 */
	public void addOwner(SCClassInstance own) {
		owner = own;
	}

	/**
	 * adds a class instance to the connected List
	 * 
	 * @param mdlToAdd
	 * @return true if it was added, false if not
	 */
	public boolean addChannel(SCKnownType chnToAdd) {
		if (existChannel(chnToAdd)) {
			return false;
		} else {
			connectedChannels.add(chnToAdd);
			return true;
		}
	}

	/**
	 * adds an PortInstance to the List of connected PortInstances, if there
	 * isn't one with the same name already
	 * 
	 * @param piToAdd
	 *            the PortInstance which should be added
	 * @return true if the PortInstance have been added, false if not
	 */
	public boolean addPortSocketInstance(SCConnectionInterface connIf) {
		if (connIf instanceof SCPortInstance) {
			SCPortInstance piToAdd = (SCPortInstance) connIf;
			if (connectedPortinstances.contains(piToAdd)) {
				return false;
			} else {
				connectedPortinstances.add(piToAdd);
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * adds an ModuleInstance to the List of connected ModuleInstances, if it is
	 * not already added.
	 * 
	 * @param inst
	 *            the ModuleInstance which should be added
	 * @return true if the ModuleInstance have been added, false if not
	 */
	public boolean addInstanceConnection(SCClassInstance inst) {
		if (connectedClassInstances.contains(inst)) {
			return false;
		} else {
			connectedClassInstances.add(inst);
			return true;
		}
	}

	/**
	 * return the instance with the right name
	 * 
	 * @param name
	 *            name of the instance
	 * @return SCKnownType
	 */
	public SCKnownType getChannel(String name) {
		for (SCKnownType p : connectedChannels) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public SCPortInstance getPortSocketInstance(String psi_nam) {
		for (SCPortInstance p : connectedPortinstances) {
			if (p.getName().equals(psi_nam)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * return the list of module instances where this port or socket is
	 * connected to
	 * 
	 * @return List<SCModuleInstance>
	 */
	public List<SCKnownType> getChannels() {
		return connectedChannels;
	}

	/**
	 * returns the list of connected PortInstances
	 * 
	 * @return List<SCPortSCSocketInstance>
	 */
	public List<SCPortInstance> getPortSocketInstances() {
		return connectedPortinstances;
	}

	/**
	 * returns the list of connected ModuleInstances
	 * 
	 * @return List<SCModuleInstance>
	 */
	public List<SCClassInstance> getModuleInstances() {
		return connectedClassInstances;
	}

	/**
	 * checks if a module-instance already exists in the list
	 * 
	 * @param mdl
	 *            module which has to be checked
	 * @return true if it exists, false if not
	 */
	public boolean existChannel(SCKnownType mdl) {
		for (SCKnownType m : connectedChannels) {
			if (m.getName().equals(mdl.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((owner == null) ? 0 : owner.toString().hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
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
		SCPortInstance other = (SCPortInstance) obj;
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
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name + ": " + port.toString();
	}

}
