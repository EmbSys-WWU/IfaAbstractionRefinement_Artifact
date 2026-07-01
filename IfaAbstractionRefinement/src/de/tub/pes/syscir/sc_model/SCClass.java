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
import java.util.LinkedList;
import java.util.List;

import de.tub.pes.syscir.sc_model.variables.SCArray;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCEvent;

/**
 * Represents all classes (and structs) of the SystemC model. We do not
 * differentiate between structs and scmodules. Instead all classes can have
 * ports and sockets. This eases handling of structs and modules.
 * 
 * @author pockrandt
 * 
 */
public class SCClass implements Serializable, Cloneable {

	private static final long serialVersionUID = 8483500264938392358L;

	/**
	 * name of the SCClass (e.g. data)
	 */
	protected String name = "";

	/**
	 * list of variable which represent the members of the SCClass
	 */
	protected List<SCVariable> members = null;

	/**
	 * list of SCFunctions which represent the memberfunctions of the SCClass
	 */
	protected List<SCFunction> memberFunctions = null;

	/**
	 * a reference to the constructor-function, null if there is no constructor
	 */
	protected SCFunction constructor = null;

	/**
	 * list of SCStructs from which this struct inherits
	 */
	protected List<SCClass> inheritFrom;

	/**
	 * Determines if the struct is a channel (not NO_CHANNEL), and what kind of
	 * channel.
	 */
	protected SCCHANNELTYPE channelType;

	/**
	 * This flag describes whether the given SCClass is defined in an external
	 * source (true) or in the global xml file. This is needed to prevent
	 * multiple parsing runs for the same SCClass.
	 */
	protected boolean isExternal;

	/**
	 * All instances of SCClasses the class contains. This list is only for
	 * convenience as all instances are also members of the SCClass.
	 */
	protected List<SCClassInstance> instances;

	/**
	 * All ports and sockets of the SCClass. Only SC_MODULEs can have ports and
	 * sockets, so this list is empty for a standard C++ class.
	 */
	protected List<SCPort> portsSockets;

	/**
	 * All processes of the SCClass. Only SC_MODULEs can have ports and sockets,
	 * so this list is empty for a standard C++ class.
	 */
	protected List<SCProcess> processes;

	/**
	 * All ports and sockets of the SCClass. Only SC_MODULEs can have ports and
	 * sockets, so this list is empty for a standard C++ class.
	 */
	protected List<SCEvent> events;

	public SCClass(String name) {
		this.name = name;
		this.channelType = SCCHANNELTYPE.NO_CHANNEL;
		this.constructor = null;
		this.events = new LinkedList<SCEvent>();
		this.inheritFrom = new LinkedList<SCClass>();
		this.instances = new LinkedList<SCClassInstance>();
		this.isExternal = false;
		this.memberFunctions = new LinkedList<SCFunction>();
		this.members = new LinkedList<SCVariable>();
		this.portsSockets = new LinkedList<SCPort>();
		this.processes = new LinkedList<SCProcess>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SCVariable> getMembers() {
		return members;
	}

	/**
	 * Returns the member with the specified name or null if no member with the
	 * name was found. As we do not support shadowing of variables all variable
	 * names should be unique and therefore there exist only one variable with
	 * the specified name per class.
	 * 
	 * @param name
	 * @return
	 */
	public SCVariable getMemberByName(String name) {
		for (SCVariable var : members) {
			if (var.getName().equals(name)) {
				return var;
			}
		}
		return null;
	}

	/**
	 * Adds the specified variable to the list of members if it is not already
	 * part of the list. Returns true if the variable was added during this
	 * function call, false if not.
	 * 
	 * @param memberFunction
	 * @return
	 */
	public boolean addMember(SCVariable member) {
		if (!members.contains(member)) {
			members.add(member);
			return true;
		}
		return false;
	}

	public void setMembers(List<SCVariable> members) {
		this.members = members;
	}

	public List<SCFunction> getMemberFunctions() {
		return memberFunctions;
	}

	/**
	 * Returns the member function with the specified name or null if no
	 * function with the specified name was found. As we do not support
	 * overloading all function names should be unique per module and therefore
	 * we can return the first occurence of the name.
	 * 
	 * @param name
	 * @return
	 */
	public SCFunction getMemberFunctionByName(String name) {
		for (SCFunction fun : memberFunctions) {
			if (fun.getName().equals(name)) {
				return fun;
			}
		}

		return null;
	}

	/**
	 * Adds the specified function to the list of member functions if it is not
	 * already part of the list. Returns true if the function was added during
	 * this function call, false if not.
	 * 
	 * @param memberFunction
	 * @return
	 */
	public boolean addMemberFunction(SCFunction memberFunction) {
		if (!memberFunctions.contains(memberFunction)) {
			memberFunctions.add(memberFunction);
			memberFunction.setSCClass(this);
			return true;
		}
		return false;
	}

	public void setMemberFunctions(List<SCFunction> memberFunctions) {
		this.memberFunctions = memberFunctions;
	}

	public SCFunction getConstructor() {
		return constructor;
	}

	public void setConstructor(SCFunction constructor) {
		this.constructor = constructor;
	}

	public List<SCClass> getInheritFrom() {
		return inheritFrom;
	}

	public void setInheritFrom(List<SCClass> inheritFrom) {
		this.inheritFrom = inheritFrom;
	}

	public void addInheritFrom(SCClass superClass) {
		this.inheritFrom.add(superClass);
	}

	public SCCHANNELTYPE getChannelType() {
		return channelType;
	}

	public void setChannelType(SCCHANNELTYPE channelType) {
		this.channelType = channelType;
	}

	public boolean isExternal() {
		return isExternal;
	}

	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}

	public List<SCClassInstance> getInstances() {
		return instances;
	}

	/**
	 * Returns the class instance with the specified name or null, if no class
	 * instance with this name exists.
	 * 
	 * @param name
	 * @return
	 */
	public SCClassInstance getInstanceByName(String name) {
		for (SCClassInstance ci : instances) {
			if (ci.getName().equals(name)) {
				return ci;
			}
		}

		return null;
	}

	public void addInstance(SCClassInstance instance) {
		this.instances.add(instance);
	}

	public boolean removeInstance(SCClassInstance instance) {
		return instances.remove(instance);
	}

	public List<SCPort> getPortsSockets() {
		return portsSockets;
	}

	public SCPort getPortSocketByName(String name) {
		for (SCPort ps : portsSockets) {
			if (ps.getName().equals(name)) {
				return ps;
			}
		}
		return null;
	}

	public void setPortsSockets(List<SCPort> portsSockets) {
		this.portsSockets = portsSockets;
	}

	/**
	 * Adds the specified port or socket to the list of known ports and sockets
	 * if it is not already part of the list. Returns true if the port or socket
	 * was added to the list during this function call, false if not.
	 * 
	 * @param portSocket
	 * @return
	 */
	public boolean addPortSocket(SCPort portSocket) {
		if (!portsSockets.contains(portSocket)) {
			portsSockets.add(portSocket);
			return true;
		}
		return false;
	}

	public List<SCProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(List<SCProcess> processes) {
		this.processes = processes;
	}

	/**
	 * Adds the specified process to the list of known processes if it is not
	 * already part of it. Returns true of the process was added to the list
	 * during this function call, false if not.
	 * 
	 * @param process
	 * @return
	 */
	public boolean addProcess(SCProcess process) {
		if (!processes.contains(process)) {
			processes.add(process);
			return true;
		}
		return false;
	}

	public List<SCEvent> getEvents() {
		return events;
	}

	/**
	 * Returns the event with the specified name if it exists or null if not. As
	 * we do not support shadowing of variables all events should have a unique
	 * name per module.
	 * 
	 * @param name
	 * @return
	 */
	public SCEvent getEventByName(String name) {
		for (SCEvent ev : events) {
			if (ev.getName().equals(name)) {
				return ev;
			}
		}

		return null;
	}

	public void setEvents(List<SCEvent> events) {
		this.events = events;
	}

	/**
	 * Adds the specified event to the list of events if it is not already part
	 * of the list. Returns true if the event was added during this function
	 * call, false if not.
	 * 
	 * @param event
	 * @return
	 */
	public boolean addEvent(SCEvent event) {
		if (!events.contains(event)) {
			events.add(event);
			return true;
		}
		return false;
	}

	/**
	 * Makes the SCCLass a hierarchical channel. As it is possible that a struct
	 * inherits from sc_prim_channel and sc_interface and is therefore a
	 * primitive channel this method will not do anything if the struct is
	 * already a primitive channel.
	 */
	public void setHierarchicalChannel() {
		if (channelType != SCCHANNELTYPE.PRIMITIVE_CHANNEL) {
			channelType = SCCHANNELTYPE.HIERARCHICAL_CHANNEL;
		}
	}

	/**
	 * Makes the SCClass a primitive channel. This method blocks the use of the
	 * setHierarchicalChannel() method.
	 */
	public void setPrimitiveChannel() {
		channelType = SCCHANNELTYPE.PRIMITIVE_CHANNEL;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((channelType == null) ? 0 : channelType.hashCode());
		result = prime * result
				+ ((constructor == null) ? 0 : constructor.hashCode());
		result = prime * result + ((events == null) ? 0 : events.hashCode());
		result = prime * result
				+ ((inheritFrom == null) ? 0 : inheritFrom.hashCode());
		result = prime * result
				+ ((instances == null) ? 0 : instances.hashCode());
		result = prime * result + (isExternal ? 1231 : 1237);
		result = prime * result
				+ ((memberFunctions == null) ? 0 : memberFunctions.hashCode());
		result = prime * result + ((members == null) ? 0 : members.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((portsSockets == null) ? 0 : portsSockets.hashCode());
		result = prime * result
				+ ((processes == null) ? 0 : processes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SCClass)) {
			return false;
		}
		SCClass other = (SCClass) obj;
		if (channelType != other.channelType) {
			return false;
		}
		if (constructor == null) {
			if (other.constructor != null) {
				return false;
			}
		} else if (!constructor.equals(other.constructor)) {
			return false;
		}
		if (events == null) {
			if (other.events != null) {
				return false;
			}
		} else if (!events.equals(other.events)) {
			return false;
		}
		if (inheritFrom == null) {
			if (other.inheritFrom != null) {
				return false;
			}
		} else if (!inheritFrom.equals(other.inheritFrom)) {
			return false;
		}
		if (instances == null) {
			if (other.instances != null) {
				return false;
			}
		} else if (!instances.equals(other.instances)) {
			return false;
		}
		if (isExternal != other.isExternal) {
			return false;
		}
		if (memberFunctions == null) {
			if (other.memberFunctions != null) {
				return false;
			}
		} else if (!memberFunctions.equals(other.memberFunctions)) {
			return false;
		}
		if (members == null) {
			if (other.members != null) {
				return false;
			}
		} else if (!members.equals(other.members)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (portsSockets == null) {
			if (other.portsSockets != null) {
				return false;
			}
		} else if (!portsSockets.equals(other.portsSockets)) {
			return false;
		}
		if (processes == null) {
			if (other.processes != null) {
				return false;
			}
		} else if (!processes.equals(other.processes)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the number of processes of this class which need initialization
	 * (= the *dont_initialize* flag is not set). This is done recursively for
	 * all members, which are class instances.
	 * 
	 * @return
	 */
	public int countInitProcesses() {
		int count = 0;
		for (SCProcess pro : processes) {
			if (!pro.getModifier().contains(SCMODIFIER.DONTINITIALIZE)) {
				count++;
			}
		}
		for (SCVariable member : getMembers()) {
			if (member instanceof SCClassInstance) {
				count += ((SCClassInstance) member).getSCClass()
						.countInitProcesses();
			}
		}
		if (constructor != null) {
			for (SCVariable member : constructor.getLocalVariables()) {
				if (member instanceof SCClassInstance) {
					count += ((SCClassInstance) member).getSCClass()
							.countInitProcesses();
				}
			}
		}
		return count;
	}

	@Override
	public String toString() {
		String ret = "struct " + name + " ";
		if (!inheritFrom.isEmpty()) {
			ret += ": ";
			for (SCClass in : inheritFrom) {
				ret += in.getName() + ", ";
			}
			ret = ret.substring(0, ret.lastIndexOf(", ")) + " ";
		}
		ret += "{\n";
		for (SCPort ps : portsSockets) {
			ret += ps.toString() + "\n";
		}
		for (SCVariable var : members) {
			ret += var.toString() + "\n";
		}
		for (SCFunction fun : memberFunctions) {
			ret += fun.toString() + "\n";
		}
		ret += "}";

		return ret;

	}

	/**
	 * Returns a string representation of the sc_class containing only the name
	 * of the class and its members but neither sockets nor functions.
	 * 
	 * @return
	 */
	public String toStringWithoutFunctions() {
		String ret = "struct " + this.name + "{\n";
		for (SCVariable variable : getMembers()) {
			if (!(variable instanceof SCArray))
				ret += variable.getDeclarationString() + ";\n";
		}

		ret += "};";
		return ret;
	}

	/**
	 * Returns true if the class is a sc_module. Classes are sc_modules if they
	 * inherit from sc_module, directly or indirectly (e.g. by inheriting from a
	 * class which inherits from sc_module).
	 * 
	 * @return
	 */
	public boolean isSCModule() {
		for (SCClass cl : inheritFrom) {
			if (cl.getName().equals("sc_module") || cl.isSCModule()) {
				return true;
			}
		}
		return false;
	}

	public boolean isPrimitiveChannel() {
		return channelType == SCCHANNELTYPE.PRIMITIVE_CHANNEL;
	}

	public boolean isHirarchicalChannel() {
		return channelType == SCCHANNELTYPE.HIERARCHICAL_CHANNEL;
	}

	public boolean isChannel() {
		return channelType != SCCHANNELTYPE.NO_CHANNEL;
	}

	public boolean hasInstances() {
		return !this.instances.isEmpty();
	}

	public String toStringWithoutFunctionsWithArrays() {
		String ret = "struct " + this.name + "{\n";
		for (SCVariable variable : getMembers()) {
				ret += variable.getDeclarationString() + ";\n";
		}
		ret += "};";
		return ret;
	}
	
	public SCClass createClone() {
		try {
			return (SCClass) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
