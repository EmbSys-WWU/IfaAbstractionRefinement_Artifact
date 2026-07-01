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

public class ASTsc_port_specifier extends SimpleNode implements BitSizeIface {

  static final int NUMBER_OF_CHANELS_DEFAULT_VALUE = 1;

  public ASTsc_port_specifier(int id) {
    super(id);
    setNumberOfChanels(NUMBER_OF_CHANELS_DEFAULT_VALUE);
    setLength(SIZE_UNKNOWN);
  }

  public ASTsc_port_specifier(SCParser p, int id) {
    super(p, id);
    setNumberOfChanels(NUMBER_OF_CHANELS_DEFAULT_VALUE);
    setLength(SIZE_UNKNOWN);
  }

  /*
    added by PPA
    BEGIN
  */

  // variable(s)
  protected int m_numOfChanels;
  protected int m_size;

  public void setNumberOfChanels(int numOfChanels) {

    m_numOfChanels = numOfChanels;
  }

  public int numberOfChanels() {

    return m_numOfChanels;
  }

  public void setLength(int i) throws IndexOutOfBoundsException {
    if (i < SIZE_UNKNOWN) {
      throw new IndexOutOfBoundsException("Bit size is invalid: negative");
    }
    m_size = i;
  }
  
  public int length() {
    return m_size;
  }
  
  
  /** Accept the visitor. **/
  public Object jjtAccept(SCParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
