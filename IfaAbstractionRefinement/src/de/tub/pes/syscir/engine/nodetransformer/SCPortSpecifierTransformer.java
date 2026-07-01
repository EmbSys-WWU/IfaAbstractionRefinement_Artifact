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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.TransformerFactory;
import de.tub.pes.syscir.engine.typetransformer.KnownTypeTransformer;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCPORTSCSOCKETTYPE;
/**
 * first we get the portType
 * according to this type, we set the lastPortSocketType in the environment and handle the childnodes
 * afterwards we create a new known-type because the channel-types are knowntypes
 * @author Florian
 *
 */
public class SCPortSpecifierTransformer extends AbstractNodeTransformer {
	
	private static Logger logger = LogManager.getLogger(SCPortSpecifierTransformer.class.getName());
	
	public void transformNode(Node node, Environment e){
		String portType = NodeUtil.getAttributeValueByName(node, "name");
		e.setFoundMemberType("PortSocket");
		handleChildNodes(node, e);
		e.setLastPortSocketType(null);
		
		if(portType.equals("sc_port")) {
			e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_PORT);
			return;
		} else if(portType.equals("sc_in") ){
			e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_IN);
			portType = "sc_signal";
			handleChildNodes(node, e);
		} else if (portType.equals("sc_out") ){
			e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_OUT);
			portType = "sc_signal";
			handleChildNodes(node, e);
		} else if (portType.equals("sc_inout")) {
			e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_INOUT);
			portType = "sc_signal";
			handleChildNodes(node, e);
		} else if(portType.equals("sc_fifo_in")){
			e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_FIFO_IN);
			portType = "sc_fifo";
			handleChildNodes(node, e);
		} else if(portType.equals("sc_fifo_out") ) {
			e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_FIFO_OUT);
			portType = "sc_fifo";
			handleChildNodes(node, e);
		}	else if (portType.equals("tlm_initiator_socket") ||
					 portType.equals("tlm_target_socket")) {
			e.setLastPortSocketType(SCPORTSCSOCKETTYPE.SC_SOCKET);
			portType = "tlm_fw_bw_if";
			handleChildNodes(node, e);
		}  else {
			e.setLastPortSocketType(null);
			logger.error("{}: {}: Unknown port type.",NodeUtil.getFixedAttributes(node),portType);
			return;
		}
		
		
		
		KnownTypeTransformer tpTrans = e.getTransformerFactory().getTypeTransformer(portType, e);
	    if(tpTrans != null) {
	      tpTrans.createType(e);
	    } else {
	      logger.error("{}: Configuration error: Can not find implementation for type {}."
	    		  ,NodeUtil.getFixedAttributes(node),portType);
	    }
	}
	
}
