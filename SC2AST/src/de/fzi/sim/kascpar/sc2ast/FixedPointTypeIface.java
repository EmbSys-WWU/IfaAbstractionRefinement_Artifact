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
/**
 * Provides a interface to the work with the fixed point types.
 */
public interface FixedPointTypeIface {
  
  public final String QUANTIZATION_MODE_DEFAULT_VALUE = "SC_TRN";
  public final String OVERFLOW_MODE_DEFAULT_VALUE = "SC_WRAP";
  public final int NUMBER_OF_SATURATED_BITS_DEFAULT_VALUE = 0;
  
  public void setTotalWordLength(int i);
  public int totalWordLength();

  public void setIntegerWordLength(int i);
  public int integerWordLength();

  public void setQuantizationMode(String s );
  public String quantizationMode();

  public void setOverflowMode(String s);
  public String overflowMode();

  public void setNumberOfSaturatedBits(int i);
  public int numberOfSaturatedBits();

}

                                   