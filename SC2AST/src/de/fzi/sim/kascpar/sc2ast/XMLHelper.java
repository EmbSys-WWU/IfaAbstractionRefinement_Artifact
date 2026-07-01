/*****************************************************************************

 Copyright (c) 2005-2007, FZI Forschungszentrum Informatik,
 Microelectronic System Design (SIM),
 Haid-und-Neu-Str. 10-14, 76131 Karlsruhe, Germany.
 All rights reserved.

 The contents of this file are subject to the restrictions and limitations
 set forth in the License as specified in the file LICENSE. You may not
 use this file except in compliance with such restrictions and limitations.
 You may obtain instructions on how to receive a copy of the License at
 http://www.fzi.de/kascpar.html. Software distributed by Contributors
 under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 ANY KIND, either express or implied. See the License for the specific
 language governing rights and limitations under the License.

 ****************************************************************************/

/*****************************************************************************

 This file is part of KaSCPar - Karlsruhe SystemC Parser Suite
 - Subproject: SC2AST - SystemC2AST

 Please report any problems or bugs to: kascpar@fzi.de

 Author: Pavel Parfuntseu

 ****************************************************************************/
  
package de.fzi.sim.kascpar.sc2ast;

import org.w3c.dom.Document; 
import org.w3c.dom.Element;
import java.util.ArrayList;
/**
 * Stores an inportant information for the XML-Document from the AST generation.
 * @see SCDumpVisitor
 * @see SCParser
 * @see SC2AST
 */
public class XMLHelper {
	
	//for the internal XML representation
	public  Document docInt;
	public Element mainInt;
	public ArrayList xmlNodesInt;

}
