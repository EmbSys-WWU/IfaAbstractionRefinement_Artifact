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

package de.tub.pes.syscir.sc_model.expressions;

import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.engine.util.Pair;
import de.tub.pes.syscir.sc_model.SCFunction;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

/**
 * This Expression is the superclass of all Expressions
 * 
 * @author Marcel Pockrandt
 */
public abstract class Expression implements Serializable {

    private static final long serialVersionUID = 2051413859219437508L;

    private static transient Logger logger = LogManager.getLogger(Expression.class.getName());

    private final Node node;
    private Expression parent;
    protected String label;

    /**
     * Constructor for expressions. Can be used to set fields in the abstract class.
     * 
     * @param nodeId - the xml node id of the expression
     * @param line - the line number in the corresponding file.
     */
    protected Expression(Node n) {
        this(n, "");
    }

    /**
     * Constructor for expressions. Can be used to set fields in the abstract class.
     * 
     * @param nodeId - the xml node id of the expression
     * @param line - the line number in the corresponding file.
     * @param label - the label (for goto statements) of the expression.
     */
    protected Expression(Node n, String label) {
        // Ammar put label instead of ""
        this(n, label, null);
    }

    protected Expression(Node n, Expression parent) {
        this(n, "", parent);
    }

    protected Expression(Node n, String label, Expression parent) {
        this.node = n;
        this.label = label;
        this.parent = parent;
    }

    /**
     * Writes the expression to the outputstreamwriter.
     * 
     * @param writer
     * @throws IOException
     */
    public void print(OutputStreamWriter writer) throws IOException {
        writer.append(toString());
    }

    @Override
    public String toString() {
        if (!this.label.equals("")) {
            return this.label + ": ";
        } else {
            return "";
        }

    }

    /**
     * Prints a C like string of this expression without semicolon
     * 
     * @return String without semicolon
     */
    public String toStringNoSem() {
        String str = toString();
        int i = str.indexOf(";");
        if (i != str.length() - 1 && i != -1) {
            logger.warn("toStringNoSem called for {}", str);
        }
        return toString().replace(";", "");
    }

    public synchronized int getNodeId() {
        if (this.node != null) {
            return Integer.valueOf(NodeUtil.getAttributeValueByName(this.node, "idref"));
        } else {
            return -1;
        }
    }

    public synchronized int getLine() {
        if (this.node != null) {
            String lineNo = NodeUtil.getAttributeValueByName(this.node, "line");
            if (lineNo != null) {
                return Integer.valueOf(lineNo);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public synchronized String getFile() {
        if (this.node != null) {
            return NodeUtil.getAttributeValueByName(this.node, "file");
        } else {
            return "";
        }
    }

    public Node getNode() {
        return this.node;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the parent expression of the current expression. A parent expression is the expression
     * which contains the current expression.
     * 
     * @return
     */
    public Expression getParent() {
        return this.parent;
    }

    /**
     * Sets the parent expression of the current expression. A parent expression is the expression which
     * contains the current expression.
     * 
     * @param parent
     */
    public void setParent(Expression parent) {
        this.parent = parent;
    }

    /**
     * Returns all Expression which are part of the given expression. WARNING: the returned list might
     * have changes in order. Use only for debugging purpose. This method HAS to be overwritten by any
     * Expression which contains expressions.
     * 
     * @return
     */
    public List<Expression> getInnerExpressions() {
        return new LinkedList<>();
    }

    /**
     * Returns all Expressions which are direct fields of this Expression. (e.g., a unaryExpression
     * should return a list containing the Expression it contains).
     * 
     * @return
     */
    public abstract List<Expression> crawlDeeper();

    /**
     * Returns the direct child of this expression with the given index.
     * 
     * @param index an index
     * @return the direct child of this expression with that index
     * @throws IndexOutOfBoundsException if this expression has no such child
     */
    public Expression getChild(int index) throws IndexOutOfBoundsException {
        return crawlDeeper().get(index);
    }

    /**
     * Returns the number of direct children of this expression.
     * 
     * @return number of children
     */
    public int getNumOfChildren() {
        return crawlDeeper().size();
    }

    /**
     * Returns the function that contains this expression, if any.
     * 
     * @return the containing SCFunction, or null if not contained in a function
     */
    public SCFunction getContainingFunction() {
        Expression parent = getParent();
        if (parent != null) {
            return parent.getContainingFunction();
        }
        return null;
    }

    /**
     * Replaces all inner expressions matching to the first element of a pair in the list with the
     * second element of the corresponding pair.
     * 
     * @param replacements
     */
    public abstract void replaceInnerExpressions(List<Pair<Expression, Expression>> replacements);

    /**
     * Replaces the expression exp with the replacement specified in replacements if exp has a
     * replacement. Recursively replaces all inner expressions of exp if exp is not replaced.
     * 
     * @param exp
     * @param replacements
     * @return the expression which should be used instead of exp (might be exp).
     */
    protected Expression replaceSingleExpression(Expression exp, List<Pair<Expression, Expression>> replacements) {
        boolean replaced = false;
        for (Pair<Expression, Expression> pair : replacements) {
            // yes, we really mean ==
            if (exp == pair.getFirst()) {
                pair.getSecond().setParent(exp.getParent());
                exp = pair.getSecond();
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            exp.replaceInnerExpressions(replacements);
        }
        return exp;
    }

    /**
     * Replaces all expressions in exps with the replacement specified in replacements if the expression
     * has a replacement. Recursively replaces all inner expressions of an expression if the expression
     * is not replaced.
     * 
     * @param exp
     * @param replacements
     */
    protected void replaceExpressionList(List<Expression> exps, List<Pair<Expression, Expression>> replacements) {
        for (int i = 0; i < exps.size(); i++) {
            exps.set(i, replaceSingleExpression(exps.get(i), replacements));
        }
    }

    /**
     * Replaces all expressions in exps with the replacement specified in replacements if the expression
     * has a replacement. Recursively replaces all inner expressions of an expression if the expression
     * is not replaced.
     * 
     * @param exp
     * @param replacements
     */
    protected void replaceExpressionArray(Expression[] exps, List<Pair<Expression, Expression>> replacements) {
        for (int i = 0; i < exps.length; i++) {
            exps[i] = replaceSingleExpression(exps[i], replacements);
        }
    }

    // attempts to extract the var name from a given expression
    public static String Expr2VarName(Expression expr) {
        if (expr instanceof SCVariableExpression) {
            return ((SCVariableExpression) expr).getVar().getName();
        }
        if (expr instanceof SCClassInstanceExpression) {
            return ((SCClassInstanceExpression) expr).getInstance().getName();
        }
        if (expr instanceof ConstantExpression) {
            return ((ConstantExpression) expr).getValue();
        }
        if (expr instanceof RefDerefExpression) {
            return Expr2VarName(((RefDerefExpression) expr).getExpression());
        }
        return "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.label == null) ? 0 : this.label.hashCode());
        result = prime * result + ((getFile() == null) ? 0 : getFile().hashCode());
        result = prime * result + getLine();
        result = prime * result + getNodeId();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Expression other = (Expression) obj;
        if (this.label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!this.label.equals(other.label)) {
            return false;
        }
        if (getFile() == null) {
            if (other.getFile() != null) {
                return false;
            }
        } else if (!getFile().equals(other.getFile())) {
            return false;
        }
        if (getLine() != other.getLine()) {
            return false;
        }
        if (getNodeId() != other.getNodeId()) {
            return false;
        }
        return true;
    }
}
