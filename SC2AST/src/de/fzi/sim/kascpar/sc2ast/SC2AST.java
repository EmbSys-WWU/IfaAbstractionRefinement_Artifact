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


import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.File;

import java.util.ArrayList;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

import de.fzi.sim.kascpar.util.GCCPreprocessorCaller;
import de.fzi.sim.kascpar.util.LineMapper;
import de.fzi.sim.kascpar.util.ParamInf;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/** 
 *  Generates a AST from input C/C++ and SystemC source files.
 *  The generated AST contains a token for each statement of the original sourse.
 *  The AST is represented as a XML tree
 *  and it is also possible to save this tree in the file.
 *  The class can be used as a program entry point (it has the main routine).
 *  There is also possible to use the SC2AST class in other Java sources.
 */
public class SC2AST {

  /**
   * An expected command line format.
   */
  protected final static String FORMAT = "SC2AST [-help]\n       [-f source_file1 .. source_fileN]\n       [-i user_included_file]\n       [-o output_source_cpp_file]";
  /**
   * GNU gcc preprocessor parameters.
   */
  protected final static String GCC_COMMAND_LINE = "g++ -E -C -w -H -nostdinc -nostdinc++ ";
  /**
   * A report file of the line mapping part of the tool. It will be generated if the DebugLevel constant greater than 0.
   */
  protected final static String LINE_MAPPING_REPORT_FILE = "line_mapping_report.txt";
  /**
   * A default value of the result xml file.
   */
  protected final static String DEF_OUTPUT_INTERNAL_XML = "result.cpp";
  /**
   * A command line key for the file list
   */
  protected final static String FILE_LIST_KEY = "-f";
  /**
   * A command line key for the result common source file
   */
  protected final static String RES_FILE_KEY = "-o";
  /**
   * A command line key for the user included file with data types from standart headers.
   */
  protected final static String user_included_file_KEY = "-i";
  /**
   * A command line key for the help information.
   */
  protected final static String HELP_KEY = "-help";
  /**
   * Controls the debug information.
   */
  protected final static int DebugLevel = 0;
  /**
   * A tool behaviour with the comments in the original source code.
   */
  protected static boolean m_processComments = true;
  /**
   * A tool behaviour with the token position information (file, line, column) in the original source code.
   */
  protected static boolean m_saveFilelineColumn = true;

  /**
   * Registers a tool behaviour with the comments in the original source code.
   * @param value - a behaviour with the comments.
                    If the value is true all comments will be saved in the AST, otherwise each comment will be ignored.
   */
  public static void setCommentsProcessing(boolean value) {
    m_processComments = value;
  }

  /**
   * Registers a tool behaviour with the token position information (file, line, column) in the original source code.
   * @param value - a behaviour with the token position.
   *                If the value is true a file, a line and a column will be saved
   *                as attributes in the nodes of the AST, otherwise this information will be ignored.
   */
  public static void setFileLineColumnSaving(boolean value) {
    m_saveFilelineColumn = value;
  }
  /**
   * Returns a tool behaviour with the comments.
   */
  public static boolean commentsProcessing(){
    return m_processComments;
  }

  /**
   * Returns a tool behaviour with the token position information (file, line, column).
   */
  public static boolean fileLineColumnSaving() {
    return m_saveFilelineColumn;
  }
  /**
   * Returns the state of the command parameter line.
   * @param param - a command line token.
   * @return the state code: 0 - unknown, 1 - a help info, 2 - a result output file, 3 - a file list, 4 - an user included file
   */
  static int param_line_state(String param) {

    int state;
    if (param.equals(HELP_KEY)) {
      state = 1;
    }
    else
    if (param.equals(RES_FILE_KEY)) {
      state = 2;
    }
    else
    if (param.equals(FILE_LIST_KEY)) {
      state = 3;
    }
    else
    if (param.equals(user_included_file_KEY)) {
      state = 4;
    }
    else
      state = 0;

    return state;
  }
  /**
   * Parses the command line and saves the extracted information.
   * @param args - command line tokens.
   * @param inf -  found parameters are saved here.
   * @param fileList - found input source files are saved here.
   * @return true if the function finished successfully, false otherwise.
   */
  static boolean getParams(String args[], ParamInf inf, ArrayList fileList) {

    boolean isSetOFile = false, isSetFL = false, isSetUF = false;
    int state = 0;

    for (int i = 0; i < args.length; ++i) {
      if ( i == 0) {
        if (!(args[0].equals(RES_FILE_KEY) || args[0].equals(FILE_LIST_KEY) || args[0].equals(HELP_KEY)) ) {
          System.err.println("ERROR: Incorrect format. Expected: \n" + FORMAT);
          return false;
        }
      }



      state = param_line_state(args[i]);
      if (state == 1 && i == 0) {
        System.out.println("Usage: \n" + FORMAT);
        return false;
      }

      if (state == 2 && !isSetOFile) {

        if ( i + 1 >= args.length) {
          System.err.println("ERROR: Incorrect format. Can not find value of " + RES_FILE_KEY + " parameter. Expected: \n" + FORMAT);
          return false;
        }

        inf.m_file = args[++i];
        isSetOFile = true;
        continue;
      }
      else
      if (state == 3 && !isSetFL) {

        int j = i + 1;
        for (; j < args.length; ++j) {

          if (param_line_state(args[j]) != 0) {

            if (fileList.size() == 0) {
              System.err.println("ERROR: Incorrect format. Can not find value(s) of " + FILE_LIST_KEY + " parameter. Expected: \n" + FORMAT);
             return false;
            }
            else {
              break;
            }
          }

          fileList.add(args[j]);

        } // for (...)

        isSetFL = true;
        i = j-1; // next iteration
        continue;
      }
      else if (state == 4 && !isSetUF) {
        isSetUF = true;
        if ( i + 1 >= args.length) {
          System.err.println("ERROR: Incorrect format. Can not find value of " + user_included_file_KEY + " parameter. Expected: \n" + FORMAT);
          return false;
        }
        inf.m_user_included_file  = args[++i];
        continue;
      }

    } // for (param_list)
    return true;
  }

  /**
   * Returns all source file in the specific folder.
   * @param folder - where the search will be performed.
   * @return the found source files in the specific folder.
   */
  static String [] getSourceFiles(File folder) {

    class SourceCFF implements FilenameFilter {

      protected String [] SRC_EXT = { ".cpp", ".CPP", ".c", ".C"  };

      public boolean accept(File dir, String name)  {
        for (int i = 0; i < SRC_EXT.length; ++i) {
          int pos = name.lastIndexOf(SRC_EXT[i]);
          if (pos != -1 && pos + SRC_EXT[i].length() == name.length())
            return true;

        }
        return false;
      }
    }

    return folder.list(new SourceCFF());
  }

  /**
   * Tests if the parameters from the command line are correct.
   * @param inf -  found parameters with the method getParams(...).
   * @param fileList - found input source files with the method getParams(...).
   * @return true if the parameters from the command line are correct.
   */

  static boolean elaborateParam(ParamInf inf, ArrayList fileList) {


    File folder = new File(System.getProperty("user.dir"));


    // files do not set -> tries to find them in working dir
    if (fileList.size() == 0) {

      String [] f = getSourceFiles(folder);

      for (int i = 0; i < f.length; ++i)
        fileList.add(f[i]);

      if (fileList.size() == 0) {

        System.out.println("WARNING: Can not find source files in " + folder.getAbsolutePath() +" directory. Please check path to correct or specify source files by options '-f'.");
        return false;
      }
      if (DebugLevel != 0) {
        System.out.println("   found files: ");
        for (int i = 0; i < fileList.size(); ++i) {
          System.out.println("    " + (String) fileList.get(i));
        }
      }
    }
    else {
      // tries to find specified files

      int shift = 0;
      for (int i = 0; i < fileList.size(); ++i ) {
        File f = new File((String)fileList.get(i));
        if (!(f.exists() && f.isFile())) {
          System.out.println("WARNING: Can not find file " + (String)fileList.get(i - shift));
          fileList.remove(i - shift);
          ++shift;
        }
      }

    }

    if (inf.m_user_included_file != null) {
      File f = new File(inf.m_user_included_file);
      if (!f.exists()) {
        System.out.println("WARNING: Can not find user header file " + inf.m_user_included_file);
        inf.m_user_included_file = null;
      }
    }

    return true;
  }

  /**
   *  Returns a identifier of the Operation System: -1 -undefined, 0 - linux, 1 - solaris, 2 - windows
   *  @return a identifier of the OS. (-1 -undefined, 0 - linux, 1 - solaris, 2 - windows)
   */
  static int getOSId() {

    //String OS = System.getenv("OS");

    String OS = System.getProperty("os.name");


    if (DebugLevel > 0) {
      System.out.println("OS -> " + OS);
    }

    OS = OS.toLowerCase();

    int os_id = -1; // -1 -undefined, 0 - linux, 1 - solaris, 2 - windows
    if (OS.indexOf("linux") != -1) {
       os_id = 0;
    }
    else
    if (OS.indexOf("solaris") != -1 || OS.indexOf("sunos") != -1) {
       os_id = 1;
    }
    else
    if (OS.indexOf("windows") != -1) {
       os_id = 2;
    }
    else
    if (OS.indexOf("mac os x") != -1) {
    	os_id = 3;
    }
    else {
      os_id = -1;
    }
    return  os_id;
  }

  /**
   * Performs the parse of the input source files and the AST generation.
   * @param files - input source files.
   * @param output_source_file - an output common source file.This is the result of the gcc preprocessor call.
   * @param user_included_file - a written by user header file with forward declaration
   *                             of the used in the input source files data types from system(standart) headers.
   * @param printMsg - controls messages output. If the printMsg is true the methods prints information into output stream,
   *                   otherwise there is no message is put.
   * @return a root of the AST if the method finished successfully, null otherwise.
   */
  public static Document parse(ArrayList files,
                               String output_source_file,
                               String user_included_file,
                               boolean printMsg) {
    
    return parse(files, output_source_file, user_included_file, false, null, printMsg);
  }

  /**
   * Performs the parse of the input source files and the AST generation.
   * @param files - input source files.
   * @param output_source_file - an output common source file.This is the result of the gcc preprocessor call.
   * @param output_xml_file - a XML file contains the AST in the XML format.
   * @param user_included_file - a written by user header file with forward declaration
   *                             of the used in the input source files data types from system(standart) headers.
   * @param printMsg - controls messages output. If the printMsg is true the methods prints information into output stream,
   *                   otherwise there is no message is put.
   * @return a root of the AST if the method finished successfully, null otherwise.
   */
  public static Document parse(ArrayList files,
                               String output_source_file,
                               String output_xml_file,
                               String user_included_file,
                               boolean printMsg) {

    return parse(files, output_source_file, user_included_file, true, output_xml_file, printMsg);
  }

  /**
   * Performs the parse of the input source files and the AST generation.
   * @param files - input source files.
   * @param output_source_file - an output common source file.This is the result of the gcc preprocessor call.
   * @param user_included_file - a written by user header file with forward declaration
   *                             of the used in the input source files data types from system(standart) headers.
   * @param saveAsXMLFile - controls the saving AST to the specified with parameter output_xml_file XML file.
   * @param output_xml_file - a XML file contains the AST in the XML format.
   * @param printMsg - controls messages output. If the printMsg is true the methods prints information into output stream,
   *                   otherwise there is no message is put.
   * @return a root of the AST if the method finished successfully, null otherwise.
   */
  protected static Document parse(ArrayList files,
                                  String output_source_file,
                                  String user_included_file,
                                  boolean saveAsXMLFile,
                                  String output_xml_file,
                                  boolean printMsg) {

    ParamInf inf = new ParamInf();
    inf.m_file = output_source_file;
    inf.m_com_source_file = null;
    inf.m_user_included_file = user_included_file;

    int os_id = getOSId();

    if (os_id == -1) {
      System.err.println("Program can not work correctly under " + System.getProperty("os.name") + " OS");
      return null;
    }

    GCCPreprocessorCaller caller = new GCCPreprocessorCaller(GCC_COMMAND_LINE);

    if (!caller.execGCC(inf, files, os_id)) {
      System.err.println("ERROR: Error encoutered during call of gcc preprocessor");

      return null;
    }


    File source = new File(inf.m_com_source_file);

    if (printMsg)
      System.out.print("Mapping lines  ...");

    LineMapper lm = new LineMapper();
    lm.setOSId(os_id);

    lm.setDebugLevel(DebugLevel);
    lm.setPreprocessorCmd(GCC_COMMAND_LINE);
    String lineMappingFile = null;
    if (DebugLevel > 0)
      lineMappingFile = LINE_MAPPING_REPORT_FILE;


    if(!lm.exec(caller.getErrorOutput(), source, lineMappingFile)) {
      System.out.println("INFO: Line mapping failed!");
    }
    else
    if (lm.lineMapping() && printMsg ) {

        System.out.println(" Ok!");

    }

    String file_name = source.getAbsolutePath();

    boolean except_occured = false;
    XMLHelper xmlHelperInst = null;

    try {
      if (printMsg)
        System.out.print("Parsing file " + file_name + " ...");

      FileInputStream fin = new FileInputStream(file_name);

      SCParser parser = new SCParser(fin);

      parser.setHasProcessMacroElab(0);

      parser.elaborateComments(commentsProcessing());

      parser.saveFileLineColumnInfoInAST(fileLineColumnSaving());

      parser.setLineMappingTable((lm.lineMapping())?lm.lineDict():null);

      ASTtranslation_unit node = parser.translation_unit();

      if (node == null) {
        System.err.println("Oops. Null reference has been returned from method SCParser::translation_unit ");
        return null;
      }

      if (printMsg)
        System.out.println(" OK!");

      String res_file = output_xml_file;

      // creates XMLHelper instance
      xmlHelperInst = new XMLHelper();

      // creates XML node

      DocumentBuilderFactory dbfInt = DocumentBuilderFactory.newInstance();
      DocumentBuilder dbInt = dbfInt.newDocumentBuilder();
      xmlHelperInst.docInt = dbInt.newDocument();

      xmlHelperInst.mainInt = xmlHelperInst.docInt.createElement("INTERNAL");
      xmlHelperInst.xmlNodesInt = new ArrayList();
      xmlHelperInst.xmlNodesInt.add(xmlHelperInst.mainInt);
      xmlHelperInst.docInt.appendChild(xmlHelperInst.mainInt);

      // creates & starts visitor
      SCDumpVisitor v = new SCDumpVisitor();

      node.jjtAccept(v, xmlHelperInst);

      if (saveAsXMLFile) {

        if (printMsg)
          System.out.print("Saving AST information as XML file " + res_file + " ...");

        // writes AST to internal XML file
        OutputFormat ofInt = new OutputFormat(xmlHelperInst.docInt, "UTF-8", true);
        ofInt.setIndent(2);

        XMLSerializer xmlInt = new XMLSerializer( new FileWriter(res_file), ofInt);
        xmlInt.serialize(xmlHelperInst.docInt);

        if (printMsg)
          System.out.println(" Ok!");

      }




    } catch (FileNotFoundException e) {

      System.err.println("\nOops. " + e.getMessage());
      e.printStackTrace();

      except_occured = true;

    } catch (ParseException e) {

      System.err.println("\nOops. Error encoutered during parse.");
      if (lm.lineMapping())
        System.err.println(SCParser.getErrorMessage());
      else {
        System.err.println(e.getMessage());
        e.printStackTrace();
      }


      except_occured = true;

    } catch (ParserConfigurationException e) {

      System.err.println("\nOops. Error encoutered during creation of XML representation of AST.");
      System.err.println(e.getMessage());
      e.printStackTrace();

      except_occured = true;

    } catch (IOException e) {

      System.err.println("\nOops. Error encoutered during write AST modell into xml file.");
      System.err.println(e.getMessage());
      e.printStackTrace();

      except_occured = true;

    } catch (Exception e) {

      System.err.println("\nOops. " + e.getMessage());
      e.printStackTrace();

      except_occured = true;
    }

    if (!except_occured)
      return xmlHelperInst.docInt;
    else
      return null;

  }
  /**
   *  program entry
   */
  public static void main(String args[]) {

    ParamInf inf = new ParamInf();
    inf.m_file = DEF_OUTPUT_INTERNAL_XML;
    inf.m_com_source_file = null;
    inf.m_user_included_file = null;

    ArrayList fileList = new ArrayList();

    if(!getParams(args, inf, fileList)) {
       System.exit(1);
       return;
    }




    if(DebugLevel != 0) {
      // prints parameters - begin

      System.out.println("Parameters:");
      System.out.println("  Result output file: " + inf.m_file);
      System.out.println("  Source file to parse: " );

      if (fileList.size() == 0) {
        System.out.println("    all source files in working dir");
      }
      else {
        for (int i = 0; i < fileList.size(); ++i)  {
          System.out.println("    " + (String) fileList.get(i));
        }
      }

      System.out.print("  User include file: " );

      if (inf.m_user_included_file != null) {
        System.out.println(inf.m_user_included_file);
      }
      else
        System.out.println(" does not set");

      // prints parameters - end

    }

    if(!elaborateParam(inf, fileList)) {
      System.err.println("ERROR: Can not elaborate command line parameters");
      System.exit(1);
      return;
    }

    // sets to save text comments in AST
    setCommentsProcessing(true);

    // sets to save original file, line, column of the tokens in AST
    setFileLineColumnSaving(true);

    if (parse(fileList, inf.m_file, inf.m_file + ".ast.xml", inf.m_user_included_file, true) == null) {
      System.exit(1);
    }
    else
      System.out.println("Programm finished successfully");
    
  }
}
