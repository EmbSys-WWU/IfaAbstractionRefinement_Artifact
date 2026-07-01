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

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.TransformerFactory;

public abstract class AbstractNodeTransformer implements NodeTransformer {

	@Override
	public void transformNode(Node node, Environment e) {
		if (node != null) {
			NodeTransformer t = e.getTransformerFactory().getNodeTransformer(node);
			t.transformNode(node, e);
		}
	}

	public void handleChildNodes(Node node, Environment e) {
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node child = nodes.item(i);
			String name = child.getNodeName();
			NodeTransformer t = e.getTransformerFactory().getNodeTransformer(child);
			t.transformNode(child, e);
		}
	}

	/**
	 * Finds a child node of the given node that has the given node name.
	 * Returns null if it cannot be found.
	 * 
	 * @param node
	 * @param name
	 * @return Child node with given name
	 */
	protected Node findChildNode(Node node, String name) {
		boolean useRecursion = false;
		return recursiveFindChildNode(node, name, useRecursion);
	}

	/**
	 * (Recusively) Finds a child node of the given node that has the given
	 * node name.  Returns null if it cannot be found.
	 * 
	 * @param node
	 * @param name
	 * @param useRecursion
	 * @return Child node with given name
	 */
	protected Node recursiveFindChildNode(Node node, String name, boolean useRecursion) {
		if (node != null) {
			NodeList nodes = node.getChildNodes();
			Node child;
			Node grandChild;
			for (int i = 0; i < nodes.getLength(); i++) {
				child = nodes.item(i);
				if (child.getNodeName().equals(name)) {
					return child;
				} else if (useRecursion) {
					grandChild = recursiveFindChildNode(child, name, useRecursion);
					if (grandChild != null) {
						return grandChild;
					}
				}
			}
		}
		return null;
	}

	protected Node recursiveFindChildNode(Node node, String name) {
		boolean useRecursion = true;
		return recursiveFindChildNode(node, name, useRecursion);
	}

	protected List<Node> getAllChildNodes(Node node) {
		NodeList nodes = node.getChildNodes();
		LinkedList<Node> list = new LinkedList<Node>();
		for (int i = 0; i < nodes.getLength(); i++) {
			list.addLast(nodes.item(i));
		}
		return list;
	}

	/**
	 * Finds the first real child node that has NOT the given name. A real child
	 * node is one that is not just a text entry. More specifically, it is one
	 * surrounded by XML tags.
	 * 
	 * @param node
	 * @param name
	 * @return
	 */
	protected Node findFirstChildNot(Node node, String name) {
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node child = nodes.item(i);
			if (!child.getNodeName().equals(name)
					&& !child.getNodeName().equals("#text")) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Finds all child nodes with the given name and returns them as a list. If
	 * no one can be found, it returns an empty list of nodes.
	 * 
	 * @param node
	 * @param name
	 * @return
	 */
	protected List<Node> findChildNodes(Node node, String name) {
		boolean useRecursion = false;
		return recursiveFindChildNodes(node, name, useRecursion);
	}

	/**
	 * (Recursively) Finds all child nodes with the given name and returns them
	 * as a list.  If no one can be found, it returns an empty list of nodes.
	 * 
	 * @param node
	 * @param name
	 * @param useRecursion
	 * @return
	 */
	protected List<Node> recursiveFindChildNodes(Node node, String name, boolean useRecursion) {
		NodeList nodes = node.getChildNodes();
		LinkedList<Node> list = new LinkedList<Node>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node child = nodes.item(i);
			if (child != null) {
				if (child.getNodeName().equals(name)) {
					list.addLast(child);
				}
				if (useRecursion) {
					list.addAll(recursiveFindChildNodes(child, name, useRecursion));
				}
			}
		}
		return list;
	}

	protected List<Node> recursiveFindChildNodes(Node node, String name) {
		boolean useRecursion = true;
		return recursiveFindChildNodes(node, name, useRecursion);
	}

	/**
	 * Finds all real child nodes of the given node. Real nodes are those that
	 * are not text entry nodes, and which are surrounded by XML tags.
	 * 
	 * @param node
	 * @return
	 */
	protected List<Node> findRealChildNodes(Node node) {
		NodeList nodes = node.getChildNodes();
		LinkedList<Node> list = new LinkedList<Node>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node child = nodes.item(i);
			if (!child.getNodeName().equals("#text")) {
				list.addLast(child);
			}
		}
		return list;
	}

	/**
	 * Finds the first real child node that has the given name. A real child
	 * node is one that is not just a text entry. More specifically, it is one
	 * surrounded by XML tags.
	 * 
	 * @param node
	 * @param name
	 * @return
	 */

	protected Node findFirstRealChild(Node node) {
		NodeList nodes = node.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node child = nodes.item(i);
			if (!child.getNodeName().equals("#text")) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Obtains a transformer for the given node from the transformer factory and
	 * runs its transformation method.
	 * 
	 * @param node
	 * @param environment
	 * @param ta
	 * @param scModel
	 */
	protected void handleNode(Node node, Environment e) {
		if (node != null) {
			NodeTransformer t = e.getTransformerFactory().getNodeTransformer(node);
			t.transformNode(node, e);
		}
	}

	/**
	 * Creates a prefix string out of the prefix stack by popping one value
	 * after the other from the stack and attaching it to the front of the
	 * string to be returned. All values are separated by the prefix delimiter
	 * character.
	 * 
	 * @param stack
	 * @return string prefix
	 */
	/*
	 * protected String createPrefix(Stack<String> stack) { String prefix = "";
	 * 
	 * if(stack == null) { return prefix; } for(int i = 0; i < stack.size();
	 * i++) { prefix += stack.get(i) + Constants.PREFIX_DELIMITER; }
	 * 
	 * return prefix; }
	 */

	public static boolean isIgnorableWhitespaceNode(Node n) {
		if (n != null && n.getAttributes() == null) {
			return true;
		}
		return false;
	}
}
