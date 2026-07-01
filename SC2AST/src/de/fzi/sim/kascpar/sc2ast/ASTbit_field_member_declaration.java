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

public class ASTbit_field_member_declaration extends SimpleNode {
  public ASTbit_field_member_declaration(int id) {
    super(id);
    m_isNamed = false;
  }

  public ASTbit_field_member_declaration(SCParser p, int id) {
    super(p, id);
    m_isNamed = false;
  }


  /** Accept the visitor. **/
  public Object jjtAccept(SCParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  /*
    added by PPA
    BEGIN
  */
  //** sets true if the bit field member declaration is unnamed, false otherwise. **/
  void setIsNamed(boolean isNamed) 
  {
    m_isNamed = isNamed;
  }

  //** sets true if the bit field member declaration is unnamed, false otherwise. **/
  boolean isNamed()
  {
     return m_isNamed;
  }
  
  boolean m_isNamed; // contains true if the bit field member declaration is unnamed, false otherwise.
  /*
    added by PPA
    END
  */
}
