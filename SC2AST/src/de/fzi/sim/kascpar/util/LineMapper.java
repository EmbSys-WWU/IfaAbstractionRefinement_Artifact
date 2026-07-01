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

/*****************************************************************************

Credits for the LineMapper update and improvement to

Andreas Deyhle und Joachim Falk

Dept. of Computer Science 12
Hardware-Software-Co-Design
University of Erlangen-Nuremberg
Am Weichselgarten 3
D-91058 Erlangen, Germany

 ****************************************************************************/

package de.fzi.sim.kascpar.util;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.BufferedWriter;

import java.lang.String;

import java.util.LinkedList;
import java.util.Stack;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Set;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.*;

 /**
  * Generates a line dictionary for the generated with GNU gcc common source file.
  * The class finds for each line in the common file a corresponding line in the original input file.
  * This dictionary will be used during the parsing of the common source file.
  */
public class LineMapper {
  /**
   * Stores the boolean result of last line mapping. The default value is false.
   * Use lineMapping() method to read the value if this field.
   */
  protected boolean m_isLineMapping;
  /**
   * Stores the result of line mapping: line dictionary.
   * This field is set to null if the algorithm not completes successfully.
   * The key is a object of the Integer, the value is a object of the RefInfo class.
   * Use lineDict() method to take a access to this dictionary.
   * @see RefInfo
   */
  protected TreeMap m_lineDict;
  /**
   * Contains an important gcc output with header files hierarchy
   */
  protected LinkedList m_gccErrStream;
  /**
   * Controls the debug information.
   */
  protected int DebugLevel;
  /**
   * GNU gcc preprocessor parameters.
   */
  protected String m_preprCmd;
  /**
   * An identifier of the Operation System: -1 -undefined, 0 - linux, 1 - solaris, 2 - windows.
   */
  protected int m_os_id;
  
  /**matcher for obsolete linemarkers*/
  protected Matcher matcherObsLM;
  /**matcher for jumping linemarkers*/
  protected Matcher matcherJumpLM;
  /**matcher for Include linemarkers*/
  protected Matcher matcherInclLM;
  
  /**
   * A default constructor.
   */
  public LineMapper() {
    // default values
    m_isLineMapping = false;
    m_lineDict = null;
    DebugLevel = 0;
    setPreprocessorCmd("g++ -E -C -w -H -nostdinc -nostdinc++ ");
    m_os_id = -1;
    //init matchers
    Pattern obsLMPattern  = Pattern.compile("# \\d+ \"<\\w+>\"( \\d+)?");
    Pattern inclLMPattern = Pattern.compile("# \\d+ \"[^\"]+\" [12]");
    Pattern jumpLMPattern = Pattern.compile("# \\d+ \"[^\"]+\"");
    matcherObsLM  =  obsLMPattern.matcher("");
    matcherJumpLM = jumpLMPattern.matcher("");
    matcherInclLM = inclLMPattern.matcher("");
  }
  /**
   * Returns true if the last line mapping was successfully, false otherwise.
   */
  public boolean lineMapping() {

    return m_isLineMapping;
  }

  /**
   * Returns a line dictionary if the last line mapping was successfully, null otherwise.
   * The key is a object of the Integer, the value is a object of the RefInfo class.
   * @see RefInfo
   */
  public TreeMap lineDict() {

    return m_lineDict;
  }

  public void setDebugLevel(int level) {
    DebugLevel = level;
  }

  /**
   * Sets a prefix command line for GNU gcc preprocessor.
   */
  public void setPreprocessorCmd(String cmd) {
    m_preprCmd = cmd;
  }

  /**
   * Sets a identifier of the OS. (-1 -undefined, 0 - linux, 1 - solaris, 2 - windows).
   */
  public void setOSId(int value) {
    m_os_id = value;
  }

  /**
   * Performs line mapping.
   * @param list - a string list. It contains an important GNU gcc output with header files hierarchy.
   * @param file - a created with GNU gcc result source file. This file contains all sources.
   * @param lineMappingReport - a file name. It will be contain line mapping table if the lineMappingReport is not null.
   * @return true if the method finished successfully, false otherwise.
   */
  
	public boolean exec(LinkedList list, File file, String lineMappingReport) {

    try {
      m_isLineMapping = false;
      
      //mapps lines
      //use new version of linemapping function
      m_lineDict = lineMapping(file.getAbsolutePath());

      if (m_lineDict == null) {
        m_isLineMapping = false;
      }
      else {
        m_isLineMapping = true;

        if (lineMappingReport != null) {

          Set set = m_lineDict.entrySet();

          FileWriter wr = new FileWriter(new File(lineMappingReport));
          wr.write("----------------------------------------------------------------------------");
          wr.write("\n| Line  |  Orig.file    | line ");
          wr.write("\n----------------------------------------------------------------------------");
          Iterator setItr = set.iterator();
          while(setItr.hasNext()) {
            Map.Entry t = (Map.Entry) setItr.next();
            wr.write("\n| " + (((Integer)t.getKey()).intValue() + 1) 
                   + "  | " + ((RefInfo)t.getValue()).file() 
                   + "  |  " + (((RefInfo)t.getValue()).line() + 1) );
          }

          set = m_lineDict.entrySet();
          wr.write("\n----------------------------------------------------------------------------\n");
          wr.write("real content of m_lineDict:\n");
          wr.write("----------------------------------------------------------------------------\n");
          wr.write("| m_line  |  m_file    | m_origLine \n");
          wr.write("----------------------------------------------------------------------------\n");
          setItr = set.iterator();
          while(setItr.hasNext()) {
            Map.Entry t = (Map.Entry) setItr.next();
            wr.write("\n| " + (((RefInfo)t.getValue()).line()) 
                   + "  | " + ((RefInfo)t.getValue()).file() 
                   + "  |  " + (((RefInfo)t.getValue()).origLine()) );
          }
          wr.write("\nOK!");
          wr.close();
        }
      }

    } catch (IOException e) {

      System.err.println("Oops. " + e.getMessage());
      e.printStackTrace();
      return false;

    } catch (Exception e) {

      System.err.println("Oops. " + e.getMessage());
      e.printStackTrace();
      return false;
    }

    return true;
	}
  
  
  //new lineMapping function
  TreeMap lineMapping(String unionFileName) {
    
    File mapperFile = new File(unionFileName +".line_mapper.tmp");
    mapperFile.deleteOnExit();

    File unionFile = new File(unionFileName);
    String s;
    String s1, s2, s3, s4;
    StringTokenizer st;
    String actualFileName = null;
    TreeMap resultMap = new TreeMap();
    boolean commentMode = false;
    int linesCommonFile = 1;
    int linesActFile = 0;

    //copy unionFile to tempFile
    try{
    if (mapperFile.exists()) {
      mapperFile.delete();
    }
    mapperFile.createNewFile();
    BufferedReader unionFileCopyReader = new BufferedReader(new FileReader(unionFile));
    BufferedWriter unionFileCopyWriter = new BufferedWriter(new FileWriter(mapperFile));
    while ((s = unionFileCopyReader.readLine()) != null) {
      //System.out.println(s);
      unionFileCopyWriter.write(s, 0, s.length());
      unionFileCopyWriter.newLine();
    }
    unionFileCopyWriter.flush();
    unionFileCopyWriter.close();
    unionFileCopyReader.close();
    }catch (Exception e) {
      System.err.println("ERROR: Can not copy common sourcefile, reason: " + e.getMessage());
      e.printStackTrace();
      return null;}

    //copy back non-Linemarker-lines to unionFile
    //create LineMapping-TreeMap
    try{
      BufferedReader unionFileReader = new BufferedReader(new FileReader(mapperFile));
      unionFile.delete();
      unionFile.createNewFile();
      BufferedWriter unionFileBackWriter = new BufferedWriter(new FileWriter(unionFile));

      while ((s = unionFileReader.readLine()) != null) {
        //copy empty lines to result file
        if(s.length() == 0) {
          unionFileBackWriter.newLine();
          //new TreeMap entry
          resultMap.put(new Integer(linesCommonFile-1), new RefInfo(actualFileName,
                                                       linesCommonFile,
                                                       linesActFile));
          linesCommonFile++;
          linesActFile++;
          continue;
        }
        //recognize linemarker by regex
        if(s.charAt(0) == '#') {
        
          //reset matchers with new input
          matcherObsLM.reset(s);
          matcherJumpLM.reset(s);
          matcherInclLM.reset(s);
          //discard obsolete linemarkers
          if ( matcherObsLM.matches() ) {
            continue;
          }
          //handle include linemarker
          else if ( matcherInclLM.matches() ) {
            st = new StringTokenizer(s);
	    
            if ( st.countTokens() != 4 ) {
              if ( DebugLevel > 0) {
                System.out.println(
                  "Warning: include-linemarker with tokencount different from 4:");
                System.out.println(s);
              }
              continue;
            } 
            s1 = st.nextToken();
            s2 = st.nextToken();
            s3 = st.nextToken();
            s4 = st.nextToken();
            //distinguish includes and jump-backs from includes
            //include
            if ( s4.matches("1") ) {
              unionFileBackWriter.newLine();
              //new TreeMap entry
              resultMap.put(new Integer(linesCommonFile-1), new RefInfo(actualFileName,
                                                           linesCommonFile,
                                                           linesActFile));
              linesCommonFile++;
            }
            //unknown 
            else if ( !s4.matches("2") ) {
              System.out.println("Unknown include-linemarker:");
              System.out.println(s);
              continue;
            }
            //include and jump-back of includes
            linesActFile = Integer.parseInt(s2) - 1;
            actualFileName = s3.substring(1, s3.length()-1);
            File tempActualFile = new File(actualFileName);
            if ( tempActualFile.exists() ) {
              actualFileName = tempActualFile.getAbsolutePath();
            }
          }
          //handle jump-linemarker
          else if ( matcherJumpLM.matches() ) {
            st = new StringTokenizer(s);
            if ( st.countTokens() != 3 ) {
              if ( DebugLevel > 0) {
                System.out.println(
                  "Warning: jump-linemarker with tokencount different from 3:");
                System.out.println(s);
              }
              continue;
            }
            s1 = st.nextToken();
            s2 = st.nextToken();
            s3 = st.nextToken();
            linesActFile = Integer.parseInt(s2) - 1;
            actualFileName = s3.substring(1, s3.length()-1);
            File tempActualFile = new File(actualFileName);
            if ( tempActualFile.exists() ) {
              actualFileName = tempActualFile.getAbsolutePath();
            }
          }
        }
        //no Preprecessor command
        else {
        //copy actual line to new common File
        unionFileBackWriter.write(s);
        unionFileBackWriter.newLine();
        resultMap.put(new Integer(linesCommonFile-1), new RefInfo(actualFileName,
                                                     linesCommonFile,
                                                     linesActFile));
        //update linecounters
        linesActFile++;
        linesCommonFile++;
        }
      }//end of while

      unionFileBackWriter.flush();
      unionFileBackWriter.close();
    } catch (Exception e) {
        System.err.println("ERROR: Can not map lines, reason: " + e.getMessage());
        e.printStackTrace();
        return null;}

    return resultMap;
  }

}
