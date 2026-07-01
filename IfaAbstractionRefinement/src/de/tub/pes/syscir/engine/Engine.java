/*****************************************************************************
 *
 * Copyright (c) 2008-17, Joachim Fellmuth, Holger Gross, Florian Greiner, Bettina Hünnemeyer, Paula
 * Herber, Verena Klös, Timm Liebrenz, Tobias Pfeffer, Marcel Pockrandt, Rolf Schröder, Simon Schwan
 * Technische Universitaet Berlin, Software and Embedded Systems Engineering Group,
 * Ernst-Reuter-Platz 7, 10587 Berlin, Germany. All rights reserved.
 * 
 * This file is part of STATE (SystemC to Timed Automata Transformation Engine).
 * 
 * STATE is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * STATE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with STATE. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * Please report any problems or bugs to: state@pes.tu-berlin.de
 *
 ****************************************************************************/

package de.tub.pes.syscir.engine;

import de.tub.pes.syscir.engine.nodetransformer.NodeTransformer;
import de.tub.pes.syscir.engine.util.IOUtil;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCSystem;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Engine {


    /*
     * Log4j2 logger for this class (named after the class, as advised by Apache).
     */
    private static Logger logger = LogManager.getLogger(Engine.class.getName());


    /**
     * Parses the specified AST file and returns an SCSystem-object representing the AST.
     * 
     * @param file absolute path to the AST file
     * @return SCSystem-object representing the AST.
     */
    public static SCSystem buildModelFromFile(String file) {
        // parsing the xml document into a dom-document
        Document xml = null;
        try {
            xml = IOUtil.readXML(file);
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            System.err.println("Could not find file \"" + file + "\". Please check path.");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }

        Environment e = parseSystem(xml);

        return e.getSystem();
    }

    /**
     * Generates a filled environment by stepwise parsing of the submitted xml document. This method can
     * be used to parse whole SystemC models.
     * 
     * @param xml
     * @return
     */
    public static Environment parseSystem(Document xml) {
        // generating the java model
        // phase 1 - building modules, knowntypes and method headers
        Environment e = parseTree(xml);
        e.setFunctionBlock(false);
        // phase 2 - generating sc_main body
        e = SystemBuilding(e);
        // phase 3 - generating body of global functions
        e = SystemFunctionParsing(e);
        // phase 4 - generating body for all class functions
        e = ModuleFunctionParsing(e);
        // phase 5 - generating body of known type functions
        e = KnownTypeFunctionParsing(e);
        e.setFunctionBlock(true);
        return e;
    }

    /**
     * Generates a filled environment by stepwise parsing of the submitted xml document. This method can
     * be used to parse implementation files only containing parts of a systemc system (e.g.,
     * implementations for a specific channel).
     * 
     * @param xml
     * @return
     */
    public static Environment parseImplementation(Document xml) {
        // generating the java model
        // phase 1 - building modules, knowntypes and method headers
        Environment e = parseTree(xml);
        e.setFunctionBlock(false);
        // ignore phase 2, as there is no main function
        // phase 3 - generating body of global functions
        e = SystemFunctionParsing(e);
        // phase 4 - generating body of module functions
        e = ModuleFunctionParsing(e);
        // phase 5 - generating body of known type functions
        e = KnownTypeFunctionParsing(e);
        e.setFunctionBlock(true);
        return e;
    }

    /**
     * Starts the parsing phase of the transformation by retrieving a transformer for the root node of
     * the given XML document.
     * 
     * @param doc
     */
    private static Environment parseTree(Document doc) {
        Node start = doc.getFirstChild();


        Environment e = new Environment();
        NodeTransformer t = e.getTransformerFactory().getNodeTransformer(start);

        t.transformNode(start, e);

        return e;
    }

    /**
     * builds the system by parsing the mainfunction
     * 
     * @param e gets the environment which contains all modules, Structs, Knowntypes, Functions,
     *        Functionbodys etc.
     * @return this environment after the systembuilding, with moduleInstances, and portbindings, etc.
     */
    private static Environment SystemBuilding(Environment e) {
        HashMap<String, List<Node>> system_functions = e.getFunctionBodys().get("system");
        for (SCFunction fct : e.getSystem().getGlobalFunctions()) {
            if (fct.getName().equals("sc_main")) {
                List<Node> building = system_functions.get(fct.getName());
                e.setCurrentFunction(fct);
                e.setCurrentClass(null);
                e.setSystemBuilding(true);
                for (Node n : building) {
                    NodeTransformer t = e.getTransformerFactory().getNodeTransformer(n);
                    t.transformNode(n, e);
                }
                e.setSystemBuilding(false);
                e.getFunctionBodys().get("system").remove(fct.getName());
                return e;
            } else {

            }
        }
        logger.error("No Mainfunction found");
        return e;
    }

    /**
     * parses the global functions of the system
     * 
     * @param e gets the environment which contains all modules, Structs, Knowntypes, Functions,
     *        Functionbodys etc.
     * @return this environment where the functionbodys of the global functions are parsed
     */
    private static Environment SystemFunctionParsing(Environment e) {
        HashMap<String, List<Node>> system_functions = e.getFunctionBodys().get("system");
        for (SCFunction fct : e.getSystem().getGlobalFunctions()) {
            if (!fct.getName().equals("sc_main")) {
                List<Node> building = system_functions.get(fct.getName());
                e.setCurrentFunction(fct);
                e.setCurrentClass(null);
                e.setSystemBuilding(false);
                if (building != null) {
                    for (Node n : building) {
                        NodeTransformer t = e.getTransformerFactory().getNodeTransformer(n);
                        t.transformNode(n, e);
                    }
                    e.setSystemBuilding(false);
                    e.getFunctionBodys().get("system").remove(fct.getName());
                } else {
                    logger.debug("Encountered a function without any body. System, Function:  {}", fct.getName());
                }
            }
        }

        return e;
    }

    /**
     * parses the functions of all found modules
     * 
     * @param e gets the environment which contains all modules, Structs, Knowntypes, Functions,
     *        Functionbodys etc.
     * @return this environment where the functionbodys of the module functions are parsed
     */
    private static Environment ModuleFunctionParsing(Environment e) {
        for (SCClass cl : e.getClassList().values()) {

            // if the module is generated from an external source, it's already
            // finished when we get here
            if (!cl.isExternal()) {
                HashMap<String, List<Node>> functions = e.getFunctionBodys().get(cl.getName());
                e.setCurrentClass(cl);
                e.setSystemBuilding(false);
                for (SCFunction fct : cl.getMemberFunctions()) {
                    // if (fct.getBody() == null || fct.getBody().isEmpty()) {
                    if (fct.getName().equals(cl.getName())) {
                        e.setInConstructor(true);
                    }
                    List<Node> building = functions.get(fct.getName());
                    if (building != null) {
                        e.setCurrentFunction(fct);
                        for (Node n : building) {
                            NodeTransformer t = e.getTransformerFactory().getNodeTransformer(n);
                            t.transformNode(n, e);
                        }
                        if (fct.getBody().isEmpty()) {
                            logger.debug("Encountered a function without any body. Module: {} Function: {}",
                                    cl.getName(), fct.getName());
                        }
                        e.getFunctionBodys().get(cl.getName()).remove(fct.getName());
                    } else {
                        if (fct.getBody().isEmpty()) {
                            logger.debug("Encountered a function without any body. Module: {} Function: {}",
                                    cl.getName(), fct.getName());
                        }

                    }
                    e.setInConstructor(false);
                    // }
                }
                e.getFunctionBodys().remove(cl.getName());
            }

        }
        return e;
    }

    /**
     * parses the functions of all found knwontypes
     * 
     * @param e gets the environment which contains all modules, Structs, Knowntypes, Functions,
     *        Functionbodys etc.
     * @return this environment where the functionbodys of the knowntype functions are parsed
     */
    private static Environment KnownTypeFunctionParsing(Environment e) {
        for (Entry<String, SCClass> classSet : e.getKnownTypes().entrySet()) {
            SCClass cl = classSet.getValue();
            HashMap<String, List<Node>> functions = e.getFunctionBodys().get(cl.getName());
            e.setCurrentClass(cl);

            e.setSystemBuilding(false);
            if (functions != null) {
                for (SCFunction fct : cl.getMemberFunctions()) {
                    List<Node> building = functions.get(fct.getName());
                    if (building != null) {
                        e.setCurrentFunction(fct);
                        for (Node n : building) {
                            NodeTransformer t = e.getTransformerFactory().getNodeTransformer(n);
                            t.transformNode(n, e);
                        }
                        e.getFunctionBodys().get(cl.getName()).remove(fct.getName());
                    } else {
                        logger.debug("Encountered a function without any body. Module: {} Function: {}", cl.getName(),
                                fct.getName());
                    }
                }
                e.getFunctionBodys().remove(cl.getName());
            }
        }
        return e;
    }

    /**
     * opens a filedialoge for picking a .ast.xml-file to transform
     * 
     * @return the string of the filepath
     */
    public static String getFilePath() {
        JFileChooser fc = new JFileChooser();

        fc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".ast.xml") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "AST-Files(*.ast.xml)";
            }
        });

        int state = fc.showOpenDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            return file.getAbsolutePath();
        } else {
            logger.debug("Selection cancelled");
        }
        return "";
    }



}
