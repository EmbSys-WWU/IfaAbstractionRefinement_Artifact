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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCVariable;

/**
 * This class represents a payload event queue in SystemC.
 * 
 * @author Florian
 * 
 */
public class SCPeq extends SCVariable {

	private static final long serialVersionUID = 3140481869471730100L;

	private static Logger logger = LogManager.getLogger(SCPeq.class.getName());

	protected SCClass owner = null;
	protected List<String> subtypes;

	protected SCFunction callback;

	public SCPeq(String n, SCClass owner, List<String> type, boolean stat,
			boolean cons, List<String> other_mods) {
		super(n);
		this.type = "peq_with_cb_and_phase";
		this.owner = owner;
		this.subtypes = type;
		this._static = stat;
		this._const = cons;
		this.otherModifiers = other_mods;
	}

	/**
	 * Constructor to initiate a new SCPeq with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param old SCVariable which will be copied
	 * @param newName name that will be set in new instantiation
	 */
	public SCPeq(SCPeq old, String newName) {
		super(old, newName);
		this.owner = null;
		this.subtypes = null;
		this.callback = null;
	}
	
	public boolean setCallback(SCFunction callback) {
		if (this.callback == null) {
			this.callback = callback;
			return true;
		} else {
			return false;
		}
	}

	public SCFunction getCallback() {
		return this.callback;
	}

	public List<String> getSubtypes() {
		return new ArrayList<String>(subtypes);
	}

	public SCClass getOwner() {
		return this.owner;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((owner == null) ? 0 : owner.getName().hashCode());
		result = prime * result
				+ ((subtypes == null) ? 0 : subtypes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof SCPeq)) {
			return false;
		}
		SCPeq other = (SCPeq) obj;
		if (owner == null) {
			if (other.owner != null) {
				return false;
			}
		} else if (!owner.getName().equals(other.owner.getName())) {
			return false;
		}
		if (subtypes == null) {
			if (other.subtypes != null) {
				return false;
			}
		} else if (!subtypes.equals(other.subtypes)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the type of the phase of the peq. This should be the same for
	 * each PEQ.
	 * 
	 * @return
	 */
	public String getPhaseType() {
		return "int";
	}

	/**
	 * Returns the type of the payload of the peq. This differs between
	 * different peqs, depending on the peq-declaration.
	 * 
	 * @return
	 */
	public String getPayloadType() {
		if (subtypes.size() > 0) {
			return subtypes.get(0);
		} else {
			logger.error(
					"Could not derive the type of the payload of the peq {}.",
					this.name);
			return "";
		}

	}

	/**
	 * initiates a new SCPeq with a different name but the same internal state EXCEPT:
	 * all linked objects will be set to null to avoid changes by accident
	 * @param newName newName name that will be set in new instantiation
	 * @return new SCPeq
	 */
	@Override
	public SCVariable flatCopyVariableWitNewName(String newName) {
		return new SCPeq(this, newName);
	}

}
