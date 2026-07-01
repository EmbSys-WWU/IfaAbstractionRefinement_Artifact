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

package de.tub.pes.syscir.engine.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class provides convenience methods for accessing and manipulating
 * {@link org.w3c.dom.Node}s.
 * 
 * @author pockrandt
 * 
 */
public class NodeUtil {

	/**
	 * Returns true if the submitted node contains an attribute of the specified
	 * name, false if not.
	 * 
	 * @param node
	 * @param attribute
	 * @return
	 */
	public static boolean containsAttribute(Node node, String attribute) {
		NamedNodeMap nnm = node.getAttributes();
		return nnm.getNamedItem(attribute) != null;
	}

	/**
	 * Returns the value of the specified attribute of the submitted node or
	 * null if the node does not contain the attribute.
	 * 
	 * @param node
	 * @param attribute
	 * @return
	 */
	public static String getAttributeValueByName(Node node, String attribute) {
		Node atNode = node.getAttributes().getNamedItem(attribute);
		if (atNode == null) {
			return null;
		} else
			return atNode.getNodeValue();
	}

	/**
	 * Returns a map, containing all attributes which are set in the submitted
	 * node mapped to their corresponding values.
	 * 
	 * @param node
	 * @param attributes
	 * @return
	 */
	public static Map<String, String> getAttributeValuesByName(Node node,
			List<String> attributes) {
		Map<String, String> result = new HashMap<String, String>();
		for (String attribute : attributes) {
			String value = getAttributeValueByName(node, attribute);
			if (value != null) {
				result.put(attribute, value);
			}
		}
		return result;
	}

	/**
	 * Returns a string consisting of all fixed attributes of a given node.
	 * Fixed attributes are filename, column, line number and the unique node id
	 * (idref).
	 * 
	 * @param node
	 * @return
	 */
	public static String getFixedAttributes(Node node) {
		List<String> fixedAttributes = new LinkedList<String>();
		fixedAttributes.add("file");
		fixedAttributes.add("line");
		fixedAttributes.add("column");
		fixedAttributes.add("idref");
		
		Map<String, String> attributes = getAttributeValuesByName(node, fixedAttributes);
		String ret = "<";
		for (String key : attributes.keySet()) {
			ret += " " + key + "=\"" + attributes.get(key) + "\"";
		}
		ret += ">";
		return ret;
	}
}
