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

public class Constants {
	/**
	 * Most of the identifiers in our model need to be prefixed in some way, either 
	 * depending on scope, or thread, or to create some keywords. This String is used
	 * to separated the prefixes internally. 
	 */
	public static final String PREFIX_DELIMITER = "#";
	
	/**
	 * Delimiter for struct and class methods from custom datatypes
	 * was "_" before, in case this leads to problems: reverse only here to "_"
	 */
	public static final String STRUCT_METHOD_PREFIX_DELIMITER = "$";
	
	public static final String GENERIC_TYPE = "GTYPE";
	public static final String GENERIC_TYPE_LENGTH = "GLENGTH";
	public static final String GENERIC_TYPE_DELIMITER = "_";
	
	/**
	 * The version number of SysCIR.
	 */
	public static final String VERSION = "0.5";
}
