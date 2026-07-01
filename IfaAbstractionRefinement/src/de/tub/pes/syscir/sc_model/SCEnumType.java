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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * Represents an enum which is defined in the model.
 * 
 * @author Timm Liebrenz
 * 
 */
public class SCEnumType implements Serializable {

	private static final long serialVersionUID = -396027654081396147L;

	private static Logger logger = LogManager
			.getLogger(SCEnumType.class.getName());
	/**
	 * Name of the enum
	 */
	protected String name;

	/**
	 * List of elements in this enum
	 */
	protected List<SCEnumElement> elements;

	private int lastInsertedValue;

	/**
	 * true if the elements in the enum are continuous
	 * e.g. if value 2 and 5 exists, so do 3 and 4!
	 */
	private boolean isContinuous = true;

	/**
	 * The min value stored in the elements
	 */
	private int min = Integer.MAX_VALUE;
	/**
	 * The max value stored in the elements
	 */
	private int max= Integer.MIN_VALUE;

	public SCEnumType(String name) {
		this.name = name;
		this.lastInsertedValue = -1;
		this.elements = new LinkedList<SCEnumElement>();
	}

	public String getName() {
		return name;
	}

	public void addElement(String name, int value) {
		lastInsertedValue = value;
		elements.add(new SCEnumElement(name, value, this));
		if(value < min)
			min = value;
		if(value > max)
			max = value;
		
		
		if(value < lastInsertedValue)
			logger.error("Implementation of SCEnum does not safly support adding smaller enum values than the values before");
		else if (value == lastInsertedValue)
			logger.error("The enum Value "+value+" is added a second time to the enum "+this.getName());
		else if ( (value - lastInsertedValue) != 1)
			isContinuous = false;
	}

	public void addElement(String name) {
		elements.add(new SCEnumElement(name, ++lastInsertedValue, this));
		if(lastInsertedValue < min)
			min = lastInsertedValue;
		if(lastInsertedValue > max)
			max = lastInsertedValue;
	}

	public List<SCEnumElement> getElements() {
		return elements;
	}
	
	/**
	 * Checks if the elements in the enum are continuous
	 * e.g. if value 2 and 5 exists, so do 3 and 4!
	 * @return true if continuous, false otherwise
	 */
	public boolean isContinuous(){
		return isContinuous;
	}
	
	public int getMinValue(){
		if(elements.size() < 1){
			logger.error("Returned a minValue of an empty Enum");
			return -1;
		}
		return min;
	}
	
	public int getMaxValue(){
		if(elements.size() < 1){
			logger.error("Returned a maxValue of an empty Enum");
			return -1;
		}
		return max;
	}

	/**
	 * Checks if the given name is an element of this enum. Returns true if the
	 * given String is equal to one of the elements of the enum, false
	 * otherwise.
	 * 
	 * @param name
	 * @return
	 */
	public boolean containsElement(String name) {
		for (SCEnumElement el : elements) {
			if (el.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the element of this enum with the given name. If there is no
	 * element with this name it returns null.
	 * 
	 * @param name
	 * @return
	 */
	public SCEnumElement getElementByName(String name) {
		for (SCEnumElement el : elements) {
			if (el.getName().equals(name)) {
				return el;
			}
		}
		return null;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((elements == null) ? 0 : elements.hashCode());
		result = prime * result + lastInsertedValue;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SCEnumType other = (SCEnumType) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		if (lastInsertedValue != other.lastInsertedValue)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	// TODO: just for debugging
	@Override
	public String toString(){
		String elemStr = "";
		for (SCEnumElement elem : elements) {
			elemStr += elem.toString() + ","; 
		}
		return this.name +  "[" + elemStr + "]";
	}
}
