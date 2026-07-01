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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Provides functions to ease handling of inputstreams.
 * 
 * @author pockrandt
 * 
 */
public class IOUtil {

	public static boolean createCopy(String file, String copy) {
		
		Path filePath = FileSystems.getDefault().getPath(file);
		Path copyPath = FileSystems.getDefault().getPath(copy);
		try {
			Files.copy(filePath, copyPath);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void replaceAllInFile(String pathToFile, String replace, String replacement) {
		Path path = Paths.get(pathToFile);
		Charset charset = StandardCharsets.UTF_8;
		try {
			String content = new String(Files.readAllBytes(path), charset);
			content = content.replaceAll(Pattern.quote(replace), replacement);
			Files.write(path, content.getBytes(charset));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeFileIfExist(String target) {
		// Remove previous created files
		File f = new File(target);
		if (f.exists())
			f.delete();
	}
	
	/**
	 * Returns an InputStream for the specified location. This is either done by
	 * using a simple FileInputStream or by using a classLoader (only necessary
	 * if the file is contained in a jar).
	 * 
	 * @param location
	 * @return
	 * @throws FileNotFoundException
	 */
	public static InputStream getInputStream(String location)
			throws FileNotFoundException {
		InputStream is = ClassLoader.getSystemClassLoader()
				.getResourceAsStream(location);
		if (is == null) {
			is = new FileInputStream(location);
		}
		return is;
	}

	/**
	 * this function reads the XML-File
	 * 
	 * @param file
	 *            path to the file
	 * @return the XML-Document
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document readXML(String file) throws SAXException,
			IOException, ParserConfigurationException {
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(IOUtil.getInputStream(file));
		return d;
	}
}
