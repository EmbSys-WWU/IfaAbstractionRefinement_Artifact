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

import de.tub.pes.syscir.sc_model.variables.SCClassInstance;

public interface SCConnectionInterface {

	/**
	 * return the name of the port- or socket-instance
	 * 
	 * @return String
	 */
	public String getName();

	/**
	 * return the orginalport or orginalsocket
	 * 
	 * @return SCPortSCSocket
	 */
	public SCPort getPortSocket();

	/**
	 * returns the owner of the port-socket-instance
	 * 
	 * @return SCModuleInstance
	 */
	public SCClassInstance getOwner();

	/**
	 * sets the owner of the port-socket-instance
	 * 
	 * @param own
	 */
	public void addOwner(SCClassInstance own);

	/**
	 * adds an PortSocketInstance to the List of connected PortSocketInstances,
	 * if their isn't one with the same name
	 * 
	 * @param psiToAdd
	 *            the PortSocketInstance which should be added
	 * @return true if the PortSocketInstance have been added, false if not
	 */
	public boolean addPortSocketInstance(SCConnectionInterface psiToAdd);

	public SCConnectionInterface getPortSocketInstance(String psi_nam);

}
