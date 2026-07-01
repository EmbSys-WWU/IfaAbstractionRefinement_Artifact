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
import java.util.Map;

/**
 * Parses the string arguments of a method (i.e. string[] of any main method)
 * for convenience. Returns a Map<String,String> which maps every parameter used
 * to the value.
 * 
 * @author Marcel Pockrandt
 * 
 */
public class CommandLineParser {

	/**
	 * Can be used if no separator is needed. Every element of args will be
	 * handled as a parameter.
	 */
	public static final String NO_SEPARATOR = "";

	/**
	 * Should be the standard separator. Every parameter starting with - will be
	 * handled as a new parameter with the following parameter as a value if not
	 * starting with a -.
	 */
	public static final String MINUS_SEPARATOR = "-";

	/**
	 * Parses a String array beginning at startIndex. The array should look like
	 * {-x, arg1, -t, arg2, -b}. The Method will return a Map which maps any
	 * String beginning with a separatorChar ("-" in the example) to either the
	 * next element of the array or to true if it doesn't exist or the next
	 * element also starts with the separatorChar. The resulting Map will look
	 * like {x=arg1, t=arg2, b="true"}.
	 * 
	 * @param args
	 *            String array to parse
	 * @param startIndex
	 *            index to start the parsing with
	 * @param separatorChar
	 *            character which begins every command line argument
	 * @return
	 */
	public static Map<String, String> parseArgs(String[] args, int startIndex,
			String separatorChar) {
		Map<String, String> result = new HashMap<String, String>();
		int i = startIndex;
		if (args != null) {
			while (i < args.length) {
				if (args[i].startsWith(separatorChar)) {
					if (i + 1 < args.length
							&& !args[i + 1].startsWith(separatorChar)) {
						result.put(args[i].substring(separatorChar.length()),
								args[i + 1]);
						// jump over next value
						i++;
					} else {
						result.put(args[i].substring(separatorChar.length()),
								"true");
					}
				}
				i++;
			}
		}

		return result;
	}

	/**
	 * Parses a String array. The array should look like {-x, arg1, -t, arg2,
	 * -b}. The Method will return a Map which maps any String beginning with a
	 * separatorChar ("-" in the example) to either the next element of the
	 * array or to true if it doesn't exist or the next element also starts with
	 * the separatorChar. The resulting Map will look like {x=arg1, t=arg2,
	 * b="true"}.
	 * 
	 * @param args
	 *            String array to parse
	 * @param separatorChar
	 *            character which begins every command line argument
	 * @return
	 */
	public static Map<String, String> parseArgs(String[] args,
			String separatorChar) {
		return parseArgs(args, 0, separatorChar);
	}

	/**
	 * Parses a String array beginning at startIndex. The array should look like
	 * {-x, arg1, -t, arg2, -b}. The Method will return a Map which maps any
	 * String beginning with a "-" to either the next element of the array or to
	 * true if it doesn't exist or the next element also starts with the
	 * separatorChar. The resulting Map will look like {x=arg1, t=arg2,
	 * b="true"}.
	 * 
	 * @param args
	 *            String array to parse
	 * @param startIndex
	 *            index to start the parsing with
	 * @return
	 */
	public static Map<String, String> parseArgs(String[] args, int startIndex) {
		return parseArgs(args, startIndex, MINUS_SEPARATOR);
	}

	/**
	 * Parses a String array. The array should look like {-x, arg1, -t, arg2,
	 * -b}. The Method will return a Map which maps any String beginning with a
	 * "-" to either the next element of the array or to true if it doesn't
	 * exist or the next element also starts with the separatorChar. The
	 * resulting Map will look like {x=arg1, t=arg2, b="true"}.
	 * 
	 * @param args
	 *            String array to parse
	 * @return
	 */
	public static Map<String, String> parseArgs(String[] args) {
		return parseArgs(args, 0, MINUS_SEPARATOR);
	}
}
