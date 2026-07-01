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

package de.tub.pes.syscir.engine.nodetransformer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCConnectionInterface;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.SCPort;
import de.tub.pes.syscir.sc_model.SCPortInstance;
import de.tub.pes.syscir.sc_model.SCSocket;
import de.tub.pes.syscir.sc_model.SCSocketInstance;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.AccessExpression;
import de.tub.pes.syscir.sc_model.expressions.AssertionExpression;
import de.tub.pes.syscir.sc_model.expressions.BinaryExpression;
import de.tub.pes.syscir.sc_model.expressions.BracketExpression;
import de.tub.pes.syscir.sc_model.expressions.EndlineExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.FunctionCallExpression;
import de.tub.pes.syscir.sc_model.expressions.NameExpression;
import de.tub.pes.syscir.sc_model.expressions.RefDerefExpression;
import de.tub.pes.syscir.sc_model.expressions.SCClassInstanceExpression;
import de.tub.pes.syscir.sc_model.expressions.SCDeltaCountExpression;
import de.tub.pes.syscir.sc_model.expressions.SCPortSCSocketExpression;
import de.tub.pes.syscir.sc_model.expressions.SCStopExpression;
import de.tub.pes.syscir.sc_model.expressions.SCTimeStampExpression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableExpression;
import de.tub.pes.syscir.sc_model.expressions.UnaryExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;
import de.tub.pes.syscir.sc_model.variables.SCPointer;

/**
 * Handles all postfix expression nodes. Postfix expression nodes are used for many different
 * constructs. They can represent field and method accesses to classes (e.g. a.x, p->y, t.foo())
 * 
 * note: whenever it says n.getNextSibling().getNextSibling() you *may* encounter a problem when you
 * use an optimized xml file (with no whitespaces between ">" and "<"). Please use
 * getNextUsefulSiblig() instead.
 *
 * @author pockrandt, Timm Liebrenz
 * 
 */
public class PostfixExpressionTransformer extends AbstractNodeTransformer {
    
    private static Logger logger = LogManager.getLogger(PostfixExpressionTransformer.class.getName());
    
    @Override
    public void transformNode(Node node, Environment e) {
        
        SCClass oldClass = e.getCurrentClass();
        SCFunction oldFunction = e.getCurrentFunction();
        
        if (oldFunction != null && oldClass == null && e.isSystemBuilding()
                && e.getCurrentFunction().getName().equals("sc_main")) {
            // We are in the sc_main function. no other PostfixExpression than a
            // Port/Socket Binding is allowed here
            handlePortSocketBindings(node, e);
        } else if (oldFunction != null && oldClass != null && oldFunction.equals(oldClass.getConstructor())
                && findChildNode(findChildNode(findChildNode(findChildNode(node, "arguments"), "arguments_list"),
                        "primary_expression"), "id_expression") != null) {
            // TODO mp: this is not really good. Is there a better way to
            // discern between port/socket bindings and other postfix expression
            // (e.g. sensitive-statements) in a constructor?
            // we are in the constructor of a class and the post fix expression
            // has arguments. Therefore it should not be a sensitivity
            // expression but a port/socket binding
            handlePortSocketBindings(node, e);
        } else {
            Node n = findChildNode(node, "primary_expression");
            handleChildNodes(n, e);
            Expression exp = e.getExpressionStack().pop();
            
            if (exp instanceof NameExpression || exp instanceof SCTimeStampExpression
                    || exp instanceof SCDeltaCountExpression || exp instanceof EndlineExpression) {
                e.getExpressionStack().add(exp);
                return;
            }
            
            // There are some cases when the expression is encapsuled inside a
            // unary expression. We unwrap the expression in this case.
            if (exp instanceof UnaryExpression ue) {
                exp = ue.getExpression();
            }
            
            if (exp instanceof SCClassInstanceExpression classInstExp) {
                // ModuleInstance
                handleSCClassInstanceExpression(e, n, oldClass, oldFunction, classInstExp);
            } else if (exp instanceof SCPortSCSocketExpression scPSCSExp) {
                handleSCPortSCSocketExpression(e, n, oldClass, oldFunction, scPSCSExp);
            } else if (exp instanceof FunctionCallExpression fcExp) {
                handleFunctionCallExpression(n, e, oldClass, oldFunction, fcExp);
            } else if (exp instanceof SCVariableExpression varExp) {
                // Variable
                handleSCVariableExpression(e, n, oldClass, oldFunction, varExp);
            } else if (exp instanceof BinaryExpression binaryExp) {
                handleBinaryExpression(e, n, oldClass, oldFunction, binaryExp);
            } else if (exp instanceof SCStopExpression) {
                // do nothing
                e.getExpressionStack().push(exp);
            } else if (exp instanceof RefDerefExpression refDerefExp) {
                handleRefDerefExpression(e, n, oldClass, oldFunction, refDerefExp);
            } // If it is an assertionExpression, we have to get the condition of
              // the assertion.
            else if (exp instanceof AssertionExpression assExp) {
                handleAssertionExpression(e, n, oldClass, oldFunction, assExp);
            } else {
                // its not a variable, but we handle it like one
                standardHandling(e, n, oldClass, oldFunction, exp);
            }
            restoreEnvironment(oldClass, oldFunction, e);
            
        }
        
    }
    
    /**
     * method to support referencing structs with access to member variable
     *
     * @param e
     * @param n
     * @param oldClass
     * @param oldFunction
     * @param refDerefExp
     */
    private void handleRefDerefExpression(Environment e, Node n, SCClass oldClass, SCFunction oldFunction,
            RefDerefExpression refDerefExp) {
        // handle referenced RefDerefExpression with SCClass (*class_ptr).var !
        Expression innerExpr = refDerefExp.getExpression();
        if (innerExpr instanceof SCVariableExpression varExpr) {
            SCVariable var = varExpr.getVar();
            if (var.isSCClassInstanceOrArrayOfSCClassInstances()) {
                SCClass scc = var.getSClassIfPossible();
                if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
                    handleClass(scc, refDerefExp, getNextUsefulSibling(n), e, oldClass, oldFunction);
                    
                } else {
                    e.getExpressionStack().push(refDerefExp);
                }
            } else if (var instanceof SCPointer pt) {
                // SCPointer
                handlePointer(pt, e, refDerefExp, n, oldClass, oldFunction);
            } else {
                // otherType
                // ==> not supported
                if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
                    handleVariable(var, varExpr, getNextUsefulSibling(n), e, oldClass, oldFunction);
                } else {
                    e.getExpressionStack().push(refDerefExp);
                }
            }
            return;
        } else {
            // Standard handling
            standardHandling(e, n, oldClass, oldFunction, refDerefExp);
        }
    }
    
    private void handleBinaryExpression(Environment e, Node n, SCClass oldClass, SCFunction oldFunction,
            BinaryExpression exp) {
        // its not a variable, but we handle it like one
        if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
            handleVariable(null, exp, n.getNextSibling().getNextSibling(), e, oldClass, oldFunction);
        } else {
            e.getExpressionStack().push(exp);
        }
        
    }
    
    private void standardHandling(Environment e, Node n, SCClass oldClass, SCFunction oldFunction, Expression exp) {
        if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
            handleVariable(null, exp, getNextUsefulSibling(n), e, oldClass, oldFunction);
        } else {
            e.getExpressionStack().push(exp);
        }
    }
    
    private void handleSCClassInstanceExpression(Environment e, Node n, SCClass oldClass, SCFunction oldFunction,
            SCClassInstanceExpression classInstExp) {
        if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
            handleClass(classInstExp.getInstance().getSCClass(), classInstExp, n.getNextSibling().getNextSibling(), e,
                    oldClass, oldFunction);
        } else {
            e.getExpressionStack().push(classInstExp);
        }
    }
    
    private void handleSCPortSCSocketExpression(Environment e, Node n, SCClass oldClass, SCFunction oldFunction,
            SCPortSCSocketExpression psExp) {
        // Port/Socket
        if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
            handlePortSocket(psExp.getSCPortSCSocket(), psExp, getNextUsefulSibling(n), e, oldClass, oldFunction);
        } else {
            e.getExpressionStack().push(psExp);
        }
    }
    
    private void handleAssertionExpression(Environment e, Node n, SCClass oldClass, SCFunction oldFunction,
            AssertionExpression assExp) {
        n = getNextUsefulSibling(n);
        SCClass tempClass = e.getCurrentClass();
        SCFunction tempFunction = e.getCurrentFunction();
        
        restoreEnvironment(oldClass, oldFunction, e);
        handleNode(n, e);
        restoreEnvironment(tempClass, tempFunction, e);
        
        Expression cond = null;
        if (e.getLastArgumentList().size() == 1) {
            cond = e.getLastArgumentList().get(0);
            assExp.setCondition(cond);
            e.getExpressionStack().push(assExp);
        } else {
            logger.error(
                    "Encountered AssertionExpression with more than one argument. We can only handle single argument assertions, e.g., assert(true) or 'assert(x == 42 && b == 7)'. Arguments were: '{}'",
                    e.getLastArgumentList());
        }
        e.getLastArgumentList().clear();
        return;
    }
    
    private void handleSCVariableExpression(Environment e, Node n, SCClass oldClass, SCFunction oldFunction,
            SCVariableExpression var_exp) {
        SCVariable var = var_exp.getVar();
        if (var.isSCClassInstanceOrArrayOfSCClassInstances()) {
            SCClass scc = var.getSClassIfPossible();
            if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
                handleClass(scc, var_exp, getNextUsefulSibling(n), e, oldClass, oldFunction);
                
            } else {
                e.getExpressionStack().push(var_exp);
            }
        } else if (var instanceof SCPointer pt) {
            // SCPointer
            handlePointer(pt, e, var_exp, n, oldClass, oldFunction);
        } else {
            // otherType
            // ==> not supported
            if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
                handleVariable(var_exp.getVar(), var_exp, n.getNextSibling().getNextSibling(), e, oldClass,
                        oldFunction);
            } else {
                e.getExpressionStack().push(var_exp);
            }
        }
    }
    
    private void handleFunctionCallExpression(Node n, Environment e, SCClass oldClass, SCFunction oldFunction,
            FunctionCallExpression fct_exp) {
        // FunctionCall
        n = getNextUsefulSibling(n);
        
        SCClass tempClass = e.getCurrentClass();
        SCFunction tempFunction = e.getCurrentFunction();
        
        restoreEnvironment(oldClass, oldFunction, e);
        handleNode(n, e);
        restoreEnvironment(tempClass, tempFunction, e);
        
        List<Expression> clone = new ArrayList<>();
        for (Expression expr : e.getLastArgumentList()) {
            clone.add(expr);
        }
        e.getLastArgumentList().clear();
        
        fct_exp.setParameters(clone);
        String returnType = fct_exp.getFunction().getReturnType();
        if (!e.getKnownTypes().isEmpty() && e.getKnownTypes().containsKey(returnType)) {
            if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
                handleClass(e.getKnownTypes().get(returnType), fct_exp, n.getNextSibling().getNextSibling(), e,
                        oldClass, oldFunction);
                
            } else {
                e.getExpressionStack().push(fct_exp);
            }
        } else if (!e.getClassList().isEmpty() && e.getClassList().containsKey(returnType)) {
            if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
                handleClass(e.getClassList().get(returnType), fct_exp, n.getNextSibling().getNextSibling(), e, oldClass,
                        oldFunction);
            } else {
                e.getExpressionStack().push(fct_exp);
            }
        } else {
            standardHandling(e, n, oldClass, oldFunction, fct_exp);
            
        }
    }
    
    private void handlePointer(SCPointer pt, Environment e, Expression exp, Node n, SCClass oldClass,
            SCFunction oldFunction) {
        String type = pt.getType();
        
        if (!e.getKnownTypes().isEmpty() && e.getKnownTypes().containsKey(type)) {
            if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
                handleClass(e.getKnownTypes().get(type), exp, n.getNextSibling().getNextSibling(), e, oldClass,
                        oldFunction);
            } else {
                e.getExpressionStack().push(exp);
            }
        } else if (!e.getClassList().isEmpty() && e.getClassList().containsKey(type)) {
            if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
                handleClass(e.getClassList().get(type), exp, getNextUsefulSibling(n), e, oldClass, oldFunction);
            } else {
                e.getExpressionStack().push(exp);
            }
            
        } else if (exp instanceof SCVariableExpression varExp) {
            if (n != null && n.getNextSibling() != null && n.getNextSibling().getNextSibling() != null) {
                handleVariable(varExp.getVar(), varExp, n.getNextSibling().getNextSibling(), e, oldClass, oldFunction);
            } else {
                e.getExpressionStack().push(varExp);
            }
        } else {
            standardHandling(e, n, oldClass, oldFunction, exp);
        }
    }
    
    /**
     * Handles the access to all members of classes.
     * 
     * @param cl
     * @param exp
     * @param Sibling
     * @param e
     * @param oldClass
     * @param oldFunction
     */
    private void handleClass(SCClass cl, Expression exp, Node sibling, Environment e, SCClass oldClass,
            SCFunction oldFunction) {
        String op = handleOperator(sibling);
        
        // TODO this might not be necessary (or even wrong)
        e.setCurrentClass(cl);
        e.setCurrentFunction(null);
        
        // current sibling is useless, skip
        sibling = getNextUsefulSibling(sibling);
        
        handleNode(sibling, e);
        Expression foundExp = e.getExpressionStack().pop();
        
        // we might encounter a unary expression but in this case, we can just
        // unwrap it
        UnaryExpression ue = null;
        if (foundExp instanceof UnaryExpression) {
            ue = (UnaryExpression) foundExp;
            foundExp = ue.getExpression();
        }
        
        // there are several possibilities now:
        if (foundExp instanceof FunctionCallExpression funExp) {
            // 1. we have a function call (e.g. a.foo())
            // handling the parameters of the function call
            // depending on the function parameter count there might be some
            // nodes which can be
            // ignored. After these steps sibling should be the argument_list of
            // the function.
            sibling = getNextUsefulSibling(sibling);
            
            // saving the current class
            SCClass curClass = e.getCurrentClass();
            SCFunction curFunction = e.getCurrentFunction();
            
            // setting current class and current function to other values,
            // handling the node and restore the current class and function
            restoreEnvironment(oldClass, oldFunction, e);
            handleNode(sibling, e);
            restoreEnvironment(curClass, curFunction, e);
            
            // Getting the arguments from the environment and set them as
            // parameters for the function call expression
            List<Expression> arguments = new LinkedList<>(e.getLastArgumentList());
            funExp.setParameters(arguments);
            e.getLastArgumentList().clear();
            
            // Getting the return type of the function
            String returnType = funExp.getFunction().getReturnType();
            
            if (sibling == null || sibling.getNextSibling() == null
                    || sibling.getNextSibling().getNextSibling() == null) {
                // the easiest case is that there are no more siblings of the
                // current node, which means that we do not have a chain of
                // member
                // accesses like a.foo().bar()
                e.getExpressionStack().push(new AccessExpression(sibling, exp, op, funExp));
            } else {
                // or we have a chain
                if (!e.getKnownTypes().isEmpty() && e.getKnownTypes().containsKey(returnType)) {
                    // ... and the next element is a known type
                    AccessExpression newExp = new AccessExpression(sibling, exp, op, funExp);
                    SCClass newClass = e.getKnownTypes().get(returnType);
                    handleClass(newClass, newExp, sibling.getNextSibling().getNextSibling(), e, oldClass, oldFunction);
                } else if (!e.getClassList().isEmpty() && e.getKnownTypes().containsKey(returnType)) {
                    // ... and the next element is a class
                    AccessExpression newExp = new AccessExpression(sibling, exp, op, funExp);
                    SCClass newClass = e.getClassList().get(returnType);
                    handleClass(newClass, newExp, sibling.getNextSibling().getNextSibling(), e, oldClass, oldFunction);
                } else {
                    // ... and something goes wrong
                    logger.error(
                            "{}: Encountered the return type {} of function {} which is used in a member access chain (e.g., a.foo().bar()) "
                                    + "but no class was found for this return type.",
                            NodeUtil.getFixedAttributes(sibling), returnType, funExp.getFunction().getName());
                }
            }
        } else if (foundExp instanceof SCVariableExpression varExp) {
            // 2. we have a variable access (e.g., a.x)
            SCVariable var = varExp.getVar();
            
            if (sibling == null || sibling.getNextSibling() == null
                    || sibling.getNextSibling().getNextSibling() == null) {
                // the easiest case is that there are no more siblings of the
                // current node, which means that we do not have a chain of
                // member
                // accesses like a.foo().bar()
                e.getExpressionStack().push(new AccessExpression(sibling, exp, op, varExp));
            } else {
                sibling = getNextUsefulSibling(sibling);
                // or we have a chain...
                if (var instanceof SCClassInstance clI) {
                    // ... and the next element is a class
                    AccessExpression newExp = new AccessExpression(sibling, exp, op, varExp);
                    handleClass(clI.getSCClass(), newExp, sibling, e, oldClass, oldFunction);
                } else if (var instanceof SCPointer ptr) {
                    // ... and the next element is a pointer
                    String type = ptr.getType();
                    if (e.getKnownTypes().get(type) != null) {
                        // ... which points to a known type
                        AccessExpression newExp = new AccessExpression(sibling, exp, op, varExp);
                        handleClass(e.getKnownTypes().get(type), newExp, sibling, e, oldClass, oldFunction);
                    } else if (e.getClassList().get(type) != null) {
                        // ... which points to another class
                        AccessExpression newExp = new AccessExpression(sibling, exp, op, varExp);
                        handleClass(e.getClassList().get(type), newExp, sibling, e, oldClass, oldFunction);
                    } else {
                        // ... and we have a problem
                        logger.error(
                                "{}: Encountered a pointer pointing to {}, which is used in a member access chain but is not a class.",
                                NodeUtil.getFixedAttributes(sibling), type);
                    }
                    
                } else {
                    // ... or we have a problem
                    logger.error(
                            "{}: Encountered a member {} which is used in a member access chain (e.g., a.x.y) but whose class could not be determined.",
                            NodeUtil.getFixedAttributes(sibling), var.getName());
                }
            }
        } else if (foundExp instanceof SCClassInstanceExpression ciExp) {
            // 3. we have an access to a class instance
            if (sibling == null || sibling.getNextSibling() == null
                    || sibling.getNextSibling().getNextSibling() == null) {
                // the easiest case is that there are no more siblings of the
                // current node, which means that we do not have a chain of
                // member
                // accesses like a.foo().bar()
                e.getExpressionStack().push(new AccessExpression(sibling, exp, op, ciExp));
            } else {
                // or we have a chain...
                AccessExpression newExp = new AccessExpression(sibling, exp, op, ciExp);
                handleClass(ciExp.getInstance().getSCClass(), newExp, sibling, e, oldClass, oldFunction);
            }
        } else if (foundExp instanceof SCPortSCSocketExpression psExp) {
            // 4. we have a port or socket access here
            if (e.isInConstructor()) {
                // this might belong to the binding of ports and sockets in the
                // constructor of a module
                restoreEnvironment(oldClass, oldFunction, e);
                handlePortSocketBindings(sibling.getParentNode(), e);
            } else {
                // or it is an access to the port or socket inside a normal
                // function
                if (sibling == null || sibling.getNextSibling() == null
                        || sibling.getNextSibling().getNextSibling() == null) {
                    // the easiest case is that there are no more siblings of
                    // the
                    // current node, which means that we do not have a chain of
                    // member
                    // accesses like a.foo().bar()
                    e.getExpressionStack().push(new AccessExpression(sibling, exp, op, psExp));
                } else {
                    // or we have a chain...
                    AccessExpression newExp = new AccessExpression(sibling, exp, op, psExp);
                    handlePortSocket(psExp.getSCPortSCSocket(), newExp, sibling, e, oldClass, oldFunction);
                }
            }
        } else {
            // 5. it's something else we cannot handle
            logger.error("{}: Encountered an access which we cannot handle.", NodeUtil.getFixedAttributes(sibling));
        }
        
        // remember to wrap the Expression in an UnaryExpression if we unwrapped
        // it before
        if (ue != null) {
            Expression innerExp = e.getExpressionStack().pop();
            ue.setExpression(innerExp);
            e.getExpressionStack().push(ue);
        }
        
    }
    
    private void handleVariable(SCVariable var, Expression prev_exp, Node sibling, Environment e, SCClass oldStruct,
            SCFunction oldFunction) {
        // we only expect functions after we had a variable
        // AccessExpression be;
        while (sibling != null) {
            String op = handleOperator(sibling);
            
            sibling = getNextUsefulSibling(sibling); // sibling.getNextSibling().getNextSibling();
            
            handleNode(sibling, e);
            
            Expression exp = e.getExpressionStack().pop();
            FunctionCallExpression fct_exp = null;
            try {
                fct_exp = (FunctionCallExpression) exp;
            } catch (Exception exc) {
                logger.error("{}: Function expected, but not found, we have something other",
                        NodeUtil.getFixedAttributes(sibling));
                return;
            }
            
            sibling = getNextUsefulSibling(sibling);
            
            SCClass tempStruct = e.getCurrentClass();
            SCFunction tempFun = e.getCurrentFunction();
            
            restoreEnvironment(oldStruct, oldFunction, e);
            
            handleNode(sibling, e);
            
            restoreEnvironment(tempStruct, tempFun, e);
            
            List<Expression> clone = new ArrayList<>();
            for (Expression expr : e.getLastArgumentList()) {
                clone.add(expr);
            }
            e.getLastArgumentList().clear();
            
            fct_exp.setParameters(clone);
            
            prev_exp = new AccessExpression(null, prev_exp, op, exp);
            
            // iterate only if here are more siblings.
            if (sibling != null) {
                sibling = getNextUsefulSibling(sibling, true);
            }
            
        }
        
        e.getExpressionStack().push(prev_exp);
    }
    
    /**
     * Returns the correct operator for the member access. This can be either a "->" or a ".".
     * 
     * @param exp
     * @param node
     * @param e
     * @return
     */
    private String handleOperator(Node node) {
        String op = NodeUtil.getAttributeValueByName(node, "name");
        if (op.equals("*")) {
            op = "->";
        } else if (op.equals("&")) {
            op = ".";
        } else {
            logger.error(
                    "{}: Encountered unexpected member access. Expected was either \"->\" (*) or \".\" (&) but was {}.",
                    NodeUtil.getFixedAttributes(node), op);
        }
        return op;
    }
    
    public void handlePortSocket(SCPort ps, Expression prev_exp, Node sibling, Environment e, SCClass oldClass,
            SCFunction oldFunction) {
        // we only can have a function which work on this port or socket, lets
        // create a dummy function
        // then create a Expression an return that
        String op = handleOperator(sibling);
        sibling = getNextUsefulSibling(sibling);
        SCPort psOld = e.getCurrentPortSocket();
        e.setCurrentPortSocket(ps);
        
        handleNode(sibling, e);
        e.setCurrentPortSocket(psOld);
        Expression exp = e.getExpressionStack().pop();
        if (exp instanceof FunctionCallExpression fct_exp) {
            String name = fct_exp.getFunction().getName();
            sibling = getNextUsefulSibling(sibling);
            
            SCClass tempClass = e.getCurrentClass();
            SCFunction tempFunction = e.getCurrentFunction();
            
            restoreEnvironment(oldClass, oldFunction, e);
            
            handleNode(sibling, e);
            
            restoreEnvironment(tempClass, tempFunction, e);
            
            if (name.equals("bind")) {
                // although bind CAN be called on ports, it usually isn't. Hence
                // we don't support it (yet).
                if (!(ps instanceof SCSocket soc)) {
                    logger.warn("Found bind() call on port (currently unimplemented).");
                    return;
                } else {
                    if (e.getLastArgumentList().get(0) instanceof SCClassInstanceExpression) {
                        // the easiest way is a direct SCModuleExpression
                        SCClassInstanceExpression classInstExp =
                                (SCClassInstanceExpression) e.getLastArgumentList().get(0);
                        handleBinding(soc, prev_exp, sibling, e, op, fct_exp, classInstExp);
                        
                    } else if (e.getLastArgumentList().get(0) instanceof RefDerefExpression) {
                        // or it can be a pointer to the module instance itself
                        // (*this)
                        RefDerefExpression rde = (RefDerefExpression) e.getLastArgumentList().get(0);
                        SCClassInstanceExpression classInstExp = (SCClassInstanceExpression) rde.getExpression();
                        
                        handleBinding(soc, prev_exp, sibling, e, op, fct_exp, classInstExp);
                        
                    } else {
                        logger.error(
                                "{}: Port or Socket was bound on something that isn't an Instance of the Module where we are",
                                NodeUtil.getFixedAttributes(sibling));
                        // at this time it's not possible to connect a Socket to
                        // an
                        // other Module with Bind
                    }
                }
                
            } else {
                // if invoked on a socket, resolve function call
                List<SCConnectionInterface> psis = e.getSystem().getPortSocketInstances(ps);
                for (SCConnectionInterface psi : psis) {
                    if (psi instanceof SCSocketInstance si) {
                        si.addCalledFunction(name, fct_exp);
                        oldFunction.addFunctionCall(fct_exp);
                    }
                }
                
                List<Expression> clone = new ArrayList<>();
                for (Expression expr : e.getLastArgumentList()) {
                    clone.add(expr);
                }
                e.getLastArgumentList().clear();
                
                fct_exp.setParameters(clone);
                
                if (sibling != null && sibling.getNextSibling() != null
                        && sibling.getNextSibling().getNextSibling() != null) {
                    // handling is similar to a variable
                    
                    AccessExpression current_exp = new AccessExpression(sibling, prev_exp, op, fct_exp);
                    handleVariable(null, current_exp, getNextUsefulSibling(sibling), e, oldClass, oldFunction);
                } else {
                    
                    e.getExpressionStack().push(new AccessExpression(sibling, prev_exp, op, fct_exp));
                }
                
            }
        }
        
    }
    
    /**
     * Handles the binding of an SCModuleInstance to an SCPortSocketInstance
     * 
     * @param soc
     * @param prev_exp
     * @param sibling
     * @param e
     * @param op
     * @param fct_exp
     * @param classInstExp
     */
    private void handleBinding(SCSocket soc, Expression prev_exp, Node sibling, Environment e, String op,
            FunctionCallExpression fct_exp, SCClassInstanceExpression classInstExp) {
        
        List<SCConnectionInterface> psis = e.getSystem().getPortSocketInstances(soc);
        
        for (SCConnectionInterface psi : psis) {
            // binding only occurs on sockets, so this should be an
            // SCSocketInstance
            if (!(psi instanceof SCSocketInstance si)) {
                logger.error("No SocketInstance for Socket {} found: {}", soc, psi);
                return;
            }
            si.setSocketFunctionLocation(classInstExp.getInstance().getSCClass());
        }
        
        List<Expression> clone = new ArrayList<>();
        for (Expression expr : e.getLastArgumentList()) {
            clone.add(expr);
        }
        e.getLastArgumentList().clear();
        
        fct_exp.setParameters(clone);
        
        AccessExpression current_exp = new AccessExpression(sibling, prev_exp, op, fct_exp);
        e.getExpressionStack().push(current_exp);
    }
    
    public void handlePortSocketBindings(Node node, Environment e) {
		// we have to find out which ModuleInstance owns the port or socket
		// then we have look if this port at this moduleInstance already exist
		// otherwise we have to crate a PortSocketInstance
		// and then we look for the ModuleInstance or the Channel we have to
		// bound on it
		Node mdl_ins_nam = findChildNode(
				findChildNode(node, "primary_expression"), "id_expression");
		Node ps_name = findChildNode(node, "id_expression");
		Node channel = findChildNode(
				findChildNode(
						findChildNode(findChildNode(node, "arguments"),
								"arguments_list"), "primary_expression"),
				"id_expression");
		SCClassInstance classInst = null;
		String modInstName = NodeUtil.getAttributeValueByName(mdl_ins_nam,
				"name");
		if (e.getCurrentClass() != null) {
			// we have a port/socket binding inside of a constructor and
			// therefore a hierarchical model.
			// first we try to locate the scclass whose port/socket we want to
			// bind.
			// This is either a local variable of the constructor
			SCVariable bound = e.getCurrentFunction()
					.getLocalVariableOrParameterAsSCVar(modInstName);

			// TODO mp: can it also be a parameter of the constructor?
			// or a member of the surrounding class
			if (bound == null) {
				bound = e.getCurrentClass().getMemberByName(modInstName);
			}
			if (bound != null) {
				if (bound instanceof SCClassInstance) {
					classInst = (SCClassInstance) bound;
				} else {
					logger.error(
							"Encountered port/socket binding on {}, which is not a module.",
							bound.getName());
				}
			}
			// mod.getInstanceByName(NodeUtil.getAttributeValueByName(
			// mdl_ins_nam, "name"));
		} else {
			classInst = e.getSystem().getInstanceByName(modInstName);

		}
		if (classInst == null) {
			logger.error(
					"Encountered port/socket binding but could not find the bounded module {}.",
					modInstName);
			return;
		}
		SCPort ps = classInst.getSCClass().getPortSocketByName(
				NodeUtil.getAttributeValueByName(ps_name, "name"));
		if (ps == null) {
            logger.warn(
					"Encountered port/socket binding but could not find the port {}.",
					ps_name);
        }

		SCClassInstance clI = null;
		SCVariable var = null;
		if (channel != null) {
			String chan_nam = NodeUtil.getAttributeValueByName(channel, "name");
			var = e.getCurrentFunction().getLocalVariableOrParameterAsSCVar(
					chan_nam);
			if (var == null) {
				if (e.getCurrentClass() != null) {
					var = e.getCurrentClass().getMemberByName(chan_nam);
				}
				if (var == null) {
					var = e.getSystem().getGlobalVariables(chan_nam);
				}
			}
			if (var == null || !(var instanceof SCClassInstance)) {
				if (e.getCurrentClass() != null) {
					// clI = e.getCurrentClass().getInstanceByName(chan_nam);
					if (clI == null) {
						clI = (SCClassInstance) e.getCurrentClass()
								.getMemberByName(chan_nam);

					}

				} else {
					clI = e.getSystem().getInstanceByName(chan_nam);
				}
				if (clI == null && e.getCurrentClass() != null) {
					SCClass cl = e.getCurrentClass();
					SCPort newPS = cl.getPortSocketByName(chan_nam);
					if (newPS != null) {
						SCConnectionInterface psi = classInst
								.getPortSocketInstanceByName(ps.getName());
						// we have a channel: this is a port
						SCPortInstance psi_in_Constr = new SCPortInstance(
								newPS.getName(), newPS);
						SCClassInstance newInst = new SCClassInstance("this",
								cl, e.getCurrentClass());
						psi_in_Constr.addOwner(newInst);
						if (psi != null) {
							psi.addPortSocketInstance(psi_in_Constr);

						} else {
							psi = new SCPortInstance(ps.getName(), ps);
							psi.addPortSocketInstance(psi_in_Constr);
							psi.addOwner(classInst);
							classInst.addPortSocketInstance(psi);
						}

						SCClassInstanceExpression mdl = new SCClassInstanceExpression(
								mdl_ins_nam, classInst);
						SCPortSCSocketExpression pse = new SCPortSCSocketExpression(
								ps_name, ps);
						SCPortSCSocketExpression connect_to = new SCPortSCSocketExpression(
								channel, newPS);
						BracketExpression brac = new BracketExpression(channel,
								connect_to);
						AccessExpression bound = new AccessExpression(channel,
								pse, "", brac);

						e.getExpressionStack().push(
								new AccessExpression(channel, mdl, ".", bound));
						e.getSystem().addPortSocketInstance(psi);
						e.getSystem().addPortSocketInstance(psi_in_Constr);
						return;

					} else {
						logger.error(
								"{}: Encountered an unknown binding point for a port or socket.",
								NodeUtil.getFixedAttributes(node));
						return;
					}
				} else {
					SCConnectionInterface psi = classInst
							.getPortSocketInstanceByName(ps.getName());
					if (psi == null) {
						psi = new SCPortInstance(ps.getName(), ps);
						psi.addOwner(classInst);
						classInst.addPortSocketInstance(psi);
					}
					// still a channeled case: still a port
					if (!(psi instanceof SCPortInstance pi)) {
						logger.error(
								"Trying to connect a channel to a socket: {}",
								psi);
						return;
					}
					pi.addInstanceConnection(clI);

					SCClassInstanceExpression mdl = new SCClassInstanceExpression(
							mdl_ins_nam, classInst);
					SCPortSCSocketExpression pse = new SCPortSCSocketExpression(
							ps_name, ps);
					SCClassInstanceExpression mdl_connect_to = new SCClassInstanceExpression(
							mdl_ins_nam, clI);
					BracketExpression brac = new BracketExpression(channel,
							mdl_connect_to);
					AccessExpression bound = new AccessExpression(channel, pse,
							"", brac);

					e.getSystem().addPortSocketInstance(pi);
					e.getExpressionStack().push(
							new AccessExpression(channel, mdl, ".", bound));
					return;

				}
			}
			SCConnectionInterface psi = classInst
					.getPortSocketInstanceByName(ps.getName());
			if (psi == null) {
				SCPortInstance pi = new SCPortInstance(ps.getName(), ps);
				pi.addOwner(classInst);
				// psi.addModuleInstance(mdlI);
				if (var instanceof SCKnownType) {
					pi.addChannel((SCKnownType) var);
				} else if (var instanceof SCClassInstance) {
					pi.addInstanceConnection((SCClassInstance) var);
				}
				classInst.addPortSocketInstance(pi);
				psi = pi;
			} else {
				if (!(psi instanceof SCPortInstance pi)) {
					logger.error(
							"Trying to connect a module instance to a socket: {}",
							psi);
					return;
				}
				pi.addChannel((SCKnownType) var);
			}
			SCClassInstanceExpression mdl = new SCClassInstanceExpression(
					mdl_ins_nam, classInst);
			SCPortSCSocketExpression pse = new SCPortSCSocketExpression(
					ps_name, ps);
			// SCModuleExpression connect_to = new
			// SCModuleExpression(Integer.valueOf(getAttributeValue(mdl_ins_nam,
			// "idref")), Integer.valueOf(getAttributeValue(mdl_ins_nam,
			// "line")), mdlI);
			SCVariableExpression connect_to = new SCVariableExpression(
					mdl_ins_nam, var);
			BracketExpression brac = new BracketExpression(channel, connect_to);
			AccessExpression bound = new AccessExpression(channel, pse, "",
					brac);

			// We have a port socket instance definition outside of a
			// constructor which (most likely) means inside the sc_main. In this
			// case we add
			// the resulting port socket instance to the SCSystem. In all other
			// cases the instances are added to the corresponding
			// SCClassInstance.
			if (e.getCurrentClass() == null) {
				e.getSystem().addPortSocketInstance(psi);
			}
			e.getExpressionStack().push(
					new AccessExpression(channel, mdl, ".", bound));
			return;
		} else {
			// No channel in use. Could be a TLM socket or port forwarding.
			if (!(ps instanceof SCSocket soc)) {
				logger.warn(
						"Found port {} with no channel. Might be port forwarding (currently unimplemented)",
						ps == null ? "null" : ps.getName());
			} else {
				Node module_socket = findChildNode(
						findChildNode(findChildNode(node, "arguments"),
								"arguments_list"), "postfix_expression");
				if (module_socket != null) {
					Node module_inst = findChildNode(
							findChildNode(module_socket, "primary_expression"),
							"id_expression");
					Node socket_inst = findChildNode(module_socket,
							"id_expression");
					String mdl_nam = NodeUtil.getAttributeValueByName(
							module_inst, "name");
					clI = e.getSystem().getInstanceByName(mdl_nam);
					if (clI != null) {
						String soc_nam = NodeUtil.getAttributeValueByName(
								socket_inst, "name");
						SCPort socket = clI.getSCClass().getPortSocketByName(
								soc_nam);
						SCConnectionInterface con_to = clI
								.getPortSocketInstanceByName(soc_nam);
						if (con_to == null) {
							con_to = new SCSocketInstance(soc_nam,
									(SCSocket) socket);
							con_to.addOwner(clI);
							clI.addPortSocketInstance(con_to);
						}

						SCConnectionInterface psi = classInst
								.getPortSocketInstanceByName(soc.getName());
						// socket binding is always bidirectional
						if (psi != null) {
							psi.addPortSocketInstance(con_to);
							con_to.addPortSocketInstance(psi);
						} else {
							psi = new SCSocketInstance(soc.getName(), soc);
							psi.addPortSocketInstance(con_to);
							con_to.addPortSocketInstance(psi);
							psi.addOwner(classInst);
							classInst.addPortSocketInstance(psi);
						}
						SCClassInstanceExpression mdl = new SCClassInstanceExpression(
								mdl_ins_nam, classInst);
						SCPortSCSocketExpression pse = new SCPortSCSocketExpression(
								ps_name, soc);
						SCClassInstanceExpression connect_to_Mdl = new SCClassInstanceExpression(
								module_inst, clI);
						SCPortSCSocketExpression connect_to_pse = new SCPortSCSocketExpression(
								socket_inst, con_to.getPortSocket());
						AccessExpression connect_to = new AccessExpression(
								module_socket, connect_to_Mdl, ".",
								connect_to_pse);
						BracketExpression brac = new BracketExpression(
								module_socket, connect_to);
						AccessExpression bound = new AccessExpression(
								module_socket, pse, "", brac);

						e.getSystem().addPortSocketInstance(psi);
						e.getSystem().addPortSocketInstance(con_to);
						e.getExpressionStack().push(
								new AccessExpression(module_socket, mdl, ".",
										bound));
						return;
					}
				}
			}
			return;
		}
	}
    
    private void restoreEnvironment(SCClass cl, SCFunction fun, Environment e) {
        e.setCurrentClass(cl);
        e.setCurrentFunction(fun);
    }
    
    /*
     * Workaroung for all the 'sibling.getNextSibling().getNextSibling()' up there. Some siblings are
     * useless. They represent the empty space between ">" and "<" in the xml file. They do not exist in
     * the optimized xml file. We therefore keep the first nextsibling (instead of skipping it) if this
     * is a 'useful' sibling (no empty text). But: sometimes, the code supposes to get the second
     * ("if (sibling == null)") so there is this additional parameter for these cases. TODO: use
     * getNextUsefulSibling() everywhere and/or use only optimized xml files (see
     * example/dyn_mem/Makefile) OR configure DOM parser in order to leave out all ignorable
     * whitespaces.
     */
    private Node getNextUsefulSibling(Node n, boolean returnNullIfNeeded) {
        Node s = n.getNextSibling();
        if (returnNullIfNeeded && s != null && s.getNextSibling() == null) {
            return null;
        }
        if (s != null && s.getAttributes() == null && s.getNextSibling() != null) {
            s = s.getNextSibling();
        }
        return s;
    }
    
    private Node getNextUsefulSibling(Node n) {
        return getNextUsefulSibling(n, false);
    }
}
