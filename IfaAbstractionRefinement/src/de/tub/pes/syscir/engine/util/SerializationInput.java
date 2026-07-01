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

import java.io.File;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.sc_model.SCSystem;

public class SerializationInput {

	private static Logger logger = LogManager.getLogger(SerializationInput.class.getName());
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, String> argMap = CommandLineParser.parseArgs(args);

		if (argMap.size() == 0) {
			System.out
					.println("Insufficent number of parameters. Use -h for help.");
			return;
		}

		if (argMap.containsKey("h") || argMap.containsKey("H")) {
			System.out.println("SysCIR ImportTool ver. " + Constants.VERSION);
			System.out.println("");
			System.out.println("Mandatory Arguments:");
			System.out
					.println("-i [INPUTFILE] - absolute path to the syscir-File which should be transformed");
			System.out.println("or");
			System.out.println("-g - start with a GUI");
			System.out.println("add");
			return;
		}

		String file = "";
		if (argMap.containsKey("i")) {
			file = argMap.get("i");
		} else if (argMap.containsKey("g")) {
			file = getFilePath();
			if (file.equals("")) {
				return;
			}
		} else {
			System.out.println("No input file specified. Use -h for help.");
			return;
		}

		SCSystem.load(file);
		System.out.print("");
	}

	public static String getFilePath() {
		JFileChooser fc = new JFileChooser();

		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".syscir")
						|| f.isDirectory();
			}

			public String getDescription() {
				return "SysCir-Files(*.syscir)";
			}
		});

		int state = fc.showOpenDialog(null);
		if (state == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return file.getAbsolutePath();
		} else {
			logger.debug("Selection cancelled");
		}
		return "";
	}

}
