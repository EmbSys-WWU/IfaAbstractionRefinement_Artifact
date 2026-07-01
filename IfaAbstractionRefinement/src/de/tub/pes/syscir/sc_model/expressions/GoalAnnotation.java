package de.tub.pes.syscir.sc_model.expressions;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.util.Pair;

public class GoalAnnotation extends MarkerExpression {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5548460814640403707L;

	public GoalAnnotation(Node n) {
		super(n);
		// TODO Auto-generated constructor stub
	}

	@Override
	public LinkedList<Expression> crawlDeeper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void replaceInnerExpressions(
			List<Pair<Expression, Expression>> replacements) {
		// TODO Auto-generated method stub

	}

}
