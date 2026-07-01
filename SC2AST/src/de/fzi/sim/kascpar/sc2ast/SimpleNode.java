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

import org.w3c.dom.Element;

import de.fzi.sim.kascpar.util.TokenInfo;
/**
 * Provides th base functionality for the AST nodes.
 */
public class SimpleNode implements Node {
  protected Node parent;
  protected Node[] children;
/*
  added by PPA
  BEGIN
*/
  /**
   * A JJTree node id.
   */
  protected int id;
  /**
   * A reference to the parser.
   */
  protected SCParser parser;
  /**
   * A name of the object of the SimpleNode type.
   */
  protected String m_name;
  /**
   * Contains a line mapping information for this node.
   * @see de.fzi.sim.kascpar.util.TokenInfo
   */
  protected TokenInfo m_refInfo;
 
  /**
   * An unique AST instance id. 
   * It might be used for referencing between AST objects.
   *
   */
  protected long m_ASTInstanceId;
  /**
   * Controls the comment elabaraton during the parse.
   */  
  static boolean m_elaborateComments = false;
  /**
   * Controls the line mapping informatoin saving into the XML Document.
   */  
  static boolean m_fileLineColumnSaving = false;
  /**
   * A Constructor.
   * @param i - a JJTree node id.
   */  
  public SimpleNode(int i) {
    id = i;
    m_name = null;
    m_refInfo = SCParser.getExtLineInfo(1);

    if(m_elaborateComments)
      SCParser.addCommentToAST();

    m_ASTInstanceId = SCParser.getNewASTInstanceID();
  }
  /**
   * A Constructor.
   * @param p - a reference to the parser.
   * @param i - a JJTree node id.
   */  
  public SimpleNode(SCParser p, int i) {
    this(i);
    parser = p;
    m_name = null;
    m_refInfo = SCParser.getExtLineInfo(1);

    if(m_elaborateComments)
      SCParser.addCommentToAST();

    m_ASTInstanceId = SCParser.getNewASTInstanceID();
  }
  /**
   * Sets a name of the object of the SimpleNode type.
   * It is used in derived classes.
   * @param n - a name of the object of the SimpleNode type.
   */
  public void setName(String n){
    m_name = n;
  }

  /**
   * Gets the name of the object of the SimpleNode type.
   */
  public String name() {
    return m_name;
  }

  public long getASTInstanceId() {
    return m_ASTInstanceId;
  }
   
  /**
   * Returns path to the original file where this node was found
   * if the line mapping information for this node is exist, null otherwise.
   */
  public String sourceFilePath() {
    if (m_refInfo != null)
  	  return m_refInfo.refInfo().file();
    else
      return null;	
  }

  /**
   * Returns only original file (without directories) where this node was found
   * if the line mapping information for this node is exist, null otherwise.
   */
  public String sourceFile() {
    if (m_refInfo != null) {
      String d = m_refInfo.refInfo().file();
      int pos = d.lastIndexOf("/");
      if (pos == -1)
        return d;
      else
        return d.substring(pos+1);
    } 
    else {
      
      return null;
    }  
  }

  /**
   * Returns line in the original file where this node was found
   * if the line mapping information for this node is exist, -1 otherwise.
   */
  public int line() {
    if (m_refInfo != null)
      return m_refInfo.refInfo().line() + 1;
      else
        return -1;
  }

  /**
   * Sets two attributes: "file" and "line" to the element  
   * if the line mapping information for this node was found, nothing otherwise.
   * 
   */
  public void addSourceAndLineAttributesToElement(Element e)
  {

    if (m_fileLineColumnSaving == false)
      return;
      
    if (e != null) {
      if (sourceFile() != null) {
        e.setAttribute(SCDumpVisitor.PROP_FILE, sourceFile());
        e.setAttribute(SCDumpVisitor.PROP_LINE, Integer.toString(line()));
        e.setAttribute(SCDumpVisitor.PROP_COLUMN, Integer.toString( m_refInfo.column()));
      }
      else {
        
      }  
    }

  }
  /**
   * Sets the "name" attribute to the element
   * if the name of this node is known, nothing otherwise.
   *
   */
  public void addNameAttributeToElement(Element e) {
    if (e != null && name() != null) {
      e.setAttribute(SCDumpVisitor.PROP_NAME, name());
    }
  }

  /**
   * Sets the "idref" attribute to the element. 
   * Idref means that a value of the idref attribute will be unique for all objects in the xml file.
   */

  public void addASTInstanceIdToElement(Element e) {
    if (e != null) {
      e.setAttribute(SCDumpVisitor.IDREF, Long.toString(getASTInstanceId()));
    }
  }
  
  /*
    added by PPA
    END
  */
  
  public void jjtOpen() {
  }

  public void jjtClose() {
  }
  
  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  /** Accept the visitor. **/
  public Object jjtAccept(SCParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  /** Accept the visitor. **/
  public Object childrenAccept(SCParserVisitor visitor, Object data) {
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        children[i].jjtAccept(visitor, data);
      }
    }
    return data;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() { return SCParserTreeConstants.jjtNodeName[id]; }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
	SimpleNode n = (SimpleNode)children[i];
	if (n != null) {
	  n.dump(prefix + " ");
	}
      }
    }
  }
}

