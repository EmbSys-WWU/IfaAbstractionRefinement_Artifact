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
 * Stores a associated with a token of the JavaCC information: a RefInfo object and a column.
 * @see de.fzi.sim.kascpar.util.RefInfo
 */

 public class TokenInfo {

    public TokenInfo(RefInfo ref, int column) {
      m_ref = ref;
      m_column = column;
    }

    public RefInfo refInfo() {
      return m_ref;
    }

    public int column() {
      return m_column;
    }


    protected RefInfo m_ref;
    protected int m_column;
  }
