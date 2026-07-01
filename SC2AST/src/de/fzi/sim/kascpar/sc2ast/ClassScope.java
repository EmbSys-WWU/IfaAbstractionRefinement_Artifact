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

import java.util.Vector;

/**
 * Class scope extends Scope in that its search method also searches all its
 * superclasses.
 * @see Scope
 * @see SymtabManager
 */

public class ClassScope extends Scope {
    /**
     * The list of scopes corresponding to classes this class inherits.
     */
    Vector superClasses;

    /**
     * Add a super class.
     * @param sc - a scope of the inherited class.
     */
    public void AddSuper(Scope sc) {
        if (sc == null)
            return;

        if (superClasses == null)
            superClasses = new Vector();

        superClasses.addElement(sc);
    }

    /**
     * Overrides the method in Scope class. It also searches in the inherited classes'
     * scopes also.
     * @param name - a possible type.
     * @return true if the specified name is a type, false otherwise.
     * @see Scope
     */
    public boolean IsTypeName(String name) {
        if (super.IsTypeName(name))
            return true;

        if (superClasses == null)
            return false;

        for (int i = 0; i < superClasses.size(); i++)
            if (((Scope) superClasses.elementAt(i)).IsTypeName(name))
                return true;

        return false;
    }

    /**
     * Creates a new class scope in a given scope.
     * @param name - a class scope name.
     * @param parent - a scope of the inherited class.
     * @see Scope
     */
    public ClassScope(String name, Scope parent) {
        super(name, true, parent);
    }
}
