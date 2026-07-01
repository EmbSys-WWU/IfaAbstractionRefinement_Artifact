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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * this class represents the ports and sockets in systemC it have an name, a
 * connection type and a type. This type tells which kind of data is send
 * through this port or socket.
 * 
 * @author Florian
 * 
 */
public class SCSocket extends SCPort implements Serializable {

	private static final long serialVersionUID = 8949874004040514703L;

	private static transient final Logger logger = LogManager
			.getLogger(SCSocket.class.getName());

	/**
	 * creats a new socket
	 * 
	 * @param nam
	 *            name of the socket
	 * @param t
	 *            datatype of the socket
	 * @param ct
	 *            specifier whether its an input-port or an output-port or both,
	 *            or a socket connection specifier
	 */
	public SCSocket(String nam, String t, SCPORTSCSOCKETTYPE ct) {
		super(nam, t, ct);
		if (ct != SCPORTSCSOCKETTYPE.SC_SOCKET) {
			logger.warn(
					"Created socket '{}' with type other than SC_SOCKET: {}",
					nam, ct);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((con_type == null) ? 0 : con_type.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		SCSocket other = (SCSocket) obj;
		if (con_type != other.con_type)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return con_type.toString().toLowerCase() + "<" + type + "> " + name;
	}

}
