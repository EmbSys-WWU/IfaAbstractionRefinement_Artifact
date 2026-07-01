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

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.sc_model.SCPROCESSTYPE;
import de.tub.pes.syscir.sc_model.SCProcess;
import de.tub.pes.syscir.sc_model.variables.SCEvent;

/**
 * we clear the arguments of a scmethod, so we can handle the childnode where
 * they are declared with this arguments we create a new scmethod and add it to
 * the curren module
 * 
 * @author Florian
 * 
 */
public class SCMethodBlockTransformer extends AbstractNodeTransformer {
	public void transformNode(Node node, Environment e) {
		e.setSensitivityList(new ArrayList<SCEvent>());
		e.setLastProcessModifier(null);
		e.setLastProcessFunction(null);
		e.setLastProcessName("");

		handleChildNodes(node, e);

		SCProcess mth = new SCProcess(e.getLastProcessName(),
				SCPROCESSTYPE.SCMETHOD, e.getLastProcessFunction(),
				e.getSensitivityList(), e.getLastProcessModifier());
		//this should always work
		e.getCurrentClass().addProcess(mth);

		e.setSensitivityList(new ArrayList<SCEvent>());
		e.setLastProcessModifier(null);
		e.setLastProcessFunction(null);
		e.setLastProcessName("");
	}
}
