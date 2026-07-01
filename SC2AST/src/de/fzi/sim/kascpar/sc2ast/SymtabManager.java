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
 * Manages the symbol table and scopes within a given compilation unit.
 * @see Scope
 * @see ClassScope   
 */
public class SymtabManager {

    /**
     * Pointers to the root scope, the scope is added extra for the access to the information only.
     */
    static Scope root = new Scope(null);

    /**
     * Pointers to the current scope.
     */
    static Scope currScope = null;
    
    /**
     * Opens a new scope (with optional name and type flag).
     *
     * @param scopeName  the name to set. The new scope will be created with this name.
     *                   If the scopeName is null, the routine creats a unnamed scope.
     * @param isType    the boolean flag is used to control a type of the created object.
     *                  It can be object of the Scope (isType == false) or the ClassScope (isType == true) type.
     * @return   the new object of the Scope or the ClassScope type.
     * @see Scope
     * @see ClassScope     
     */
    public static Scope OpenScope(String scopeName, boolean isType) {
        Scope newScope;

        if (currScope == null) {
          currScope = root;
        }
        
        if (scopeName != null) {

            if (isType) {
                newScope = new ClassScope(scopeName, currScope);
            } else {
                newScope = new Scope(scopeName, isType, currScope);
            }


        } else {

            newScope = new Scope(currScope);
        }    

        currScope = newScope;
        return newScope;
    }
    /**
     * Registers the scope sc as a current scope.
     * @see Scope
     * @see ClassScope     
     */
    public static void SetCurrentScope(Scope sc) {

        currScope = sc;
    }

    /**
     * Inserts type with name tname in the type table of the current scope.
     */
    public static void PutTypeName(String tname) {

        currScope.PutTypeName(tname);
    }


    /**
     * Tests if the specified name is a fully scoped type.
     *
     * The method tries to find all scopes in name and then searchs a type in scope type table.
     * For instance, A::B::MyType,
     * where A, B are scopes, MyType - is a type and it is searched in type table of the scope B;
     * @param name - possible type. 
     * @return true if and only if the specified name is a fully scoped type, false otherwise.
     */
    public static boolean IsFullyScopedTypeName(String name) {

        if (name == null)
            return false;

        if (name.indexOf("::") == -1)
            return IsTypeName(name);

        Scope sc = GetScopeOfFullyScopedName(name);

        if (sc != null)
            return sc.IsTypeName(name.substring(name.lastIndexOf("::") + 2,
                    name.length()));

        return false;
    }

    /**
     * Tests if the specified name is a type.
     *
     * For that the method searchs this type in the current scope, then continues in the super class scopes.
     *
     * @param name - a possible type.
     * @return true if the specified name is a type, false otherwise.
     * @see Scope
     * @see ClassScope
     */
    public static boolean IsTypeName(String name) {
        return currScope.IsTypeName(name);
    }

    /**
     * Sets a parent of the current scope as a current.
     */
    public static void CloseScope() {
         
        if (currScope != root)
          currScope = currScope.Parent();
  
    }

   /**
    * For now, we just say if it is a class name, it is OK to call it a
    * constructor.
    * @param name - a possible constructor name.
    * @return true if the specified name is a constructor, false otherwise.
    */
    public static boolean IsCtor(String name) {

        if (name == null)
            return false;

        if (name.indexOf("::") == -1)
            return GetScope(name) != null;

        Scope sc = GetScopeOfFullyScopedName(name);

        if (sc != null && sc.parent != null)
            return sc.parent.GetScope(name.substring(name.lastIndexOf("::") + 2,
                    name.length())) == sc;

        return false;
    }

    /**
     * Returns a current scope.
     * @return a current scope.
     * @see Scope     
     */
    public static Scope GetCurScope() {
        return currScope;
    }

    /**
     * Searches a scope by name scope_name.
     *
     *
     * @param scope_name - a scope name.
     * @return a found scope object, null otherwise.
     * @see Scope#GetScope(String)
     * @see Scope
     */

    public static Scope GetScope(String scope_name) {
          return currScope.GetScope(scope_name);
    }

    /**
     * Returns the Scope of B in A::B::C.
     * @param name - a fully scoped name.
     * @return a found scope object, null otherwise.     
     * @see Scope
     */
    public static Scope GetScopeOfFullyScopedName(String name) {
        Scope sc;
        int i = 0, j = 0;

        if (name.indexOf("::") == -1)
            return GetScope(name);

        if (name.indexOf("::") == 0) {
            sc = (Scope)root.getChildren().getFirst();//scopeStack[1];
            j = 2;
        } else
            sc = GetCurScope();

        int k = name.lastIndexOf("::");
        
        if (k < j)
          return sc;
          
        String tmp = name.substring(j, k);

        while ((j = tmp.indexOf("::", i)) != -1) {
            sc = sc.GetScope(tmp.substring(i, j));
            i = j + 2;

            if (sc == null)
                return null;
        }

        if (sc == GetCurScope())
            return GetScope(tmp.substring(i, tmp.length()));

        return sc.GetScope(tmp.substring(i, tmp.length()));
    }

    /**
     * Returns the scope object of the scope name.
     * For instance, the method returns C in "A::B::C" or C in "C" if C is a nested scope in the current scope.
     * @param scope_name - a scope or a scoped name.
     * @return a found scope object, null otherwise.
     * @see Scope
     */
    public static Scope GetScopeOfScopedName(String scope_name) {
      int id = scope_name.lastIndexOf("::");
      if (id == -1) {
        return GetScope(scope_name);
      }

      Scope sc = GetScopeOfFullyScopedName(scope_name);
      if (sc == null)
        return null;

      return sc.GetScope(scope_name.substring(id + 2));
      
    }

        

}
