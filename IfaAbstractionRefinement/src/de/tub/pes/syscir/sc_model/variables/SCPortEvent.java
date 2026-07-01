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

package de.tub.pes.syscir.sc_model.variables;

import java.util.ArrayList;

import de.tub.pes.syscir.sc_model.SCPort;

/**
 * this class represents a Portevent, which is required for sensitivity on ports
 * or sockets
 * 
 * @author Florian
 * 
 */
public class SCPortEvent extends SCEvent {

	private static final long serialVersionUID = -1980590687800171057L;

	private SCPort port_socket;
	private String eventType;

	public SCPortEvent(String nam, SCPort ps, String eventType) {
		super(nam, false, false, new ArrayList<String>());

		this.port_socket = ps;
		this.eventType = eventType;

	}

	public SCPortEvent(String nam, SCPort ps) {
		this(nam, ps, "default_event");
	}

	public String getEventType() {
		return eventType;
	}

	public SCPort getPort() {
		return port_socket;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result
				+ ((port_socket == null) ? 0 : port_socket.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SCPortEvent other = (SCPortEvent) obj;
		if (eventType == null) {
			if (other.eventType != null)
				return false;
		} else if (!eventType.equals(other.eventType))
			return false;
		if (port_socket == null) {
			if (other.port_socket != null)
				return false;
		} else if (!port_socket.equals(other.port_socket))
			return false;
		return true;
	}
	

}
