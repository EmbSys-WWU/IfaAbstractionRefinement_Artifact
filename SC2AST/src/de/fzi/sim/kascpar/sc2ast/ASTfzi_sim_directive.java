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

public class ASTfzi_sim_directive extends SimpleNode {
  public ASTfzi_sim_directive(int id) {
    super(id);
    m_typeId = 0;
  }

  public ASTfzi_sim_directive(SCParser p, int id) {
    super(p, id);
    m_typeId = 0;
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SCParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  /*
    added by PPA
    BEGIN
  */


  // variable(s)
  String m_signalName;
  int m_min, m_max, m_typeId;
  

  void setMinValue(int min) {
    m_min = min;
  }

  int minValue() {

    return m_min;
  }
  
  void setMaxValue(int max) {
    m_max = max;
  }

  int maxValue() {

    return m_max;
  }

  void setTypeId(int id) {
    m_typeId = id;
  }

  int typeId() {

    return m_typeId;
  }

  void setSignalName(String s) {
    m_signalName = s;
  }
  
  String signalName() {
    return m_signalName;
  }
  
  /*
    added by PPA
    END
  */
     
}
