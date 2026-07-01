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
 * Represents the systemC built-in types.
 */
public class SCPortTypes extends SimpleNode implements BitSizeIface, FixedPointTypeIface {


  protected int m_size; // SIZE_UNKNOWN - bit size is undefined
                        // [0...n] - bit size

  protected boolean m_scDataType; // true if type is systemC built in type otherwise false
  
  protected int m_twl; // total word length
  protected int m_iwl; // integer word length
  protected String m_qMode; // quantization mode
  protected String m_oMode; // overflow mode
  protected int m_bitNumber; // number of saturated bits


  public SCPortTypes(int id) {
    super(id);
    setLength(SIZE_UNKNOWN);
    m_twl = 0;
    m_iwl = 0;
    m_qMode = QUANTIZATION_MODE_DEFAULT_VALUE;
    m_oMode = OVERFLOW_MODE_DEFAULT_VALUE;
    m_bitNumber = NUMBER_OF_SATURATED_BITS_DEFAULT_VALUE;
  }

  public SCPortTypes(SCParser p, int id) {
    super(p, id);
    setLength(SIZE_UNKNOWN);
    m_twl = 0;
    m_iwl = 0;
    m_qMode = QUANTIZATION_MODE_DEFAULT_VALUE;
    m_oMode = OVERFLOW_MODE_DEFAULT_VALUE;
    m_bitNumber = NUMBER_OF_SATURATED_BITS_DEFAULT_VALUE;
  }

  public SCPortTypes(int id, int length) throws IndexOutOfBoundsException{
    super(id);
    setLength(length);
    m_twl = 0;
    m_iwl = 0;
    m_qMode = QUANTIZATION_MODE_DEFAULT_VALUE;
    m_oMode = OVERFLOW_MODE_DEFAULT_VALUE;
    m_bitNumber = NUMBER_OF_SATURATED_BITS_DEFAULT_VALUE;
  }

  /*
    i := [0..MAX_INT] or SIZE_UNKNOWN
  */
  public void setLength(int i) throws IndexOutOfBoundsException {
    if (i < SIZE_UNKNOWN) {
      throw new IndexOutOfBoundsException("Bit size is invalid: negative");
    }
    m_size = i;
  }

  // returns bit size
  public int length() {
    return m_size;
  }


  public void setSystemCDataType(boolean value) {

    m_scDataType = value;
  }
  // returns true, if stored type is one of SystemC data-types
  public boolean systemCDataType() {

    return m_scDataType;
  }

  
  public void setTotalWordLength(int i)
  {
    m_twl = i;
  }
  
  public int totalWordLength()
  {
    return m_twl;
  }

  public void setIntegerWordLength(int i)
  {
    m_iwl = i;
  }

  public int integerWordLength()
  {
    return m_iwl;
  }

  public void setQuantizationMode(String s)
  {
    m_qMode = s;
  }
  
  public String quantizationMode()
  {
    return m_qMode;
  }

  public void setOverflowMode(String s)
  {
    m_oMode = s;
  }  

  public String overflowMode()
  {
    return m_oMode;
  }  

  public void setNumberOfSaturatedBits(int i)
  {
    m_bitNumber = i;
  }

  public int numberOfSaturatedBits()
  {
    return m_bitNumber;
  }  
}
