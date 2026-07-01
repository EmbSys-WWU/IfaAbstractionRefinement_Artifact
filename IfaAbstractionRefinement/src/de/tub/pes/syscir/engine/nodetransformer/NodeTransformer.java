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

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;

/**
 * 
 * 
 * A transformer in this context is a class that is able to process specific XML
 * nodes that represent nodes in a SystemC abstract syntax tree of KaSCPar
 * format. To achieve that, every transformer class implements this interface by
 * containing an implementation of the <code>transformNode</code> method. Every
 * supported node type is assigned to a specialized transformer class.
 * <p>
 * The abstract class {@link AbstractNodeTransformer} extends this interface with
 * further methods and variables which are useful in all transformer classes.
 * Deriving from that class instead implementing this interface is recommended.
 * 
 * @author Joachim Fellmuth
 * 
 */
public interface NodeTransformer {

	/**
	 * This method is intended to contain or trigger all the transformation
	 * logic of an implementing transformer class. It is triggered for a
	 * specific node and thought to take all information out of the node and
	 * update the internal models with it. Additional, transient information
	 * such as the current location in the XML tree, are stored in the
	 * environment, which is singleton, and passed to all transformers. The keys
	 * of the transient object mappings in the environment can be found in
	 * {@link AbstractNodeTransformer}. Values in that map can be of any type. That
	 * means that objects have to be obtained from the map through type-unsafe
	 * casts, which requires discipline in documenting and defining new keys and
	 * value types in this map.
	 * <p>
	 * The transformer method is also responsible for the processing of the
	 * child nodes, which are contained by the given node. This can best be done
	 * by calling this method on the transformers associated with the nodes
	 * types. It is also possible to obtain the required information directly
	 * from the child nodes. However, this should only be done in cases where it
	 * is absolutely safe and useful to do so. Using a specific transformer
	 * classes for each node type helps to enhance the modularity, flexibility
	 * and exchangeability of the system. Static coupling of nodes over more
	 * than one hierarchy level can complicate further development and the
	 * adjustment to newer versions of SystemC and KaSCPar.
	 * 
	 * @param node
	 *            XML KaSCPar node to process.
	 * @param environment
	 *            Map for storing transient information in parsing process
	 * @param ta
	 *            TA model to store information that is complete and does not
	 *            have to be in the intermediate SystemC representation. TA
	 *            method templates and their contents are the most prominent
	 *            example.
	 * @param scModel
	 *            Intermediate internal representation of the SystemC model,
	 *            basically contains the class headers.
	 */
	public void transformNode(Node node, Environment e);

}
