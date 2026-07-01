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

/**
 * Implements a JJTree visitor concept.
 * The class generates the XML Document from the generated with SCParser AST.
 */
public class SCDumpVisitor implements SCParserVisitor {
 
  public static final String PROP_NAME = "name";
  public static final String PROP_VALUE = "value";
  public static final String PROP_LENGTH = "length";
  public static final String PROP_NUM_OF_CHANELS = "numOfChanels";
  public static final String PROP_UNARY_OPERATOR = "operator";
  public static final String PROP_SC_FIXED = "sc_fixed";
  public static final String PROP_TOTAL_WORD_LENGTH = "wl";
  public static final String PROP_INTEGER_WORD_LENGTH = "iwl";
  public static final String PROP_QUANT_MODE ="q_mode";
  public static final String PROP_OVERFLOW_MODE ="o_mode";
  public static final String PROP_N_BITS ="n_bits";
  public static final String PROP_CLOCK ="clock";
  public static final String PROP_EDGE ="edge";
  public static final String PROP_ARRAY_COUNTER = "arrayCounter";
  public static final String PROP_TYPE = "type";
  public static final String PROP_RANGE = "range";
  public static final String PROP_KEYWORD = "keyword";
  public static final String PROP_SLAVE_PORT = "slave_port";
  public static final String PROP_SLAVE_METHOD = "slave_method";
  public static final String PROP_SC_DT = "sc_dt";
  public static final String PROP_OBJECT = "object";
  public static final String PROP_TARGET = "target";
  public static final String PROP_INST_ID = "inst_id";
  public static final String PROP_SIG_NAME = "signal";
  public static final String PROP_MIN_VALUE = "min";
  public static final String PROP_MAX_VALUE = "max";
  public static final String PROP_FILE = "file";
  public static final String PROP_LINE = "line";
  public static final String PROP_COLUMN = "column";
  public static final String PROP_IS_ALREADY_DECLARED = "is_declared";  
  public static final String PROP_IS_QUESTIONMARK_EXPR = "is_questionmark_expression";  
  public static final String PROP_IS_UNNAMED = "is_unnamed";  
  
  public static final String SC_IN_RV = "sc_in_rv";
  public static final String SC_OUT_RV = "sc_out_rv";
  public static final String SC_INOUT_RV = "sc_inout_rv";
  public static final String GL_SC_ROUTINE = "sc_routine";
  public static final String IS_KIND_OF_SC = "extSCModule";
  public static final String SC_INDEXED = "sc_indexed";
  public static final String IDREF = "idref";

    private int indent = 0;

    private String indentString() {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < indent; ++i) {
        sb.append(" ");
      }
      return sb.toString();
    }
    
	protected void setBaseNodeAttributes(SimpleNode node, Element el )
        {
           if ( node == null ) 
           {
             return; 
           }
           
           if ( el == null ) 
           {
             return; 
           }
	   
           node.addNameAttributeToElement(el);
           node.addSourceAndLineAttributesToElement(el);
           node.addASTInstanceIdToElement(el);
        }

	public Object visit(SimpleNode node, Object data) {

/*        System.out.println(indentString() + node +
        ": acceptor not unimplemented in subclass?");
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;//dd
        return data;
*/
 	++indent;

        XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());

        
        setBaseNodeAttributes( node, el );
	last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;

	}

	public Object visit(ASTtranslation_unit node, Object data) {
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
         return data;
	}

	public Object visit(SCPortTypes node, Object data) {
 	++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());
        setBaseNodeAttributes( node, el );
  if (node.name() != null) {
    
    el.setAttribute(PROP_LENGTH, Integer.toString(node.length()));
    el.setAttribute(PROP_SC_DT,Boolean.toString(node.systemCDataType()));

    if (node.name().equals(PROP_SC_FIXED)) {
      el.setAttribute(PROP_TOTAL_WORD_LENGTH, Integer.toString(node.totalWordLength()));
      el.setAttribute(PROP_INTEGER_WORD_LENGTH, Integer.toString(node.integerWordLength()));
      el.setAttribute(PROP_QUANT_MODE, node.quantizationMode());
      el.setAttribute(PROP_OVERFLOW_MODE, node.overflowMode());
      el.setAttribute(PROP_N_BITS, Integer.toString(node.numberOfSaturatedBits()));
    }
  }
	last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;
	}



	public Object visit(ASTsc_cthread node, Object data) {
 	++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());

        setBaseNodeAttributes( node, el );
	el.setAttribute(PROP_CLOCK, node.clock());
	el.setAttribute(PROP_EDGE, node.edge());

	last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;
	}


	/* (non-Javadoc)
	 * @see oase.ooas.frontend.systemc.parser.SCParserVisitor#visit(oase.ooas.frontend.systemc.parser.ASTdeclarator_suffixes, java.lang.Object)
	 */
	public Object visit(ASTdeclarator_suffixes node, Object data) {
 	++indent;
        
  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());

        setBaseNodeAttributes( node, el );
	el.setAttribute(PROP_ARRAY_COUNTER, Integer.toString(node.arrayCounter()));

	last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;
	}

	public Object visit(ASTconstant node, Object data) {

 	++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());
        
        if (node.name() != null) {
          el.setAttribute(PROP_VALUE, node.name());
        }
        node.addSourceAndLineAttributesToElement(el);
        node.addASTInstanceIdToElement(el);
  
	last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;

	}


	public Object visit(ASTunary_operator node, Object data) {

 	++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());

        setBaseNodeAttributes( node, el );
        el.setAttribute(PROP_UNARY_OPERATOR, node.operator());
  
        last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;

	}

  public Object visit(ASTsc_port_specifier node, Object data) {

 	++indent;

        XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());

        setBaseNodeAttributes( node, el );
        el.setAttribute(PROP_NUM_OF_CHANELS, Integer.toString(node.numberOfChanels()));

        if (node.name() != null) {

          if (node.name().equals(SC_IN_RV) || node.name().equals(SC_OUT_RV) || node.name().equals(SC_INOUT_RV) ) {
            el.setAttribute(PROP_LENGTH, Integer.toString(node.length()));
          }

        }

	last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;

}
  public Object visit(ASTclass_specifier node, Object data)
  {

  ++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());
  
        setBaseNodeAttributes( node, el );  
        el.setAttribute(IS_KIND_OF_SC, Boolean.toString(node.extOfSCClass()));
        if (node.keyword() != null) {
          el.setAttribute(PROP_KEYWORD, node.keyword());
        }
  
  
	last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;

  }

  public Object visit(ASTm_s_protocol_declaration node, Object data)
  {

  ++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());
  
  setBaseNodeAttributes( node, el );
  if (node.name() != null) {

    if (node.name().equals(SC_INDEXED)) {
      el.setAttribute(PROP_RANGE, Integer.toString(node.range()));
    }
  }

  if (node.protocol() != null) {
    el.setAttribute(PROP_TYPE, node.protocol());
  }
  
  
  last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;

  }
   

public Object visit(ASTsc_slave node, Object data)
{
  ++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());
  if (node.slaveMethod() != null) {

    el.setAttribute(PROP_SLAVE_METHOD, node.slaveMethod());

  }

  if (node.slavePort() != null) {
    el.setAttribute(PROP_SLAVE_PORT, node.slavePort());
  }

  setBaseNodeAttributes( node, el );
  
	last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;
  
}

public Object visit(ASTsc_map_to_target node, Object data)
{
  ++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());
  
  setBaseNodeAttributes( node, el );
  el.setAttribute(PROP_OBJECT, node.object());
  el.setAttribute(PROP_TARGET, node.target());
  el.setAttribute(PROP_INST_ID, node.instanceId());
  
  
  last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;

}

public Object visit(ASTfzi_sim_directive node, Object data)
{
  ++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());

        setBaseNodeAttributes( node, el );  

  if ( node.typeId() == 0) {
    el.setAttribute(PROP_MIN_VALUE, Integer.toString(node.minValue()));
    el.setAttribute(PROP_MAX_VALUE, Integer.toString(node.maxValue()));    
  }
  else
  if ( node.typeId() == 1) {
    el.setAttribute(PROP_SIG_NAME, node.signalName());
  }

  
  
  last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;

}

public Object visit(ASTconditional_expression node, Object data)
{
  ++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());
  setBaseNodeAttributes( node, el );
  el.setAttribute(PROP_IS_QUESTIONMARK_EXPR, Boolean.toString(node.isQuestionmarkExpression()));

       
  last.appendChild(el);
	helperRef.xmlNodesInt.add(el);

	data = node.childrenAccept(this, data);

	helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

	--indent;
 	return data;

}

public Object visit(ASTbit_field_member_declaration node, Object data)
{
  ++indent;

  XMLHelper helperRef = (XMLHelper)data;
	Element last;
	last = (Element)helperRef.xmlNodesInt.get(helperRef.xmlNodesInt.size()-1);
	Element el;
	el = helperRef.docInt.createElement(node.toString());
  setBaseNodeAttributes( node, el );
  el.setAttribute(PROP_IS_UNNAMED, Boolean.toString( ! node.isNamed() ) );
  
       
  last.appendChild(el);
  helperRef.xmlNodesInt.add(el);

  data = node.childrenAccept(this, data);

  helperRef.xmlNodesInt.remove(helperRef.xmlNodesInt.size()-1);

  --indent;
  return data;

}

}

  