/*****************************************************************************
 *
 * Copyright (c) 2008-17, Joachim Fellmuth, Holger Gross, Florian Greiner, 
 * Bettina Hünnemeyer, Paula Herber, Verena Klös, Timm Liebrenz, 
 * Tobias Pfeffer, Marcel Pockrandt, Rolf Schröder, Simon Schwan
 * Technische Universitaet Berlin, Software and Embedded Systems
 * Engineering Group, Ernst-Reuter-Platz 7, 10587 Berlin, Germany.
 * All rights reserved.
 * 
 * This file is part of STATE (SystemC to Timed Automata Transformation Engine).
 * 
 * STATE is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * STATE is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with STATE.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  Please report any problems or bugs to: state@pes.tu-berlin.de
 *
 ****************************************************************************/

package de.tub.pes.syscir.engine.modeltransformer;

import java.util.LinkedList;
import java.util.List;

import de.tub.pes.syscir.engine.util.Pair;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.expressions.Expression;

/**
 * This class represents a crawler for all SCFunctions. With this class, it is
 * possible to easily crawl through the function expression trees and to replace
 * parts of it. Note that this class is an abstract class implemented for the
 * latch & hook pattern.
 * 
 * @author pockrandt
 * 
 */
public abstract class FunctionCrawler {

	/**
	 * Starting point for the crawler. Invokes the crawl method on the
	 * functionbody and afterwards the replace method to replace all expressions
	 * with their generated replacements.
	 * 
	 * @param exps
	 */
	public void start(SCFunction fun) {
		List<Pair<Expression, Expression>> replacements = crawl(fun.getBody());
		fun.replaceExpressions(replacements);
	}

	/**
	 * This method crawls through all Expressions and the corresponding
	 * subexpressions and invokes the method matches on every expression it
	 * encounters. If matches returns true, replace is invoked afterwards.
	 * 
	 * @param exps
	 */
	protected List<Pair<Expression, Expression>> crawl(List<Expression> exps) {
		List<Pair<Expression, Expression>> replacements = new LinkedList<Pair<Expression, Expression>>();
		for (int i = 0; i < exps.size(); i++) {
			Expression exp = exps.get(i);
			if (matches(exp)) {
				replacements.add(new Pair<Expression, Expression>(exp,
						generateReplacement(exp)));
			} else {
				if (goDeeper(exp)) {
					replacements.addAll(crawl(exp.crawlDeeper()));
				}
			}
		}

		return replacements;
	}

	/**
	 * This method is called by the crawler on every Expression the crawler
	 * encounters. On a return value of true, the crawler invokes the replace
	 * method afterwards.
	 * 
	 * @param exp
	 * @return
	 */
	protected abstract boolean matches(Expression exp);

	/**
	 * This method is called by the crawler on every Expression the matches
	 * method returns true for. It should return a new Expression, which should
	 * replace the old one.
	 * 
	 * @param exp
	 * @return
	 */
	protected abstract Expression generateReplacement(Expression exp);

	/**
	 * Determines whether the crawler should crawl deeper (e.g., get all
	 * expressions of the submitted expression). This is some kind of extra stop
	 * criteria in order to prevent duplicate or contradictory changes.
	 * 
	 * @param exp
	 * @return
	 */
	protected boolean goDeeper(Expression exp) {
		return exp != null;
	}
}
