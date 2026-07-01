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
 * Collects the information about a file: a file name, a folder, a priority and boolean flag isAtom.
 * This flag is true if there is no include preprocessor directive was not found in this file.
 * This class is used in the LineMapper class implementation.
 * @see LineMapper
 */

public class FileDscr {

  /**
   * Constructs the object.
   */
  public FileDscr(String file, String dir, int prior, boolean isAtom)
  {
    m_file = file;
    m_prior = prior;
    m_dir = dir;
    setFileType();
    m_isAtom = isAtom;
  }
  
  public String file() { return m_file; }
  public int priority() { return m_prior; }
  public int type() { return m_type;}
  public String dir() { return m_dir;}

  public String path() {

    if ( m_dir != null && m_dir.length()>0)
      return m_dir + "/" + m_file;
    else
      return m_file;
  }
      
  public boolean isAtom() { return m_isAtom;}
  public void setIsAtom(boolean isAtom ) { m_isAtom = isAtom;  } 
  
  protected String m_file;
  protected int m_prior;
  protected int m_type; // 0 - header, 1 - source, 2 - undefined
  protected String m_dir;
  protected boolean m_isAtom;

  private void setFileType() {
    int res = 2;
    int pos = m_file.indexOf(".");
    
    if (pos != -1) {
      if (pos + 1 < m_file.length() ) {
        String ext = m_file.substring(pos + 1);
        if (ext.equalsIgnoreCase("cpp") ||
            ext.equalsIgnoreCase("cc") ||
            ext.equalsIgnoreCase("c")) {
          res = 1;
        }
        else
        if (ext.equalsIgnoreCase("h") ||
            ext.equalsIgnoreCase("hpp")) {
          res = 0;
        }
        
      }  
    }
    
    m_type = res;
  }  
}
