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

import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;

 /**
  * Provides the interface to save named and unnamed scopes, types.
  * The class supports also namespace using declarations and directives of the ANSI C/C++ standart.
  * @see ClassScope
  * @see SymtabManager
  */

public class Scope {
    /**
     * Name of the scope (set only for class/function scopes).
     */
    String scopeName;

    /**
     * Indicates whether this is a class scope or not.
     */
    boolean type;     // Indicates if this is a type.


    /**
     * (partial) table of type symbols introduced in this scope.
     */
    Hashtable typeTable = new Hashtable();

    /**
     * stores children scopes.
     */
    LinkedList children = new LinkedList();

    /**
     * Parent scope. (null if it is the global scope).
     */
    Scope parent;

    /**
     *  stores using directives
     */
    Vector includedNamespaces = null;

    /**
     *  stores using declarations 
     */
    Vector includedTypesFromNamespace = null; 

    /**
     *  it is used for namespace renaming
     */
    Scope refNamespace = null;

    /**
     * Registers the scope sc as a original namespace for this scope.
     * @param sc - a original namespace for this scope
     */ 
    public void SetReferenceNamespace(Scope sc) {
      refNamespace = sc;
    }
    
    /**
     * Adds a named scope from the using declaration.
     * @param sc - a declared in the namespace data type
     */
    public void AddTypeFromNamespace(Scope sc) {
        if (sc == null)
            return;

        if (includedTypesFromNamespace == null)
            includedTypesFromNamespace = new Vector();

        includedTypesFromNamespace.addElement(sc);
    }
    
    /**
     * Adds a type name from the using declaration.
     * @param name - a declared in the namespace data type
     */
    public void AddTypeFromNamespace(String name) {
        if (name == null)
            return;

        if (includedTypesFromNamespace == null)
            includedTypesFromNamespace = new Vector();

        includedTypesFromNamespace.addElement(name);
    }

    /**
     * Adds a namespace from the namespace directive.
     * @param sc - a namespace scope
     */

    public void AddNamespace(Scope sc) {
        if (sc == null)
            return;

        if (includedNamespaces == null)
            includedNamespaces = new Vector();

        includedNamespaces.addElement(sc);
    }

    /**
     * Returns a scope name.
     * @return - a scope name.
     */
    public String getName()
    {
      return scopeName;
    }

    /**
     * Tests if the scope is a type.
     * @return - true if the scope is a type, false otherwise.
     */
     public boolean IsType() { return type; }

    /**
     * Registers the scope sc as a child for this scope.
     * @param sc - a scope.
     */ 
    void AddChild(Scope sc) {

      if ( sc != null ) {
        children.add(sc);
        if (sc.getName() != null ) {
          PutTypeName(sc.getName(), sc);
        }
      }
    }

    /**
     * Returns a list of children scopes of this scope.
     * @return a list of children scopes of this scope.
     */
    LinkedList getChildren() {
      return children;
    }
    
    /**
     * Creates a scope object with a given name.
     * @param name - a created name of scope.
     * @param isType - indicates if the scope is a type or not.
     * @param p - a parent scope.
     */
    public Scope(String name, boolean isType, Scope p) {
        scopeName = name;
        type = isType;
        parent = p;
        if (parent != null)
          parent.AddChild(this);
    }

    /**
     * Creates an unnamed scope (like for compound statements).
     * @param p - a parent scope.
     */
    public Scope(Scope p) {
        type = false;
        parent = p;
        if (parent != null)
          parent.AddChild(this);
    }

    /**
     * Inserts a name into the table to say that it is the name of a type.
     * @param name - a type name.
     */
    public void PutTypeName(String name) {

        typeTable.put(name, name);
    }

    /**
     * Inserts a type with a scope (class/struct/union) into the table.
     * @param name - a scope name.
     * @param sc - a scope.
     */
    public void PutTypeName(String name, Scope sc) {
        typeTable.put(name, sc);
    }

    /**
     * Tests if the scope is a namespace.
     * @return true if the scope is a namespace, false otherwise.
     */

    public boolean IsNamespace() {

      return (this instanceof Scope && getName() != null && IsType() == false);
    }
    /**
     * Tests if the scope is an alias namespace.
     * @return - true if the scope is an alias namespace, false otherwise.
     */
    public boolean IsAliasNamespace() {
      return ( refNamespace != null && IsNamespace() );
        
    }

    /**
     * Tests if the scope is a class (struct, union).
     * @return true if the scope is a class (struct, union), false otherwise.
     */
     
    public boolean IsClass() {
      return ((this instanceof Scope || this instanceof ClassScope) && getName() != null && IsType() == true);
    }
    
    /**
     * Checks if a given name is the name of a type in this scope.
     * @param name - a possible type name.
     * @return true if the specified name is a type, false otherwise.
     */
    public boolean IsTypeName2(String name) {

      Object obj = typeTable.get(name);
        if(obj != null) {
          if(obj instanceof Scope)
            return !((Scope)obj).IsNamespace();
          else
            return true; 
        }
        return false;
    }

    
    /**
     * Checks if a given name is the name of a type.
     * If a name is not found in this scope then the method tries to find in
     * the original namespace (if it is set) or into list of using declaration,
     * into specified scopes with using directives and into the parent scopes.
     * @param name - a possible type name.
     * @return true if the specified name is a type, false otherwise.
     */

    public boolean IsTypeName(String name) {
      
      return IsTypeName(name, true);
    }
    
    /**
     * Checks if a given name is the name of a type.
     * If a name is not found in this scope then the method tries to find in
     * the original namespace (if it is set) or into list of using declaration,
     * into specified scopes with using directives and into the parent scopes.
     * @param name - a possible type name.
     * @param searchInParent - allows/prohibits the searching of type into parent scopes.
     * @return true if the specified name is a type, false otherwise.
     */
    boolean IsTypeName(String name, boolean searchInParent) {

        if (refNamespace != null) {
          return refNamespace.IsTypeName(name, searchInParent);
        }
        

        if(IsTypeName2(name)) {
          return true;
        }


        if (includedTypesFromNamespace != null) {


          for (int i = 0; i < includedTypesFromNamespace.size(); i++) {
              Object sc = includedTypesFromNamespace.elementAt(i);
              String type = null;
              if (sc instanceof Scope || sc instanceof ClassScope) {
                 type = ((Scope) sc).getName();
              }
              else {
                 type = (String)sc;
              }
              if (type.equals(name))
                  return true;
          }        
        }

        
        if (includedNamespaces != null) {

          for (int i = 0; i < includedNamespaces.size(); i++) {
              Scope sc = (Scope) includedNamespaces.elementAt(i);
              if (sc.IsTypeName(name,false))
                  return true;
                  
          }
        }
        

        if (parent != null && searchInParent) {
          return parent.IsTypeName(name);
        }
                
        return false;        
    }
    /**
     * Returns a parent scope.
     * @return a parent scope.
     */
    public Scope Parent() { return parent; }

    /**
     * Checks if a given name is the name of a scope.
     * If a name is not found in this scope then the method tries to find in
     * the original namespace (if it is set) or into specified scopes with using directives
     * and into the parent scopes.
     * @param name - a possible scope name.
     * @return fount object if the specified name is a scope, null otherwise.
     */
    public Scope GetScope(String name) {
      return GetScope(name, true);
    }

    /**
     * Checks if a given name is the name of a scope.
     * If a name is not found in this scope then the method tries to find in
     * the original namespace (if it is set) or into specified scopes with using directives
     * and into the parent scopes.
     * @param name - a possible scope name.
     * @param searchInParent - allows/prohibits the searching of type into parent scopes.
     * @return fount object if the specified name is a scope, null otherwise.
     */
    Scope GetScope(String name, boolean searchInParent) {

        if (refNamespace != null) {
          
          return refNamespace.GetScope(name, searchInParent);
        }
      
        Object sc = typeTable.get(name);
        if (sc != null) {
          if (sc instanceof Scope || sc instanceof ClassScope)
              return (Scope) sc;
        }

        if (includedNamespaces != null) {

          for (int i = 0; i < includedNamespaces.size(); i++) {
              Scope res = ((Scope) includedNamespaces.elementAt(i)).GetScope(name,false);
              if (res != null)
                return res;  
          }
        }

        
        if (parent != null && searchInParent) {
          return parent.GetScope(name);
        }
        
        
        return null;
    }

   
}
