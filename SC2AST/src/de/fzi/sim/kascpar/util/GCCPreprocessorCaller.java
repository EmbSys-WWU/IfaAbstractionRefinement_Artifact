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


import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;

import java.util.LinkedList;
import java.util.ArrayList;


/**
 * Performs GNU gcc preprocessor call in a new process.
 */
public class GCCPreprocessorCaller {

   /**
    * A contents of error stream of GNU gcc.
    */
   protected LinkedList m_errOut;

   /**
    * A contents of output stream of GNU gcc.
    */
   protected LinkedList m_stOut;

   /**
    * A variable is used for acceleration of the new file searching.
    * It saves the last used file index;
    */
   protected int m_countTmp, m_countScr, m_countRes;
   
   /**
    *  A variable controls 'delete on exit' flag of the created file.
    */
   protected boolean m_remTmp, m_remScr, m_remRes;
   
   /**
    * The common created with GNU gcc result file name creation flag:
    * if this field is true the algorithm searchs a new not used file name, uses specified name otherwise.  
    */
   protected boolean m_isResAutoFind;
   
   /**
    * A special parameters for GNU gcc call.
    */
   protected String m_gcc_preprocessor_cmd_line_prefix;

   /**
    * Pointers to a result common source file.
    */
   protected File m_resFile;
   
   /**
    * A constructor with a preprocessor command line prefix.
    */
   public GCCPreprocessorCaller(String gcc_preprocessor_cmd_line_prefix)
    {
      m_countTmp = m_countScr = m_countRes = 0;

      m_remTmp = m_remScr = true;
      m_remRes = false;
      m_isResAutoFind = false;
      
      m_errOut = new LinkedList();
      m_stOut = new LinkedList();
      m_resFile = null;
      
      
      m_gcc_preprocessor_cmd_line_prefix = gcc_preprocessor_cmd_line_prefix;
      
    }
    
   /**
    * Returns true if a common result source file will be created with not used file name.
    */
   public void isResultFileNameAutoDetection(boolean value) {
     
     m_isResAutoFind = value;
   }

   /**
    * Returns a contents of error stream of GNU gcc.
    * @return a contents of error stream of GNU gcc.
    * The returned LinkedList contains the String objects.
    */        
   public LinkedList getErrorOutput() {
     return m_errOut;
   }
   
   /**
    * Returns a contents of output stream of GNU gcc.
    * @return a contents of output stream of GNU gcc.
    * The returned LinkedList contains the String objects.
    */
   public LinkedList getStandartOutput() {
     return m_stOut;
   }

   /**
    * Sets a strategy of delete on exit to the temporary source files.
    * A default value of strategy is true.
    */
   public void removeTmpFilesOnExit(boolean value) {
     m_remTmp = value;
   }

   /**
    * Sets a strategy of delete on exit to the temorary script files.
    * A default value of strategy is true.
    */
   public void removeScrFilesOnExit(boolean value) {
     m_remScr = value;
   }

   /**
    * Sets a strategy of delete on exit to the output common source files.
    * A default value of strategy is false.    
    */
   public void removeResFilesOnExit(boolean value) {
     m_remRes = value;
   }

   /**
    * Calls GNU gcc for files in the fileList.
    * @param inf - in/output parameter
    * @param fileList - source files for GNU gcc input
    * @param os_id - a identifier of the OS. (-1 -undefined, 0 - linux, 1 - solaris, 2 - windows)
    * @return true if the method finished successfully, false otherwise.
    * @see ParamInf
    */   
   public boolean execGCC(ParamInf inf, ArrayList fileList, int os_id) {

    String [] s_env = null;
    

    
    String tmp_file_str = ".temp__";

    String error_file_str = ".error_out__";

    
    m_resFile = null;

    File tmp_file = null;
    File error_output_file = null;
    
    String file_separator = System.getProperty("file.separator");
    String system_tmp_folder = System.getProperty("java.io.tmpdir");
    String tmp_s = null;
    // finds unique names for tmp & error output files
    do {
      tmp_s = Integer.toString(m_countTmp++);
      String full_param = system_tmp_folder;

      int ind = full_param.lastIndexOf(file_separator);
      // inserts separator if it is necessary
      if (ind == -1 || (ind + file_separator.length() < full_param.length())) {
        full_param += file_separator;
      }
      full_param += tmp_file_str + tmp_s + ".cpp";

      tmp_file = new File(full_param);
    } while (tmp_file.exists());
    
    do {
        tmp_s = Integer.toString(m_countScr++);
        String full_param = system_tmp_folder;

        int ind = full_param.lastIndexOf(file_separator);
        // inserts separator if it is necessary
        if (ind == -1 || (ind + file_separator.length() < full_param.length())) {
          full_param += file_separator;
        }
        full_param += error_file_str + tmp_s + ".txt";

        error_output_file = new File(full_param);
     } while (error_output_file.exists());
    
    if (m_isResAutoFind) {
      tmp_s = null;
      do {
        tmp_s = Integer.toString(m_countRes++);
        String full_param = system_tmp_folder;

        int ind = full_param.lastIndexOf(file_separator);
        // inserts separator if it is necessary
        if (ind == -1 || (ind + file_separator.length() < full_param.length())) {
          full_param += file_separator;
        }
        full_param += tmp_file_str + tmp_s + ".cpp";
        m_resFile = new File(full_param);
      } while (m_resFile.exists());

      inf.m_com_source_file = m_resFile.getAbsolutePath();

    }
    else {

      inf.m_com_source_file = inf.m_file;
      m_resFile = new File(inf.m_com_source_file);
      
    }


    error_output_file.deleteOnExit();
    
    if (m_remTmp)
      tmp_file.deleteOnExit();

    if (m_remRes)
      m_resFile.deleteOnExit();
      
    String folder_str = System.getProperty("user.dir");
    
    int ind = folder_str.lastIndexOf(file_separator);
    // inserts separator if it is necessary
    if (ind != -1 && (ind + file_separator.length() == folder_str.length())) {
      folder_str = folder_str.substring(0,ind);
    }
      
    File folder = new File(folder_str);

    String arg = m_gcc_preprocessor_cmd_line_prefix;
    arg += "-D _SC_MAP_TO_TARGET_MACRO_H_ ";
    
    if (inf.m_user_included_file != null) {
      arg = arg + new String("-include'" + inf.m_user_included_file + "' ");
    }
    
    for (int i = 0; i < fileList.size(); ++i) {
      arg = arg + new String("-include'" + (String)fileList.get(i) + "' ");
    }
    arg = arg + tmp_file.getAbsolutePath() + " > " + m_resFile.getAbsolutePath();

    Process p = null;
    try {

      FileWriter wr = new FileWriter(tmp_file);
      wr.close();
      String err_report = null;
      String [] ss2 = {"sh", "-c",  arg + " 2> " + error_output_file.getAbsolutePath() };
      
      // starts gcc preprocessor 
      p = Runtime.getRuntime().exec(ss2,  s_env, folder);
       

      // waits process finish
      if (p.waitFor() != 0) {

      }

      FileInputStream stream = new FileInputStream(error_output_file);
      if(!elaborateStream(stream, m_errOut)) {
          System.err.println("ERROR: Can not elaborate error stream of gcc preprocessor correctly");
          return false;
      }

    } catch (IOException e) {

      System.err.println("Oops. " + e.getMessage());
      e.printStackTrace();
      return false;

    } catch (InterruptedException e) {

      System.err.println("Oops. " + e.getMessage());
      e.printStackTrace();
      return false;

    } catch (Exception e) {

      System.err.println("Oops. Error encountered during call gcc compiler.\n" + e.getMessage());
      e.printStackTrace();
      return false;
    }

     return true;
  }
  /**
   * Transforms a stream to the list of strings.
   * Each string is a line of the stream contents.
   * @param stream - a input stream.
   * @param list - a contents of the stream will be saved here.
   * @return @return true if the transformation finished successfully, false otherwise.
   *
   */
  boolean elaborateStream(InputStream stream, LinkedList list) {

    list.clear();
    InputStreamReader isr = new InputStreamReader(stream);
    
    BufferedReader br = new BufferedReader(isr);
    
    String line = null;
    try {
      while ( (line = br.readLine()) != null) {
          list.add(line);
          //System.out.println("->: " + line );
      }    
    }
    catch (IOException e) {

      System.err.println("Oops. " + e.getMessage());
      e.printStackTrace();
      return false;
    }  
    return true;    
  }
  
}
