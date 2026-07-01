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
package de.fzi.sim.kascpar.util;

/**
 * Stores a information about a original file, a original line and a line in the result common file.
 * This class is used to represent value in the line dictionary.
 * @see de.fzi.sim.kascpar.util.LineMapper
 */

 public class RefInfo {

    public RefInfo(String file, int origLine, int line) {
      m_file = file;
      m_line = line;
      m_origLine = origLine;
    }

    public String file() {
      return m_file;
    }

    public int line() {
      return m_line;
    }

    public int origLine() {
      return m_origLine;
    }

    public void setOrigLine(int orLine) {
      m_origLine = orLine;
    }

    protected String m_file;
    protected int m_line;
    protected int m_origLine;
  }
